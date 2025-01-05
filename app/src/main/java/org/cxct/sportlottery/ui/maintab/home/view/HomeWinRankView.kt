package org.cxct.sportlottery.ui.maintab.home.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.drake.spannable.addSpan
import com.drake.spannable.replaceSpan
import com.drake.spannable.span.CenterImageSpan
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ViewHomeWinRankBinding
import org.cxct.sportlottery.network.service.record.RecordNewEvent
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.home.MainHomeViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator
import org.cxct.sportlottery.util.isHalloweenStyle
import org.cxct.sportlottery.util.setTextTypeFace
import splitties.systemservices.layoutInflater
import splitties.views.dsl.constraintlayout.startOfParent
import splitties.views.dsl.core.horizontalMargin
import splitties.views.dsl.core.startMargin
import kotlin.random.Random

class HomeWinRankView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : ConstraintLayout(context, attrs, defStyle), OnItemClickListener {

    val binding = ViewHomeWinRankBinding.inflate(layoutInflater,this)
    val pageSize = 5

    private var winsRequest: (() -> Unit)? = null
    private var betRequest: (() -> Unit)? = null

    private val gameRecordAdapter by lazy { HomeWinRankAdapter().apply { setOnItemClickListener(this@HomeWinRankView) } }
    private val httpBetDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新投注
    private val httpWinsDataList: MutableList<RecordNewEvent> = mutableListOf()//接口返回的最新大奖
    private lateinit var fragment: BaseFragment<out MainHomeViewModel,*>

    init {
        initViews()
    }

    private var recordHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            var newItem: RecordNewEvent? = null
            if (binding.rbtnLb.isChecked) {
                if (httpBetDataList.isNotEmpty()) {
                    newItem = httpBetDataList.removeAt(0)
                }
            } else if (binding.rbtnLbw.isChecked) {
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
            if (binding.rbtnLb.isChecked) {
                betRequest?.invoke()
            } else if (binding.rbtnLbw.isChecked) {
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
        fragment.viewModel.recordBetHttp.observe(fragment) {
            if (!it.isNullOrEmpty()) {
                onNewHttpBetData(it.reversed())
            }
        }
        fragment.viewModel.recordWinHttp.observe(fragment) {
            if (!it.isNullOrEmpty()) {
                onNewHttpWinsData(it.reversed())
            }
        }
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
        gameRecordAdapter.setList(listOf())
    }

    private fun postLoop() {
        recordHandler.sendEmptyMessageDelayed(3, (Random.nextLong(1000) + 400))
    }

    private fun stopPostLoop() {
        recordHandler.removeCallbacksAndMessages(null)
    }

    private fun initViews() = binding.run{
        // 暂时保持原样
//        rvOkgameRecord.itemAnimator = null
        rvOkgameRecord.setHasFixedSize(false)
        rvOkgameRecord.adapter = gameRecordAdapter
//        rvOkgameRecord.addItemDecoration(RCVDecoration()
//            .setDividerHeight(2f)
//            .setColor(rvOkgameRecord.context.getColor(R.color.color_EEF3FC))
//            .setMargin(10.dp.toFloat())
//        )

        rGroupRecord.setOnCheckedChangeListener { _, checkedId ->
            if (rbtnLb.id== checkedId){
                rbtnLb.setTextTypeFace(Typeface.BOLD)
                rbtnLbw.setTextTypeFace(Typeface.NORMAL)
            }else{
                rbtnLb.setTextTypeFace(Typeface.NORMAL)
                rbtnLbw.setTextTypeFace(Typeface.BOLD)
            }
            changeTabHalloweenStyle()
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
        gameRecordAdapter.setList(newDataList)
    }

    private fun reecordAdapterNotify(it: RecordNewEvent) {
        if (gameRecordAdapter.data.size >= pageSize) {
            gameRecordAdapter.removeAt(gameRecordAdapter.data.size - 1)
        }
        gameRecordAdapter.addData(0, it)
    }

    private fun onNewHttpWinsData(dataList: List<RecordNewEvent>) {
        httpWinsDataList.clear()
        httpWinsDataList.addAll(dataList)
    }

    private fun onNewHttpBetData(dataList: List<RecordNewEvent>) {
        httpBetDataList.clear()
        httpBetDataList.addAll(dataList)
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        WinsDialog.newInstance(adapter.getItem(position) as RecordNewEvent) .show((context as AppCompatActivity).supportFragmentManager)
    }

    fun applyHalloweenStyle() = binding.run {
        (layoutParams as MarginLayoutParams).let {
            it.horizontalMargin = 0
            it.bottomMargin = 0
        }
        rvOkgameRecord.layoutParams.height = 280.dp
        (tvTitle.layoutParams as MarginLayoutParams).let {
            it.topMargin = 20.dp
            it.startMargin = 38.dp
        }

        (rGroupRecord.layoutParams as MarginLayoutParams).horizontalMargin = 12.dp

        ivChampion.setImageResource(R.drawable.img_monster_3_h)
        val championLP = ivChampion.layoutParams as MarginLayoutParams
        championLP.topMargin = 13.dp
        championLP.height = 72.dp
        championLP.width = 74.dp

        val bgView2 = View(context)
        bgView2.setBackgroundResource(R.drawable.img_home_wins_bg_h)
        val dp280 = 280.dp
        val lp2 = LayoutParams(dp280, dp280)
        lp2.topToTop = rvOkgameRecord.id
        lp2.bottomToBottom = rvOkgameRecord.id
        lp2.startToStart = rvOkgameRecord.id
        lp2.endToEnd = rvOkgameRecord.id
        addView(bgView2, (rvOkgameRecord.parent as ViewGroup).indexOfChild(rvOkgameRecord), lp2)

        val imageView = AppCompatImageView(context)
        imageView.id = View.generateViewId()
        imageView.setImageResource(R.drawable.ic_halloween_logo_6)
        val dp24 = 24.dp
        val lp3 = LayoutParams(dp24, dp24)
        lp3.marginEnd = 2.dp
        lp3.topToTop = tvTitle.id
        lp3.bottomToBottom = tvTitle.id
        lp3.endToStart = tvTitle.id
        addView(imageView, 0, lp3)

        (tvTitle.layoutParams as LayoutParams).startToEnd = imageView.id
        tvTitle.layoutParams = tvTitle.layoutParams

        val dp8 = 8.dp.toFloat()
        changeTabHalloweenStyle()
        rbtnLb.background = DrawableCreator.Builder()
            .setCornersRadius(0f, 0f, dp8, dp8)
            .setGradientAngle(270)
            .setGradientColor(Color.parseColor("#b3dff7"), Color.WHITE)
            .build()
        rbtnLbw.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.selector_indicate_trans_orange)
        rbtnLbw.background = DrawableCreator.Builder()
            .setCornersRadius(0f, 0f, dp8, dp8)
            .setGradientAngle(270)
            .setGradientColor(Color.parseColor("#ffd3be"), Color.WHITE)
            .build()

    }

    private fun changeTabHalloweenStyle() = binding.run {
        if (!isHalloweenStyle()) {
            return@run
        }
        if (rbtnLb.isChecked) {
            rbtnLb.text = "".addSpan("AAA", CenterImageSpan(context, R.drawable.ic_halloween_logo_7).setDrawableSize(26.dp))
                .addSpan(context.resources.getString(R.string.N708))
            rbtnLbw.setText(R.string.N709)
        } else {
            rbtnLbw.text = "".addSpan("AAA", CenterImageSpan(context, R.drawable.ic_halloween_logo_8).setDrawableSize(26.dp))
                .addSpan(context.resources.getString(R.string.N709))
            rbtnLb.setText(R.string.N708)
        }
    }

}