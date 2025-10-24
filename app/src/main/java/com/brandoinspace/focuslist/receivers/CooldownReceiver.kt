package com.brandoinspace.focuslist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.brandoinspace.focuslist.BlockingService
import com.brandoinspace.focuslist.BlockingService.Actions

class CooldownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context, BlockingService::class.java).also {
            it.action = Actions.COOLDOWN_IS_FINISHED.toString()
            context!!.startService(it)
        }
    }
}