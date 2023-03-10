package org.cxct.sportlottery.proguard

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

// 可配注解与类、方法、属性，注解后将不会被混淆.(如果注解到类上面，则：方法和属性都不会被混淆)
@Retention(RetentionPolicy.SOURCE)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FIELD
)
annotation class KeepMembers 