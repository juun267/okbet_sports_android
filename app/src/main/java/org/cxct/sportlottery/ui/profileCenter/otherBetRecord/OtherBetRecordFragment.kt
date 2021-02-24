package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_other_bet_record.*
import org.cxct.sportlottery.R

class OtherBetRecordFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_other_bet_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        date_search_bar.setOnClickSearchListener {
            Log.e(">>>", "get date = ${date_search_bar.getStartAndEndDate().first}, ${date_search_bar.getStartAndEndDate().second}")
        }
    }

}