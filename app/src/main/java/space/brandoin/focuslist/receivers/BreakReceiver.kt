package space.brandoin.focuslist.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import space.brandoin.focuslist.BlockingService
import space.brandoin.focuslist.BlockingService.Actions

class BreakReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context, BlockingService::class.java).also {
            it.action = Actions.BREAK_IS_FINISHED.toString()
            context!!.startService(it)
        }
    }
}