package org.cxct.sportlottery.ui.main.next

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

class RvLiveAdapter : RecyclerView.Adapter<RvLiveAdapter.ItemViewHolder>() {

    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private var mDataList: MutableList<GameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_live_game_rv, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = mDataList[position].thirdGameData
        holder.bind(data)
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

        //若不為偶數，最一項顯示 coming soon item
        if (mDataList.size % 2 != 0)
            mDataList.add(GameItemData(null))

        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }


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