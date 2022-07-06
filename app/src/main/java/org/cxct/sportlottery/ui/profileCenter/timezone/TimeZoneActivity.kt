package org.cxct.sportlottery.ui.profileCenter.timezone

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_timezone.*
import kotlinx.android.synthetic.main.bottom_navigation_item.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.setTitleLetterSpacing
import org.cxct.sportlottery.util.toJson
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

/**
 * @app_destination 外觀(日間/夜間)切換
 */
class TimeZoneActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    lateinit var adapter: TimeZoneAdapter
    var items= listOf<TimeZone>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timezone)
        initToolbar()
        initView()
    }

    private fun initToolbar() {
        tv_toolbar_title.setTitleLetterSpacing()
        tv_toolbar_title.text = getString(R.string.timezone)
        btn_toolbar_back.setOnClickListener {
            finish()
        }
    }

    private fun initView() {
        et_search.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                filter(et_search.text.toString().trim())
            }
        })
        rv_list.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        rv_list.addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(this, R.drawable.divider_color_gray_light2)))
        adapter= TimeZoneAdapter(ItemClickListener {
                items.forEach { item->
                    item.isSelected= false
                }
                it.isSelected = true
                adapter.notifyDataSetChanged()
                var zone= java.util.TimeZone.getTimeZone(it.name)
                zone.id=it.country_en+"/"+it.city_en
                java.util.TimeZone.setDefault(zone)
            MultiLanguagesApplication.timeZone=zone
        })

        items=Gson().fromJson(
            String(assets.open("timezone.json").readBytes()),
            object :TypeToken<List<TimeZone>>(){}.type
        )
        initSelect()
        adapter.setItems(items)
        rv_list.adapter=adapter
    }
    fun initSelect(){
        val curTimeZone=java.util.TimeZone.getDefault()
        val displayName=curTimeZone.getDisplayName(false,java.util.TimeZone.SHORT)
        val id=curTimeZone.id
        var selecItem=items.find {
             displayName.contains(it.name,true)
                     &&id.contains(it.city_en,true)
        }
        selecItem?.let {
            it.isSelected=true
            var mutableList=items.toMutableList()
            mutableList.remove(selecItem)
            mutableList.add(0,selecItem)
            items=mutableList.toList()
        }
    }

    fun filter(key:String){
        if (key.isNullOrBlank()){
            adapter.setItems(items)
            lin_empty.visibility = View.GONE
        }else{
            var filterData=items.filter {
                when(LanguageManager.getSelectLanguage(this)){
                    LanguageManager.Language.ZH->{
                        it.city_zh.contains(key,true)
                    }
                    LanguageManager.Language.EN->{
                        it.city_en.contains(key,true)
                    }
                    LanguageManager.Language.VI->{
                        it.city_en.contains(key,true)
                    }
                    else->{
                        it.city_en.contains(key,true)
                    }
                }
            }
            adapter.setItems(filterData)
            lin_empty.visibility =  if(filterData.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
}