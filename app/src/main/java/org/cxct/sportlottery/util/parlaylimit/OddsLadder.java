package org.cxct.sportlottery.util.parlaylimit;

import java.math.BigDecimal;

/**
 * 盤口轉換
 */
public class OddsLadder {

    public static BigDecimal oddsEuToHk(BigDecimal euOdds){
        return euOdds.subtract(BigDecimal.ONE);
    }
}
