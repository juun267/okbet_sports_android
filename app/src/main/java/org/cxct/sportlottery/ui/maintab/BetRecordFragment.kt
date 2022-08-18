import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseBottomNavViewModel
import org.cxct.sportlottery.ui.base.BaseBottomNavigationFragment

class BetRecordFragment() :
    BaseBottomNavigationFragment<BaseBottomNavViewModel>(BaseBottomNavViewModel::class) {

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

}