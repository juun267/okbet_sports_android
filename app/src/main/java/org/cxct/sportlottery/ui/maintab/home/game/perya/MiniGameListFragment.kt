package org.cxct.sportlottery.ui.maintab.home.game.perya

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.setLinearLayoutManager
import org.cxct.sportlottery.databinding.FragmentMinigameListBinding
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.maintab.games.OKGamesViewModel
import org.cxct.sportlottery.util.AppFont
import org.cxct.sportlottery.util.DisplayUtil.dp

class MiniGameListFragment: BaseFragment<OKGamesViewModel, FragmentMinigameListBinding>() {

    override fun onInitView(view: View) {
        binding.recyclerView.setLinearLayoutManager()
        binding.recyclerView.adapter = GameListAdapter()
    }

    private class GameListAdapter: BaseQuickAdapter<String, BaseViewHolder>(0) {

        init {
            setNewInstance(mutableListOf("as", "as", "as", "as", "as", "as", "as", "as"))
        }

        private val imgId = View.generateViewId()
        private val textId = View.generateViewId()
        override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
            val dp8 = 8.dp
            val dp12 = 12.dp
            val root = CardView(context)
            root.cardElevation = 0f
            root.radius = dp8.toFloat()
            val lp = FrameLayout.LayoutParams(-1, 140.dp)
            lp.setMargins(dp12, dp8, dp12, 0)
            root.layoutParams = lp

            val lpChild = FrameLayout.LayoutParams(-1, -1)
            val img = AppCompatImageView(context)
            img.scaleType = ImageView.ScaleType.CENTER_CROP
            img.id = imgId
            root.addView(img, lpChild)

            val text = AppCompatTextView(context)
            text.id = textId
            text.typeface = AppFont.helvetica_bold
            text.gravity = Gravity.CENTER
            text.setTextColor(Color.WHITE)
            text.textSize = 18f
            text.setText(R.string.M013)
            root.addView(text, lpChild)

            return BaseViewHolder(root)
        }

        override fun convert(holder: BaseViewHolder, item: String) {
            if (holder.bindingAdapterPosition < getDefItemCount() - 1) {
                holder.setImageResource(imgId, R.drawable.img_mini_game_cover)
                holder.setVisible(textId, false)
            } else {
                holder.setImageResource(imgId, R.drawable.img_minigame_unknow)
                holder.setVisible(textId, true)
            }
        }

    }


}