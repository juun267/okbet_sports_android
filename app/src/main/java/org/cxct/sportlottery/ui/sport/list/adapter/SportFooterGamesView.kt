package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.repository.StaticData
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.GameChildAdapter
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.home.view.HomeButtomView
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.TrialGameDialog
import org.cxct.sportlottery.view.transform.TransformInDialog

class SportFooterGamesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnItemClickListener {

    private val okGamesAdapter by lazy { GameChildAdapter(::onFavoriteClick,gameEntryType = GameEntryType.OKGAMES, showFavorite = false).apply { setOnItemClickListener(this@SportFooterGamesView) } }
    private lateinit var fragment: BaseFragment<*>
    private lateinit var okGamesViewModel: OKGamesViewModel
    private lateinit var noMoreText: TextView
    private lateinit var moreLabelLayout: LinearLayout
    private lateinit var homeButtomView: HomeButtomView
    private val gameItemViewPool by lazy {
        RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(0, 20) }
    }

    init {
        setPadding(0, 0, 0, 10.dp)
        orientation = VERTICAL
        setBackgroundResource(R.color.color_F8F9FD)
        addNomoreText()
        initOKGameList()
        initBottomView()
    }

    private fun addNomoreText() {
        noMoreText = AppCompatTextView(context)
        noMoreText.setPadding(0, 10.dp, 0, 0)
        noMoreText.setTextColor(ContextCompat.getColor(context, R.color.color_BEC7DC))
        noMoreText.gravity = Gravity.CENTER
        noMoreText.textSize = 12f
        noMoreText.text = "- ${resources.getString(R.string.N111)} -"
        addView(noMoreText, ViewGroup.LayoutParams(-1, -2))
    }

    private fun initOKGameList() {
        moreLabelLayout = LinearLayout(context)
        val dp12 = 12.dp
        moreLabelLayout.setPadding(dp12, 0, dp12, 0)
        val params = LayoutParams(-1, -2)
        params.gravity = Gravity.CENTER_VERTICAL
        params.topMargin = 16.dp
        params.bottomMargin = 8.dp
        addView(moreLabelLayout, params)

//        val icon = AppCompatImageView(context)
//        icon.setImageResource(R.drawable.ic_home_okgames_title)
//        24.dp.let { moreLabelLayout.addView(icon, LayoutParams(it, it)) }

        val text = AppCompatTextView(context)
//        text.setPadding(4.dp, 0, 0, 0)
        text.setTextColor(ContextCompat.getColor(context, R.color.color_000000))
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
        text.paint.style = Paint.Style.FILL_AND_STROKE
        text.paint.strokeWidth = 0.9f
        text.setText(R.string.casino)
        moreLabelLayout.addView(text, LayoutParams(0, -2, 1f))


        val recyclerView = RecyclerView(context)
        recyclerView.setPadding(dp12, 0, dp12, 0)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        recyclerView.adapter = okGamesAdapter
        recyclerView.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
        recyclerView.setRecycledViewPool(gameItemViewPool)
        addView(recyclerView)
    }
    private fun initBottomView() {
        homeButtomView = HomeButtomView(context)
        addView(homeButtomView)
    }
    fun setUp(fragment: BaseFragment<*>, viewmodel: OKGamesViewModel) {
        this.fragment = fragment
        this.okGamesViewModel = viewmodel
        initObserver(fragment, viewmodel)
        viewmodel.getFooterGames()
        homeButtomView.bindServiceClick(fragment.parentFragmentManager)
        okGamesAdapter.bindLifecycleOwner(fragment)
        okLiveAdapter.bindLifecycleOwner(fragment)
    }

    private fun initObserver(lifecycleOwner: BaseFragment<*>, viewmodel: OKGamesViewModel) = viewmodel.run {

        sportFooterGames.observe(lifecycleOwner) {
            if (it.isNullOrEmpty()) {
                return@observe
            }

            moreLabelLayout.isVisible = it.isNotEmpty()
            okGamesAdapter.setNewInstance(it.toMutableList())
        }

        enterThirdGameResult.observe(lifecycleOwner) {
            if (fragment.isVisible) enterThirdGame(fragment, viewmodel, it.second, it.first)
        }

        gameBalanceResult.observe(fragment) {
            it.getContentIfNotHandled()?.let { event ->
                TransformInDialog(event.first, event.second, event.third) {
                    enterThirdGame(fragment, viewmodel,it, event.first)
                }.show(fragment.childFragmentManager, null)
            }
        }

        enterTrialPlayGameResult.observe(lifecycleOwner) {
            lifecycleOwner.hideLoading()
            if (it == null) {
                //不支持试玩
                context.startLogin()
            } else {
                //试玩弹框
                val trialDialog = TrialGameDialog(context, it.first, it.second) { firmType, thirdGameResult->
                    enterThirdGame(lifecycleOwner, viewmodel, thirdGameResult, firmType)
                }
                trialDialog.show()
            }
        }
    }

    private fun onFavoriteStatus(adapter: GameChildAdapter, gameData: OKGameBean) {
        adapter.data.forEachIndexed { index, okGameBean ->
            if (okGameBean.id == gameData.id) {
                okGameBean.markCollect = gameData.markCollect
                adapter.notifyItemChanged(index, okGameBean)
                return
            }
        }
    }

    private fun onFavoriteClick(view: View, gameBean: OKGameBean) {
        loginedRun(context) {
            okGamesViewModel.collectGame(gameBean,gameBean.gameEntryType ?: GameEntryType.OKGAMES)
            view.animDuang(1.3f)
        }

    }

    private fun onGameClick(gameBean: OKGameBean) {
        if (LoginRepository.isLogined()) {
            okGamesViewModel.requestEnterThirdGame(gameBean, fragment)
            okGamesViewModel.addRecentPlay(gameBean)
        } else {
            fragment.loading()
            okGamesViewModel.requestEnterThirdGameNoLogin(gameBean)
        }
    }
    fun sportNoMoreEnable(enable: Boolean) {
        noMoreText.isVisible = enable
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        onGameClick(adapter.getItem(position) as OKGameBean)
    }


}