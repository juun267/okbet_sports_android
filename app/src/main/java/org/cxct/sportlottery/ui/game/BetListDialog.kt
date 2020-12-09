package org.cxct.sportlottery.ui.game

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cxct.sportlottery.R

/**
 * A simple [Fragment] subclass.
 */
class BetListDialog : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_bet_list, container, false)
    }

}
