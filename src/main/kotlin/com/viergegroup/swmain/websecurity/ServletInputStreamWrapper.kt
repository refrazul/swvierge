package com.viergegroup.swmain.websecurity

import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream

class ServletInputStreamWrapper(private val inputStream: InputStream) : ServletInputStream() {
    override fun isFinished() = inputStream.available() == 0
    override fun isReady() = true
    override fun setReadListener(readListener: ReadListener) = throw UnsupportedOperationException()
    override fun read() = inputStream.read()
}
