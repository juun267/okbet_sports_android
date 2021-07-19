package org.cxct.sportlottery.network.matchCategory

import org.cxct.sportlottery.network.Constants
import org.cxct.sportlottery.network.matchCategory.result.MatchCategoryResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MatchCategoryService {

    @POST(Constants.MATCH_CATEGORY_SPECIAL_MATCH)
    suspend fun getHighlightMatch(
        @Body matchCategoryRequest: MatchCategoryRequest
    ): Response<MatchCategoryResult>

    @POST(Constants.MATCH_CATEGORY_SPECIAL_MENU)
    suspend fun getHighlightMenu(
        @Body matchCategoryRequest: MatchCategoryRequest
    ): Response<MatchCategoryResult>

}