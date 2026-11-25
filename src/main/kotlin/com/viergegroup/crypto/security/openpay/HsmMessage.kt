class HsmMessage {
    /** Versión del mensaje. */
     var version: String = "001"

    /** Tipo de encriptación. */
     var type: String = "000"

    /** Firma de mensaje. */
    var signature: String=""

    /** Vector de inicialización. */
    var iv: String=""

    /** Llave AES encriptada con RSA. */
    var aesKey: String=""

    /** Mensaje encriptado con AES. */
    var encryptedMessage: String=""

    /**
     * Obtiene la cadena que se firma en un mensaje.
     */
    fun getDataToSign(): String {
        return aesKey.orEmpty() + iv.orEmpty() + encryptedMessage.orEmpty()
    }

    /**
     * Obtiene el mensaje en su representación textual.
     */
    fun getMessageAsString(): String {
        val builder = StringBuilder()
        appendField(builder, 3, version)
        appendField(builder, 3, type)
        appendField(builder, 3, signature)
        appendField(builder, 3, iv)
        appendField(builder, 3, aesKey)
        appendField(builder, 4, encryptedMessage)
        return builder.toString()
    }

    private fun appendField(builder: StringBuilder, lengthDigits: Int, content: String?) {
        val length = content?.length ?: 0
        builder.append("_")
            .append(length.toString().padStart(lengthDigits, '0'))
            .append(content)
    }

    override fun toString(): String {
        return getMessageAsString()
    }
}
