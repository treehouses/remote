package io.treehouses.remote.bluetoothv2.ui.home.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.scan.ScanResult

internal class ScanResultsAdapter(
        private val onClickListener: (ScanResult) -> Unit
) : RecyclerView.Adapter<ScanResultsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val device: TextView = itemView.findViewById(android.R.id.text1)
        val rssi: TextView = itemView.findViewById(android.R.id.text2)
    }

    private val data = mutableListOf<ScanResult>()

    fun addScanResult(bleScanResult: ScanResult) {
        data.withIndex()
                .firstOrNull { it.value.bleDevice == bleScanResult.bleDevice }
                ?.let {
                    data[it.index] = bleScanResult
                    notifyItemChanged(it.index)
                }
                ?: run {
                    with(data) {
                        add(bleScanResult)
                        sortBy { it.bleDevice.macAddress }
                    }
                    notifyDataSetChanged()
                }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(data[position]) {
            holder.device.text = String.format("%s (%s)", bleDevice.macAddress, bleDevice.name)
            holder.rssi.text = String.format("RSSI: %d", rssi)
            holder.itemView.setOnClickListener { onClickListener(this) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.two_line_list_item, parent, false)
                    .let { ViewHolder(it) }
}