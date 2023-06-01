package org.cxct.sportlottery.ui.chat;

import com.google.gson.annotations.SerializedName;

public class UserDataOutput {

    @SerializedName("token")
    public String token = ""; //登入成功後獲得的 token

    //@SerializedName("serverTime")
    //public String serverTime = "";

    @SerializedName("userId")
    public String userId = "";

    @SerializedName("userName")
    public String userName = "";

    @SerializedName("fullName")
    public String fullName = "";

    @SerializedName("loginTime")
    public String loginTime = "";

    @SerializedName("lastLoginTime")
    public String lastLoginTime = "";

    @SerializedName("money")
    public Double money = 0.0;

    @SerializedName("chess")
    public Double chess = 0.0;

    @SerializedName("transRemaining")
    public Integer transRemaining = 0;

    @SerializedName("email")
    public String email = "";

    @SerializedName("phone")
    public String phone = "";

    @SerializedName("qq")
    public String qq = "";

    @SerializedName("wx")
    public String wx = "";

    @SerializedName("rechLevel")
    public String rechLevel = ""; //用戶能看到公告訊息的層級

    @SerializedName("hasFundPwd")
    public Boolean hasFundPwd;

    @SerializedName("testFlag")
    public String testFlag = ""; //20190815 testFlag = 1 遊客

    @SerializedName("updatePw")
    public String updatePw = "";

    @SerializedName("updatePayPw")
    public String updatePayPw = "";

    @SerializedName("state")
    public String state = "";

    @SerializedName("hotGames")
    public String hotGames;

    @SerializedName("nickName")
    public String nickName; //使用者暱稱

    @SerializedName("iconUrl")
    public String iconUrl; //使用者上傳頭像

    @SerializedName("setted")
    public String setted; // "1": 已修改過暱稱, "0" or null: 未修改

    @SerializedName("platCode")
    public String platCode; //平台編碼
}
