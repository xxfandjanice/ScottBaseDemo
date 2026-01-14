package com.crazy.demo.web

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.webkit.*
import com.crazy.kotlin_mvvm.base.BaseActivity
import com.crazy.kotlin_mvvm.base.BaseViewModel
import com.crazy.kotlin_mvvm.ext.loge
import android.webkit.ValueCallback
import com.crazy.demo.constant.PageConstant
import com.crazy.demo.BR
import com.crazy.demo.R
import com.crazy.demo.databinding.ActivityWebViewBinding


/**
 * Created by wtc on 2019/11/20
 */
class WebViewActivity : BaseActivity<ActivityWebViewBinding, BaseViewModel>() {

    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1000

    companion object {
        const val URL = "url"
        const val IS_URL = "is_url"
        const val IS_OPEN_NEW_PAGE = "is_open_new_page"
    }

    private var url: String? = ""
    private var isUrl = true
    private var isOpenNewPage = false

    //服务协议
    var is_service_agreement = false
    //隐私条款
    var is_privacy = false

    override fun getLayoutResId() = R.layout.activity_web_view

    override fun initVariableId() = BR.viewModel

    override fun initView() {
        toolbarBinding?.toolbarTitle?.text = resources.getString(R.string.loading)
        initWebView()
    }

    override fun initData() {
        toolbarBinding?.toolbar?.setNavigationOnClickListener { onBackPressed() }

        intent?.extras?.run {
            is_privacy = getBoolean(PageConstant.PRIVACY,false)
            if (is_privacy){
                binding.webView.loadUrl("file:///android_asset/privacy.html")
                return
            }
            is_service_agreement = getBoolean(PageConstant.SERVICE_AGREEMENT,false)
            if (is_service_agreement){
                binding.webView.loadUrl("file:///android_asset/agree.html")
                return
            }
            is_service_agreement = getBoolean(PageConstant.SERVICE_AGREEMENT,false)
            url = getString(URL)
            isUrl = getBoolean(IS_URL, true)
            isOpenNewPage = getBoolean(IS_OPEN_NEW_PAGE, false)
        }

        url?.run {
            loge("url:" + this)
            if (isUrl) binding.webView.loadUrl(this)
            else binding.webView.loadDataWithBaseURL(null, this, "text/html", "utf-8", null)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.progressBar.progressDrawable = this.resources
            .getDrawable(R.drawable.color_progressbar)
        binding.webView.run {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    toolbarBinding?.toolbarTitle?.text = title
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    uploadMessageAboveL = filePathCallback
                    openImageChooserActivity()
                    return true
                }
            }
        }

        binding.webView.settings.run {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true // 支持通过JS打开新窗口
            allowFileAccess = true
//            setSupportMultipleWindows(true)
            defaultFontSize = 14
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            builtInZoomControls = false // 支持缩放
            loadWithOverviewMode = true // 初始加载时，是web页面自适应屏幕
            val screenDensity = resources.displayMetrics.densityDpi
            var zoomDensity: WebSettings.ZoomDensity = WebSettings.ZoomDensity.MEDIUM
            when (screenDensity) {
                DisplayMetrics.DENSITY_LOW -> zoomDensity = WebSettings.ZoomDensity.CLOSE
                DisplayMetrics.DENSITY_MEDIUM -> zoomDensity = WebSettings.ZoomDensity.MEDIUM
                DisplayMetrics.DENSITY_HIGH -> zoomDensity = WebSettings.ZoomDensity.FAR
            }
            defaultZoom = zoomDensity
        }
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView?.run {
            loadDataWithBaseURL(null, "", "html", "utf-8", null)
            removeView(binding.webView)
            clearCache(true)
            destroy()
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else finish()
    }

    /**
     * 打开本地相册
     */
    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE && uploadMessageAboveL != null) {
            onActivityResultAboveL(requestCode, resultCode, data)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    val list = arrayListOf<Uri>()
                    for (i in 0 until clipData.itemCount) {
                        val item = clipData.getItemAt(i)
                        list.add(item.uri)
                    }
                    results = list.toArray() as Array<Uri>?
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL?.onReceiveValue(results)
        uploadMessageAboveL = null
    }
}