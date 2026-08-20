package org.fossify.clock.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import org.fossify.clock.fragments.AlarmFragment
import org.fossify.clock.fragments.ClockFragment
import org.fossify.clock.fragments.StopwatchFragment
import org.fossify.clock.fragments.TimerFragment
import org.fossify.clock.helpers.*
import org.fossify.commons.models.AlarmSound

class ViewPagerAdapter(fm: FragmentManager, private val tabs: List<Int>) : FragmentStatePagerAdapter(fm) {
    private val fragments = HashMap<Int, Fragment>()

    override fun getItem(position: Int): Fragment {
        return getFragment(position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position)
        if (fragment is Fragment) {
            fragments[position] = fragment
        }
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        fragments.remove(position)
        super.destroyItem(container, position, item)
    }

    override fun getCount() = tabs.size

    private fun getFragment(position: Int) = when (tabs[position]) {
        TAB_CLOCK -> ClockFragment()
        TAB_ALARM -> AlarmFragment()
        TAB_STOPWATCH -> StopwatchFragment()
        TAB_TIMER -> TimerFragment()
        else -> throw RuntimeException("Trying to fetch unknown fragment id ${tabs[position]}")
    }

    fun showAlarmSortDialog() {
        fragments.values.filterIsInstance<AlarmFragment>().firstOrNull()?.showSortingDialog()
    }

    fun showTimerSortDialog() {
        fragments.values.filterIsInstance<TimerFragment>().firstOrNull()?.showSortingDialog()
    }

    fun updateClockTabAlarm() {
        fragments.values.filterIsInstance<ClockFragment>().firstOrNull()?.updateAlarm()
    }

    fun updateAlarmTabAlarmSound(alarmSound: AlarmSound) {
        fragments.values.filterIsInstance<AlarmFragment>().firstOrNull()?.updateAlarmSound(alarmSound)
    }

    fun updateTimerTabAlarmSound(alarmSound: AlarmSound) {
        fragments.values.filterIsInstance<TimerFragment>().firstOrNull()?.updateAlarmSound(alarmSound)
    }

    fun updateTimerPosition(timerId: Int) {
        fragments.values.filterIsInstance<TimerFragment>().firstOrNull()?.updatePosition(timerId)
    }

    fun startStopWatch() {
        fragments.values.filterIsInstance<StopwatchFragment>().firstOrNull()?.startStopWatch()
    }
}
