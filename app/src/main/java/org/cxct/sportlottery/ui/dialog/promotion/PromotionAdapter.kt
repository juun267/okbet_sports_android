package org.cxct.sportlottery.ui.dialog.promotion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerAdapter
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.DialogPromotionPopupBinding
import org.cxct.sportlottery.databinding.ItemPromotionBinding
import timber.log.Timber

class PromotionAdapter(val promotionList: List<PromotionData>) :
    BannerAdapter<PromotionData, RecyclerView.ViewHolder>(promotionList) {
    val requestOptions = RequestOptions()
        .placeholder(R.drawable.ic_image_load)
        .error(R.drawable.ic_image_broken)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .dontTransform()

    override fun onCreateHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return PromotionViewHolder(
            ItemPromotionBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
        )
    }

    override fun onBindView(holder: RecyclerView.ViewHolder?, data: PromotionData?, position: Int, size: Int) {
        val itemData = promotionList[position]
        when (holder) {
            is PromotionViewHolder -> {
                holder.bind(itemData)
            }
        }
    }

    inner class PromotionViewHolder(val binding: ItemPromotionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData: PromotionData) {
            Timber.e("Dean, url = ${itemData.imgUrl}")
            binding.tvTitle.text = itemData.title
            Glide.with(binding.ivImage)
                .load(itemData.imgUrl)
                .apply(requestOptions)
                .into(binding.ivImage)
        }
    }
}

/*
*//**
 * 自定义布局，下面是常见的图片样式，更多实现可以看demo，可以自己随意发挥
 *//*
class ImageAdapter(mDatas: List<DataBean?>?) :
    BannerAdapter<DataBean?, ImageAdapter.BannerViewHolder?>(mDatas) {
    //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView = ImageView(parent.getContext())
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        imageView.setLayoutParams(
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
        return BannerViewHolder(imageView)
    }

    override fun onBindView(holder: BannerViewHolder, data: DataBean, position: Int, size: Int) {
        holder.imageView.setImageResource(data.imageRes)
    }

    inner class BannerViewHolder(@NonNull view: ImageView) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView

        init {
            imageView = view
        }
    }*/
