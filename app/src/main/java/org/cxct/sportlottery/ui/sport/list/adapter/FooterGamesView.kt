package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.enterThirdGame
import org.cxct.sportlottery.util.loginedRun
import org.cxct.sportlottery.view.transform.TransformInDialog
import splitties.views.dsl.core.add

class FooterGamesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val gameAdapter by lazy { FooterGameAdapter(::onFavoriteClick, ::onGameClick) }
    private lateinit var gameList: RecyclerView
    private lateinit var fragment: BaseFragment<*>
    private lateinit var okGamesViewModel: OKGamesViewModel
    private lateinit var nomoeText: TextView

    init {
        12.dp.let { setPadding(it, 0, it, 0) }
        orientation = VERTICAL
        setBackgroundResource(R.color.color_F8F9FD)
        addNomoreText()
        initGameList()
        addOKBingo()
    }

    private fun addNomoreText() {
        nomoeText = AppCompatTextView(context)
        nomoeText.setPadding(0, 10.dp, 0, 0)
        nomoeText.setTextColor(ContextCompat.getColor(context, R.color.color_BEC7DC))
        nomoeText.gravity = Gravity.CENTER
        nomoeText.textSize = 12f
        nomoeText.text = "- No more -"
        addView(nomoeText, ViewGroup.LayoutParams(-1, -2))
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
        titleLayout.add(img, LayoutParams(dp24, dp24))

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

    private fun initGameList() {
        gameList = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = gameAdapter
        }

        addView(gameList)
    }

    fun setUp(fragment: BaseFragment<*>, viewmodel: OKGamesViewModel) {
        this.fragment = fragment
        this.okGamesViewModel = viewmodel
        initObserver(fragment, viewmodel)
        viewmodel.getOKGamesHall()
    }

    private fun initObserver(lifecycleOwner: LifecycleOwner, viewmodel: OKGamesViewModel) = viewmodel.run {
        gameHall.observe(lifecycleOwner) {
            gameAdapter.setupData(it.categoryList?.getOrNull(0)?.gameList?.toMutableList(),
                it.categoryList?.getOrNull(1)?.gameList?.toMutableList())

        }

        collectOkGamesResult.observe(lifecycleOwner) {
            gameAdapter.updateFavoriteStatu(it.second)
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
    }

    private fun onFavoriteClick(view: View, gameBean: OKGameBean) {
        loginedRun(context) {
            okGamesViewModel.collectGame(gameBean)
            view.animDuang(1.3f)
        }

    }

    private fun onGameClick(gameBean: OKGameBean) {
        loginedRun(context) {
            okGamesViewModel.requestEnterThirdGame(gameBean, fragment)
            okGamesViewModel.addRecentPlay(gameBean)
        }
    }

}