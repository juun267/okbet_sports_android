package org.cxct.sportlottery.ui.main.next

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.util.GameConfigManager

class RvBYAdapter : RecyclerView.Adapter<RvBYAdapter.ItemViewHolder>() {

    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private var mDataList: MutableList<GameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    private enum class ViewType { LEFT, RIGHT }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = when (viewType) {
            ViewType.LEFT.ordinal -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.content_by_game_rv_left, viewGroup, false)
            }
            else -> {
                LayoutInflater.from(viewGroup.context).inflate(R.layout.content_by_game_rv_right, viewGroup, false)
            }
        }
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = mDataList[position].thirdGameData
        holder.bind(position, data)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 == 0) ViewType.LEFT.ordinal else ViewType.RIGHT.ordinal
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun avoidFastDoubleClick() {
        mIsEnabled = false
        Handler().postDelayed({ mIsEnabled = true }, 500)
    }

    fun setData(newDataList: MutableList<GameItemData>?) {
        mDataList = newDataList ?: mutableListOf() //若 newDataList == null，則給一個空 list
        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIvBg: ImageView = itemView.findViewById(R.id.iv_bg)
        private val mIvLogo: ImageView = itemView.findViewById(R.id.iv_logo)
        private val mBtnStart: ImageView = itemView.findViewById(R.id.btn_start)
        private val mTvTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(position: Int, data: ThirdDictValues?) {
            val bgCode = (position + 1).toString()
            val bgUrl = GameConfigManager.getThirdGameHallIconUrl(data?.gameCategory, bgCode)
            Glide.with(itemView.context)
                .load(bgUrl)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvBg)

            val logoUrl = GameConfigManager.getThirdGameHallIconUrl(data?.gameCategory, data?.firmCode)
            Glide.with(itemView.context)
                .load(logoUrl)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvLogo)

            mBtnStart.setOnClickListener {
                if (!mIsEnabled) return@setOnClickListener
                avoidFastDoubleClick()
                mOnSelectThirdGameListener?.onClick(data)
            }

            mTvTitle.text = data?.firmName
        }
    }
}