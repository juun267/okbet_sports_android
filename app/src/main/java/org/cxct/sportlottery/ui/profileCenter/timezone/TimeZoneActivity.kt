package org.cxct.sportlottery.ui.profileCenter.timezone

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_timezone.*
import kotlinx.android.synthetic.main.view_base_tool_bar_no_drawer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeZoneUitl
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber

/**
 * @app_destination 外觀(日間/夜間)切換-时区切换
 */
class TimeZoneActivity : BaseActivity<MainViewModel>(MainViewModel::class) {

    lateinit var adapter: TimeZoneAdapter
    private var originItems = listOf<TimeZone>()
    private var selectItem: TimeZone? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
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
                if (originItems.isNotEmpty()) {
                    sortList()
                }
            }
        })
        rv_list.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        adapter= TimeZoneAdapter(ItemClickListener {
            selectItem = it
            sortList()
            val zone = java.util.TimeZone.getTimeZone(it.name)
//            zone.id = it.country_en + "/" + it.city_en

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (runCatching {zone.toZoneId() }.getOrNull() == null) {
                    return@ItemClickListener
                }
            }

            java.util.TimeZone.setDefault(zone)
            TimeZoneUitl.timeZone = zone
        })

        setup()
    }


    private fun setup() = lifecycleScope.launch(Dispatchers.IO) {

        var zoneList = TimeZoneUitl.getTimeZoneList(this@TimeZoneActivity)
        originItems = zoneList
        selectItem = findCurrentZone()

        withContext(Dispatchers.Main) {
            rv_list.adapter = adapter
            sortList()
        }
    }

    private fun findCurrentZone(): TimeZone? {
        val curTimeZone = java.util.TimeZone.getDefault()
        val displayName = curTimeZone.getDisplayName(false, java.util.TimeZone.SHORT)
        val id = curTimeZone.id
        Timber.d(originItems.toString())
        return originItems.find {
            displayName.contains(it.name, true)
                    && id.contains(it.city_en, true)
        }
    }

    private fun sortList() {
        val key = et_search.text.toString().trim()
        var currentItems = arrayListOf<TimeZone>()
        currentItems.addAll(originItems)
        currentItems.forEach { item ->
            item.isSelected = false
        }
        selectItem?.let {
            currentItems.remove(it)
            currentItems.add(0, it)
            it.isSelected = true
        }
        if (key.isNotEmpty()) {
            val currentLanguage = LanguageManager.getSelectLanguage(this)
            currentItems = currentItems.filter {
                when (currentLanguage) {
                    LanguageManager.Language.ZH -> {
                        it.city_zh.contains(key, true)
                    }
                    LanguageManager.Language.EN -> {
                        it.city_en.contains(key, true)
                    }
                    LanguageManager.Language.VI -> {
                        it.city_vi.contains(key, true)
                    }
                    LanguageManager.Language.PHI ->{
                        it.city_ph.contains(key,true)
                    }
                    else -> {
                        it.city_en.contains(key, true)
                    }
                }
            } as ArrayList<TimeZone>
        }

        adapter.setItems(currentItems, commitCallback)
        lin_empty.visibility = if (currentItems.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    private val commitCallback = Runnable {
        adapter.notifyDataSetChanged()
        scrollTop()
    }

    private fun scrollTop() {
        rv_list.removeCallbacks(topRunnable)
        rv_list.postDelayed(topRunnable, 100)
    }

    private val topRunnable = Runnable {
        if (rv_list == null) {
            return@Runnable
        }
        if ((rv_list.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0) {
            rv_list.smoothScrollToPosition(0)
        }
    }

    override fun onStop() {
        super.onStop()
        rv_list.removeCallbacks(topRunnable)
    }

}