package org.cxct.sportlottery.network.common

/**
 * 用來比對多局(盤)數
 */
enum class ComparePlayCate(val compareCode: String, val mainPlayCate: PlayCate) {
    //region FT 足球
    //O/U 大小
    PENALTY_OU_SEG("PENALTY-O/U-SEG", PlayCate.PENALTY_OU_SEG1),
    CORNER_OU_SEG("CORNER-O/U-SEG", PlayCate.CORNER_OU_SEG1),

    //HDP 讓球
    PENALTY_HDP_SEG("PENALTY-HDP-SEG", PlayCate.PENALTY_HDP_SEG1),
    CORNER_HDP_SEG("CORNER-HDP-SEG", PlayCate.CORNER_HDP_SEG1),

    //1X2 獨贏
    PENALTY_SINGLE_SEG("PENALTY-1X2-SEG", PlayCate.PENALTY_SINGLE_SEG1),
    CORNER_SINGLE_SEG("CORNER-1X2-SEG", PlayCate.CORNER_SINGLE_SEG1),

    //CORNER 角球
    N_CORNER_GOAL("N-CORNER-GOAL", PlayCate.N_CORNER_GOAL),
    //endregion

    //region BK 籃球
    TG_OU_C_SEG("TG&O/U-C-SEG", PlayCate.TG_OU_C_SEG1),
    TG_OU_H_SEG("TG&O/U-H-SEG", PlayCate.TG_OU_H_SEG1),
    SINGLE_SEGX_ND("1X2-SEG@X-ND", PlayCate.SINGLE_SEG1_ND),
    LS_SEG("LS-SEG", PlayCate.LS_SEG1),
    //endregion

    //region TN 網球
    SINGLE_SEGX_GAMES_X("1X2_SEG@X_GAMES:@X", PlayCate.SINGLE_SEG1_GAMES_1),
    WIN_SEGX_CHAMP("WIN-SEG@X&CHAMP", PlayCate.WIN_SEG1_CHAMP),
    LOSE_SEGX_CHAMP("LOSE-SEG@X&CHAMP", PlayCate.LOSE_SEG1_CHAMP),
    CS_SEG("CS-SEG", PlayCate.CS_SEG1),
    //endregion

    //共用
    OU_SEG("O/U-SEG", PlayCate.OU_SEG1),
    HDP_SEG("HDP-SEG", PlayCate.HDP_SEG1),
    OE_SEG("O/E-SEG", PlayCate.OE_SEG1),
    SINGLE_SEG("1X2-SEG", PlayCate.SINGLE_SEG1);

    companion object {
        private fun compareFTPlayCateCode(playCateCode: String): PlayCate = when {
            playCateCode.contains(PENALTY_OU_SEG.compareCode) -> PENALTY_OU_SEG.mainPlayCate
            playCateCode.contains(CORNER_OU_SEG.compareCode) -> CORNER_OU_SEG.mainPlayCate
            playCateCode.contains(PENALTY_HDP_SEG.compareCode) -> PENALTY_HDP_SEG.mainPlayCate
            playCateCode.contains(CORNER_HDP_SEG.compareCode) -> CORNER_HDP_SEG.mainPlayCate
            playCateCode.contains(PENALTY_SINGLE_SEG.compareCode) -> PENALTY_SINGLE_SEG.mainPlayCate
            playCateCode.contains(CORNER_SINGLE_SEG.compareCode) -> CORNER_SINGLE_SEG.mainPlayCate
            playCateCode.contains(N_CORNER_GOAL.compareCode) -> N_CORNER_GOAL.mainPlayCate
            else -> compareCommonCateCode(playCateCode)
        }

        private fun compareBKPlayCateCode(playCateCode: String): PlayCate = when {
            playCateCode.contains(TG_OU_C_SEG.compareCode) -> TG_OU_C_SEG.mainPlayCate
            playCateCode.contains(TG_OU_H_SEG.compareCode) -> TG_OU_H_SEG.mainPlayCate
            checkMultiCateCode(playCateCode, SINGLE_SEGX_ND) -> SINGLE_SEGX_ND.mainPlayCate
            playCateCode.contains(LS_SEG.compareCode) -> LS_SEG.mainPlayCate
            else -> compareCommonCateCode(playCateCode)
        }

        private fun compareTNPlayCateCode(playCateCode: String): PlayCate = when {
            checkMultiCateCode(playCateCode, SINGLE_SEGX_GAMES_X) -> SINGLE_SEGX_GAMES_X.mainPlayCate
            checkMultiCateCode(playCateCode, WIN_SEGX_CHAMP) -> WIN_SEGX_CHAMP.mainPlayCate
            checkMultiCateCode(playCateCode, LOSE_SEGX_CHAMP) -> LOSE_SEGX_CHAMP.mainPlayCate
            playCateCode.contains(CS_SEG.compareCode) -> CS_SEG.mainPlayCate
            else -> compareCommonCateCode(playCateCode)
        }

        /**
         * 以@X為切割符比對數字以外的字串是否都符合
         */
        private fun checkMultiCateCode(playCateCode: String, comparePlayCate: ComparePlayCate): Boolean {
            val compareStringList = comparePlayCate.compareCode.split("@X")
            return compareStringList.map { compareString ->
                playCateCode.contains(compareString)
            }.all { it }
        }

        /**
         * 比對共用的玩法
         */
        private fun compareCommonCateCode(playCateCode: String): PlayCate = when {
            playCateCode.contains(OU_SEG.compareCode) -> OU_SEG.mainPlayCate
            playCateCode.contains(HDP_SEG.compareCode) -> HDP_SEG.mainPlayCate
            playCateCode.contains(OE_SEG.compareCode) -> OE_SEG.mainPlayCate
            playCateCode.contains(SINGLE_SEG.compareCode) -> SINGLE_SEG.mainPlayCate
            else -> PlayCate.UNCHECK
        }

        /**
         * 比對多局或盤的玩法, 若比對到則回傳第一局(盤)的PlayCate
         *
         * @see PlayCate
         * @see ComparePlayCate.mainPlayCate
         */
        fun comparePlayCateCode(sportCode: GameType?, playCateCode: String): PlayCate = when (sportCode) {
            GameType.FT -> compareFTPlayCateCode(playCateCode)
            GameType.BK -> compareBKPlayCateCode(playCateCode)
            GameType.TN -> compareTNPlayCateCode(playCateCode)
            else -> {
                compareCommonCateCode(playCateCode)
            }
        }
    }
}