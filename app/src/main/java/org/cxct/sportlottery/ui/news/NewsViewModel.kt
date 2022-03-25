package org.cxct.sportlottery.ui.news

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cxct.sportlottery.network.OneBoSportApi
import org.cxct.sportlottery.network.common.NewsType
import org.cxct.sportlottery.network.news.News
import org.cxct.sportlottery.network.news.NewsResult
import org.cxct.sportlottery.repository.*
import org.cxct.sportlottery.ui.base.BaseSocketViewModel
import org.cxct.sportlottery.util.TimeUtil

class NewsViewModel(
    androidContext: Application,
    userInfoRepository: UserInfoRepository,
    loginRepository: LoginRepository,
    betInfoRepository: BetInfoRepository,
    infoCenterRepository: InfoCenterRepository,
    favoriteRepository: MyFavoriteRepository
) : BaseSocketViewModel(
    androidContext,
    userInfoRepository,
    loginRepository,
    betInfoRepository,
    infoCenterRepository,
    favoriteRepository
) {

    companion object {
        private const val NEWS_PAGE_SIZE = 20
    }

    private val _newsResult = MutableLiveData<NewsResult>()
    private val newsResult: LiveData<NewsResult>
        get() = _newsResult

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>>
        get() = _newsList

    private var newsPage = 1

    fun getNewsData(newsType: NewsType, refresh: Boolean = false) {
        if (newsResult.value?.total ?: 0 <= newsList.value?.size ?: 0) return

        viewModelScope.launch {
            doNetwork(androidContext) {
                OneBoSportApi.newsService.getMessageList(newsType.code, newsPage, NEWS_PAGE_SIZE)
            }?.let { newsResponse ->
                if (newsResponse.success) {
                    newsResponse.setupNewsDate()

                    newsPage++

                    _newsList.postValue(mutableListOf<News>().apply {
                        if (!refresh) {
                            addAll(newsList.value ?: listOf())
                        }

                        addAll(newsResponse.news)
                    })

                    _newsResult.postValue(newsResponse)
                }
            }
        }
    }

    /**
     * 設置公告時間
     */
    private fun NewsResult.setupNewsDate() {
        news.forEach {
            it.showDate = TimeUtil.timeFormat(it.addTime.toLong(), "yyyy.MM.dd")
        }
    }
}