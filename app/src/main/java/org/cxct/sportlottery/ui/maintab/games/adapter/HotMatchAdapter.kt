package org.cxct.sportlottery.ui.maintab.games.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.application.MultiLanguagesApplication
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemHotGameViewBinding
import org.cxct.sportlottery.network.sport.publicityRecommend.Recommend
import org.cxct.sportlottery.ui.maintab.home.HomeRecommendListener
import org.cxct.sportlottery.view.OKVideoPlayer

class HotMatchAdapter(
    val lifecycleOwner: LifecycleOwner,
    val listener: HomeRecommendListener,
) :
    RecyclerView.Adapter<ItemHotMatchHolder>() {

    var playerView: OKVideoPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHotMatchHolder {
        return ItemHotMatchHolder(
            lifecycleOwner,
            ItemHotGameViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }


    var oddsType: OddsType = MultiLanguagesApplication.mInstance.mOddsType.value ?: OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }
    var data: List<Recommend> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ItemHotMatchHolder, position: Int) {

        val itemData = data[position]
        holder.bind(data = itemData, oddsType = oddsType)
    }

    override fun getItemCount(): Int = data.size

    //缓存订阅过的赛事
    private var subCacheList= hashSetOf<String>()
    private lateinit var subBlock:(data:Recommend)->Unit

    /**
     * 设置赛事绑定block
     */
    fun setOnViewAttach(block:(data:Recommend)->Unit){
        subBlock=block
    }

    /**
     * 清除记录订阅过的赛事
     */
    fun clearSubCache(){
        subCacheList.clear()
    }
    override fun onViewAttachedToWindow(holder: ItemHotMatchHolder) {
        super.onViewAttachedToWindow(holder)
        //检测是否已订阅
        if(subCacheList.contains(holder.data.id)){
            return
        }
        //订阅该赛事
        subBlock(holder.data)
        subCacheList.add(holder.data.id)
    }

}