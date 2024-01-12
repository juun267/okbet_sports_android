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
import org.cxct.sportlottery.network.news.SportNewsRequest
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
        private const val NEWS_PAGE_SIZE = 3
    }

    private val _newsResult = MutableLiveData<NewsResult>()
    private val newsResult: LiveData<NewsResult>
        get() = _newsResult

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>>
        get() = _newsList

    val sportsNewsList: LiveData<List<News>>
        get() = _sportsNewsList
    private val _sportsNewsList = MutableLiveData<List<News>>()

    private val _showAllNews = MutableLiveData<Boolean>()
    val showAllNews: LiveData<Boolean>
        get() = _showAllNews

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean>
        get() = _loading

    private var newsPage = 1
    private var loadingNews = false

    var sportPageIndex=1
    private var sportPageSize=10
    var sportStartTime:Long?=0L
    var sportEndTime:Long?=0L
    fun getSportsNewsData() {
        if(loadingNews){
            return
        }

        loadingNews=true
        viewModelScope.launch {
            val params= SportNewsRequest(NewsType.SYSTEM.code,(sportStartTime?:0).toString(),(sportEndTime?:0).toString(),sportPageIndex,sportPageSize)
            val result= doNetwork(androidContext) {
                OneBoSportApi.newsService.getMessageListByTime(params)
            }

            loadingNews=false
            if(result!=null){
                sportPageIndex++
                _sportsNewsList.postValue(result.news)
            }
        }
    }
    fun getNewsData(newsType: NewsType, refresh: Boolean = false) {
        if ((!refresh && ((newsResult.value?.total ?: 0) <= (newsList.value?.size ?: 0))) || loadingNews) return

        loadingNews = true
        if (refresh) newsPage = 1

        _loading.postValue(true)

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

                        _showAllNews.postValue(newsResponse.total == size)
                    })

                    _newsResult.postValue(newsResponse)

                    loadingNews = false
                }
                _loading.postValue(false)
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