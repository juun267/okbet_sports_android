package org.cxct.sportlottery.network.sport

data class SportMenuFilter(
    val gameType: String,
    val menuList: List<Menu>
) {
    data class Menu(
        val code: String,
        val name: String,
        val selectionType: Int,
        val num: Int,
        val playCateList: List<PlayCate>,
        val nameMap: NameMap,
        val oddsSort: String
    ) {
        data class PlayCate(
            val code: String,
            val name: String,
            val playCateNameMap: PlayCateNameMap
        ) {
            data class PlayCateNameMap(
                val mapKey: String
            )
        }

        data class NameMap(
            val mapKey: String
        )
    }
}