package org.cxct.sportlottery.ui.profileCenter.timezone

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_timezone.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.main.MainViewModel
import org.cxct.sportlottery.util.setTitleLetterSpacing

/**
 * @app_destination 外觀(日間/夜間)切換
 */
class TimeZoneActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    lateinit var adapter: TimeZoneAdapter
    var items= arrayListOf<TimeZone>(
        TimeZone("","","",""),
        TimeZone("","","",""),
        TimeZone("","","",""),
        TimeZone("","","",""),
        TimeZone("","","","")
    ).apply {
        this[0].isSelected =true
    }

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
        })
        adapter.setItems(items)
        rv_list.adapter=adapter
    }
    fun filter(key:String){
        if (key.isNullOrBlank()){
            adapter.setItems(items)
            lin_empty.visibility = View.GONE
        }else{
            var filterData=items.filter {
                it.city!!.contains(key)
            }
            adapter.setItems(filterData)
            lin_empty.visibility =  if(filterData.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

    }
}