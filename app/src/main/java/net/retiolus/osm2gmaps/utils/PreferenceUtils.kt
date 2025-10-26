package net.retiolus.osm2gmaps.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.CheckBox
import net.retiolus.osm2gmaps.R

object PreferencesUtil {

    fun setupGeneralPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("osm2gmaps", Context.MODE_PRIVATE)

        val checkBoxIds = arrayOf(
            R.id.copyToClipboardOnShareCheckBox,
            R.id.openShareMenuOnShareCheckBox,
            R.id.openDefaultAppOnShareCheckBox,
            R.id.copyToClipboardOnOpenWithCheckBox,
            R.id.openShareMenuOnOpenWithCheckBox,
            R.id.openDefaultAppOnOpenWithCheckBox,
            R.id.saveHistoryCheckBox
        )
        val preferences = arrayOf(
            "copyToClipboardOnShare",
            "openShareMenuOnShare",
            "openDefaultAppOnShare",
            "copyToClipboardOnOpenWith",
            "openShareMenuOnOpenWith",
            "openDefaultAppOnOpenWith",
            "saveHistory"
        )

        preferences.forEachIndexed { index, preference ->
            val defaultValue = false
            setDefaultBoolean(sharedPreferences, preference, defaultValue)
            initializeCheckBox(context, checkBoxIds[index], preference, sharedPreferences)
        }
    }

    fun setupDisplayedLinksPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("osm2gmaps", Context.MODE_PRIVATE)

        val checkBoxIds = arrayOf(
            R.id.displayAppleMapsMainCheckBox,
            R.id.displayBingMapsMainCheckBox,
            R.id.displayCartesAppMainCheckBox,
            R.id.displayCopernixMainCheckBox,
            R.id.displayDMSMainCheckBox,
            R.id.displayGeoHackMainCheckBox,
            R.id.displayGeoLinkMainCheckBox,
            R.id.displayGoogleMapsMainCheckBox,
            R.id.displayHereWeGoMainCheckBox,
            R.id.displayMagicEarthMainCheckBox,
            R.id.displayMapQuestMainCheckBox,
            R.id.displayOpenHistoricalMapMainCheckBox,
            R.id.displayOpenLocationCodeMainCheckBox,
            R.id.displayOpenStreetMapMainCheckBox,
            R.id.displayOpenTopoMapMainCheckBox,
            R.id.displayOrganicMapsMainCheckBox,
            R.id.displayWazeMainCheckBox,
            R.id.displayWhat3WordsMainCheckBox,
            R.id.displayWikiMapiaMainCheckBox,
            R.id.displayWikiMiniAtlasMainCheckBox,
            R.id.displayYandexMainCheckBox
        )
        val preferences = arrayOf(
            "displayAppleMapsMain",
            "displayBingMapsMain",
            "displayCartesAppMain",
            "displayCopernixMain",
            "displayDMSMain",
            "displayGeoHackMain",
            "displayGeoLinkMain",
            "displayGoogleMapsMain",
            "displayHereWeGoMain",
            "displayMagicEarthMain",
            "displayMapQuestMain",
            "displayOpenHistoricalMapMain",
            "displayOpenLocationCodeMain",
            "displayOpenStreetMapMain",
            "displayOpenTopoMapMain",
            "displayOrganicMapsMain",
            "displayWazeMain",
            "displayWhat3WordsMain",
            "displayWikiMapiaMain",
            "displayWikiMiniAtlasMain",
            "displayYandexMain"
        )

        setDefaultBoolean(sharedPreferences, "displayAppleMapsMain", true)
        setDefaultBoolean(sharedPreferences, "displayBingMapsMain", false)
        setDefaultBoolean(sharedPreferences, "displayCartesAppMain", false)
        setDefaultBoolean(sharedPreferences, "displayCopernixMain", false)
        setDefaultBoolean(sharedPreferences, "displayDMSMain", false)
        setDefaultBoolean(sharedPreferences, "displayGeoHackMain", false)
        setDefaultBoolean(sharedPreferences, "displayGeoLinkMain", true)
        setDefaultBoolean(sharedPreferences, "displayGoogleMapsMain", true)
        setDefaultBoolean(sharedPreferences, "displayHereWeGoMain", false)
        setDefaultBoolean(sharedPreferences, "displayMagicEarthMain", false)
        setDefaultBoolean(sharedPreferences, "displayMapQuestMain", false)
        setDefaultBoolean(sharedPreferences, "displayOpenHistoricalMapMain", false)
        setDefaultBoolean(sharedPreferences, "displayOpenLocationCode", false)
        setDefaultBoolean(sharedPreferences, "displayOpenStreetMapMain", true)
        setDefaultBoolean(sharedPreferences, "displayOpenTopoMapMain", false)
        setDefaultBoolean(sharedPreferences, "displayOrganicMapsMain", false)
        setDefaultBoolean(sharedPreferences, "displayWazeMain", false)
        setDefaultBoolean(sharedPreferences, "displayWhat3WordsMain", false)
        setDefaultBoolean(sharedPreferences, "displayWikiMapiaMain", false)
        setDefaultBoolean(sharedPreferences, "displayWikiMiniAtlasMain", false)
        setDefaultBoolean(sharedPreferences, "displayYandexMain", false)

        preferences.forEachIndexed { index, preference ->
            initializeCheckBox(context, checkBoxIds[index], preference, sharedPreferences)
        }
    }

    private fun setDefaultBoolean(sharedPreferences: SharedPreferences, key: String, defaultValue: Boolean) {
        if (!sharedPreferences.contains(key)) {
            sharedPreferences.edit().putBoolean(key, defaultValue).apply()
        }
    }

    private fun initializeCheckBox(context: Context, checkBoxId: Int, preferenceKey: String, sharedPreferences: SharedPreferences) {
        val checkBox = (context as? Activity)?.findViewById<CheckBox>(checkBoxId)
        checkBox?.isChecked = sharedPreferences.getBoolean(preferenceKey, false)
        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(preferenceKey, isChecked).apply()
        }
        if (checkBox == null) {
            Log.e("CheckBox", "CheckBox with ID $checkBoxId not found")
        }
    }
}
