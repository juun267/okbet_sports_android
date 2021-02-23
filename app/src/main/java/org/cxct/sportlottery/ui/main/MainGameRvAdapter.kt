package org.cxct.sportlottery.ui.main

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
import org.cxct.sportlottery.ui.main.entity.HomeGameItemData

class MainGameRvAdapter(private val spanCount: Int) : RecyclerView.Adapter<MainGameRvAdapter.ItemViewHolder>() {

    private var mDataList: MutableList<HomeGameItemData> = mutableListOf()
    private var mOnSelectThirdGameListener: OnSelectItemListener<ThirdDictValues?>? = null
    private val mRequestOptions = RequestOptions()
        .centerCrop()
        .placeholder(R.drawable.ic_image_load)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemViewHolder {
        val itemLayout = LayoutInflater.from(viewGroup.context).inflate(R.layout.content_main_game_rv, viewGroup, false)
        val width = viewGroup.measuredWidth
        itemLayout.layoutParams.width = width / spanCount
        return ItemViewHolder(itemLayout)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        try {
            val infiniteRvPosition = position % mDataList.size
            val entity = mDataList[infiniteRvPosition]
            val data = entity.thirdGameData
            viewHolder.bind(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return Integer.MAX_VALUE
    }

    //20190115 避免快速連點，所有的 item 一次只能點擊一個
    private var mIsEnabled = true
    fun avoidFastDoubleClick() {
        mIsEnabled = false
        Handler().postDelayed({ mIsEnabled = true }, 500)
    }

    fun setData(newDataList: MutableList<HomeGameItemData>?) {
        mDataList = newDataList ?: mutableListOf() //若 newDataList == null，則給一個空 list
        notifyDataSetChanged()
    }

    //設定選擇 遊戲 的listener
    fun setOnSelectThirdGameListener(onSelectItemListener: OnSelectItemListener<ThirdDictValues?>?) {
        mOnSelectThirdGameListener = onSelectItemListener
    }


    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mIvImage: ImageView = itemView.findViewById(R.id.iv_image)

        fun bind(data: ThirdDictValues?) {
            //TODO simon test 第三方遊戲icon獲取
//            val imgDrawable = GameConfigManager.getThirdGameIconFromLocal(GameConfigManager.ThirdGameIconType.HOME,
//                viewHolder.ivImage.context,
//                data?.gameCategory,
//                data?.firmCode)
            val imgDrawable = R.drawable.ic_image_load
            Glide.with(itemView.context)
                .load(imgDrawable)
                .apply(mRequestOptions)
                .thumbnail(0.5f)
                .into(mIvImage)

            itemView.setOnClickListener {
                if (!mIsEnabled) return@setOnClickListener
                avoidFastDoubleClick()
                mOnSelectThirdGameListener?.onClick(data)
            }
        }
    }

}
