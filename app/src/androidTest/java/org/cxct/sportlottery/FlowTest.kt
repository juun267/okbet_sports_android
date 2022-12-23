package org.cxct.sportlottery

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() {

    /**
     * flow的基础用法
     */
    runBlocking {
        val flow = flow<Int> {
            emit(111)
        }
        flow.collect {
            println(it)
        }
    }





}