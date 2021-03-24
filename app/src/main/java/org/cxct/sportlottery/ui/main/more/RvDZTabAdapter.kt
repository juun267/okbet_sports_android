package org.cxct.sportlottery.ui.main.more

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
import org.cxct.sportlottery.ui.main.entity.GameTabData
import org.cxct.sportlottery.util.GameConfigManager

class RvDZTabAdapter(var mSelectPosition: Int) : RecyclerView.Adapter<RvDZTabAdapter.ItemViewHolder>() {

    private var mDataList: MutableList<GameTabData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<GameTabData?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val layout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_dz_game_tab, viewGroup, false)
        return ItemViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = mDataList[position]
        holder.bind(position, data)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun setData(newDataList: MutableList<GameTabData>?) {
        mDataList = newDataList ?: mutableListOf() //若 newDataList == null，則給一個空 list
        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<GameTabData?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIvImage: ImageView = itemView.findViewById(R.id.iv_image)

        fun bind(position: Int, data: GameTabData?) {
            val iconUrl = GameConfigManager.getThirdGameHallIconUrl(data?.gameCategory?.code, data?.gameFirm?.firmCode)
            Glide.with(itemView.context)
                .load(iconUrl)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvImage)

            itemView.setOnClickListener {
                mSelectPosition = position
                notifyDataSetChanged()
                mOnSelectThirdGameListener?.onClick(data)
            }

            mIvImage.alpha = if (mSelectPosition == position) 1f else 0.2f
        }
    }
}