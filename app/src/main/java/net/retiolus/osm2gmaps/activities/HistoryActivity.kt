package net.retiolus.osm2gmaps.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import net.retiolus.osm2gmaps.R
import net.retiolus.osm2gmaps.adapters.ConvertedLinksAdapter
import net.retiolus.osm2gmaps.utils.maps.LocationInfo

class HistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var historyList: ListView
    private lateinit var popupWindow: PopupWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        sharedPreferences = getSharedPreferences("osm2gmaps", Context.MODE_PRIVATE)
        Log.d("HistoryActivity", "SharedPreferences initialized")
        historyList = findViewById(R.id.historyList)

        findViewById<FloatingActionButton>(R.id.clearHistoryButton).setOnClickListener {
            clearHistory()
        }
        val historyJson = loadJSONHistory()
        displaySourceHistory(historyJson)

        historyList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedText = historyList.getItemAtPosition(position).toString()
            showPopup(selectedText)
        }

        historyList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, view, position, _ ->
                val selectedText = historyList.getItemAtPosition(position).toString()
                copyToClipboard(selectedText)
                Toast.makeText(
                    this@HistoryActivity,
                    "Copied to clipboard: $selectedText",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }

        popupWindow = PopupWindow()
    }

    private fun loadJSONHistory(): String {
        Log.d("HistoryActivity", "Loading history...")
        val historyJson = sharedPreferences.getString("history", "[]") ?: "[]"
        Log.d("HistoryActivity", "History JSON: $historyJson")
        return historyJson
    }

    private fun convertJsonToLocationInfoList(historyJson: String): List<LocationInfo> {
        val gson = Gson()
        val locationInfoArray = gson.fromJson(historyJson, Array<LocationInfo>::class.java)
        return locationInfoArray.toList()
    }

    private fun displaySourceHistory(historyJson: String) {
        val gson = Gson()
        val historyListItems = gson.fromJson(historyJson, Array<LocationInfo>::class.java)
            .map { it.source }
            .reversed()
        Log.d("HistoryActivity", "History list items: $historyListItems")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyListItems)
        historyList.adapter = adapter
    }

    private fun clearHistory() {
        sharedPreferences.edit().remove("history").apply()
        val historyJson = loadJSONHistory()
        displaySourceHistory(historyJson)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showPopup(selectedText: String) {
        popupWindow?.dismiss()

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)

        val textView = popupView.findViewById<TextView>(R.id.popupTextView)
        textView.text = selectedText

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.convertedLinksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val historyJson = loadJSONHistory()
        val historyItems = convertJsonToLocationInfoList(historyJson)
        val locationInfo = historyItems.find { it.source == selectedText }
        if (locationInfo != null) {
            val links = locationInfo.getLinksList().map { it.second }
            val smallText = locationInfo.getLinksList().map { it.first }
            val adapter = ConvertedLinksAdapter(links, smallText)
            recyclerView.adapter = adapter
        }

        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.showAtLocation(
            historyList,
            Gravity.CENTER,
            0,
            0
        )

        val dismissButton = popupView.findViewById<Button>(R.id.dismissButton)
        dismissButton.setOnClickListener {
            popupWindow.dismiss()
        }
    }

}
