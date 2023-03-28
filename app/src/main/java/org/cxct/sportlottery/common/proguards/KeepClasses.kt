package org.cxct.sportlottery.common.proguards

// 被注解的类将整个不会被混淆
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class KeepClasses 