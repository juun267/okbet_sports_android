package org.cxct.sportlottery.ui.game.publicity

import org.cxct.sportlottery.network.index.config.ImageData
import org.cxct.sportlottery.repository.sConfigData

class PublicityPromotionItemData(val imageUrl: String, val title: String?, val content: String?) {
    companion object {
        fun createData(data: ImageData): PublicityPromotionItemData {
            return PublicityPromotionItemData(
                imageUrl = "${sConfigData?.resServerHost}${data.imageName4}",
                title = data.imageText1,
                content = data.imageText3
            )
        }
    }
}