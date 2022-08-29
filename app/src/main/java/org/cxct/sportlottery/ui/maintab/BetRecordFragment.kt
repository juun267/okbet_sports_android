import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.gyf.immersionbar.ImmersionBar
import com.luck.picture.lib.tools.ScreenUtils
import kotlinx.android.synthetic.main.fragment_bet_record.*
import kotlinx.android.synthetic.main.view_toolbar_home.iv_menu_left
import org.cxct.sportlottery.R
import org.cxct.sportlottery.event.MenuEvent
import org.cxct.sportlottery.ui.base.BaseFragment
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.component.StatusSpinnerAdapter
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.menu.MenuItemData
import org.cxct.sportlottery.ui.login.signIn.LoginActivity
import org.cxct.sportlottery.ui.main.accountHistory.AccountHistoryViewModel
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.clickCustomService
import org.greenrobot.eventbus.EventBus

class BetRecordFragment() :
    BaseFragment<AccountHistoryViewModel>(AccountHistoryViewModel::class) {

    companion object {
        fun newInstance(): BetRecordFragment {
            val args = Bundle()
            val fragment = BetRecordFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_bet_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel.getConfigData()
        initView()
        initObservable()
        initPopwindow()
//        queryData()
//        initSocketObservers()
    }

    private fun initView() {
        initToolBar()
    }

    private var dataList = mutableListOf<StatusSheetData>()
    private var spinnerAdapter: StatusSpinnerAdapter? = null
    private lateinit var mListPop: ListPopupWindow

    private fun initPopwindow(){
        mListPop = ListPopupWindow(requireContext())
        mListPop.width = ScreenUtils.getScreenWidth(context) / 2
        mListPop.height = FrameLayout.LayoutParams.WRAP_CONTENT
        mListPop.setBackgroundDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bg_play_category_pop
            )
        )
        mListPop.setAdapter(spinnerAdapter)
        mListPop.anchorView = cl_bet_all_sports //设置ListPopupWindow的锚点，即关联PopupWindow的显示位置和这个锚点
        mListPop.isModal = true //设置是否是模式
        mListPop.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                mListPop.dismiss()
//                selectItem = dataList.get(position)
//                setSelectCode(selectItem.code)
//                itemSelectedListener?.invoke(selectItem)
//                setSelectInfo(selectItem)
            }
        })
    }

    fun initObservable() {
        viewModel.sportCodeList.observe(viewLifecycleOwner) {
            System.out.println("============ initObservable size ================"+it.size)
            spinnerAdapter = StatusSpinnerAdapter(dataList)
        }

//        viewModel.sportCodeList.observe(viewLifecycleOwner) {
//            rvAdapter.setSportCodeSpinner(it)
//        }

    }

    fun initToolBar() {
        view?.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
        //setupLogin()
        iv_menu_left.setOnClickListener {
            EventBus.getDefault().post(MenuEvent(true))
        }

//        iv_money_refresh.setOnClickListener {
//            //refreshMoneyLoading()
//            viewModel.getMoney()
//        }
//        btn_login.setOnClickListener {
//            startActivity(Intent(requireActivity(), LoginActivity::class.java))
//        }
        //EventBus.getDefault().post(MenuEvent(true))
//        bet_record_status_spinner.tv_name.gravity = Gravity.CENTER_VERTICAL or Gravity.START
//        bet_record_status_spinner.setItemData(data.spinnerList.toMutableList())
//            bet_record_status_spinner.setOnItemSelectedListener {
//                //sportSelectListener.onSelect(it.code)
//                System.out.println("============ it.code ================"+it.code) }
        cl_bet_all_sports.setOnClickListener(View.OnClickListener {
            System.out.println("============ 000yetetet ================")
            if (mListPop.isShowing) {
                mListPop.dismiss()
            } else {
                mListPop.show()
            }
        })

    }

    private fun initData() {
        viewModel.getSportList()
    }

}