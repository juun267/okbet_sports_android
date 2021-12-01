package org.cxct.sportlottery.network.error

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.bet.add.BetAddErrorData

object BetAddErrorParser {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    fun getBetAddErrorData(msg: String): List<BetAddErrorData>? {
        val listType = Types.newParameterizedType(List::class.java, BetAddErrorData::class.java)
        val adapter: JsonAdapter<List<BetAddErrorData>> = moshi.adapter(listType)
        return adapter.fromJson(msg)
    }
}