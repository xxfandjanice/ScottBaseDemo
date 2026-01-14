package com.crazy.demo.refresh

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.databinding.ViewDataBinding
import com.crazy.kotlin_mvvm.base.BaseViewModel
import com.crazy.kotlin_mvvm.base.LazyBaseFragment
import java.util.*

abstract class BaseRefreshFragment<V : ViewDataBinding, VM : BaseViewModel> :
    LazyBaseFragment<V, VM>() {

    private val TAG_TIMER: Int? = 1000

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                TAG_TIMER -> reFreshData()
            }
        }
    }

    private var timer: Timer? = null

    override fun onFragmentResume(isFirstResume: Boolean) {
        super.onFragmentResume(isFirstResume)
        //开启定时器
        if (timer == null) timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.sendEmptyMessage(TAG_TIMER!!)
            }
        }, 1000, 1000)
    }

    override fun onFragmentPause() {
        super.onFragmentPause()
        //关闭定时器
        stopTimer()
    }

    /**
     * 子类定时刷新数据
     */
    abstract fun reFreshData()


    override fun onDestroyView() {
        super.onDestroyView()
        //关闭定时器
        stopTimer()
    }

    /**
     * 关闭定时器
     */
    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }
}