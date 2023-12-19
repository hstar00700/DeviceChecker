package kr.hstar.devicechecker

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.view.KeyEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate


class MainViewModel() : BaseViewModel() {

    // 버전정보
    @Suppress("Unused")
    val versionInfo get() = BuildConfig.VERSION_NAME

    private val _root = MutableLiveData<String>()
    val root: LiveData<String> = _root
    fun setRoot(root: String) = _root.postValue(root)

    private val _click = MutableLiveData<Boolean>()
    val click: LiveData<Boolean> = _click
    fun setClick(click: Boolean) = _click.postValue(click)

    private val _update = MutableLiveData<Boolean>()
    val update: LiveData<Boolean> = _update
    fun setUpdate(update: Boolean) = _update.postValue(update)

    val bunClickFlow: MutableSharedFlow<String> = defaultSharedFlow()
    val bunCountFlow: MutableSharedFlow<Int> = defaultSharedFlow()
    val countSumFlow: MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())

    suspend fun getSumOfClicks(list: List<Int>): Flow<List<Int>> = flow { countSumFlow.emit(list) }

    private val binaryPaths = arrayOf(
        "/data/local/",
        "/data/local/bin/",
        "/data/local/xbin/",
        "/sbin/",
        "/su/bin/",
        "/system/bin/",
        "/system/bin/.ext/",
        "/system/bin/failsafe/",
        "/system/sd/xbin/",
        "/system/usr/we-need-root/",
        "/system/xbin/",
        "/system/app/Superuser.apk",
        "/cache",
        "/data",
        "/dev"
    )
    var keyDownPublisher = PublishSubject.create<KeyEvent>()
    var barcodeSubject: Flowable<String> = Flowable.empty()

    fun barcodeListen() {
        barcodeSubject = keyDownPublisher.toFlowable(BackpressureStrategy.BUFFER)
            .filter {
                val c = it.unicodeChar.toChar()
                ((c in '0'..'9') || (c in 'A'..'Z') || (c in 'a'..'z'))
            }
            .map {
                it.unicodeChar.toChar()
            }
            .buffer(
                // buffer boundary
                keyDownPublisher.toFlowable(BackpressureStrategy.BUFFER)
                    .debounce(80, TimeUnit.MILLISECONDS)
            )
            .filter {
                it.isNotEmpty()
            }
            .map {
                String(it.toCharArray())
            }
            .doOnNext { barcode: String ->
                Logger.i("BARCODE: %s", barcode)
            }
            .publish()
            .refCount()
    }


    // 태스크용 타이머(광고호출, 누락된 광고보고 재시도, 광고 파일 다운로드)
    private var taskTimer: Timer? = null

    //TODO: test
    val barcodeTestPublishSubject = PublishSubject.create<KeyEvent>()

    @SuppressLint("CheckResult")
    fun observerBarcode() {
        barcodeTestPublishSubject.toFlowable(BackpressureStrategy.BUFFER)
            .filter {
                val char = it.unicodeChar.toChar()
                (char in '0' .. '9' || char in 'a' .. 'z' || char in 'A' .. 'Z')
            }.map {
                it.unicodeChar.toChar()
            }.buffer(
                barcodeTestPublishSubject.toFlowable(BackpressureStrategy.BUFFER)
                    .debounce(100, TimeUnit.MILLISECONDS)
            ).filter { it.isNotEmpty() }
            .map {
                String(it.toCharArray())
            }.map {
                Logger.w("Current Barcode : $it")
            }.share()
    }

    init {
        taskTimer = Timer()
    }

    override fun onCleared() {
        super.onCleared()
        taskTimer?.cancel()
    }

    // Toast message
    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast
    fun sendToast(msg: String) = _toast.postValue(msg)

    //rooting check
    fun isDeviceRooted(): String {
        val buildTags = Build.TAGS
//        if (buildTags != null && buildTags.contains("test-keys")) {
//            Logger.e("0. buildTags = $buildTags")
//            return true
//        }
        try {
            val file = File("/system/app/Superuser.apk")
            if (file.exists()) {
                Logger.e("1. Super user file exist...")
                return "1. Super user file exist..."
            }
        } catch (e: Exception) {
            Logger.w("Root check error case 1 : ${e.message}")
        }

        return checkForBinary("su")
//        val rootBinaryPaths = arrayOf(
//            "/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sd/xbin/su",
//            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
//        )
//
//        for (path in rootBinaryPaths) {
//            try {
//                val file = File(path)
//                if (file.exists()) {
//                    Logger.e("2. Root Binary Paths exist... : ${file.name} -> $path")
//                    return true
//                }
//            } catch (e: Exception) {
//                Logger.w("Root check error case 2 : ${e.message}")
//            }
//        }
        //return false
    }

    private fun checkForBinary(fileName: String): String {
        try {
            for (path in binaryPaths) {
                val f = File(path, fileName)
                val fileExists = f.exists()
                if (fileExists) {
                    Logger.e("2. Root Binary Paths exist... : $fileName -> $path")
                    return "2. Root Binary Paths exist... : $fileName -> $path"
                }
            }
        } catch (e: Exception) {
            Logger.w("Root check error case 2 : ${e.message}")
        }
        return "No root"
    }

//    fun checkDeviceInfo() {
//        Logger.i("Device BRAND: ${Build.BRAND}")
//        Logger.i("Device MODEL: ${Build.MODEL}")
//        Logger.i("Device BOOTLOADER: ${Build.BOOTLOADER}")
//        Logger.i("Device HARDWARE: ${Build.HARDWARE}")
//        Logger.i("Device DEVICE: ${Build.DEVICE}")
//        Logger.i("Device BOARD: ${Build.BOARD}")
//    }



    fun takeScreenShot() {
        val clickTime = System.currentTimeMillis()
        Logger.e("Take screen shot!! : $clickTime")
        //TODO: flow test
        viewModelScope.launch {
            bunClickFlow.emit("$clickTime")
            bunCountFlow.emit(globalCount.incrementAndGet())
        }
        //setClick(true)
    }

    fun updateApk() {
        Logger.e("Take screen shot!!")
        setUpdate(true)
    }

    // repeatTask
    fun repeatTask() {
        taskTimer?.let {
            with(it) {
                scheduleAtFixedRate(SCHEDULE_VAST_CYCLE_PERIOD, 1L) {
                    //TODO: background app check
//                    serviceList()
//                    processList()
                }
            }
        }
    }

    private val am get() = App.APP.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    @Suppress("DEPRECATION")
    private fun serviceList() {
        /*서비스 리스트*/
        val rs = am.getRunningServices(100)
        for (i in rs.indices) {
            val rsi = rs[i]
            Logger.i("run service Package Name : " + rsi.service.packageName)
            Logger.i("run service Class Name : " + rsi.service.className)
        }
    }

    private fun processList() {
        /* 실행중인 process 목록 보기*/
        val appList = am.runningAppProcesses
        for (i in appList.indices) {
            val rapi = appList[i]
            Logger.i("run Process", "Package Name : " + rapi.processName)
        }
    }


    companion object {
        // 현재 광고 API 호출 주기(1분)
        const val SCHEDULE_VAST_CYCLE_PERIOD = (1 * 60 * 1000).toLong()
    }
}