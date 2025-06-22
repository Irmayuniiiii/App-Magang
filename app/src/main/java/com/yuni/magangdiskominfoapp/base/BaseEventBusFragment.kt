package com.yuni.magangdiskominfoapp.base

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.yuni.magangdiskominfoapp.LoginActivity
import com.yuni.magangdiskominfoapp.utils.Events
import com.yuni.magangdiskominfoapp.utils.SessionManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseEventBusFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTokenExpired(event: Events.TokenExpired) {
        handleTokenExpired()
    }

    protected open fun handleTokenExpired() {
        SessionManager.clearSession(requireContext())
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        requireActivity().finish()
    }
}