## 專案相關資源

[體育項目的 spec](https://2r9nym.axshare.com/#g=1&p=%E3%80%90%E7%89%88%E6%9C%AC%E8%AE%B0%E5%BD%95%E3%80%91
)

[體育 API 文檔](https://sportsapidoc.cxct.org/%E4%BD%93%E8%82%B2%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3/)

[H5 參考](https://sportsapi.cxct.org)

[後台]( https://sportsadmin.cxct.org/#/login?redirect=%2Fsub-account-list)


## Network

[Retrofit Codelab](https://developer.android.com/codelabs/kotlin-android-training-internet-data#0)
[Coroutines Codelab](https://codelabs.developers.google.com/codelabs/kotlin-coroutines/?hl=da#12)

```
viewModelScope.launch{
    try{
        val loginResponse = SportApi.IndexService.login(
            LoginRequest(
                account,
                password,
                loginsrc
            )
        )
    }catch(e:Exception){}
}
```