package com.brandoinspace.focuslist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.brandoinspace.focuslist.BlockingService
import com.brandoinspace.focuslist.BlockingService.Actions
import com.brandoinspace.focuslist.data.GlobalJsonStore

class BreakReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context, BlockingService::class.java).also {
            it.action = Actions.BREAK_IS_FINISHED.toString()
            it.putExtra("cooldown_time_minutes_extra", GlobalJsonStore.readCooldownTime())
            context!!.startService(it)
        }
    }
}