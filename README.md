## 專案相關資源

[體育項目的 spec](https://2r9nym.axshare.com/#g=1&p=%E3%80%90%E7%89%88%E6%9C%AC%E8%AE%B0%E5%BD%95%E3%80%91
)

[體育 API 文檔](https://sportsapidoc.cxct.org/apidoc/%E4%BD%93%E8%82%B2%E6%8E%A5%E5%8F%A3%E6%96%87%E6%A1%A3/)

[H5 參考](https://sportsapi.cxct.org)

[後台]( https://sportsadmin.cxct.org/#/login?redirect=%2Fsub-account-list)

[Color res 命名參考網站](https://chir.ag/projects/name-that-color/)


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


### Error Handling

- ErrorUtils parseError會回傳Api Service相對應result (ex. login api -> LoginResult)
- Error Result只會代 success,msg,code三個欄位，資料欄位皆為null
- 如果回傳Error Result為空，表示發生無法預期的錯誤，之後可能會送出log紀錄

```
viewModelScope.launch{
    val response = SportApi.Service.doSomething()

    if(response.isSuccessful){

    }else{
        val result = ErrorUtils.parseError(response)
    }
}
```

###Base View Model

- 把上述API呼叫與Error Handling放到BaseViewModel
- 之後可以透過doNetwork{}傳入API suspend fun來執行
- doNetwork會自己處理token狀態與unknown Exception
- doNetwork會回傳non null result可照自己需求要set給live data或其他處理

```
viewModelScope.launch {
    val result = doNetwork {
        OneSportApi.messageService.getMessageList(messageType)
    }
    _yourLiveData.postValue(result)
}
```

## Dependency Injection

[Koin](https://insert-koin.io/)

須先在MyApplication定義依賴注入關係

```
class MyApplication : Application(){
    private val viewModelModule = module{
        viewModel{ YourViewModel(get())} //這裡的get是指將傳入view model的repository
    }

    private val repoModule = module{
        single{ YourRepository(get())} //這裡的get是指將傳入repositroy的androidContext
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MultiLanguagesApplication)
            modules(
                listOf(
                    viewModelModule,
                     repoModule
                 )
             )
        }
    }
}
```

目前會在BaseActivity/BaseFragment透過泛型方式統一注入ViewModel

```
abstract class BaseActivity<T : BaseViewModel>(clazz: KClass<T>) : AppCompatActivity() {

    val viewModel: T by viewModel(clazz = clazz)
}
```

繼承BaseActivity/BaseFragment只需要傳入泛型就可以取得對應ViewModel

```
class LoginActivity : BaseActivity<LoginViewModel>(LoginViewModel::class) {

    viewmodel.doSomthing()
}
```


## Mock

Mock Request Data Instructions :
1. assets -> mock_api資料夾內添加副檔名為.mock的文件
2. 添加判斷式 MockApiInterceptor -> fun interceptRequestWhenDebug
3. build.gradle -> buildConfigField 修改參數
