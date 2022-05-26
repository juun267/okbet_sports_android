package org.cxct.sportlottery.util;

import java.util.regex.Pattern;

public class RegxUtil {
    public static boolean isValidChineseWord(CharSequence inputStr) {
        return Pattern.matches("[$CHINESE_WORD]", inputStr);
    }

    public static void main(String[] args) {
        boolean aa=isValidChineseWord("谷歌");
        System.out.println("aa="+aa);
        boolean aa1=isValidChineseWord("谷歌123");
        System.out.println("aa1="+aa1);
        boolean aa2=isValidChineseWord("谷歌aaajd");
        System.out.println("aa2="+aa2);
        boolean aa3=isValidChineseWord("12232");
        System.out.println("aa3="+aa3);
        boolean aa4=isValidChineseWord("ii3ii3");
        System.out.println("aa4="+aa4);
    }
}
