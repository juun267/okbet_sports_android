package org.cxct.sportlottery.ui.maintab.betdetails

import android.os.Bundle
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_help_center.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ActivityBetDetailsBinding
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.base.BaseSocketActivity
import org.cxct.sportlottery.ui.game.betList.BetListViewModel

/**
 * 串关组合详情
 */
class BetDetailsActivity : BaseActivity<BetListViewModel>(BetListViewModel::class){

    private lateinit var binding: ActivityBetDetailsBinding

//    private val mNavController by lazy {
//        findNavController(R.id.bet_details_container)
//    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusbar(R.color.color_232C4F_FFFFFF, true)
        binding = ActivityBetDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val data: Row? =intent.getParcelableExtra("data")
        replaceFragment(R.id.fl_container,BetDetailsFragment())
//        setupEvent()
//        val data: Row =intent.getSerializableExtra("data") as Row
//        val directions=BetDetailsFragmentDirections.toBetDetailsFragment(data)
//        mNavController.navigate(directions)
    }



//    override fun onBackPressed() {
//        backEvent()
//    }
//
//    private fun setupEvent() {
//        custom_tool_bar.setOnBackPressListener {
//            finish()
//        }
//    }
//
//    private fun backEvent() {
//        if (mNavController.previousBackStackEntry == null) {
//            finish()
//        } else {
//            mNavController.popBackStack()
//        }
//    }

}