package org.cxct.sportlottery.util

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.jvm.Throws

object LocalJsonUtil {

    fun getLocalJson(context: Context, assetPath: String): String {
        val stringBuilder = StringBuilder()
        var bf: BufferedReader? = null
        try {
            //获取assets资源管理器
            val assetManager: AssetManager = context.assets

            //通过管理器打开文件并读取
            bf = BufferedReader(InputStreamReader(assetManager.open(assetPath)))
            var line = bf.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bf.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bf?.let { kotlin.runCatching { it.close() } }
        }
        return stringBuilder.toString()
    }


}