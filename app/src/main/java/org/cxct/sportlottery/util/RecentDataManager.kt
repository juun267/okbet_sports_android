package org.cxct.sportlottery.util

import android.app.Activity
import android.content.Intent
import com.didichuxing.doraemonkit.util.GsonUtils
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.net.games.data.OKGameBean

object RecentDataManager {
    const val SPORT_RECENT = "sportRecent"
    const val GAME_RECENT = "gameRecent"

   val sportRecord = mutableListOf<SportRecent>()
   val gameRecord = mutableListOf<OKGameBean>()

  init {
      KvUtils.decodeString(SPORT_RECENT).let {
          if (!it.isNullOrEmpty()){
              it.fromJson<List<SportRecent>>()?.let {
                      it1 -> sportRecord.addAll(it1)
              }
          }
      }
      KvUtils.decodeString(GAME_RECENT).let {
          if (!it.isNullOrEmpty()){
              it.fromJson<List<OKGameBean>>()?.let {
                      it1 -> gameRecord.addAll(it1)
              }
          }
      }
  }

}
@KeepMembers
@Parcelize
data class SportRecent(val gameType: String,val timeStamp: Long)
