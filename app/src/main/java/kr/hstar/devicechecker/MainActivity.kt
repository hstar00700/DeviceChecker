package kr.hstar.devicechecker

import android.annotation.SuppressLint
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.orhanobut.logger.Logger
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kr.hstar.commonutil.repeatOnState
import kr.hstar.commonutil.throttleLast
import kr.hstar.devicechecker.databinding.ActivityMainBinding
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.vm = vm
        binding.lifecycleOwner = this
        setContentView(binding.root)
        setStatusBarTransparent()
        initApplication()
        observerData()
        //showVideo()
        //playVideo(currentVideoIndex)
    }

    override fun onResume() {
        super.onResume()
        setStatusBarTransparent()
        Logger.w("version : ${vm.versionInfo}")
    }

    private fun showVideo() {
        val videoView = binding.vvFlash
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        val uri = Uri.parse("android.resource://$packageName/${R.raw.video}")
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(uri)
        videoView.start()
    }

    private val videoResources = intArrayOf(R.raw.normal, R.raw.r2cast_2, R.raw.video) // 비디오 리소스 배열
    private var currentVideoIndex = 0

    private fun playVideo(index: Int) {
        Logger.w("Play video - $index / ${videoResources.size}")
        val videoView = binding.vvFlash
        if (index < videoResources.size) {
            val path = "android.resource://" + packageName + "/" + videoResources[index]
            videoView.setVideoURI(Uri.parse(path))
            videoView.setMediaController(MediaController(this))
            videoView.setOnCompletionListener { mp ->
                Logger.w("current index - $currentVideoIndex : mp - ${mp.currentPosition}")
                // 비디오 재생이 완료되면 다음 비디오를 재생
                currentVideoIndex++
                playVideo(currentVideoIndex)
            }
            videoView.start()
        } else {
            // 모든 비디오 재생이 완료됨
            // 필요한 처리를 추가할 수 있음
            Logger.w("Finished!!")
        }
    }

    @SuppressLint("SetTextI18n")
    @OptIn(FlowPreview::class)
    private fun observerData() {
        vm.root.observe(this) {
            Logger.w("Root : $it")
            binding.txtRooting.text = "Device rooted : $it"
        }

        vm.click.observe(this) {
            Logger.w("Click event : $it")
            if(it) takeScreenShot()
        }

        vm.update.observe(this) {
            Logger.w("Update event : $it")
            if(it) installAPKV2("app-root1.1.apk")
        }

        vm.toast.observe(this) {
            Logger.d("Toast : $it")
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }

        repeatOnState(Lifecycle.State.RESUMED) {
            vm.bunClickFlow.debounce(1000).collectLatest {
                Logger.i("BtnClick time : $it")
                globalCount = AtomicInteger(0)
            }
        }

        val arrSum = ArrayList<Int>()
        repeatOnState(Lifecycle.State.RESUMED) {
            //TODO: 현 플로우를 클릭플로우 + 1초 타임 플로우로 결합해서 처리하는 방법?
            vm.bunCountFlow.map {
                Logger.i("Count = $it")
                it
            }.map {
                arrSum.add(it)
                arrSum.takeLast(5)
            }.throttleLast(1000L).collect {
                Logger.d("Result : $it -> sum = ${it.sum()}")
                arrSum.clear()
            }
        }
    }

    private fun initApplication() {
        with(vm) {
            //Logger.w("Device root check result is ${isDeviceRooted()}")
            setRoot(isDeviceRooted())
            repeatTask()
        }
        checkDeviceInfo()
        checkDensity()

    }

    @SuppressLint("SetTextI18n")
    private fun checkDeviceInfo() {
        Logger.i("Device BRAND: ${Build.BRAND}")
        Logger.i("Device MODEL: ${Build.MODEL}")
        Logger.i("Device BOOTLOADER: ${Build.BOOTLOADER}")
        Logger.i("Device HARDWARE: ${Build.HARDWARE}")
        Logger.i("Device DEVICE: ${Build.DEVICE}")
        Logger.i("Device BOARD: ${Build.BOARD}")
        with(binding) {
            txtDpi.text = checkDeviceDpi()
            txtBrand.text = "Device BRAND: ${Build.BRAND}"
            txtModel.text = "Device MODEL: ${Build.MODEL}"
            txtBootloader.text = "Device BOOTLOADER: ${Build.BOOTLOADER}"
            txtHardware.text = "Device HARDWARE: ${Build.HARDWARE}"
            txtDevice.text = "Device DEVICE: ${Build.DEVICE}"
            txtOsVersion.text = "Device SDK Ver: ${Build.VERSION.SDK_INT}"
            txtOsVersionCode.text = "Device OS Ver: ${Build.VERSION.RELEASE}"

        }
        Build.VERSION_CODES.P
    }

    @SuppressLint("SetTextI18n")
    private fun checkDensity() {
        val density = this.checkDeviceDisplayInfo()
        binding.txtDensity.text = "Display size = ${density.x} X ${density.y}"
    }

    private fun checkOsVersion() {


    }

    private fun takeScreenShot() {
        //val rootView: View = window.decorView
        //val screenShot: File? = takeScreenshot()
        val screenShot: Boolean = captureScreen()
        Logger.w("Scrrenshot : $screenShot")
        if(screenShot) vm.sendToast("스크린샷 성공")
//        screenShot?.let {
//            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(screenShot)))
//        } ?: kotlin.run {
//            vm.sendToast("스샷 실패!!! 이미지 null")
//            Logger.e("Error : file is null")
//        }
    }

    //@RequiresApi(N)
    private fun takeScreenShot2() {
        val mgr = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_CODE_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_MEDIA_PROJECTION -> {
                if (resultCode == RESULT_OK) {
                    //send screen capture intent (data) to service
                    Logger.w("Success")
//                    setUpMediaProjection();
//                    setUpVirtualDisplay();
                }
                else {
                    //FAIL
                }
                finishAndRemoveTask();
            }
            else -> Logger.e("Else")
        }
    }
}