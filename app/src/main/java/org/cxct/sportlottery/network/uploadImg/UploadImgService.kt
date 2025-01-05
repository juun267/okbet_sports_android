package org.cxct.sportlottery.network.uploadImg

import okhttp3.MultipartBody
import org.cxct.sportlottery.network.Constants.UPLOAD_IMG
import org.cxct.sportlottery.network.Constants.UPLOAD_VERIFY_PHOTO
import org.cxct.sportlottery.network.interceptor.HEADER_UPLOAD_IMG
import org.cxct.sportlottery.network.interceptor.KEY_BASE_URL
import retrofit2.Response
import retrofit2.http.*

interface UploadImgService {

    @Headers("$KEY_BASE_URL:$HEADER_UPLOAD_IMG")
    @Multipart
    @POST(UPLOAD_IMG)
    suspend fun uploadImg(@Part parts: List<MultipartBody.Part>): Response<UploadImgResult>

}