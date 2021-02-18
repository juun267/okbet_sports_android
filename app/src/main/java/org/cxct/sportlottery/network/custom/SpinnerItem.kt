package org.cxct.sportlottery.network.custom


data class SpinnerItem(
    val code: String ?= null,
    val showName: String ?= null
) {
    var isChecked: Boolean = false
}