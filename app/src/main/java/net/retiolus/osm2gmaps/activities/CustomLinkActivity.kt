package net.retiolus.osm2gmaps.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.retiolus.osm2gmaps.R
import net.retiolus.osm2gmaps.adapters.CustomLinkAdapter

class CustomLinkActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var nameEditText: EditText
    private lateinit var linkEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomLinkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_link)

        sharedPreferences = getSharedPreferences("custom_links", Context.MODE_PRIVATE)
        nameEditText = findViewById(R.id.nameEditText)
        linkEditText = findViewById(R.id.linkEditText)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = CustomLinkAdapter(sharedPreferences.all.toList(), { name ->
            deleteCustomLink(name)
        }, { name, link ->
            editCustomLink(name, link)
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val link = linkEditText.text.toString()
            saveCustomLink(name, link)
            adapter.updateData(sharedPreferences.all.toList())
            nameEditText.setText("")
            linkEditText.setText("")
        }

        val infoButton: Button = findViewById(R.id.infoButton)
        infoButton.setOnClickListener{
            val url = "https://codeberg.org/retiolus/osm2gmaps/wiki/Custom-Links";
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        setUpToolbar()
    }

    private fun saveCustomLink(name: String, link: String) {
        val editor = sharedPreferences.edit()
        editor.putString(name, link)
        editor.apply()
    }

    private fun deleteCustomLink(name: String) {
        val editor = sharedPreferences.edit()
        editor.remove(name)
        editor.apply()
        adapter.updateData(sharedPreferences.all.toList())
    }

    private fun editCustomLink(name: String, link: String) {
        nameEditText.setText(name)
        linkEditText.setText(link)

        val editor = sharedPreferences.edit()
        editor.remove(name)
        editor.apply()
        adapter.updateData(sharedPreferences.all.toList())
    }
}
