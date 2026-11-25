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

    // API: Host info
    if (pathname === '/api/host-info') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            hostIp: getHostLanIp(),
            defaultHotspotIp: '192.168.43.1',
            port: PORT,
            timestamp: Date.now()
        }));
        return;
    }

    // API: Room Sync
    if (pathname === '/api/room/sync' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => body += chunk);
        req.on('end', () => {
            try {
                const payload = JSON.parse(body);
                if (payload && payload.roomId) {
                    rooms.set(payload.roomId, {
                        ...payload,
                        updatedAt: Date.now()
                    });
                    res.writeHead(200, { 'Content-Type': 'application/json' });
                    res.end(JSON.stringify({ status: 'ok', roomId: payload.roomId }));
                    return;
                }
            } catch (err) {}
            res.writeHead(400, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'invalid_payload' }));
        });
        return;
    }

    if (pathname === '/api/room/state') {
        const roomId = urlObj.searchParams.get('roomId');
        if (roomId && rooms.has(roomId)) {
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify(rooms.get(roomId)));
            return;
        }
        res.writeHead(404, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'room_not_found' }));
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
