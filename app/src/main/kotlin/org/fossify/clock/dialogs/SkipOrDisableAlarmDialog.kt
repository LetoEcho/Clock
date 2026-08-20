package org.fossify.clock.dialogs

import androidx.appcompat.app.AlertDialog
import org.fossify.clock.R
import org.fossify.clock.helpers.getTimeOfNextAlarm
import org.fossify.clock.models.Alarm
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.databinding.DialogMessageBinding
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.setupDialogStuff
import java.util.Calendar

/**
 * Asked when switching off a repeating alarm from the alarms list: skip just the next scheduled
 * occurrence (the alarm stays enabled and keeps repeating afterward), or turn it off entirely.
 */
class SkipOrDisableAlarmDialog(
    val activity: BaseSimpleActivity,
    val alarm: Alarm,
    val onSkipNext: () -> Unit,
    val onDisableForever: () -> Unit,
    val onCancelled: () -> Unit,
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogMessageBinding.inflate(activity.layoutInflater, null, false)

    init {
        binding.message.text = activity.getString(R.string.skip_or_disable_alarm_message)

        val skipLabel = getTimeOfNextAlarm(alarm)?.let {
            activity.getString(R.string.skip_next_time, getSkipDateLabel(it))
        } ?: activity.getString(R.string.skip_next_time_no_date)

        activity.getAlertDialogBuilder()
            .setPositiveButton(skipLabel) { _, _ -> onSkipNext() }
            .setNegativeButton(R.string.turn_off_alarm_forever) { _, _ -> onDisableForever() }
            .setOnCancelListener { onCancelled() }
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.skip_or_disable_alarm_title) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    // e.g. "24 ago (Lun)", or "21 ago (Mañana)" when the date is tomorrow
    private fun getSkipDateLabel(calendar: Calendar): String {
        val now = Calendar.getInstance()
        val isTomorrow = calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) + 1

        val dayLabel = if (isTomorrow) {
            activity.getString(org.fossify.commons.R.string.tomorrow)
        } else {
            val mondayIndexedDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
            activity.resources.getStringArray(org.fossify.commons.R.array.week_days_short)[mondayIndexedDay]
        }

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val monthShort = activity.resources.getStringArray(org.fossify.commons.R.array.months_short)[calendar.get(Calendar.MONTH)]
        return "$dayOfMonth $monthShort ($dayLabel)"
    }
}
