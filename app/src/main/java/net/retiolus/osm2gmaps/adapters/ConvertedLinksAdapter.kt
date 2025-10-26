package net.retiolus.osm2gmaps.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.retiolus.osm2gmaps.R

class ConvertedLinksAdapter(
    private var convertedLinks: List<String>,
    private var smallText: List<String>
) :
    RecyclerView.Adapter<ConvertedLinksAdapter.ConvertedLinksViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertedLinksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_converted_link, parent, false)
        return ConvertedLinksViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConvertedLinksViewHolder, position: Int) {
        holder.bind(convertedLinks[position], smallText[position])
    }

    override fun getItemCount(): Int {
        return convertedLinks.size
    }

    fun updateData(newConvertedLinks: List<String>, newConvertedTitles: List<String>) {
        val oldSize = convertedLinks.size
        convertedLinks = newConvertedLinks
        smallText = newConvertedTitles
        val newSize = newConvertedLinks.size

        if (oldSize < newSize) {
            notifyItemRangeInserted(oldSize, newSize - oldSize)
        } else if (oldSize > newSize) {
            notifyItemRangeRemoved(newSize, oldSize - newSize)
        } else {
            for (i in 0 until newSize) {
                notifyItemChanged(i)
            }
        }
    }

    inner class ConvertedLinksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        private val convertedLinkTextView: TextView =
            itemView.findViewById(R.id.convertedLinkTextView)
        private val convertedLinkTitle: TextView = itemView.findViewById(R.id.smallText)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val link = convertedLinks[position]
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                itemView.context.startActivity(intent)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val link = convertedLinks[position]
                val clipboardManager =
                    itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Link", link)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(itemView.context, "Link copied to clipboard", Toast.LENGTH_SHORT)
                    .show()
                return true
            }
            return false
        }

        fun bind(mainText: String, smallText: String) {
            convertedLinkTextView.text = mainText
            convertedLinkTitle.text = smallText
        }
    }

}
