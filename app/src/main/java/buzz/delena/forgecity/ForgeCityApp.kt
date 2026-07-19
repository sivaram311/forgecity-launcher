package buzz.delena.forgecity

import android.app.Application
import buzz.delena.forgecity.usage.ResourceHarvestWorker

class ForgeCityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ResourceHarvestWorker.schedule(this)
    }
}
