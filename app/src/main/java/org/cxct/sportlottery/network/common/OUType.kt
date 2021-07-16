package org.cxct.sportlottery.network.common

enum class OUType(val code: String, val title: String? = null) {
    O_TYPE("HOME", "O"),
    U_TYPE("AWAY", "U")
}