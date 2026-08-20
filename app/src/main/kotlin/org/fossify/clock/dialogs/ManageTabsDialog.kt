package org.fossify.clock.dialogs

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.fossify.clock.R
import org.fossify.clock.databinding.DialogManageTabsBinding
import org.fossify.clock.databinding.ItemManageTabBinding
import org.fossify.clock.helpers.getTabIconRes
import org.fossify.clock.helpers.getTabLabelRes
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.getAlertDialogBuilder
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.setupDialogStuff
import org.fossify.commons.extensions.toast

/**
 * Lets the user show/hide and reorder the app's tabs (Clock, Alarm, Stopwatch, Timer). At least
 * one tab must stay checked.
 */
class ManageTabsDialog(
    val activity: BaseSimpleActivity,
    orderedTabIds: List<Int>,
    visibleTabIds: Set<Int>,
    val callback: (newOrder: List<Int>, newVisible: Set<Int>) -> Unit,
) {
    private var dialog: AlertDialog? = null
    private val binding = DialogManageTabsBinding.inflate(activity.layoutInflater, null, false)
    private val items = orderedTabIds.mapTo(ArrayList()) {
        ManageTabsAdapter.TabRow(it, visibleTabIds.contains(it))
    }

    init {
        val adapter = ManageTabsAdapter(activity, items)
        binding.manageTabsList.layoutManager = LinearLayoutManager(activity)
        binding.manageTabsList.adapter = adapter

        val touchHelper = ItemTouchHelper(ManageTabsTouchCallback(adapter))
        touchHelper.attachToRecyclerView(binding.manageTabsList)
        adapter.touchHelper = touchHelper

        activity.getAlertDialogBuilder()
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ ->
                callback(items.map { it.tabId }, items.filter { it.isChecked }.mapTo(LinkedHashSet()) { it.tabId })
            }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.manage_tabs) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }
}

private class ManageTabsAdapter(
    private val activity: BaseSimpleActivity,
    private val items: MutableList<TabRow>,
) : RecyclerView.Adapter<ManageTabsAdapter.ViewHolder>() {
    var touchHelper: ItemTouchHelper? = null

    data class TabRow(val tabId: Int, var isChecked: Boolean)

    class ViewHolder(val binding: ItemManageTabBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManageTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            val textColor = activity.getProperTextColor()
            manageTabIcon.setImageDrawable(activity.getDrawable(getTabIconRes(item.tabId)))
            manageTabIcon.applyColorFilter(textColor)
            manageTabLabel.text = activity.getString(getTabLabelRes(item.tabId))
            manageTabLabel.setTextColor(textColor)

            manageTabCheckbox.setOnCheckedChangeListener(null)
            manageTabCheckbox.isChecked = item.isChecked
            manageTabCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked && items.count { it.isChecked } <= 1) {
                    manageTabCheckbox.isChecked = true
                    activity.toast(R.string.at_least_one_tab_required)
                } else {
                    item.isChecked = isChecked
                }
            }

            manageTabDragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    touchHelper?.startDrag(holder)
                }
                false
            }
        }
    }

    fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val moved = items.removeAt(fromPosition)
        items.add(toPosition, moved)
        notifyItemMoved(fromPosition, toPosition)
    }
}

private class ManageTabsTouchCallback(private val adapter: ManageTabsAdapter) :
    ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        adapter.onRowMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}
