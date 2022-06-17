package org.cxct.sportlottery.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.cxct.sportlottery.network.sport.query.Play
import org.cxct.sportlottery.util.Event

/**
 * @author kevin
 * @create 2022/6/10
 * @description
 */
object PlayRepository {
    val mPlayList = MutableLiveData<Event<List<Play>>>()
    val playList: LiveData<Event<List<Play>>>
        get() = mPlayList
}