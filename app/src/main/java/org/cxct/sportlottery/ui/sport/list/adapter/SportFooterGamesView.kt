package org.cxct.sportlottery.ui.sport.list.adapter

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.android.synthetic.main.view_home_bottom.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.GameEntryType
import org.cxct.sportlottery.common.extentions.animDuang
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.net.games.data.OKGameBean
import org.cxct.sportlottery.repository.LoginRepository
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.MainTabActivity
import org.cxct.sportlottery.ui.maintab.games.GameChildAdapter
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.ui.maintab.games.SportFootGameAdapter
import org.cxct.sportlottery.ui.maintab.home.view.HomeBottomView
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.drawable.DrawableCreator

class SportFooterGamesView @JvmOverloads constructor(
    context: Context,
    val esportTheme: Boolean = false,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), OnItemClickListener {

    private val okGamesAdapter by lazy { SportFootGameAdapter(::onFavoriteClick,gameEntryType = GameEntryType.OKGAMES, showFavorite = false,esportTheme=esportTheme).apply { setOnItemClickListener(this@SportFooterGamesView) } }
    private lateinit var fragment: BaseFragment<*,*>
    private lateinit var okGamesViewModel: OKGamesViewModel
    private lateinit var noMoreText: TextView
    private lateinit var moreLabelLayout: LinearLayout
    lateinit var homeBottomView: HomeBottomView
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
        if (esportTheme){
            homeBottomView.linAward.setBackgroundResource(R.drawable.bg_white_alpha90_radius_8)
        }
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
        text.paint.isFakeBoldText = true
        text.setText(R.string.P230)
        moreLabelLayout.addView(text, LayoutParams(0, -2, 1f))


        val recyclerView = RecyclerView(context)
        recyclerView.setPadding(dp12, 0, dp12, 0)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setLinearLayoutManager(RecyclerView.HORIZONTAL)
        recyclerView.adapter = okGamesAdapter
        recyclerView.addItemDecoration(SpaceItemDecoration(context, R.dimen.margin_8))
        recyclerView.setRecycledViewPool(gameItemViewPool)
        addView(recyclerView)
    }

    private fun initBottomView() {
        setBackgroundResource(R.color.color_FFFFFF)
        homeBottomView = HomeBottomView(context)
        homeBottomView.apply {
            findViewById<View>(R.id.linPayment).gone()
            findViewById<View>(R.id.homeFollowView).gone()
            setBackgroundResource(R.color.color_FFFFFF)
            DrawableCreator.Builder()
                .setSolidColor(ContextCompat.getColor(context, R.color.color_F7F7F7))
                .setCornersRadius(8.dp.toFloat())
                .build().let {
                    findViewById<View>(R.id.linAward).background=it
                }

        }
        addView(homeBottomView)
    }
    fun setUp(fragment: BaseFragment<*,*>, viewmodel: OKGamesViewModel) {
        this.fragment = fragment
        this.okGamesViewModel = viewmodel
        initObserver(fragment, viewmodel)
        viewmodel.getFooterGames()
        homeBottomView.bindServiceClick(fragment.parentFragmentManager)
        okGamesAdapter.bindLifecycleOwner(fragment)
    }

    private fun initObserver(lifecycleOwner: BaseFragment<*,*>, viewmodel: OKGamesViewModel) = viewmodel.run {

        sportFooterGames.observe(lifecycleOwner) {
            if (it.isNullOrEmpty()) {
                return@observe
            }
            moreLabelLayout.isVisible = it.isNotEmpty()
            okGamesAdapter.setNewInstance(it.toMutableList())
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
        (fragment.activity as MainTabActivity?)?.enterThirdGame(gameBean)
    }
    fun sportNoMoreEnable(enable: Boolean) {
        noMoreText.isVisible = enable
    }

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        onGameClick(adapter.getItem(position) as OKGameBean)
    }


}