package org.cxct.sportlottery.util.parlaylimit;

import java.util.List;

/**
 *
 */
public class ParlayCom {

    /**
     * 串关 1C1, 2C1, 3C1, 4C1, 4C11
     */
    private String parlayType;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 组合列表
     */
    List<int[]> comList;

    public String getParlayType() {
        return parlayType;
    }

    public void setParlayType(String parlayType) {
        this.parlayType = parlayType;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public List<int[]> getComList() {
        return comList;
    }

    public void setComList(List<int[]> comList) {
        this.comList = comList;
    }

    @Override
    public String toString() {
        return "ParlayCom{" +
                "parlayType='" + parlayType + '\'' +
                ", num=" + num +
                ", comList=" + comList +
                '}';
    }
}
