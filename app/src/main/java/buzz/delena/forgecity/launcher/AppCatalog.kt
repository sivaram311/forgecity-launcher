package buzz.delena.forgecity.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import buzz.delena.forgecity.city.CityBuilding
import buzz.delena.forgecity.city.DistrictClassifier
import java.text.Collator

class AppCatalog(private val context: Context) {
    fun loadBuildings(): List<CityBuilding> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
        val collator = Collator.getInstance()
        val apps = resolved
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .mapNotNull { info ->
                runCatching {
                    val label = info.loadLabel(pm).toString()
                    val component = ComponentName(info.activityInfo.packageName, info.activityInfo.name)
                    val district = DistrictClassifier.classify(component.packageName, label)
                    CityBuilding(
                        id = component.flattenToString(),
                        label = label,
                        packageName = component.packageName,
                        component = component,
                        icon = info.loadIcon(pm),
                        district = district,
                        col = 0,
                        row = 0,
                    )
                }.getOrNull()
            }
            .distinctBy { it.component }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()

        return placeOnGrid(apps)
    }

    fun launch(building: CityBuilding) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(building.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        context.startActivity(intent)
    }

    private fun placeOnGrid(apps: List<CityBuilding>): List<CityBuilding> {
        val byDistrict = apps.groupBy { it.district }
        val placed = mutableListOf<CityBuilding>()
        byDistrict.entries.forEachIndexed { districtIndex, (_, members) ->
            val originCol = (districtIndex % 3) * 5
            val originRow = (districtIndex / 3) * 5
            members.forEachIndexed { index, building ->
                val col = originCol + (index % 4)
                val row = originRow + (index / 4)
                placed += building.copy(col = col, row = row)
            }
        }
        return placed
    }
}
