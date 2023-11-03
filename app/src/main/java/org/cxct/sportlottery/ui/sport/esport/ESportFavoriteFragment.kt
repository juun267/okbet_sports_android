package org.cxct.sportlottery.ui.sport.esport

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.extentions.gone
import org.cxct.sportlottery.databinding.FragmentSportList2Binding
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.sport.CategoryItem
import org.cxct.sportlottery.network.sport.Item
import org.cxct.sportlottery.ui.sport.list.SportListViewModel
import org.cxct.sportlottery.util.ToastUtil
import org.cxct.sportlottery.view.layoutmanager.ScrollCenterLayoutManager

class ESportFavoriteFragment: ESportListFragment<SportListViewModel, FragmentSportList2Binding>() {

    override var matchType = MatchType.MY_EVENT
    private var favoriteItem: Item?=null
    override fun observeSportList() { }

    override fun observerMenuData() { }

    override fun onInitView(view: View) {
        super.onInitView(view)
        binding.ivFilter.gone()
        setupSportTypeList()
        setESportType()
    }

    override fun onInitData() {
        if (favoriteItem!=null){
            updateESportType(favoriteItem!!)
        }else{
            setSportDataList(null)
        }
        viewModel.esportTypeMenuData.observe(this@ESportFavoriteFragment.viewLifecycleOwner){
            it.first?.let {
                updateESportType(it)
                return@observe
            }
            dismissLoading()
            setSportDataList(null)
            if (!it.second) {
                ToastUtil.showToast(activity, it.third)
                return@observe
            }
        }
    }
    override fun updateSportType(gameTypeList: List<Item>) {
        //这里是体育大厅的结果显示，电竞用 updateESportType 方法，
    }

//    override fun updateESportType(item: Item) {
//        if (item?.categoryList.isNullOrEmpty()) {
//            dismissLoading()
//            setSportDataList(null)
//            return
//        }
//        currentItem = item
//        //处理默认不选中的情况
//        var targetItem: CategoryItem? = null
//        item.categoryList?.forEach {
//            it.isSelected = false
//        }
//        binding.sportTypeList.show()
//        targetItem = item.categoryList?.first()
//        if (currentCategoryItem==null){
//            currentCategoryItem = targetItem
//            currentCategoryItem?.isSelected = true
//            load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
//        }else{
//            val existItem = item.categoryList?.firstOrNull { it.code == currentCategoryItem!!.code }
//            currentCategoryItem = existItem?:targetItem
//            currentCategoryItem?.isSelected = true
//            if (existItem!=currentCategoryItem){
//                load(item, categoryCodeList = currentCategoryItem!!.categoryCodeList)
//            }
//        }
//        esportTypeAdapter.setNewInstance(item.categoryList)
//        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
//            binding.sportTypeList,
//            RecyclerView.State(),
//            esportTypeAdapter.data.indexOfFirst { it.isSelected })
//
//    }


    override fun load(
        item: Item,
        selectLeagueIdList: ArrayList<String>,
        selectMatchIdList: ArrayList<String>,
        categoryCodeList: List<String>?
    ) {
        //电竞主页的情况，名称要换成游戏名字
        val categoryItem=item.categoryList?.firstOrNull { it.categoryCodeList == categoryCodeList }
        if (categoryItem==null){
            currentItem?.let {
                setMatchInfo(it.name, it.num.toString())
            }
        }else{
            setMatchInfo(categoryItem.name, categoryItem.num.toString())
        }
        if (categoryCodeList.isNullOrEmpty()){
            setSportDataList(item.leagueOddsList?.toMutableList())
        }else{
            setSportDataList(item.leagueOddsList?.filter { categoryCodeList?.contains(it.league.categoryCode)}?.toMutableList())
        }
        esportTypeAdapter.setNewInstance(item.categoryList)
        (binding.sportTypeList.layoutManager as ScrollCenterLayoutManager).smoothScrollToPosition(
            binding.sportTypeList,
            RecyclerView.State(),
            esportTypeAdapter.data.indexOfFirst { it.isSelected })
    }

    override fun onESportTypeChanged(item: CategoryItem, position: Int){
        currentCategoryItem = item
        clearData()
        val layoutManager = binding.sportTypeList.layoutManager as ScrollCenterLayoutManager
        layoutManager.smoothScrollToPosition(binding.sportTypeList, RecyclerView.State(), position)
        clearSubscribeChannels()
        favoriteItem?.let { load(it, categoryCodeList = item.categoryCodeList) }
    }
    fun setFavoriteData(favoriteItem: Item?) {
        this.favoriteItem = favoriteItem
        if (isAdded){
            dismissLoading()
            if (favoriteItem==null){
                setSportDataList(null)
            }else{
                updateESportType(favoriteItem)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        esportTypeAdapter.setNewInstance(null)
        setMatchInfo("", "")
        currentCategoryItem = null
    }
    fun setESportType(){
        //电竞主题背景增加
        binding.sportTypeList.setBackgroundResource(R.drawable.bg_esport_game)
        binding.linOpt.setBackgroundResource(R.drawable.bg_white_alpha70_radius_8_top)
        binding.gameList.setBackgroundResource(R.color.color_FFFFFF)
    }

}