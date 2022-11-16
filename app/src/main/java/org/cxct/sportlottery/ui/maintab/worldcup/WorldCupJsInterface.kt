package org.cxct.sportlottery.ui.maintab.worldcup

import android.util.Log
import android.webkit.JavascriptInterface
import com.alibaba.fastjson.JSON
import com.google.gson.JsonObject

class WorldCupJsInterface {

    @JavascriptInterface
    fun tapAndroidEvent(param: String) {
        Log.e("For Test", "=====>>> WorldCupJsInterface 1111 ${param} ")
    }

    @JavascriptInterface
    fun tapAndroidEvent(param: JsParam) {
        Log.e("For Test", "=====>>> WorldCupJsInterface 2222 ${param} ")
    }

    @JavascriptInterface
    fun tapAndroidEvent(param: JsonObject) {
        Log.e("For Test", "=====>>> WorldCupJsInterface 33333 ${param} ")
    }


}