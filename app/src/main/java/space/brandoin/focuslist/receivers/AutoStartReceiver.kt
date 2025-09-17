package space.brandoin.focuslist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import space.brandoin.focuslist.BlockingService
import space.brandoin.focuslist.BlockingService.Actions

// https://stackoverflow.com/a/7690600
class AutoStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        Intent(context, BlockingService::class.java).also {
            it.action = Actions.START_BLOCKING.toString()
            context!!.startService(it)
        }
    }

}