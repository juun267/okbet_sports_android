package org.cxct.sportlottery.view.boundsEditText

interface SimpleTextChangedWatcher {
    /**
     * Called after a [TextWatcher] observes a text change event
     * @param theNewText the (now) current text of the text field
     * @param isError true if the current text means the textview shows as in error state
     */
    fun onTextChanged(theNewText: String?, isError: Boolean)
}