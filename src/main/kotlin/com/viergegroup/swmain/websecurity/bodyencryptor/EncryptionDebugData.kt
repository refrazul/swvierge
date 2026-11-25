package com.viergegroup.swmain.websecurity.bodyencryptor

enum class EncryptionDebugEnum{
    Encrypt,
    Decrypt
}
data class EncryptionDebugData(
    var originalJson: String = "",
    var originalJsonByteArray: ByteArray = byteArrayOf(),
    var encryptedMetaData: String = "",
    var uuid: String = "",
    var uuidBytesHex: String = "",
    var aesKeyClearHex: String = "",
    var ivHex: String = "",
    var hmacHex: String = "",
    var calculatedHmacHex: String = "",
    var receivedHmacHex: String = "",

    var serialKey: String = "",
    var error: String? = null,
    var encryptOrDecrypt: EncryptionDebugEnum=EncryptionDebugEnum.Encrypt
)
