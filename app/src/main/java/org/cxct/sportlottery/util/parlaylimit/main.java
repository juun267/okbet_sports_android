package org.cxct.sportlottery.util.parlaylimit;

import android.annotation.SuppressLint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class main {

    public static void main(String[] args) {

        // 最大中獎金額 最大投注限額api
        BigDecimal max = new BigDecimal(10000);
        // 最小下注额 最大投注限額api
        BigDecimal min = new BigDecimal(10);
        // 賠率列表
        List<BigDecimal> oddsList = initOddsList();
        // 賠率index
        int[] oddsIndex = new int[]{0, 1, 2, 3};

        // 取得組合(類型、數量、組合列表)
        List<ParlayCom> parlayComList = getCom(oddsIndex);
        // 打印賠率組合
//        parlayComPrintln(parlayComList);

        // 得到串关限额 排序各端請自理
        Map<String, ParlayBetLimit> parlayBetLimitMap = getParlayLimit(oddsList, parlayComList, max, min);
        System.out.println(parlayBetLimitMap);


    }

    /**
     * 得到串关限额
     *
     * @param oddsList
     * @param parlayComList
     * @param max
     * @param min
     */
    private static Map<String, ParlayBetLimit> getParlayLimit(List<BigDecimal> oddsList, List<ParlayCom> parlayComList, BigDecimal max, BigDecimal min) {
        Map<String, ParlayBetLimit> result = new LinkedHashMap<>();

        for (ParlayCom parlayCom : parlayComList) {
            ParlayBetLimit parlayBetLimit = new ParlayBetLimit();

            BigDecimal odds = getTotalOdds(oddsList, parlayCom.getComList());
            // 香港盤 可以 odds-num
            BigDecimal hkOdds = getTotalHkOdds(oddsList, parlayCom.getComList());
            // 投注限額 設定值/odds
            BigDecimal maxPayLimit = max.divide(odds, 0, RoundingMode.DOWN);

            parlayBetLimit.setOdds(odds);
            parlayBetLimit.setHdOdds(hkOdds);
            parlayBetLimit.setMax(maxPayLimit);
            parlayBetLimit.setMin(min);
            parlayBetLimit.setNum(parlayCom.getNum());

            result.put(parlayCom.getParlayType(), parlayBetLimit);

        }
        return result;
    }

    /**
     * 获取连串组合的赔率总和
     *
     * @param oddsList
     * @return
     */
    public static BigDecimal getTotalOdds(List<BigDecimal> oddsList, List<int[]> comList) {
        BigDecimal totalOdds = BigDecimal.ZERO;

        // 取出每種排列組合 [0,1] [0,2] [0,1,2,3]
        for (int[] oddsIndexArray : comList) {
            BigDecimal odd = BigDecimal.ONE;
            for (int index : oddsIndexArray) {
                //  賠率相乘
                odd = odd.multiply(oddsList.get(index));
            }
            totalOdds = totalOdds.add(odd);
        }

        return totalOdds;
    }


    private static BigDecimal getTotalHkOdds(List<BigDecimal> oddsList, List<int[]> comList) {
        BigDecimal totalOdds = BigDecimal.ZERO;

        for (int[] oddsIndexArray : comList) {
            BigDecimal odd = BigDecimal.ONE;
            for (int index : oddsIndexArray) {
                odd = odd.multiply(oddsList.get(index));
            }
            totalOdds = totalOdds.add(OddsLadder.oddsEuToHk(odd));
        }

        return totalOdds;

    }

    /**
     * 取得組合(類型、數量、組合列表)
     *
     * @param matchIdArray
     * @return
     */
    @SuppressLint("NewApi")
    public static List<ParlayCom> getCom(int[] matchIdArray) {

        List<int[]> all = new ArrayList<>();
        for (int i = 2; i <= matchIdArray.length; i++) {
            all.addAll(combine(matchIdArray, i));
        }
        Map<Integer, List<int[]>> map = new HashMap<>();
        for (int[] item : all) {
            List<int[]> list = new ArrayList<>();
            list.add(item);
            map.merge(item.length, list, (oldx, newx) -> {
                oldx.add(item);
                return oldx;
            });
        }
        List<ParlayCom> parlayComSOList = new ArrayList<>();
        //N串1场景
        map.forEach((key, list) -> {
            ParlayCom parlayCom = new ParlayCom();
            parlayCom.setNum(list.size());
            parlayCom.setParlayType(list.get(0).length + "C1");
            parlayCom.setComList(list);
            parlayComSOList.add(parlayCom);
        });
        //N串M场景
        ParlayCom nParlayM = new ParlayCom();
        nParlayM.setNum(all.size());
        nParlayM.setComList(all);
        nParlayM.setParlayType(matchIdArray.length + "C" + all.size());
        parlayComSOList.add(nParlayM);
        return parlayComSOList;

    }


    /**
     * 組合公式 C n取m
     *
     * @param source n的列表 1,2,3,4,5
     * @param m      取幾
     * @return
     */
    public static List<int[]> combine(int[] source, int m) {
        List<int[]> result = new ArrayList<>();
        if (m == 1) {
            for (int i = 0; i < source.length; i++) {
                result.add(new int[]{source[i]});
            }
        } else if (source.length == m) {
            result.add(source);
        } else {
            int[] psource = new int[source.length - 1];
            for (int i = 0; i < psource.length; i++) {
                psource[i] = source[i];
            }
            result = combine(psource, m);
            List<int[]> tmp = combine(psource, m - 1);
            for (int i = 0; i < tmp.size(); i++) {
                int[] rs = new int[m];
                for (int j = 0; j < m - 1; j++) {
                    rs[j] = tmp.get(i)[j];
                }
                rs[m - 1] = source[source.length - 1];
                result.add(rs);
            }
        }
        return result;
    }

    private static List<BigDecimal> initOddsList() {
        ArrayList<BigDecimal> oddsList = new ArrayList<>();
        oddsList.add(new BigDecimal("2.23"));
        oddsList.add(new BigDecimal("1.6"));
        oddsList.add(new BigDecimal("1.7"));
        oddsList.add(new BigDecimal("1.85"));
        return oddsList;
    }

    /**
     * 單純印出
     *
     * @param parlayComList
     */
    @SuppressLint("NewApi")
    private static void parlayComPrintln(List<ParlayCom> parlayComList) {
        for (ParlayCom parlayCom : parlayComList) {
            System.out.println("parlayType: " + parlayCom.getParlayType());
            System.out.println("num: " + parlayCom.getNum());
            System.out.println("comList: ");
            parlayCom.getComList().forEach(i -> {
                System.out.println(Arrays.toString(i));
            });
            System.out.println("-----------------");
        }
    }

}
