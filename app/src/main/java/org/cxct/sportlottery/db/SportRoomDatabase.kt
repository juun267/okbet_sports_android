package org.cxct.sportlottery.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.cxct.sportlottery.db.converter.UserRebateConverter
import org.cxct.sportlottery.db.dao.UserInfoDao
import org.cxct.sportlottery.db.entity.UserInfo

@Database(entities = [UserInfo::class], version = 1, exportSchema = false)
@TypeConverters(UserRebateConverter::class)
abstract class SportRoomDatabase : RoomDatabase() {

    abstract fun userInfoDao(): UserInfoDao

    companion object {
        @Volatile
        private var INSTANCE: SportRoomDatabase? = null

        fun getDatabase(context: Context): SportRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportRoomDatabase::class.java,
                    "sport_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}