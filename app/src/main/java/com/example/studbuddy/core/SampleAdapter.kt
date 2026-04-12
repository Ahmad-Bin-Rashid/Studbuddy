package com.example.studbuddy.core

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studbuddy.R
import com.example.studbuddy.core.models.SampleModel

/**
 * Sample Adapter following the RECYCLER_VIEW_PATTERNS.md documentation.
 */
class SampleAdapter(
    private val items: MutableList<SampleModel>,
    private val onItemClick: (SampleModel) -> Unit
) : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

    // ── 1. ViewHolder (inner class) ────────────────────────────────
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewSampleTitle: TextView = itemView.findViewById(R.id.textViewSampleTitle)
        val textViewSampleSubtitle: TextView = itemView.findViewById(R.id.textViewSampleSubtitle)
        val textViewSampleMetadata: TextView = itemView.findViewById(R.id.textViewSampleMetadata)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    // ── 2. Adapter Methods ─────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sample, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textViewSampleTitle.text = item.title
        holder.textViewSampleSubtitle.text = item.subtitle
        holder.textViewSampleMetadata.text = item.metadata
    }

    override fun getItemCount(): Int = items.size

    // ── 3. Public Update Methods ───────────────────────────────────
    fun updateList(newItems: List<SampleModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: SampleModel) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(id: String) {
        val position = items.indexOfFirst { it.id == id }
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateItem(item: SampleModel) {
        val position = items.indexOfFirst { it.id == item.id }
        if (position >= 0) {
            items[position] = item
            notifyItemChanged(position)
        }
    }
}
