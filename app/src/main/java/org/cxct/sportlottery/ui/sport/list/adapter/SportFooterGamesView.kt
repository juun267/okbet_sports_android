package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
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
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.common.extentions.visible
import org.cxct.sportlottery.databinding.ItemGameCategroyBinding
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.GameChildAdapter
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.view.dialog.TrialGameDialog
import org.cxct.sportlottery.view.transform.TransformInDialog

class SportFooterGamesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnItemClickListener {

    private val okGamesAdapter by lazy { GameChildAdapter(::onFavoriteClick).apply { setOnItemClickListener(this@SportFooterGamesView) } }
    private lateinit var fragment: BaseFragment<*>
    private lateinit var okGamesViewModel: OKGamesViewModel
    private lateinit var noMoreText: TextView
    private lateinit var okgamesBinding: ItemGameCategroyBinding
    private val gameItemViewPool by lazy {
        RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(0, 20) }
    }

    init {
        setPadding(0, 0, 0, 10.dp)
        orientation = VERTICAL
        setBackgroundResource(R.color.color_F8F9FD)
        addNomoreText()
        initOKGameList()
//        addOKBingo()
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


    private fun addOKBingo() {

        val dp24 = 24.dp
        val titleLayout = LinearLayout(context)
        titleLayout.gravity = Gravity.CENTER_VERTICAL
        titleLayout.layoutParams = LayoutParams(-1, dp24).apply {
            topMargin = 16.dp
            bottomMargin = 8.dp
        }

        val img = AppCompatImageView(context)
        img.setImageResource(R.drawable.ic_okgame_label_bingo)
        titleLayout.addView(img, LayoutParams(dp24, dp24))

        val text = AppCompatTextView(context)
        text.text = "OKBingo"
        text.textSize = 16f
        text.typeface = Typeface.DEFAULT_BOLD
        text.setTextColor(ContextCompat.getColor(context, R.color.color_14366B))
        val textParam = LayoutParams(0, -2, 1f)
        textParam.leftMargin = 4.dp
        titleLayout.addView(text, textParam)
        addView(titleLayout)

        val linearLayout = LinearLayout(context)
        val bingoRush = ImageView(context)
        bingoRush.setImageResource(R.drawable.img_bingo_rush)
        linearLayout.addView(bingoRush, LayoutParams(0, -2, 1f).apply { rightMargin = 10.dp })
        val bingoMega = ImageView(context)
        bingoMega.setImageResource(R.drawable.img_bingo_mega)
        linearLayout.addView(bingoMega, LayoutParams(0, -2, 1f))
        addView(linearLayout)
    }

    private fun initOKGameList() {
        okgamesBinding = ItemGameCategroyBinding.inflate(LayoutInflater.from(context), this, true)
        okgamesBinding.rvGameItem.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        okgamesBinding.rvGameItem.adapter = okGamesAdapter
        okgamesBinding.rvGameItem.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_10))
        okgamesBinding.rvGameItem.setRecycledViewPool(gameItemViewPool)
        okgamesBinding.tvName.setText(R.string.J203)
        okgamesBinding.ivIcon.setImageResource(R.drawable.ic_okgame_label_games)
        okgamesBinding.tvMore.setOnClickListener { (fragment.activity as MainTabActivity?)?.jumpToOKGames() }
        okgamesBinding.root.gone()
    }

    fun setUp(fragment: BaseFragment<*>, viewmodel: OKGamesViewModel) {
        this.fragment = fragment
        this.okGamesViewModel = viewmodel
        initObserver(fragment, viewmodel)
//        viewmodel.getSportOKLive() // 没有数据暂时不开放 2023.06.21
        viewmodel.getSportOKGames()
    }

    private fun initObserver(lifecycleOwner: BaseFragment<*>, viewmodel: OKGamesViewModel) = viewmodel.run {

        sportOKGames.observe(lifecycleOwner) {
            if (it.isNullOrEmpty()) {
                return@observe
            }
            okgamesBinding.root.visible()
            okGamesAdapter.setNewInstance(it.toMutableList())
        }
//        sportOKLives.observe(lifecycleOwner) { gameAdapter.setupOKLives(it, ::onMoreOKLives) }

        collectOkGamesResult.observe(lifecycleOwner) {
            onFavoriteStatus(okGamesAdapter, it.second)
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
                val trialDialog = TrialGameDialog(context)
                if (isVisible) {
                    //点击进入游戏
                    trialDialog.setEnterGameClick {
                        enterThirdGame(lifecycleOwner, viewmodel, it.second, it.first)
                    }
                    trialDialog.show()
                }
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
            okGamesViewModel.collectGame(gameBean)
            view.animDuang(1.3f)
        }

    }

    private fun onGameClick(gameBean: OKGameBean) {
        if (LoginRepository.isLogined()) {
            okGamesViewModel.requestEnterThirdGame(gameBean, fragment)
            okGamesViewModel.addRecentPlay(gameBean)
        } else {
            fragment.loading()
            okGamesViewModel.requestEnterThirdGameNoLogin(gameBean.firmType, gameBean.gameCode, gameBean.thirdGameCategory)
        }

    }

    private fun onMoreOKLives() {

    }

    fun sportNoMoreEnable(enable: Boolean) {
        noMoreText.isVisible = enable
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        onGameClick(adapter.getItem(position) as OKGameBean)
    }


}