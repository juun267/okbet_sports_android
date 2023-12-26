package org.cxct.sportlottery.common.extentions

import org.cxct.sportlottery.util.LogUtil
import java.io.Closeable

inline fun Closeable.safeClose() {
    try { close() } catch (e: Exception) {}
}

inline fun runWithCatch(block: () -> Unit) {
    try { block() } catch (e: Exception) {  }
}

inline fun runWithCatch(block: () -> Unit, onException: ((Exception) -> Unit)) {
    try { block() } catch (e: Exception) { onException.invoke(e) }
}
inline fun runWithTimeLog(tag: String, block: () -> Unit) {
    val startTime = System.currentTimeMillis()
    block.invoke()
    LogUtil.d(tag+": "+(System.currentTimeMillis()-startTime))
}