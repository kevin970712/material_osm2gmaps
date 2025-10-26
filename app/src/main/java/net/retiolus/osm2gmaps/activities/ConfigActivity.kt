package net.retiolus.osm2gmaps.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import net.retiolus.osm2gmaps.R
import net.retiolus.osm2gmaps.utils.PreferencesUtil

class ConfigActivity : BaseActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        sharedPreferences = getSharedPreferences("osm2gmaps", Context.MODE_PRIVATE)

        setUpToolbar()

        setupMapProviderSpinner()
        setupGoogleMapsSpinner()
        setuptWhat3WordsSettings()
        PreferencesUtil.setupGeneralPreferences(this)
        PreferencesUtil.setupDisplayedLinksPreferences(this)
    }

    private fun setupMapProviderSpinner() {
        val spinner = findViewById<Spinner>(R.id.selectMapProviderSpinner)
        setupSpinner(spinner, R.array.map_providers, "selectedMapProviderIndex")
    }

    private fun setupGoogleMapsSpinner() {
        val gmapsSpinner = findViewById<Spinner>(R.id.selectGoogleMapsShortLinksSpinner)
        val customLinkEditText = findViewById<EditText>(R.id.customGoogleMapsShortLinkText)

        val customLink = sharedPreferences.getString("customGoogleMapsShortLink", "")
        customLinkEditText.setText(customLink)

        setupSpinner(
            gmapsSpinner,
            R.array.proprietary_unshorteners, "selectedProprietaryLinksUnshortener"
        ) { position ->
            val selectedOption = gmapsSpinner.getItemAtPosition(position).toString()
            val isCustomSelected = selectedOption == "Custom"
            customLinkEditText.isEnabled = isCustomSelected
            customLinkEditText.setTextColor(if (isCustomSelected) getColor(android.R.color.white) else getColor(android.R.color.darker_gray))
        }

        customLinkEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val customLink = s?.toString() ?: ""
                sharedPreferences.edit().putString("customGoogleMapsShortLink", customLink).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }
        })
    }

    private fun setuptWhat3WordsSettings(){
        val customWhat3WordsApiKeyText = findViewById<EditText>(R.id.what3wordsText)

        val customWhat3WordsApiKey = sharedPreferences.getString("customWhat3WordsApiKey", "")
        customWhat3WordsApiKeyText.setText(customWhat3WordsApiKey)

        customWhat3WordsApiKeyText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val customLink = s?.toString() ?: ""
                sharedPreferences.edit().putString("customWhat3WordsApiKey", customLink).apply()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed
            }
        })
    }

    private fun setupSpinner(
        spinner: Spinner,
        arrayId: Int,
        preferenceKey: String,
        onItemSelected: ((Int) -> Unit)? = null
    ) {
        ArrayAdapter.createFromResource(
            this,
            arrayId,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val selectedIndex = sharedPreferences.getInt(preferenceKey, 0)
        spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                sharedPreferences.edit().putInt(preferenceKey, position).apply()
                onItemSelected?.invoke(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
