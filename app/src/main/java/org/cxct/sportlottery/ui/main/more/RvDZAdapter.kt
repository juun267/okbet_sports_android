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

class RvDZAdapter : RecyclerView.Adapter<RvDZAdapter.ItemViewHolder>() {

    private var mIsEnabled = true //避免快速連點，所有的 item 一次只能點擊一個
    private var mDataList: MutableList<GameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_dz_game_rv, viewGroup, false)
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
        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIvImage: ImageView = itemView.findViewById(R.id.iv_image)
        private val mTvTitle: TextView = itemView.findViewById(R.id.tv_title)

        fun bind(data: ThirdDictValues?) {
            val iconUrl = GameConfigManager.getThirdGameIconUrl(data?.gameCategory, data?.h5ImageName)
            Glide.with(itemView.context)
                .load(iconUrl)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvImage)

            mTvTitle.text = data?.englishName

            itemView.setOnClickListener {
                if (!mIsEnabled) return@setOnClickListener
                avoidFastDoubleClick()
                mOnSelectThirdGameListener?.onClick(data)
            }
        }
    }
}