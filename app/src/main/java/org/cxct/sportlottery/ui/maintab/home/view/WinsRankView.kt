package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnDestory
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.ui.maintab.games.OkGameRecordAdapter
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.RCVDecoration
import kotlin.random.Random

class WinsRankView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle) {

    private var winsRequest: (() -> Unit)? = null
    private var betRequest: (() -> Unit)? = null

    private val rbtnLb by lazy { findViewById<RadioButton>(R.id.rbtn_lb) }
    private val rbtnLbw by lazy { findViewById<RadioButton>(R.id.rbtn_lbw) }
    private val rvOkgameRecord by lazy { findViewById<RecyclerView>(R.id.rv_okgame_record) }

    private val gameRecordAdapter by lazy { OkGameRecordAdapter() }
    private val httpBetDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新投注
    private val wsBetDataList: MutableList<RecordNewEvent> = mutableListOf()//ws的最新投注
    private val betShowingData: MutableList<RecordNewEvent> = mutableListOf()//最新投注显示在界面上的数据

    private val httpWinsDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新大奖
    private val wsWinsDataList: MutableList<RecordNewEvent> = mutableListOf()//ws的最新大奖
    private val winsShowingData: MutableList<RecordNewEvent> = mutableListOf()//最新大奖显示在界面上的数据

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_wins_rank, this, true)
        orientation = VERTICAL
        minimumHeight = 453.dp
        initViews()
    }

    private var recordHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {

            var newItem: RecordNewEvent? = null
            if (rbtnLb.isChecked) {
                if (wsBetDataList.isNotEmpty()) {
                    newItem = wsBetDataList.removeAt(0)//ws 最新投注
                } else if (httpBetDataList.isNotEmpty()) {
                    newItem = httpBetDataList.removeAt(0)
                }
            } else if (rbtnLbw.isChecked) {
                if (wsWinsDataList.isNotEmpty()) {
                    newItem = wsWinsDataList.removeAt(0)//ws 最新大奖

                } else if (httpWinsDataList.isNotEmpty()) {
                    newItem = httpWinsDataList.removeAt(0)
                }
            }

            if (newItem != null) {
                reecordAdapterNotify(newItem)
            }

            postLoop()
        }
    }

    fun setUp(fragment: Fragment, blockWinsRequest: () -> Unit, blockBetRequest: () -> Unit) {
        fragment.doOnDestory { stopPostLoop() }
        winsRequest = blockWinsRequest
        betRequest = blockBetRequest
        postLoop()
    }

    fun loadData() {
        clearAllData()
        winsRequest?.invoke()
        betRequest?.invoke()
    }
    fun clearAllData(){
        httpBetDataList.clear()
        httpWinsDataList.clear()
        wsBetDataList.clear()
        wsWinsDataList.clear()
        gameRecordAdapter.setList(listOf())
    }

    private fun postLoop() {
        recordHandler.sendEmptyMessageDelayed(3, (Random.nextLong(1000) + 400))
    }

    private fun stopPostLoop() {
        recordHandler.removeCallbacksAndMessages(null)
    }

    fun setTipsIcon(@DrawableRes icon: Int) {
        findViewById<ImageView>(R.id.ivTipsIcon).setImageResource(icon)
    }

    private fun initViews() {
        // 暂时保持原样
//        rvOkgameRecord.itemAnimator = null
        rvOkgameRecord.adapter = gameRecordAdapter
        rvOkgameRecord.addItemDecoration(RCVDecoration()
            .setDividerHeight(2f)
            .setColor(rvOkgameRecord.context.getColor(R.color.color_EEF3FC))
            .setMargin(10.dp.toFloat())
        )

        findViewById<RadioGroup>(R.id.rGroupRecord).setOnCheckedChangeListener { _, checkedId ->
            if (winsRequest == null || betRequest == null) {
                return@setOnCheckedChangeListener
            }

            if (checkedId == R.id.rbtn_lb) {
                if (httpBetDataList.isNullOrEmpty()) {
                    betRequest!!.invoke()
                }
                if (gameRecordAdapter.data.isNotEmpty()) {
                    resetData(winsShowingData, betShowingData)
                }

                return@setOnCheckedChangeListener
            }

            if (httpWinsDataList.isNullOrEmpty()) {
                winsRequest!!.invoke()
            }

            if (gameRecordAdapter.data.isNotEmpty()) {
                resetData(betShowingData, winsShowingData)
            }
        }
    }

    private fun resetData(oldDataList: MutableList<RecordNewEvent>, newDataList: MutableList<RecordNewEvent>) {
        oldDataList.clear()
        oldDataList.addAll(gameRecordAdapter.data)
        gameRecordAdapter.setList(newDataList)
    }

    private fun reecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= 10) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
    }

    fun onNewWSWinsData(data: RecordNewEvent) {
        wsWinsDataList.add(data)
    }

    fun onNewWSBetData(data: RecordNewEvent) {
        wsBetDataList.add(data)
    }

    fun onNewHttpWinsData(dataList: List<RecordNewEvent>) {
        httpWinsDataList.addAll(dataList)
    }

    fun onNewHttpBetData(dataList: List<RecordNewEvent>) {
        httpBetDataList.addAll(dataList)
    }

}