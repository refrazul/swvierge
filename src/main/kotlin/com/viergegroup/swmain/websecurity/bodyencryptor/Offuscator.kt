package com.viergegroup.swmain.websecurity.bodyencryptor

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Offuscator {
    private const val SHIFT = 5

    private val customEncodingMap = mapOf(
        'A' to "01", 'B' to "02", 'C' to "03", 'D' to "04", 'E' to "05",
        'F' to "06", 'G' to "07", 'H' to "08", 'I' to "09", 'J' to "0A",
        'K' to "0B", 'L' to "0C", 'M' to "0D", 'N' to "0E", 'O' to "0F",
        'P' to "10", 'Q' to "11", 'R' to "12", 'S' to "13", 'T' to "14",
        'U' to "15", 'V' to "16", 'W' to "17", 'X' to "18", 'Y' to "19",
        'Z' to "1A", 'a' to "1B", 'b' to "1C", 'c' to "1D", 'd' to "1E",
        'e' to "1F", 'f' to "20", 'g' to "21", 'h' to "22", 'i' to "23",
        'j' to "24", 'k' to "25", 'l' to "26", 'm' to "27", 'n' to "28",
        'o' to "29", 'p' to "2A", 'q' to "2B", 'r' to "2C", 's' to "2D",
        't' to "2E", 'u' to "2F", 'v' to "30", 'w' to "31", 'x' to "32",
        'y' to "33", 'z' to "34", '0' to "35", '1' to "36", '2' to "37",
        '3' to "38", '4' to "39", '5' to "3A", '6' to "3B", '7' to "3C",
        '8' to "3D", '9' to "3E", '!' to "3F", '@' to "40", '#' to "41",
        '$' to "42", '%' to "43", '^' to "44", '&' to "45", '*' to "46",
        '(' to "47", ')' to "48", '-' to "49", '_' to "4A", '=' to "4B",
        '+' to "4C", '[' to "4D", ']' to "4E", '{' to "4F", '}' to "50",
        ';' to "51", ':' to "52", '\'' to "53", '"' to "54", ',' to "55",
        '.' to "56", '/' to "57", '<' to "58", '>' to "59", '?' to "5A",
        'ñ' to "A6", 'Ñ'  to "A7", ' '  to "A8"
    )


    val customDecodingMap = customEncodingMap.entries.associate { (k, v) -> v to k }



    fun byteArrayToHexString(bytes: ByteArray): String {
        val hexString = StringBuilder(2 * bytes.size)
        for (byte in bytes) {
            val hex = Integer.toHexString(0xff and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }


    fun offuscate(str: String): String {
        // Desplazar caracteres
        val shiftedPassword = str.map { shiftChar(it, SHIFT) }.joinToString("")
        // Invertir la cadena
        val reversedPassword = shiftedPassword.reversed()
        // Intercambiar caracteres en posiciones específicas
        val swappedPassword = swapCharacters(reversedPassword)
        // Generar prefijo y sufijo
        val (prefix, suffix) = generatePrefixSuffix(swappedPassword)
        // Añadir prefijo y sufijo
        val obfuscatedPassword = "$prefix$swappedPassword$suffix"
        // Codificar usando la codificación personalizada
        return encodeSpecialVG(obfuscatedPassword)
    }

    fun desofuscate(str: String): String {
        // Decodificar usando la codificación personalizada
        val decodedPassword = decodeSpecialVG(str)
        // Extraer prefijo y sufijo
        val prefixLength = decodedPassword.length / 4 // Asumiendo que prefijo y sufijo son de igual longitud
        val suffixLength = prefixLength
        val prefix = decodedPassword.substring(0, prefixLength)
        val suffix = decodedPassword.substring(decodedPassword.length - suffixLength)
        val withoutPrefixSuffix = decodedPassword.removePrefix(prefix).removeSuffix(suffix)
        // Revertir prefijo y sufijo
        val originalPrefixSuffix = revertPrefixSuffix(prefix, suffix)
        // Revertir el intercambio de caracteres
        val swappedPassword = unswapCharacters(withoutPrefixSuffix)
        // Invertir la cadena
        val reversedPassword = swappedPassword.reversed()
        // Revertir el desplazamiento de caracteres
        return reversedPassword.map { shiftChar(it, -SHIFT) }.joinToString("")
    }

    private   fun generatePrefixSuffix(password: String): Pair<String, String> {
        val midIndex = password.length / 2
        val firstHalf = password.substring(0, midIndex)
        val secondHalf = password.substring(midIndex)
        val prefix = firstHalf.reversed() // O cualquier otra lógica de mezcla
        val suffix = secondHalf.reversed() // O cualquier otra lógica de mezcla
        return Pair(prefix, suffix)
    }

    fun revertPrefixSuffix(prefix: String, suffix: String): String {
        val firstHalf = prefix.reversed() // O la lógica inversa de mezcla utilizada en generatePrefixSuffix
        val secondHalf = suffix.reversed() // O la lógica inversa de mezcla utilizada en generatePrefixSuffix
        return firstHalf + secondHalf
    }


    private fun shiftChar(c: Char, shift: Int): Char {
        val specialChars = "!@#$%^&*()-_=+[]{};:'\",.<>?/\\|`~"
        return when {
            c.isLowerCase() -> 'a' + (c - 'a' + shift + 26) % 26
            c.isUpperCase() -> 'A' + (c - 'A' + shift + 26) % 26
            c.isDigit() -> '0' + (c - '0' + shift + 10) % 10
            specialChars.contains(c) -> {
                val index = specialChars.indexOf(c)
                specialChars[(index + shift + specialChars.length) % specialChars.length]
            }
            else -> throw IllegalArgumentException("Character not valid for shifting: $c")
        }
    }


    private fun swapCharacters(input: String): String {
        val chars = input.toCharArray()
        if (chars.size > 1) {
            // Intercambiar el primer y último carácter, el segundo y el penúltimo, etc.
            for (i in 0 until chars.size / 2 step 2) {
                val temp = chars[i]
                chars[i] = chars[chars.size - 1 - i]
                chars[chars.size - 1 - i] = temp
            }
        }
        return String(chars)
    }

    private fun unswapCharacters(input: String): String {
        // El intercambio de caracteres es simétrico, por lo que simplemente se llama de nuevo a la misma función
        return swapCharacters(input)
    }

    private fun encodeSpecialVG(input: String): String {
        return input.map { customEncodingMap[it] ?: error("Character not in encoding map") }.joinToString("")
    }

    private fun decodeSpecialVG(input: String): String {
        val hexes = input.chunked(2) // Dividir la cadena en trozos de 4 caracteres (por ejemplo, "01")
        return hexes.map { customDecodingMap[it] ?: error("Hex code not in decoding map $it") }.joinToString("")
    }


    private  fun getCurrentDateFormatted(): String {
        val formatter = DateTimeFormatter.ofPattern("ddHHMMYY")
        return LocalDateTime.now(ZoneOffset.UTC).format(formatter)
    }

}