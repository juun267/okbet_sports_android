package org.cxct.sportlottery.view.boundsEditText

import android.text.Editable
import android.text.TextWatcher

class EditTextWatcher(val afterTextChanged: (String) -> Unit): TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        afterTextChanged.invoke(s.toString())
    }
}