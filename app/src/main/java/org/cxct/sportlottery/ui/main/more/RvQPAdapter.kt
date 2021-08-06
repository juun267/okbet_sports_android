package org.cxct.sportlottery.ui.main.more

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import org.cxct.sportlottery.R
import org.cxct.sportlottery.interfaces.OnSelectItemListener
import org.cxct.sportlottery.network.third_game.third_games.ThirdDictValues
import org.cxct.sportlottery.ui.main.entity.GameItemData
import org.cxct.sportlottery.util.GameConfigManager

class RvQPAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private var mDataList: MutableList<GameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    private enum class ViewType { HEADER, ITEM }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ViewType.HEADER.ordinal -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_qp_game_rv_header, viewGroup, false)
                HeaderViewHolder(layout)
            }
            else -> {
                val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_qp_game_rv, viewGroup, false)
                ItemViewHolder(layout)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = mDataList[position].thirdGameData
                holder.bind(data)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ViewType.HEADER.ordinal
            else -> ViewType.ITEM.ordinal
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
        private val mIvImage: ImageView = itemView.findViewById(R.id.btn_game)

        fun bind(data: ThirdDictValues?) {
            if (data == null) {
                mIvImage.setImageResource(R.drawable.live_coming_soon)
                mIvImage.setOnClickListener {}

            } else {
                val iconUrl = GameConfigManager.getThirdGameHallIconUrl(data.gameCategory, data.firmCode)
                Glide.with(itemView.context)
                    .load(iconUrl)
                    .apply(mRequestOptions)
                    .thumbnail(0.5f)
                    .into(mIvImage)

                mIvImage.setOnClickListener {
                    if (!mIsEnabled) return@setOnClickListener
                    avoidFastDoubleClick()
                    mOnSelectThirdGameListener?.onClick(data)
                }
            }
        }
    }
}