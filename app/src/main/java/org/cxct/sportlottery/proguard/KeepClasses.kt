package org.cxct.sportlottery.proguard

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

// 被注解的类将整个不会被混淆
@Retention(RetentionPolicy.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class KeepClasses 