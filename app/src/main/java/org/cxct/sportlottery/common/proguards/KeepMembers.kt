package org.cxct.sportlottery.common.proguards

import android.annotation.SuppressLint
import java.lang.annotation.ElementType


// 可配注解与类、方法、属性，注解后将不会被混淆.(如果注解到类上面，则：方法和属性都不会被混淆)

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD
)

//@SuppressLint("SupportAnnotationUsage")
//@java.lang.annotation.Target(
//    ElementType.TYPE,
//    ElementType.ANNOTATION_TYPE,
//    ElementType.CONSTRUCTOR,
//    ElementType.METHOD,
//    ElementType.FIELD
//)
annotation class KeepMembers