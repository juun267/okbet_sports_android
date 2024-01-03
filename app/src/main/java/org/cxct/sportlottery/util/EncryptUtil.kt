package org.cxct.sportlottery.util

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import android.util.Base64;
import com.lc.sports.ws.protocol.protobuf.FrontWsEvent
import org.cxct.sportlottery.common.extentions.safeClose

object EncryptUtil {
    private const val BUFFER_SIZE = 1024

    /**
     * BASE64 加密
     * @param str
     * @return
     */
    fun encryptBASE64(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return null
        }
        try {
            val encode = str.toByteArray(charset("UTF-8"))
            // base64 加密
            return String(Base64.encode(encode, 0, encode.size, Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * BASE64 解密
     * @param str
     * @return
     */
    fun decryptBASE64(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return null
        }
        try {
            val encode = str.toByteArray(charset("UTF-8"))
            // base64 解密
            return String(Base64.decode(encode, 0, encode.size, Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * GZIP 加密
     *
     * @param str
     * @return
     */
    fun encryptGZIP(str: String?): ByteArray? {
        if (str.isNullOrEmpty()) {
            return null
        }

        var gzip: GZIPOutputStream? = null
        try {
            // gzip壓縮
            val baos = ByteArrayOutputStream()
            gzip = GZIPOutputStream(baos)
            gzip.write(str.toByteArray(charset("UTF-8")))
            gzip.close()
            val encode: ByteArray = baos.toByteArray()
            baos.flush()
            baos.close()

            // base64 加密
            return encode
            //			return new String(encode, "UTF-8");
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            gzip?.let { kotlin.runCatching { it.close() } }
        }
        return null
    }

    /**
     * GZIP 解密
     *
     * @param str
     * @return
     */
    fun decryptGZIP(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return null
        }

        var gzip: GZIPInputStream? = null
        try {
            var decode = str.toByteArray(charset("UTF-8"))

            //gzip 解壓縮
            val bais = ByteArrayInputStream(decode)
            gzip = GZIPInputStream(bais)
            val buf = ByteArray(BUFFER_SIZE)
            var len: Int
            val baos = ByteArrayOutputStream()
            while (gzip.read(buf, 0, BUFFER_SIZE).also { len = it } != -1) {
                baos.write(buf, 0, len)
            }
            gzip.close()
            baos.flush()
            decode = baos.toByteArray()
            baos.close()
            return String(decode, Charsets.UTF_8)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            gzip?.let { kotlin.runCatching { it.close() } }
        }
        return null
    }

    @Throws(IOException::class)
    fun uncompress(str: String?): String? {
        if (str.isNullOrEmpty()) {
            return str
        }
        val src = Base64.decode(str.toByteArray(charset("ISO-8859-1")), Base64.DEFAULT)
        val out = ByteArrayOutputStream()
        val `in` = ByteArrayInputStream(src)
        val gunzip = GZIPInputStream(`in`)
        val buffer = ByteArray(256)
        var n: Int
        while (gunzip.read(buffer).also { n = it } >= 0) {
            out.write(buffer, 0, n)
        }

        val result = out.toString()

        kotlin.runCatching { gunzip.close() }

        return result
    }

    @Throws(IOException::class)
    fun uncompressProto(str: String?): FrontWsEvent.Events? {
        if (str.isNullOrEmpty()) {
            return null
        }

        var gZip: GZIPInputStream? = null
        return try {
            val src = Base64.decode(str.toByteArray(charset(Charsets.UTF_8.name())), Base64.DEFAULT)
            val out = ByteArrayOutputStream()
            val `in` = ByteArrayInputStream(src)
            gZip = GZIPInputStream(`in`)
            val buffer = ByteArray(1024)
            var n: Int
            while (gZip.read(buffer).also { n = it } >= 0) {
                out.write(buffer, 0, n)
            }
            val result = out.toByteArray()
            FrontWsEvent.Events.parseFrom(result)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            gZip?.safeClose()
        }
    }

}
