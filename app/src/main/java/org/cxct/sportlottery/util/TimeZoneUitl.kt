package org.cxct.sportlottery.util

import android.content.Context
import org.cxct.sportlottery.ui.profileCenter.timezone.TimeZone
import org.cxct.sportlottery.common.extentions.safeClose
import java.lang.ref.WeakReference


object TimeZoneUitl {

    private var timeZones: WeakReference<List<TimeZone>>? = null
    private val TimeZone_KEY = "App_Time_Zone_KEY"
    private val TimeZoneId_KEY = "App_Time_Zone_Id_KEY"

    var timeZone: java.util.TimeZone
        get() {
            var displayName = KvUtils.decodeString(TimeZone_KEY)
            if (displayName.isNullOrBlank()) {
                return java.util.TimeZone.getDefault()
            } else {
                var zone = java.util.TimeZone.getTimeZone(displayName)
                zone.id = KvUtils.decodeString(TimeZoneId_KEY)
                return zone
            }
        }
        set(zone) {
            KvUtils.put(TimeZone_KEY, zone.getDisplayName(false, java.util.TimeZone.SHORT))
            KvUtils.put(TimeZoneId_KEY, zone.id)
        }

    fun getTimeZoneList(context: Context): List<TimeZone> {
        var zoneList = timeZones?.get()
        if (zoneList == null) {
            val inputSystem = context.assets.open("timezone.json")
            val data = inputSystem.readBytes()
            inputSystem.safeClose()
            zoneList = JsonUtil.listFrom(String(data), TimeZone::class.java) ?: listOf()
            timeZones = WeakReference(zoneList)
        }
        return zoneList
    }

}