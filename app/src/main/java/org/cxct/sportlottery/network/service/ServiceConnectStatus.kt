package org.cxct.sportlottery.network.service

enum class ServiceConnectStatus(val value: String) {
    CONNECTING("connecting"),
    RECONNECT_FREQUENCY_LIMIT("reconnect frequency limit"),
    CONNECT_FAIL("connect fail");
}