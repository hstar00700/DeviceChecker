package kr.hstar.devicechecker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Build
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun Activity.setStatusBarTransparent() {
    window.apply {
        setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }
    if(Build.VERSION.SDK_INT >= 30) {	// API 30 에 적용
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

fun Context.statusBarHeight(): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")

    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else 0
}

fun Context.navigationHeight(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")

    return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
    else 0
}

fun Activity.setStatusBarOrigin() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}

fun Activity.changeStatusBarColor(@ColorRes colorRes: Int = R.color.transparent) {

// clear FLAG_TRANSLUCENT_STATUS flag:
// clear FLAG_TRANSLUCENT_STATUS flag:
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
// finally change the color
// finally change the color
    window.setStatusBarColor(ContextCompat.getColor(this, colorRes))
}

fun Activity.setStatusBarColor(@ColorRes color: Int) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun Activity.changeStatusBar(fullScreenMode: Boolean, @ColorRes color: Int) {
    window?.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        val screenMode =
            if (fullScreenMode) View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN else View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or screenMode
        } else {
            decorView.systemUiVisibility = screenMode
        }
        statusBarColor = ContextCompat.getColor(this.context, color)
    }
}

//화면 캡쳐
fun screenShot(view: View): File? {
    view.isDrawingCacheEnabled = true //화면에 뿌릴때 캐시를 사용하게 한다
    val screenBitmap = view.drawingCache //캐시를 비트맵으로 변환
    val filename = "screenshot.png"
    val file = File(
        Environment.getExternalStorageDirectory().toString() + "/Pictures",
        filename
    ) //Pictures폴더 screenshot.png 파일
    var os: FileOutputStream? = null
    try {
        os = FileOutputStream(file)
        screenBitmap.compress(Bitmap.CompressFormat.PNG, 90, os) //비트맵을 PNG파일로 변환
        os?.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    view.isDrawingCacheEnabled = false
    return file
}

// TEST 2 : MediaProjectionManager 사용
val REQUEST_CODE_MEDIA_PROJECTION: Int = 4578

fun Activity.takeScreenshot(): File? {
    return kotlin.runCatching {
        // image naming and path  to include sd card  appending name you choose for file
        // 저장할 주소 + 이름
        val mPath = Environment.getExternalStorageDirectory().toString() + "/" + "now" + ".jpg"

        // create bitmap screen capture
        // 화면 이미지 만들기
        val v1: View = this.window.decorView.rootView
        v1.isDrawingCacheEnabled = true
        val bitmap = Bitmap.createBitmap(v1.drawingCache)
        v1.isDrawingCacheEnabled = false

        // 이미지 파일 생성
        val imageFile = File(mPath)
        val outputStream = FileOutputStream(imageFile)
        val quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.flush()
        outputStream.close()
        imageFile
        //saveScreenshot(imageFile)
    }
        .getOrNull()
}

fun saveScreenshot(imageFile: File) {

}

@Suppress("DEPRECATION")
fun Activity.checkDeviceDisplayInfo(): Point {
    var dpi = ""

    val display = windowManager?.defaultDisplay
    val size = Point()
    display?.getSize(size)
    val metrics = DisplayMetrics()
    display?.getMetrics(metrics)
    if (metrics.densityDpi<=160) { // mdpi
        dpi = "mdpi"
    } else if (metrics.densityDpi<=240) { // hdpi
        dpi = "hdpi"
    } else if (metrics.densityDpi<=320) { // xhdpi
        dpi = "xhdpi"
    } else if (metrics.densityDpi<=480) { // xxhdpi
        dpi = "xxhdpi"
    } else if (metrics.densityDpi<=640) { // xxxhdpi
        dpi = "xxxhdpi"
    }
    Logger.d("dpi => " + metrics.densityDpi + "(" + dpi + ")")
    Logger.d( "display => size.x : " + size.x + ", size.y : " + size.y)
    return size
}

fun Activity.checkDeviceDpi(): String {
    var dpi = "UnKnown"

    val display = windowManager?.defaultDisplay
    val metrics = DisplayMetrics()
    display?.getMetrics(metrics)
    try {
        if (metrics.densityDpi<=160) { // mdpi
            dpi = "mdpi"
        } else if (metrics.densityDpi<=240) { // hdpi
            dpi = "hdpi"
        } else if (metrics.densityDpi<=320) { // xhdpi
            dpi = "xhdpi"
        } else if (metrics.densityDpi<=480) { // xxhdpi
            dpi = "xxhdpi"
        } else if (metrics.densityDpi<=640) { // xxxhdpi
            dpi = "xxxhdpi"
        }
    } catch (e: Exception) {
        dpi = "Error"
    }
    return "dpi : ${metrics.densityDpi} ($dpi)"
}


fun installAPKV2(filename: String, packageName: String = "kr.bbmc.signcast") {
    Logger.w("Install apk - $filename // pkg : $packageName")

    val filePath: String = Environment.getExternalStorageDirectory().toString() + "/Download/" + filename
    Logger.w("1. addScheduleButtonClick - path : $filePath")
    //String targetFile = filePath + File.separator + "app-root1.1.apk";
    //TODO: file check!! + finish code

    try {
        val process = Runtime.getRuntime().exec("su")
        val out = process.outputStream
        val reinstall = "pm install -r $filePath\n"
        val am = "am start -a android.intent.action.MAIN -n ${packageName}/.MainActivity"
        val cmd = "$reinstall$am &"
        out.write(cmd.toByteArray())
        out.flush()
        out.close()
        process.waitFor()
    } catch (e: Exception) {
        Logger.e("Error : " + e.message)
    } finally {
        //finishAndRemoveTask()
    }
}

/**
 * 화면을 캡쳐한 후 전달된 경로 파일로 저장
 */
fun captureScreen(): Boolean {
    // su를 이용한 화면캡쳐의 경우 Quber 제품에서 검정으로 나옴
    /*
        try {
            Process process = Runtime.getRuntime().exec("su", null, null);
            OutputStream os = process.getOutputStream();
            os.write(("/system/bin/screencap -p " + pathFile).getBytes("ASCII"));
            os.flush();
            os.close();
            int exitValue = process.waitFor();
            DebugUtil.logAuto("captureScreen: exitValue - " + exitValue);

            return exitValue == 0;
        } catch (Exception e) {
            DebugUtil.logException(TAG, "captureScreen", e);
        }
        */

    val mPath = Environment.getExternalStorageDirectory().toString() + "/" + "now" + ".jpg"

    try {
        val process = Runtime.getRuntime().exec("/system/bin/screencap -p $mPath")
        val exitValue = process.waitFor()
        return exitValue == 0
    } catch (e: java.lang.Exception) {
        Logger.e("Error : ${e.message}")
    }
    return false
}

fun Activity.launchApk(packageName: String) {
    try{
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: java.lang.Exception){
        Logger.e("Error : ${e.message}")
    }
}

fun flowTest() {

}