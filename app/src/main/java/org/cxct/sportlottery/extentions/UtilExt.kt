package org.cxct.sportlottery.extentions

import java.io.Closeable

inline fun Closeable.safeClose() {
    try { close() } catch (e: Exception) {}
}

inline fun runWithCatch(block: () -> Unit) {
    try { block() } catch (e: Exception) {e.printStackTrace()}
}