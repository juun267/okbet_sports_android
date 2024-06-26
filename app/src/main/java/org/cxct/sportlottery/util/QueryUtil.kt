package org.cxct.sportlottery.util

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object QueryUtil {

    /**
     * 把 Map<String></String>, String> 轉換成 QueryString
     *
     *
     * ex:
     * Map<String></String>, String> mapParams
     * mapParams.put(param1, value1);
     * mapParams.put(param2, value2);
     * mapParams.put(param3, value3);
     * "param1=value1&param2=value2&param3=value3"
     *
     * @param map : 要轉換的 map 參數
     * @return : query string
     */
    private fun mapToQueryString(map: Map<String, String>): String? {
        var string = StringBuilder()
        try {
            stringLoop@ for ((key, value) in map) {
                if (value.isEmpty())
                    continue@stringLoop
                string.append(URLEncoder.encode(key, "UTF-8"))
                    .append("=")
                    .append(URLEncoder.encode(value, "UTF-8"))
                    .append("&")
            }
            string.deleteCharAt(string.length - 1)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return string.toString()
    }

    fun toUrlParamsFormat(queryMap: HashMap<String, String>): String? {
        return "?" + mapToQueryString(queryMap)
    }
}