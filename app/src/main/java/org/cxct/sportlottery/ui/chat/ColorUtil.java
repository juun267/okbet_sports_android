package org.cxct.sportlottery.ui.chat;

import android.graphics.Color;

public class ColorUtil {
    /**
     * 可接收格式 #RGB、#RRGGBB、#AARRGGBB
     * 如果 #RGB 格式要做處理轉換成 #RRGGBB
     * ex: #123 => #112233、#FCE => #FFCCEE
     */
    public static int parseColor(String colorString) {
        if (colorString.length() == 4) {
            StringBuilder b = new StringBuilder();
            b.append(colorString.charAt(0)).append(colorString.charAt(1)).append(colorString.charAt(1))
                    .append(colorString.charAt(2)).append(colorString.charAt(2))
                    .append(colorString.charAt(3)).append(colorString.charAt(3));
            return Color.parseColor(b.toString());
        } else {
            return Color.parseColor(colorString);
        }
    }
}
