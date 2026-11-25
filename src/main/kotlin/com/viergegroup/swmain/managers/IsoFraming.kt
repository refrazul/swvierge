package com.viergegroup.swmain.managers

import java.io.InputStream

/**
 * Lógica pura de framing de mensajes ISO 8583 sobre el transporte del switch.
 * Formato en el cable: [2 bytes de longitud][cuerpo (empieza con "ISO")][ETX 0x03].
 * El prefijo de longitud = tamaño de (cuerpo + ETX).
 */
object IsoFraming {

    private val ISO_MARKER = byteArrayOf('I'.code.toByte(), 'S'.code.toByte(), 'O'.code.toByte())

    /** frames = cuerpos de cada mensaje; consumed = bytes procesados; hadTrailingGarbage = quedó algo sin reconocer. */
    data class FrameResult(
        val frames: List<ByteArray>,
        val consumed: Int,
        val hadTrailingGarbage: Boolean
    )

    /** Decodifica el prefijo de longitud (2 bytes, big-endian). Equivale al Integer.decode del hex del código viejo. */
    fun decodeLength(b0: Byte, b1: Byte): Int =
        ((b0.toInt() and 0xFF) shl 8) or (b1.toInt() and 0xFF)

    private fun startsWithIso(buffer: ByteArray, off: Int): Boolean {
        if (off + 3 > buffer.size) return false
        return buffer[off] == ISO_MARKER[0] && buffer[off + 1] == ISO_MARKER[1] && buffer[off + 2] == ISO_MARKER[2]
    }

    /**
     * Lee de [input] hasta completar el frame declarado por el prefijo de longitud, o hasta EOF.
     * Devuelve el total de bytes leídos (-1/0 si el socket no entregó datos).
     * TCP puede partir el mensaje en varios segmentos: por eso el loop.
     */

    fun readAvailableFrame(input: InputStream, buffer: ByteArray, prefilled: Int = 0): Int {
        val first = input.read(buffer, prefilled, buffer.size - prefilled)
        if (first < 0) return -1
        var bytesRead = prefilled + first
        if (bytesRead <= 0) return bytesRead
   /*  fun readAvailableFrame(input: InputStream, buffer: ByteArray): Int {
        var bytesRead = input.read(buffer)
        if (bytesRead <= 0) return bytesRead*/
      /*  // Asegurar los 2 bytes de longitud (un read puede traer solo 1).
        while (bytesRead < 2) {
            val n = input.read(buffer, bytesRead, 2 - bytesRead)
            if (n < 0) break
            bytesRead += n
        }
        if (bytesRead >= 2) {
            val declaredLen = decodeLength(buffer[0], buffer[1])
            val totalNeeded = minOf(declaredLen + 2, buffer.size)
            while (bytesRead < totalNeeded) {
                val n = input.read(buffer, bytesRead, totalNeeded - bytesRead)
                if (n < 0) break
                bytesRead += n
            }
        }*/

                // Asegurar la cabecera mínima: 2 (longitud) + 3 (marcador "ISO") = 5 bytes.
        while (bytesRead < 5) {
            val n = input.read(buffer, bytesRead, 5 - bytesRead)
            if (n < 0) break
            bytesRead += n
        }
        if (bytesRead >= 5) {
            val declaredLen = decodeLength(buffer[0], buffer[1])
            val bodyIsIso = buffer[2] == ISO_MARKER[0] &&
                            buffer[3] == ISO_MARKER[1] &&
                            buffer[4] == ISO_MARKER[2]
            if (bodyIsIso && declaredLen in 1..buffer.size) {
                val totalNeeded = minOf(declaredLen + 2, buffer.size)
                while (bytesRead < totalNeeded) {
                    val n = input.read(buffer, bytesRead, totalNeeded - bytesRead)
                    if (n < 0) break
                    bytesRead += n
                }
            }
        }
        return bytesRead
    }

    /**
     * Separa el buffer en los cuerpos de mensaje por prefijo de longitud (NO por el texto "ISO").
     * Cada cuerpo debe empezar con "ISO"; al primer frame que no cumpla (prefijo basura,
     * longitud imposible, no empieza con "ISO") se detiene y marca basura para instrumentar.
     */
    fun extractFrames(buffer: ByteArray, bytesRead: Int): FrameResult {
        val frames = mutableListOf<ByteArray>()
        var off = 0
        while (off + 2 <= bytesRead) {
            val len = decodeLength(buffer[off], buffer[off + 1])
            if (len <= 0) break
            val bodyStart = off + 2
            if (bodyStart >= bytesRead) break
            if (!startsWithIso(buffer, bodyStart)) break
            //val bodyEnd = minOf(bodyStart + len, bytesRead)
           // frames.add(buffer.copyOfRange(bodyStart, bodyEnd))
             if (bodyStart + len > bytesRead) break
            frames.add(buffer.copyOfRange(bodyStart, bodyStart + len))
            off = bodyStart + len
        }
        return FrameResult(frames, off, off < bytesRead)
    }
}