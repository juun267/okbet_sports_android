package org.cxct.sportlottery.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {

    companion object {

        fun setup(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        }
    }

    private val logFilePath: String

    init {
        logFilePath = context.getExternalFilesDir("crashlog")!!.absolutePath
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        val stackTraceInfo = getStackTraceInfo(ex)
        Log.e("Crash", stackTraceInfo)
        saveThrowableMessage(stackTraceInfo)
        MainTabActivity.reStart(context)
    }

    private fun getStackTraceInfo(t: Throwable): String {
        var pw: PrintWriter? = null
        val writer: Writer = StringWriter()
        try {
            pw = PrintWriter(writer)
            t.printStackTrace(pw)
        } catch (e: Exception) {
            return ""
        } finally {
            pw?.close()
        }
        return writer.toString()
    }

    private fun saveThrowableMessage(errorMessage: String) {
        if (TextUtils.isEmpty(errorMessage)) {
            return
        }
        val file = File(logFilePath)
        if (!file.exists()) {
            val mkdirs = file.mkdirs()
            if (mkdirs) {
                writeStringToFile(errorMessage, file)
            }
        } else {
            writeStringToFile(errorMessage, file)
        }
    }

    private fun writeStringToFile(errorMessage: String, file: File) {
        var fos: FileOutputStream? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val content = """
                $errorMessage[${format.format(Date())}]
                
                """.trimIndent()
            val bais = ByteArrayInputStream(content.toByteArray(charset("UTF-8")))
            val crashLog = createCrashLogFile(file)
            fos = FileOutputStream(crashLog, true)
            var len = 0
            val bytes = ByteArray(1024)
            while (bais.read(bytes).also { len = it } != -1) {
                fos.write(bytes, 0, len)
            }
            fos.flush()
            Log.e("程序出现异常", "写入本地文件成功：" + file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val allCrashFiles: List<*>
        private get() {
            val dir = File(logFilePath)
            val files = dir.listFiles()
            val fileNames = mutableListOf<String>()
            for (i in files.indices) {
                if (files[i].isFile) {
                    fileNames.add(files[i].name)
                }
            }
            return fileNames
        }

    @Throws(IOException::class)
    private fun createCrashLogFile(file: File): File {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val currentDate = sdf.format(Date())
        val crashLog = File(file, "$currentDate.txt")
        if (!crashLog.exists()) {
            crashLog.createNewFile()
            val crashLogNames = allCrashFiles
            if (crashLogNames.size > 10) {
                val currentTimeMilSeconds = Date().time
                val earliestDate =
                    sdf.format(Date(currentTimeMilSeconds - 24 * 60 * 60 * 10 * 1000))
                deleteFile("$earliestDate.txt")
            }
        }
        return crashLog
    }

    private fun deleteFile(fileName: String) {
        val file = File(logFilePath + File.separator + fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}