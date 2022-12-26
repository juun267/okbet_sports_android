package org.cxct.sportlottery

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun main() {

    /**
     * flow的基础用法
     */
//    runBlocking {
//        val flow = flow<Int> {
//            emit(111)
//        }
//        flow.collect {
//            println(it)
//        }
//    }
    /**
     * https://blog.csdn.net/weixin_42169702/article/details/127864881
     */
    val list1 = listOf(10, 1, 1, 1, 2, 12, 485, 456, 48, 48, 478, 45, 415, 415, 1)
    //下标类操作
    println("list1下标操作: ${list1.contains(10)}")
    println("list1.elementAt:${list1.elementAt(5)}")
    println("list1.firstOrNull:${list1.firstOrNull { it == 1 }}")
    println("list1.lastOrNull:${list1.lastOrNull { it == 5454 }}")
    println("list1.indexOf:${list1.indexOf(485)}")
    //返回符合条件的单个元素，如果没有符合或超过一个，返回null
    println("list1.singleOrNull:${list1.singleOrNull { it == 1 }}")

    //判断类
    println("判断集合中，是否有满足条件的元素 list1.any{it==3}:${list1.any { it == 3 }}")
    println("判断集合中，是否都满足条件 list1.all{it < 200}:${list1.all { it < 200 }}")
    println("判断集合中是否都不满足条件，是则返回true list.none{it<200} : ${list1.none { it < 200 }}")
    println("查询集合中满足条件的个数: list1.count{ it==3 }: ${list1.count { it == 3 }}")
    println(
        "从第一项到最后一项进行累计: list1.reduce { total,next ->total + next : ${
            list1.reduce { acc, i ->
                acc + i
            }
        }"
    )

    //过滤类
    println("过滤类")
    println("返回前n个元素: list,take(2) : ${list1.take(2)}")
    println("过滤所有满足条件的元素: list1.filter {it == 1 } :${list1.filter { it == 1 }}")
    println("过滤所有不满足条件的元素： list1.filterNot { it == 3} :${list1.filterNot { it == 3 }} ")
    println("过滤所有非null元素: list1.filterNotNull{ it } :${list1.filterNotNull()}")
    //转换类
    //1. map
    //2. mapIndexed
    //3. mapNotNull
    //4. flatMap
    //5. groupBy

    println("map转换成另外一个集合: list1.map{} :${list1.map { it +133 }}")
    println("除了转换成另一个集合，还可以拿到index(下标) : ${list1.mapIndexed { index, i -> 
        i + index
    }}")
    println("执行转换前过滤掉为null的元素:${list1.mapNotNull { it+1}}")
    println()
    //排序类


}