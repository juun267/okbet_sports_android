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
     * 串关最大赔率
     */
    private BigDecimal maxOdds;

    /**
     * 香港盘总赔率
     */
    private BigDecimal hdOdds;

    /**
     * 串关最大香港盘总赔率
     */
    private BigDecimal maxHdOdds;

    /**
     * 馬來盤
     */
    private BigDecimal malayOdds;

    /**
     * 印尼盤
     */
    private BigDecimal indoOdds;

    /**
     * 最大投注金额
     */
    private BigDecimal max;

    /**
     * 最小投注金额
     */
    private BigDecimal min;

    /**
     * 是否為歐洲盤系列 (僅採用歐洲盤賠率)
     */
    private Boolean isOnlyEUType;

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

    public void setMalayOdds(BigDecimal odds) {
        this.malayOdds = odds;
    }

    public void setIndoOdds(BigDecimal odds) {
        this.indoOdds = odds;
    }

    public BigDecimal getMalayOdds() {
        return malayOdds;
    }

    public BigDecimal getIndoOdds() {
        return indoOdds;
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

    public Boolean getIsOnlyEUType() {
        return isOnlyEUType;
    }

    public void setIsOnlyEUType(Boolean isOnlyEUType) {
        this.isOnlyEUType = isOnlyEUType;
    }

    public BigDecimal getMaxOdds() {
        return maxOdds;
    }

    public void setMaxOdds(BigDecimal maxOdds) {
        this.maxOdds = maxOdds;
    }

    public BigDecimal getMaxHdOdds() {
        return maxHdOdds;
    }

    public void setMaxHdOdds(BigDecimal maxHdOdds) {
        this.maxHdOdds = maxHdOdds;
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
