package org.cxct.sportlottery.db.converter

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.cxct.sportlottery.network.common.UserRebate

class UserRebateConverter {

    private val userRebatesType =
        Types.newParameterizedType(List::class.java, UserRebate::class.java)

    private val userRebatesAdapter =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter<List<UserRebate>?>(userRebatesType)

    @TypeConverter
    fun listToJson(value: List<UserRebate>?): String = userRebatesAdapter.toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = userRebatesAdapter.fromJson(value).orEmpty()
}