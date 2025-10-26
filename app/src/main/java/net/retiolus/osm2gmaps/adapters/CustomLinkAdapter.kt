package net.retiolus.osm2gmaps.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.retiolus.osm2gmaps.R

class CustomLinkAdapter(
    private var links: List<Pair<String, Any?>>,
    private val onDeleteClickListener: (String) -> Unit,
    private val onEditClickListener: (String, String) -> Unit
) : RecyclerView.Adapter<CustomLinkAdapter.CustomLinkViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomLinkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom_link, parent, false)
        return CustomLinkViewHolder(view)
    }

    override fun onBindViewHolder(holder: CustomLinkViewHolder, position: Int) {
        val (name, link) = links[position]
        holder.bind(name, link)
    }

    override fun getItemCount(): Int {
        return links.size
    }

    fun updateData(newLinks: List<Pair<String, Any?>>) {
        links = newLinks
        notifyDataSetChanged()
    }

    inner class CustomLinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val linkTextView: TextView = itemView.findViewById(R.id.linkTextView)
        private val deleteButton: TextView = itemView.findViewById(R.id.deleteButton)
        private val editButton: TextView = itemView.findViewById(R.id.editButton)

        fun bind(name: String, link: Any?) {
            nameTextView.text = name
            linkTextView.text = link.toString()
            deleteButton.setOnClickListener {
                onDeleteClickListener.invoke(name)
            }
            editButton.setOnClickListener {
                onEditClickListener.invoke(name, link.toString())
            }
        }
    }
}
