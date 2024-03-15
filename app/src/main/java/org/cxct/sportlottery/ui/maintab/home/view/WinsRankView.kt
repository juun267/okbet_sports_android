package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.doOnDestory
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.OkGameRecordAdapter
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import kotlin.random.Random

class WinsRankView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : LinearLayout(context, attrs, defStyle), OnItemClickListener {

    private var winsRequest: (() -> Unit)? = null
    private var betRequest: (() -> Unit)? = null

    private val rbtnLb by lazy { findViewById<RadioButton>(R.id.rbtn_lb) }
    private val rbtnLbw by lazy { findViewById<RadioButton>(R.id.rbtn_lbw) }
    private val rvOkgameRecord by lazy { findViewById<RecyclerView>(R.id.rv_okgame_record) }

    private val gameRecordAdapter by lazy { OkGameRecordAdapter().apply { setOnItemClickListener(this@WinsRankView) } }
    private val httpBetDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新投注
    private val httpWinsDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新大奖
    private lateinit var fragment: BaseFragment<out MainHomeViewModel,*>

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
                if (httpBetDataList.isNotEmpty()) {
                    newItem = httpBetDataList.removeAt(0)
                }
            } else if (rbtnLbw.isChecked) {
                 if (httpWinsDataList.isNotEmpty()) {
                    newItem = httpWinsDataList.removeAt(0)
                }
            }

            if (newItem != null) {
                reecordAdapterNotify(newItem)
            }

            postLoop()
        }
    }
    private var callApiHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (rbtnLb.isChecked) {
                betRequest?.invoke()
            } else if (rbtnLbw.isChecked) {
                winsRequest?.invoke()
            }
            postCallApiLoop()
        }
    }
    private fun postCallApiLoop() {
        callApiHandler.sendEmptyMessageDelayed(0, 60*1000)
    }

    private fun stopCallApiLoop() {
        callApiHandler.removeCallbacksAndMessages(null)
    }

    fun setUp(fragment: BaseFragment<out MainHomeViewModel,*>,  blockBetRequest: () -> Unit, blockWinsRequest: () -> Unit) {
        this.fragment = fragment
        betRequest = blockBetRequest
        winsRequest = blockWinsRequest
        startLoopCall()
    }
    fun startLoopCall(){
        postLoop()
        postCallApiLoop()
        loadData()
    }
    fun stopLoopCall(){
        stopCallApiLoop()
        stopPostLoop()
    }

    fun loadData() {
        clearAllData()
        betRequest?.invoke()
        winsRequest?.invoke()
    }
    fun clearAllData(){
        httpBetDataList.clear()
        httpWinsDataList.clear()
        gameRecordAdapter.setNewInstance(null)
    }

    private fun postLoop() {
        stopPostLoop()
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
                    gameRecordAdapter.setNewInstance(null)
                }

                return@setOnCheckedChangeListener
            }

            if (httpWinsDataList.isNullOrEmpty()) {
                winsRequest!!.invoke()
            }

            if (gameRecordAdapter.data.isNotEmpty()) {
                gameRecordAdapter.setNewInstance(null)
            }
        }
    }

    private fun resetData(oldDataList: MutableList<RecordNewEvent>, newDataList: MutableList<RecordNewEvent>) {
        oldDataList.clear()
        oldDataList.addAll(gameRecordAdapter.data)
        gameRecordAdapter.setNewInstance(newDataList)
    }

    private fun reecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= 10) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
    }

    fun onNewHttpWinsData(dataList: List<RecordNewEvent>) {
        httpWinsDataList.clear()
        httpWinsDataList.addAll(dataList)
    }

    fun onNewHttpBetData(dataList: List<RecordNewEvent>) {
        httpBetDataList.clear()
        httpBetDataList.addAll(dataList)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        WinsDialog.newInstance(adapter.getItem(position) as RecordNewEvent).show((context as AppCompatActivity).supportFragmentManager)
    }

    private fun enterGame(recordNewEvent: RecordNewEvent) {
        val activity = fragment.activity as MainTabActivity? ?: return
        val firmType = "${recordNewEvent.firmType}"
        val gameCode = "${recordNewEvent.gameCode}"
        val gameEntryTagName = "${recordNewEvent.gameEntryType}"
        activity.requestEnterThirdGame(firmType, gameCode, firmType, gameEntryTagName)
    }

}