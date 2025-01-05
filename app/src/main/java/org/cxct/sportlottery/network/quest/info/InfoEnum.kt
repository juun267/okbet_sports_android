package org.cxct.sportlottery.network.quest.info

enum class TaskType {
    TOP_PICKS, //精選
    BASIC, //基礎
    DAILY, //每日
    LIMITED_TIME; //限時
}

enum class RewardType(val code: Long) {
    POINT(0), //积分
    CASH(1); //彩金

    companion object {
        private val typeCodeMap: Map<Long, RewardType> by lazy {
            values().associateBy { it.code }
        }

        fun toEnum(typeCode: Long?): RewardType? {
            typeCode?.let {
                return typeCodeMap[it]
            } ?: return null
        }
    }
}

enum class TaskStatus(val code: Long) {
    TODO(0), //去完成
    IN_PROGRESS(1), //進行中
    COMPLETED(2); //已完成

    companion object {
        private val typeCodeMap: Map<Long, TaskStatus> by lazy {
            TaskStatus.values().associateBy { it.code }
        }

        fun toEnum(typeCode: Long?): TaskStatus? {
            typeCode?.let {
                return TaskStatus.typeCodeMap[it]
            } ?: return null
        }
    }
}

/**
 * 发放状态
 */
enum class DeliverStatus(val code: Long) {
    PENDING(0),    // 審核中
    CLAIMABLE(1),  // 待領取獎勵
    DELIVERING(2), // 自動發放獎勵
    COMPLETED(3),  // 獎勵已領取
    REJECTED(4),   // 審核不通過
    EXPIRED(5);     // 已過期

    companion object {
        private val typeCodeMap: Map<Long, DeliverStatus> by lazy {
            DeliverStatus.values().associateBy { it.code }
        }

        fun toEnum(typeCode: Long?): DeliverStatus? {
            typeCode?.let {
                return DeliverStatus.typeCodeMap[it]
            } ?: return null
        }
    }
}

/**
 * 任務的狀態, 根據原型區分為5種: 去完成, 領取, 處理中, 已完成, 审核未通过, 已過期
 */
enum class TaskOverallStatus {
    TODO, //去完成
    CLAIMABLE, //領取
    IN_PROGRESS, //處理中
    COMPLETED, //已完成
    REJECTED, //审核未通过
    EXPIRED; //已過期
}

enum class ConditionType(val code: String) {
    INFORMATION("I"), // 資訊完善
    FUND("F"); // 資金

    companion object {
        private val typeCodeMap: Map<String, ConditionType> by lazy {
            ConditionType.values().associateBy { it.code }
        }

        fun toEnum(typeCode: String?): ConditionType? {
            typeCode?.let {
                return ConditionType.typeCodeMap[it]
            } ?: return null
        }
    }
}

/**
 * 次任务条件类别 (F-资金)
 */
enum class ConditionSubType(val code: String) {
    RECHARGE("001"), //充值
    BET("002"), //投注
    FIRST_RECHARGE("003"); //首充

    companion object {
        private val typeCodeMap: Map<String, ConditionSubType> by lazy {
            ConditionSubType.values().associateBy { it.code }
        }

        fun toEnum(typeCode: String?): ConditionSubType? {
            typeCode?.let {
                return ConditionSubType.typeCodeMap[it]
            } ?: return null
        }
    }
}

/**
 * 跳转类别
 */
enum class RedirectType(val code: Long) {
    NONE(0), //不跳转
    HOME(1), //首页
    HALL(2), //大厅
    RECHARGE(3), //充值页
    REGISTER(4), //注册页
    PERSONAL_INFORMATION(5), //个人中心-个人资讯
    KYC(6),
    MAYA(7),
    GLIFE(8);

    companion object {
        private val typeCodeMap: Map<Long, RedirectType> by lazy {
            RedirectType.values().associateBy { it.code }
        }

        fun toEnum(typeCode: Long?): RedirectType? {
            typeCode?.let {
                return RedirectType.typeCodeMap[it]
            } ?: return null
        }
    }
}

enum class TimeType(val code: Long) {
    PERMANENT(0), //永久
    DAILY(1), //每日
    LIMITED_TIME(2); //限时

    companion object {
        private val typeCodeMap: Map<Long, TimeType> by lazy {
            TimeType.values().associateBy { it.code }
        }

        fun toEnum(typeCode: Long?): TimeType? {
            typeCode?.let {
                return TimeType.typeCodeMap[it]
            } ?: return null
        }
    }
}
