package org.cxct.sportlottery.network.common

enum class QuickPlayCate(val value: String, val rowSort: String) {
    QUICK_OU("QUICK-OU", "O/U,O/U-1ST"),
    QUICK_EPS("QUICK-EPS", "EPS"),
    QUICK_HDP("QUICK-HDP", "HDP,HDP-1ST"),
    QUICK_LCS("QUICK_LCS", ""),
    QUICK_CORNERS("QUICK-CORNERS", "CORNER-HDP,CORNER-O/U,CORNER-1X2,CORNER-OE,CORNER-1ST-HDP,CORNER-1ST-O/U,CORNER-1ST-1X2,CORNER-1ST-OE"),
    QUICK_PENALTY("QUICK-PENALTY", "PENALTY-HDP,PENALTY-O/U,PENALTY-1X2,PENALTY-OE,PENALTY-1ST-HDP,PENALTY-1ST-O/U,PENALTY-1ST-1X2,PENALTY-1ST-OE"),
    QUICK_ADVANCE("QUICK-ADVANCE", "ADVANCE")
}