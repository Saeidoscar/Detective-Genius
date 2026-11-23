package com.detectivegenius.networking.discovery

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveredRoom(
    val roomPin: String,
    val hostIp: String,
    val port: Int,
    val hostNickname: String,
    val playerCount: Int
)

object DiscoveryConstants {
    const val DEFAULT_PORT = 8080
    const val SERVICE_TYPE = "_detectivegenius._tcp."
    const val QR_SCHEME = "detectivegenius://"
}

class LocalDiscoveryManager {

    /**
     * Encodes room connection info into a safe QR payload string.
     * Contains NO long-lived authentication secrets.
     */
    fun createQrPayload(hostIp: String, port: Int, pin: String): String {
        return "${DiscoveryConstants.QR_SCHEME}join?ip=$hostIp&port=$port&pin=$pin"
    }

    /**
     * Parses a scanned QR payload string back into connection parameters.
     */
    fun parseQrPayload(qrString: String): Triple<String, Int, String>? {
        if (!qrString.startsWith(DiscoveryConstants.QR_SCHEME)) return null
        val query = qrString.substringAfter("join?", "")
        val params = query.split("&").associate {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else "" to ""
        }
        val ip = params["ip"] ?: return null
        val port = params["port"]?.toIntOrNull() ?: DiscoveryConstants.DEFAULT_PORT
        val pin = params["pin"] ?: return null
        return Triple(ip, port, pin)
    }

    /**
     * Validates manual connection inputs (IP format and 4-digit PIN).
     */
    fun validateConnectionInput(hostIp: String, pin: String): Pair<Boolean, String?> {
        val trimmedIp = hostIp.trim()
        val trimmedPin = pin.trim()

        if (trimmedIp.isBlank()) {
            return false to "Host IP address cannot be empty."
        }
        val ipParts = trimmedIp.split(".")
        if (ipParts.size != 4 || ipParts.any { it.toIntOrNull() == null || it.toInt() !in 0..255 }) {
            return false to "Invalid IPv4 address format (e.g. 192.168.1.100)."
        }
        if (trimmedPin.length != 4 || !trimmedPin.all { it.isDigit() }) {
            return false to "PIN must be exactly 4 digits."
        }
        return true to null
    }
}
