/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.treehouses.remote.ssh

import android.text.AndroidCharacter
import io.treehouses.remote.views.terminal.vt320
import io.treehouses.remote.ssh.terminal.TerminalBridge
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CoderResult
import java.nio.charset.CodingErrorAction

/**
 * @author Kenny Root
 */
class Relay(bridge: TerminalBridge, transport: SSH, buffer: vt320, encoding: String) : Runnable {
    private val bridge: TerminalBridge
    var charset: Charset? = null
        private set
    private var decoder: CharsetDecoder? = null
    private val transport: SSH
    private val buffer: vt320
    private var byteBuffer: ByteBuffer? = null
    private var charBuffer: CharBuffer? = null
    private var byteArray: ByteArray? = null
    private var charArray: CharArray? = null
    fun setCharset(encoding: String) {
        //		if (encoding.equals("CP437")) {
//			charset = new IBM437("IBM437",
//					new String[] {"IBM437", "CP437"});
//		} else {
        val charset: Charset = Charset.forName(encoding)
        //		}
        if (charset == this.charset) {
            return
        }
        val newCd = charset.newDecoder()
        newCd.onUnmappableCharacter(CodingErrorAction.REPLACE)
        newCd.onMalformedInput(CodingErrorAction.REPLACE)
        this.charset = charset
        synchronized(this) { decoder = newCd }
    }

    override fun run() {
        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE)
        charBuffer = CharBuffer.allocate(BUFFER_SIZE)

        /* for East Asian character widths */
        val wideAttribute = ByteArray(BUFFER_SIZE)
        byteArray = byteBuffer!!.array()
        charArray = charBuffer!!.array()
        byteBuffer!!.limit(0)
        try {
            handleData(wideAttribute)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun handleData(wideAttribute: ByteArray) {
        var result: CoderResult
        var bytesRead: Int; var bytesToRead: Int; var offset: Int
        while (true) {
            bytesToRead = byteBuffer!!.capacity() - byteBuffer!!.limit()
            offset = byteBuffer!!.arrayOffset() + byteBuffer!!.limit()
            bytesRead = transport.read(byteArray, offset, bytesToRead)
            if (bytesRead > 0) {
                byteBuffer!!.limit(byteBuffer!!.limit() + bytesRead)
                synchronized(this) { result = decoder!!.decode(byteBuffer, charBuffer, false) }
                if (result.isUnderflow &&
                        byteBuffer!!.limit() == byteBuffer!!.capacity()) {
                    byteBuffer!!.compact()
                    byteBuffer!!.limit(byteBuffer!!.position())
                    byteBuffer!!.position(0)
                }
                offset = charBuffer!!.position()
                AndroidCharacter.getEastAsianWidths(charArray, 0, offset, wideAttribute)
                buffer.putString(charArray!!, wideAttribute, 0, charBuffer!!.position())
                bridge.propagateConsoleText(charArray, charBuffer!!.position())
                charBuffer!!.clear()
                bridge.redraw()
            }
        }
    }

    companion object {
        private const val TAG = "CB.Relay"
        private const val BUFFER_SIZE = 4096
    }

    init {
        setCharset(encoding)
        this.bridge = bridge
        this.transport = transport
        this.buffer = buffer
    }
}