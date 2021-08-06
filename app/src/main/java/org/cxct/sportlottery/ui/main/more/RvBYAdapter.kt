package org.cxct.sportlottery.ui.main.more

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

class RvBYAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private var mDataList: MutableList<GameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    private enum class ViewType { HEADER, LEFT, RIGHT }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.HEADER.ordinal -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_by_game_rv_header, viewGroup, false)
                HeaderViewHolder(layout)
            }
            ViewType.LEFT.ordinal -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_by_game_rv_left, viewGroup, false)
                ItemViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_by_game_rv_right, viewGroup, false)
                ItemViewHolder(layout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = mDataList[position].thirdGameData
                holder.bind(position, data)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> ViewType.HEADER.ordinal
            position % 2 == 1 -> ViewType.LEFT.ordinal
            else -> ViewType.RIGHT.ordinal
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun avoidFastDoubleClick() {
        mIsEnabled = false
        Handler().postDelayed({ mIsEnabled = true }, 500)
    }

    fun setData(newDataList: List<GameItemData>?) {
        mDataList = mutableListOf(GameItemData(null)) //開頭添加一項為 Header
        newDataList?.let { mDataList.addAll(it) }
        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIvBg: ImageView = itemView.findViewById(R.id.iv_bg)
        private val mIvLogo: ImageView = itemView.findViewById(R.id.iv_logo)
        private val mBtnStart: ImageView = itemView.findViewById(R.id.btn_start)
        private val mTvTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(position: Int, data: ThirdDictValues?) {
            val bgCode = position.toString()
            val bgUrl = GameConfigManager.getThirdGameHallIconUrl(data?.gameCategory, bgCode)
            Glide.with(itemView.context)
                .load(bgUrl)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvBg)

            val logoUrl = GameConfigManager.getThirdGameLogoIconUrl(data?.gameCategory, data?.firmCode)
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