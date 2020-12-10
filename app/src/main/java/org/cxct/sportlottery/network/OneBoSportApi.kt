package org.cxct.sportlottery.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.index.IndexService
import org.cxct.sportlottery.network.match.MatchService
import org.cxct.sportlottery.network.message.MessageService
import org.cxct.sportlottery.network.odds.OddsService
import org.cxct.sportlottery.network.sport.SportService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


private const val BASE_URL = "https://sports.cxct.org"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

object OneBoSportApi {
    val indexService: IndexService by lazy {
        retrofit.create(IndexService::class.java)
    }

    val sportService: SportService by lazy {
        retrofit.create(SportService::class.java)
    }

    val matchService: MatchService by lazy {
        retrofit.create(MatchService::class.java)
    }

    val messageService: MessageService by lazy {
        retrofit.create(MessageService::class.java)
    }

    val oddsService: OddsService by lazy {
        retrofit.create(OddsService::class.java)
    }
}