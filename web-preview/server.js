const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

// In-memory room store for LAN peer synchronization
const rooms = new Map();

function getHostLanIp() {
    try {
        const interfaces = os.networkInterfaces();
        for (const devName in interfaces) {
            const iface = interfaces[devName];
            for (let i = 0; i < iface.length; i++) {
                const alias = iface[i];
                if (alias.family === 'IPv4' && !alias.internal && alias.address !== '127.0.0.1') {
                    return alias.address;
                }
            }
        }
    } catch (e) {}
    return '192.168.43.1';
}

function normalizePin(pin) {
    if (!pin) return '';
    return String(pin).replace(/dg-/i, '').trim().toLowerCase();
}

const mimeTypes = {
    '.html': 'text/html; charset=utf-8',
    '.js': 'application/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.json': 'application/json',
    '.png': 'image/png',
    '.svg': 'image/svg+xml',
    '.wav': 'audio/wav',
    '.mp3': 'audio/mpeg',
    '.docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    '.pdf': 'application/pdf',
    '.zip': 'application/zip'
};

const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', '*');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = urlObj.pathname;

    // API: Host info / Ping
    if (pathname === '/api/host-info' || pathname === '/api/ping') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            status: 'ok',
            hostIp: getHostLanIp(),
            defaultHotspotIp: '192.168.43.1',
            port: PORT,
            timestamp: Date.now()
        }));
        return;
    }

    // API: Join Room
    if (pathname === '/api/join' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                const payload = JSON.parse(body);
                const reqRoomId = payload.roomId || 'DG-4829';
                const player = payload.player || {};
                const normReq = normalizePin(reqRoomId);

                // Find matching room
                let foundRoomKey = null;
                for (const key of rooms.keys()) {
                    if (normalizePin(key) === normReq) {
                        foundRoomKey = key;
                        break;
                    }
                }

                // If room doesn't exist yet, auto-create room with host default
                if (!foundRoomKey) {
                    foundRoomKey = reqRoomId;
                    rooms.set(foundRoomKey, {
                        roomId: foundRoomKey,
                        phase: 'lobby',
                        realPlayers: [],
                        chats: [],
                        updatedAt: Date.now()
                    });
                }

                const room = rooms.get(foundRoomKey);
                if (!room.realPlayers) room.realPlayers = [];

                // Check if player already exists
                const existingIdx = room.realPlayers.findIndex(p => p.id === player.id || (player.badgeId && p.badgeId === player.badgeId));
                if (existingIdx >= 0) {
                    room.realPlayers[existingIdx] = { ...room.realPlayers[existingIdx], ...player };
                } else {
                    room.realPlayers.push(player);
                }
                room.updatedAt = Date.now();

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    status: 'ok',
                    roomId: foundRoomKey,
                    playerId: player.id,
                    gameState: room
                }));
                return;
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'error', message: err.message || 'Invalid join payload' }));
            }
        });
        return;
    }

    // API: Poll Room
    if (pathname === '/api/poll' || pathname === '/api/room/state') {
        const roomId = urlObj.searchParams.get('roomId') || 'DG-4829';
        const norm = normalizePin(roomId);

        let roomData = null;
        for (const [key, val] of rooms.entries()) {
            if (normalizePin(key) === norm) {
                roomData = val;
                break;
            }
        }

        if (!roomData) {
            roomData = {
                roomId: roomId,
                phase: 'lobby',
                realPlayers: [],
                chats: [],
                updatedAt: Date.now()
            };
            rooms.set(roomId, roomData);
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            status: 'ok',
            roomId: roomData.roomId || roomId,
            timestamp: Date.now(),
            gameState: roomData
        }));
        return;
    }

    // API: Action
    if (pathname === '/api/action' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                const payload = JSON.parse(body);
                const roomId = payload.roomId || 'DG-4829';
                const norm = normalizePin(roomId);

                let room = null;
                for (const [key, val] of rooms.entries()) {
                    if (normalizePin(key) === norm) {
                        room = val;
                        break;
                    }
                }

                if (room && payload.type === 'CHAT') {
                    if (!room.chats) room.chats = [];
                    room.chats.push({
                        id: `msg_${Date.now()}`,
                        senderId: payload.playerId,
                        text: payload.text,
                        recipientId: payload.recipientId || null,
                        timestamp: Date.now()
                    });
                }

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'ok', gameState: room || {} }));
                return;
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'error', message: err.message }));
            }
        });
        return;
    }

    // API: Room Sync
    if ((pathname === '/api/sync' || pathname === '/api/room/sync') && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                const payload = JSON.parse(body);
                const stateObj = payload.gameState || payload;
                const roomId = stateObj.roomId || payload.roomId || 'DG-4829';
                rooms.set(roomId, {
                    ...stateObj,
                    roomId: roomId,
                    updatedAt: Date.now()
                });
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'ok', roomId: roomId }));
                return;
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'invalid_payload' }));
            }
        });
        return;
    }

    let decodedPath = pathname;
    try {
        decodedPath = decodeURIComponent(pathname);
    } catch (e) {}

    let filePath = path.join(PUBLIC_DIR, decodedPath === '/' ? 'index.html' : decodedPath);
    if (fs.existsSync(filePath) && fs.statSync(filePath).isDirectory()) {
        filePath = path.join(filePath, 'index.html');
    }
    if (!fs.existsSync(filePath)) {
        filePath = path.join(PUBLIC_DIR, 'index.html');
    }

    const ext = path.extname(filePath);
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            res.writeHead(500);
            res.end(`Server Error: ${err.code}`);
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`Detective Genius Portrait Preview Server running on http://0.0.0.0:${PORT}`);
});
