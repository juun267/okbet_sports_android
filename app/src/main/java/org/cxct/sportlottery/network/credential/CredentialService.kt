package org.cxct.sportlottery.network.credential

import org.cxct.sportlottery.network.Constants.CREDENTIAL_INITIALIZE
import org.cxct.sportlottery.network.Constants.CREDENTIAL_RESULT
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CredentialService {

    @POST(CREDENTIAL_INITIALIZE)
    suspend fun getCredentialInitial(
        @Body credentialInitialRequest: CredentialInitialRequest
    ): Response<CredentialResult>

    @POST(CREDENTIAL_RESULT)
    suspend fun getCredentialComplete(
        @Body credentialInitialRequest: CredentialCompleteRequest
    ): Response<CredentialCompleteResult>

}