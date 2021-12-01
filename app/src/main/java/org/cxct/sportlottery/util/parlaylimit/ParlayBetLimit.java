package org.cxct.sportlottery.util.parlaylimit;

import java.math.BigDecimal;

/**
 *
 */
public class ParlayBetLimit {

    /**
     * 投注数量
     */
    private Integer num;

    /**
     * 总赔率
     */
    private BigDecimal odds;

    /**
     * 香港盘总赔率
     */
    private BigDecimal hdOdds;

    /**
     * 最大投注金额
     */
    private BigDecimal max;

    /**
     * 最小投注金额
     */
    private BigDecimal min;

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public BigDecimal getHdOdds() {
        return hdOdds;
    }

    public void setHdOdds(BigDecimal hdOdds) {
        this.hdOdds = hdOdds;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    @Override
    public String toString() {
        return "ParlayBetLimit{" +
                "num=" + num +
                ", odds=" + odds +
                ", hdOdds=" + hdOdds +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}
