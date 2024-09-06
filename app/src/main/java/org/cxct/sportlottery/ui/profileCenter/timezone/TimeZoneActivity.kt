package org.cxct.sportlottery.ui.profileCenter.timezone

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityTimezoneBinding
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.maintab.MainViewModel
import org.cxct.sportlottery.util.LanguageManager
import org.cxct.sportlottery.util.TimeZoneUitl
import org.cxct.sportlottery.util.setTitleLetterSpacing
import timber.log.Timber

/**
 * @app_destination 外觀(日間/夜間)切換-时区切换
 */
class TimeZoneActivity : BaseActivity<MainViewModel,ActivityTimezoneBinding>(MainViewModel::class) {
    override fun pageName() = "时区切换页面"
    lateinit var adapter: TimeZoneAdapter
    private var originItems = listOf<TimeZone>()
    private var selectItem: TimeZone? = null

    override fun onInitView() {
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        initToolbar()
        initView()
    }

    private fun initToolbar()=binding.toolBar.run {
        titleText = getString(R.string.timezone)
        setOnBackPressListener {
            finish()
        }
    }

    private fun initView() {
        binding.etSearch.addTextChangedListener(object:TextWatcher{
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
        binding.rvList.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
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
            binding.rvList.adapter = adapter
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
        val key = binding.etSearch.text.toString().trim()
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
        binding.linEmpty.root.visibility = if (currentItems.isNullOrEmpty()) View.VISIBLE else View.GONE
    }

    private val commitCallback = Runnable {
        adapter.notifyDataSetChanged()
        scrollTop()
    }

    private fun scrollTop() {
        binding.rvList.removeCallbacks(topRunnable)
        binding.rvList.postDelayed(topRunnable, 100)
    }

    private val topRunnable = Runnable {
        if (binding.rvList == null) {
            return@Runnable
        }
        if ((binding.rvList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() != 0) {
            binding.rvList.smoothScrollToPosition(0)
        }
    }

    override fun onStop() {
        super.onStop()
        binding.rvList.removeCallbacks(topRunnable)
    }

}