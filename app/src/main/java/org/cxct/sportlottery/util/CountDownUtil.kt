package org.cxct.sportlottery.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.abs

object CountDownUtil {

    private const val REQUEST_CODE_INTERVAL = 90 // 重新发送验证码的时间间隔(单位:秒)
    var SMS_CODE_TIMESTAMP = 0L // 短信发送成功的时间戳
    private set

    // 短信发送成功后记一个时间戳
    fun targSMSTimeStamp() = runOnDelay { SMS_CODE_TIMESTAMP == System.currentTimeMillis() }

    fun smsCountDown(coroutineScope: CoroutineScope, start: () -> Unit, next: (Int) -> Unit, end: () -> Unit) {
        countDown(coroutineScope, REQUEST_CODE_INTERVAL, start, next, end, SMS_CODE_TIMESTAMP)
    }

    fun runOnDelay(time: Long = 200, block: ()-> Unit)  = GlobalScope.launch(Dispatchers.Main) {
        delay(time)
        block.invoke()
    }

    fun countDown(coroutineScope: CoroutineScope,
                  time: Int,
                  start: () -> Unit,
                  next: (Int) -> Unit,
                  end: () -> Unit,
                  lastTime: Long = 0) {

        if (time <= 0) {
            end.invoke()
            return
        }

        val timeLeft = System.currentTimeMillis() - lastTime
        val endTime = if (timeLeft > time * 1000) {
            time
        }  else {
            time.coerceAtMost(abs(time - (timeLeft / 1000).toInt()))
        }

        coroutineScope.launch {
            flow {
                (endTime downTo 0).forEach {
                    delay(1000)
                    emit(it)
                }
            }.onStart {
                start.invoke()
            }.onCompletion {
                end.invoke()
            }.catch {
            }.collect {
                next.invoke(it)
            }
        }
    }

}