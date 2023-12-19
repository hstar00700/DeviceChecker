package kr.hstar.devicechecker

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        //App 디폴드 화면을 Dark 모드로 설정
        APP = this
        //fireStoreDataSource.init(SCREEN_ID) //릴리즈 버전에서는 사용하지 않는다.
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        //Logger.addLogAdapter(AndroidLogAdapter())
    }

    companion object {
        lateinit var APP: App
    }
}