package com.brandoinspace.focuslist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.brandoinspace.focuslist.BlockingService

class NewDayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context, BlockingService::class.java).also {
            it.action = BlockingService.Actions.NEW_DAY_RESET.toString()
            context!!.startService(it)
        }
    }
}