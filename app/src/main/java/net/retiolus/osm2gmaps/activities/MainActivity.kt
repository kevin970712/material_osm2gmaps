package net.retiolus.osm2gmaps.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import createWhat3WordsLink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.retiolus.osm2gmaps.R
import net.retiolus.osm2gmaps.adapters.ConvertedLinksAdapter
import net.retiolus.osm2gmaps.utils.PreferencesUtil
import net.retiolus.osm2gmaps.utils.maps.LinkWithTitle
import net.retiolus.osm2gmaps.utils.maps.LocationInfo
import net.retiolus.osm2gmaps.utils.maps.converters.*
import net.retiolus.osm2gmaps.utils.maps.extractHttpsLink
import net.retiolus.osm2gmaps.utils.maps.getMapuLink
import net.retiolus.osm2gmaps.utils.maps.handleNoValidLocationData
import java.math.BigDecimal
import java.math.BigInteger

class MainActivity : BaseActivity() {
    private lateinit var inputText: EditText
    private lateinit var convertedLinksRecyclerView: RecyclerView
    private lateinit var convertedLinksAdapter: ConvertedLinksAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var convertButton: Button
    private var locationInfo: LocationInfo? = null

    // 修改點 1: 宣告 FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setUpToolbar()
        setUpLocationButton()
        setUpConvertButton()
        handleIntent()

        PreferencesUtil.setupGeneralPreferences(this)
        PreferencesUtil.setupDisplayedLinksPreferences(this)
    }

    private fun initializeViews() {
        inputText = findViewById(R.id.inputText)
        convertedLinksRecyclerView = findViewById(R.id.convertedLinksRecyclerView)
        sharedPreferences = getSharedPreferences("osm2gmaps", MODE_PRIVATE)
        progressBar = findViewById(R.id.progressBar)
        progressTextView = findViewById(R.id.progressTextView)
        convertButton = findViewById(R.id.convertButton)

        // 修改點 2: 在這裡初始化 fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setUpLocationButton() {
        val locationButton = findViewById<Button>(R.id.locationButton)
        locationButton.setOnClickListener {
            getLocation()
        }
    }

    private fun setUpConvertButton() {
        convertButton.setOnClickListener {
            val userInput = inputText.text.toString()

            convertButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            progressTextView.text = getString(R.string.progress_querying)
            progressTextView.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.Main).launch {
                if (!isNetworkAvailable()) {
                    progressTextView.text = getString(R.string.error_no_network)
                    delay(1500)
                    progressBar.visibility = View.GONE
                    progressTextView.visibility = View.GONE
                    convertButton.isEnabled = true
                    return@launch
                }

                val coords = handleUserInput(userInput)
                val convertedLinks = convertUserInput(coords.first, coords.second, coords.third)
                val saveHistoryValue = sharedPreferences.getBoolean("saveHistory", true)

                if (saveHistoryValue) {
                    locationInfo?.let { addToHistory(it) }
                }

                displayConvertedLinks(convertedLinks)

                val errorLink = convertedLinks.firstOrNull { it.link.startsWith("Error:") }
                if (errorLink != null) {
                    progressTextView.text = getString(R.string.error_service, errorLink.link.removePrefix("Error:").trim())
                    delay(1500)
                }

                progressBar.visibility = View.GONE
                progressTextView.visibility = View.GONE
                convertButton.isEnabled = true
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleIntent() {
        GlobalScope.launch {
            when (intent?.action) {
                Intent.ACTION_SEND -> handleActionSend(intent)
                Intent.ACTION_VIEW -> handleActionView(intent)
            }
        }
    }

    private suspend fun handleActionSend(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        handleIntentData(sharedText)
    }

    private suspend fun handleActionView(intent: Intent) {
        val sharedText = intent.dataString
        handleIntentData(sharedText)
    }

    private suspend fun handleIntentData(sharedText: String?) {
        sharedText ?: return
        withContext(Dispatchers.Main) {
            inputText.setText(sharedText)
            convertButton.isEnabled = false
            progressBar.visibility = View.VISIBLE
            progressTextView.text = getString(R.string.progress_querying)
            progressTextView.visibility = View.VISIBLE
        }

        if (!isNetworkAvailable()) {
            withContext(Dispatchers.Main) {
                progressTextView.text = getString(R.string.error_no_network)
            }
            delay(1500)
            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                progressTextView.visibility = View.GONE
                convertButton.isEnabled = true
            }
            return
        }

        val coords = handleSharedText(sharedText)
        val selectedProviderIndex = sharedPreferences.getInt("selectedMapProviderIndex", 0)
        val link = when (selectedProviderIndex) {
            0 -> create2GisLink(coords.first, coords.second, coords.third)
            1 -> createAcmeMapperLink(coords.first, coords.second, coords.third)
            2 -> createAppleMapsLink(coords.first, coords.second, coords.third)
            3 -> createBingMapsLink(coords.first, coords.second, coords.third)
            4 -> createCartesAppLink(coords.first, coords.second, coords.third)
            5 -> createCopernixLink(coords.first, coords.second, coords.third)
            6 -> createGeoHackLink(coords.first, coords.second, coords.third)
            7 -> createGeoLink(coords.first, coords.second, coords.third)
            8 -> createGoogleMapsLink(coords.first, coords.second, coords.third)
            9 -> createHereWeGoLink(coords.first, coords.second, coords.third)
            10 -> createMagicEarthLink(coords.first, coords.second, coords.third)
            11 -> createMapQuestLink(coords.first, coords.second, coords.third)
            12 -> createOpenHistoricalMapLink(coords.first, coords.second, coords.third)
            13 -> createOpenStreetMapLink(coords.first, coords.second, coords.third)
            14 -> createOpenTopoMapLink(coords.first, coords.second, coords.third)
            15 -> createOmapsAppLink(coords.first, coords.second, coords.third)
            16 -> createWazeLink(coords.first, coords.second, coords.third)
            17 -> createWhat3WordsLink(coords.first, coords.second, this@MainActivity)
            18 -> createWikiMapiaConverterLink(coords.first, coords.second, coords.third)
            19 -> createWikiMiniAtlasLink(coords.first, coords.second, coords.third)
            20 -> createYandexMapsLink(coords.first, coords.second, coords.third)
            else -> ""
        }
        val convertedLinks = convertUserInput(coords.first, coords.second, coords.third)
        val copyToClipboard = if (intent.action == Intent.ACTION_SEND) {
            sharedPreferences.getBoolean("copyToClipboardOnShare", false)
        } else {
            sharedPreferences.getBoolean("copyToClipboardOnOpenWith", false)
        }
        val openShareMenu = if (intent.action == Intent.ACTION_SEND) {
            sharedPreferences.getBoolean("openShareMenuOnShare", false)
        } else {
            sharedPreferences.getBoolean("openShareMenuOnOpenWith", false)
        }
        val openDefaultApp = if (intent.action == Intent.ACTION_SEND) {
            sharedPreferences.getBoolean("openDefaultAppOnShare", false)
        } else {
            sharedPreferences.getBoolean("openDefaultAppOnOpenWith", false)
        }
        if (copyToClipboard) {
            copyToClipboard(link)
        }
        if (openShareMenu) {
            openSendMenuWithLink(this@MainActivity, link)
        }
        if (openDefaultApp){
            openDefaultApp(this@MainActivity, link)
        }
        val saveHistoryValue = sharedPreferences.getBoolean("saveHistory", true)
        if (saveHistoryValue) {
            locationInfo?.let { addToHistory(it) }
        }

        withContext(Dispatchers.Main) {
            displayConvertedLinks(convertedLinks)
        }

        val errorLink = convertedLinks.firstOrNull { it.link.startsWith("Error:") }
        if (errorLink != null) {
            withContext(Dispatchers.Main) {
                progressTextView.text = getString(R.string.error_service, errorLink.link.removePrefix("Error:").trim())
            }
            delay(1500)
        }

        withContext(Dispatchers.Main) {
            progressBar.visibility = View.GONE
            progressTextView.visibility = View.GONE
            convertButton.isEnabled = true
        }
    }

    private suspend fun handleUserInput(userInput: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        return handleSharedText(userInput)
    }

    private suspend fun handleSharedText(sharedText: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        if (sharedText.isEmpty()) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    R.string.toast_empty_input,
                    Toast.LENGTH_SHORT
                ).show()
            }
            return Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigInteger.ZERO)
        }

        return try {
            val utmRegex = Regex("^\\s*(UTM:\\s*)?\\d{1,2}[C-Xc-x]?\\s+\\d+(\\.\\d+)?\\s+\\d+(\\.\\d+)?\\s*$")
            val mgrsRegex = Regex("^\\s*(MGRS:\\s*)?\\d{1,2}[C-Xc-x]\\s*[A-HJ-NP-Z]{2}\\s*\\d{4,}\\s*\\d{4,}\\s*$")

            val coordinates = when {
                utmRegex.matches(sharedText) -> handleUTMInput(sharedText)
                mgrsRegex.matches(sharedText) -> handleMGRSInput(sharedText)
                sharedText.contains("UTM", ignoreCase = true) -> handleUTMInput(sharedText)
                sharedText.contains("MGRS", ignoreCase = true) -> handleMGRSInput(sharedText)
                sharedText.contains("2gis") -> handle2gisLink(sharedText)
                sharedText.startsWith("https://osm.org/go/") -> handleOsmOrgGoLink(sharedText)
                sharedText.contains("https://omaps.app/") -> handleOmapsAppLink(sharedText)
                sharedText.contains("openstreetmap.org/") ->
                    extractOpenStreetMapOrgCoordinates(extractHttpsLink(sharedText))
                sharedText.contains("https://maps.apple.com/") ->
                    extractAppleMapsCoordinates(extractHttpsLink(sharedText))
                sharedText.contains("bing.com") ->
                    extractBingMapsCoordinates(extractHttpsLink(sharedText))
                sharedText.contains("https://magicearth.com/") ->
                    extractMagicEarhCoordinates(extractHttpsLink(sharedText))
                sharedText.contains("waze.com") ||
                        sharedText.contains("yandex") ||
                        isGoogleMapsLink(sharedText) -> {
                    val fullLink = handleProprietaryShortLink(sharedText)
                    handleNoValidLocationData(extractHttpsLink(fullLink))
                }
                else -> handleNoValidLocationData(sharedText)
            }

            locationInfo = LocationInfo(
                source = sharedText,
                latitude = coordinates.first,
                longitude = coordinates.second,
                zoom = coordinates.third,
                timestamp = System.currentTimeMillis()
            )
            coordinates
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Invalid coordinate input",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigInteger.ZERO)
        }
    }

    private fun isGoogleMapsLink(link: String): Boolean {
        val googleMapsLinks = listOf(
            "https://maps.app.goo.gl/",
            "https://goo.gl/",
            "https://www.google.com/maps/"
        )
        return googleMapsLinks.any { link.contains(it) }
    }

    private suspend fun handleProprietaryShortLink(link: String): String {
        val selectedProprietaryUnshortenerIndex =
            sharedPreferences.getInt("selectedProprietaryLinksUnshortener", 0)
        return when (selectedProprietaryUnshortenerIndex) {
            0 -> {
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_need_mapu,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                ""
            }
            1 -> {
                val coords = getMapuLink(link, "https://mapu.retiolus.net")
                if ("Error" in coords) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            coords,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                coords
            }
            else -> {
                val customResolver = sharedPreferences.getString("customGoogleMapsShortLink", "")
                if (customResolver.isNullOrEmpty()) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.toast_need_custom_resolver,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    ""
                } else {
                    val coords = getMapuLink(link, "https://$customResolver")
                    if ("Error" in coords) {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                coords,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    coords
                }
            }
        }
    }

    private fun handleUTMInput(sharedText: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        return convertUTMToLatLng(sharedText)
    }

    private fun handleMGRSInput(sharedText: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        return convertMGRSToLatLng(sharedText)
    }

    private suspend fun handle2gisLink(sharedText: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        val extractedCoordinates = extract2GisCoordinates(extractHttpsLink(sharedText))
        return if (extractedCoordinates.first != BigDecimal.ZERO) {
            extractedCoordinates
        } else {
            val link = handleProprietaryShortLink(sharedText)
            extract2GisCoordinates(link)
        }
    }

    private fun handleOsmOrgGoLink(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        return decodeOsmGoShortCode(getOsmGoShortCode(link))
    }

    private fun handleOmapsAppLink(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
        return decodeOmapsAppShortCode(getOmapsAppShortCode(extractHttpsLink(link)))
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Google Maps Link", text)
        clipboardManager.setPrimaryClip(clip)
    }

    private fun openSendMenuWithLink(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link)
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun openDefaultApp(context: Context, link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        context.startActivity(intent)
    }

    private suspend fun convertUserInput(
        latitude: BigDecimal,
        longitude: BigDecimal,
        zoomLevel: BigInteger
    ): List<LinkWithTitle> {
        val displayAppleMapsMain = sharedPreferences.getBoolean("displayAppleMapsMain", false)
        val displayBingMapsMain = sharedPreferences.getBoolean("displayBingMapsMain", false)
        val displayCartesAppMain = sharedPreferences.getBoolean("displayCartesAppMain", false)
        val displayCopernixMain = sharedPreferences.getBoolean("displayCopernixMain", false)
        val displayDMSMain = sharedPreferences.getBoolean("displayDMSMain", false)
        val displayGeoHackMain = sharedPreferences.getBoolean("displayGeoHackMain", false)
        val displayGeoLinkMain = sharedPreferences.getBoolean("displayGeoLinkMain", false)
        val displayGoogleMapsMain = sharedPreferences.getBoolean("displayGoogleMapsMain", false)
        val displayHereWeGoMain = sharedPreferences.getBoolean("displayHereWeGoMain", false)
        val displayMagicEarthMain = sharedPreferences.getBoolean("displayMagicEarthMain", false)
        val displayMapQuestMain = sharedPreferences.getBoolean("displayMapQuestMain", false)
        val displayOpenHistoricalMapMain = sharedPreferences.getBoolean("displayOpenHistoricalMapMain", false)
        val displayOpenLocationCodeMain = sharedPreferences.getBoolean("displayOpenLocationCodeMain", false)
        val displayOpenStreetMapMain = sharedPreferences.getBoolean("displayOpenStreetMapMain", false)
        val displayOpenTopoMapMain = sharedPreferences.getBoolean("displayOpenTopoMapMain", false)
        val displayOrganicMapsMain = sharedPreferences.getBoolean("displayOrganicMapsMain", false)
        val displayWazeMain = sharedPreferences.getBoolean("displayWazeMain", false)
        val displayWhat3WordsMain = sharedPreferences.getBoolean("displayWhat3WordsMain", false)
        val displayWikiMapiaMain = sharedPreferences.getBoolean("displayWikiMapiaMain", false)
        val displayWikiMiniAtlasMain =
            sharedPreferences.getBoolean("displayWikiMiniAtlasMain", false)
        val displayYandexMain = sharedPreferences.getBoolean("displayYandexMain", false)

        val linksWithTitles = mutableListOf<LinkWithTitle>()

        if (displayGeoLinkMain) {
            val geoLink = createGeoLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("GeoLink", geoLink)
            linksWithTitles.add(LinkWithTitle(geoLink, "GeoLink"))
        }

        if (displayAppleMapsMain) {
            val appleMapsLink = createAppleMapsLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Apple Maps", appleMapsLink)
            linksWithTitles.add(LinkWithTitle(appleMapsLink, "Apple Maps"))
        }

        if (displayBingMapsMain) {
            val bingMapsLink = createBingMapsLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Bing Maps", bingMapsLink)
            linksWithTitles.add(LinkWithTitle(bingMapsLink, "Bing Maps"))
        }

        if (displayCartesAppMain) {
            val cartesAppLink = createCartesAppLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("cartes.app", cartesAppLink)
            linksWithTitles.add(LinkWithTitle(cartesAppLink, "cartes.app"))
        }

        if (displayCopernixMain) {
            val copernixLink = createCopernixLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Copernix", copernixLink)
            linksWithTitles.add(LinkWithTitle(copernixLink, "Copernix"))
        }

        if (displayDMSMain) {
            val dmsLink = createDMSLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("DMS", dmsLink)
            linksWithTitles.add(LinkWithTitle(dmsLink, "DMS"))
        }

        if (displayGeoHackMain) {
            val geoHackLink = createGeoHackLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("GeoHack", geoHackLink)
            linksWithTitles.add(LinkWithTitle(geoHackLink, "GeoHack"))
        }

        if (displayGoogleMapsMain) {
            val googleMapsLink = createGoogleMapsLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Google Maps", googleMapsLink)
            linksWithTitles.add(LinkWithTitle(googleMapsLink, "Google Maps"))
        }

        if (displayHereWeGoMain) {
            val hereWeGoLink = createHereWeGoLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("HERE WeGo", hereWeGoLink)
            linksWithTitles.add(LinkWithTitle(hereWeGoLink, "HERE WeGo"))
        }

        if (displayMagicEarthMain) {
            val magicEarthLink = createMagicEarthLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Magic Earth", magicEarthLink)
            linksWithTitles.add(LinkWithTitle(magicEarthLink, "Magic Earth"))
        }

        if (displayMapQuestMain) {
            val mapQuestLink = createMapQuestLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("MapQuest", mapQuestLink)
            linksWithTitles.add(LinkWithTitle(mapQuestLink, "MapQuest"))
        }

        if (displayOpenHistoricalMapMain) {
            val openHistoricalMapLink = createOpenHistoricalMapLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("OpenHistoricalMap", openHistoricalMapLink)
            linksWithTitles.add(LinkWithTitle(openHistoricalMapLink, "OpenHistoricalMap"))
        }

        if (displayOpenLocationCodeMain) {
            val openLocationCode = createOpenLocationCodeLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Open Location Code", openLocationCode)
            linksWithTitles.add(LinkWithTitle(openLocationCode, "Open Location Code"))
        }

        if (displayOpenStreetMapMain) {
            val openStreetMapLink = createOpenStreetMapLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("OpenStreetMap", openStreetMapLink)
            linksWithTitles.add(LinkWithTitle(openStreetMapLink, "OpenStreetMap"))
        }

        if (displayOpenTopoMapMain) {
            val openTopoMapLink = createOpenTopoMapLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("OpenTopoMap", openTopoMapLink)
            linksWithTitles.add(LinkWithTitle(openTopoMapLink, "OpenTopoMap"))
        }

        if (displayOrganicMapsMain) {
            val omapsAppLink = createOmapsAppLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Omaps App", omapsAppLink)
            linksWithTitles.add(LinkWithTitle(omapsAppLink, "Organic Maps"))
        }

        if (displayWazeMain) {
            val wazeLink = createWazeLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Waze", wazeLink)
            linksWithTitles.add(LinkWithTitle(wazeLink, "Waze"))
        }

        if (displayWhat3WordsMain){
            val what3WordsLink = createWhat3WordsLink(latitude, longitude, this@MainActivity)
            locationInfo?.addMapLink("what3words", what3WordsLink)
            linksWithTitles.add(LinkWithTitle(what3WordsLink, "what3words"))
        }

        if (displayWikiMapiaMain) {
            val wikiMapiaLink = createWikiMapiaConverterLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Wikimapia", wikiMapiaLink)
            linksWithTitles.add(LinkWithTitle(wikiMapiaLink, "Wikimapia"))
        }

        if (displayWikiMiniAtlasMain) {
            val wikiMiniAtlasLink = createWikiMiniAtlasLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("WikiMiniAtlas", wikiMiniAtlasLink)
            linksWithTitles.add(LinkWithTitle(wikiMiniAtlasLink, "WikiMiniAtlas"))
        }

        if (displayYandexMain) {
            val yandexLink = createYandexMapsLink(latitude, longitude, zoomLevel)
            locationInfo?.addMapLink("Yandex Maps", yandexLink)
            linksWithTitles.add(LinkWithTitle(yandexLink, "Yandex Maps"))
        }

        val customLinksSharedPreferences = getSharedPreferences("custom_links", Context.MODE_PRIVATE)
        val dms = convertToDMS(latitude, longitude)
        val latString = latitude.toString()
        val lonString = longitude.toString()
        val latDegrees = latString.substringBefore(".")
        val latDecimals = latString.substringAfter(".").take(5)
        val lonDegrees = lonString.substringBefore(".")
        val lonDecimals = lonString.substringAfter(".").take(5)

        customLinksSharedPreferences.all.forEach { (name, link) ->
            if (link is String) {
                val customLink = link
                    .replace("{latitude}", latitude.toString())
                    .replace("{latitude.degrees}", latDegrees)
                    .replace("{latitude.decimals}", latDecimals)
                    .replace("{longitude}", longitude.toString())
                    .replace("{longitude.degrees}", lonDegrees)
                    .replace("{longitude.decimals}", lonDecimals)
                    .replace("{dms}", createDMSLink(latitude, longitude, zoomLevel))
                    .replace("{dms.latitude}", createDMSLatitude(dms))
                    .replace("{dms.latitude.degrees}", dms.latitudeDegrees.toString())
                    .replace("{dms.latitude.minutes}", dms.latitudeMinutes.toString())
                    .replace("{dms.latitude.seconds}", dms.latitudeSeconds.toString())
                    .replace("{dms.latitude.direction}", dms.latitudeDirection)
                    .replace("{dms.longitude}", createDMSLongitude(dms))
                    .replace("{dms.longitude.degrees}", dms.longitudeDegrees.toString())
                    .replace("{dms.longitude.minutes}", dms.longitudeMinutes.toString())
                    .replace("{dms.longitude.seconds}", dms.longitudeSeconds.toString())
                    .replace("{dms.longitude.direction}", dms.longitudeDirection)
                    .replace("{openlocationcode}", createOpenLocationCodeLink(latitude, longitude, zoomLevel))
                linksWithTitles.add(LinkWithTitle(customLink, name))
            }
        }

        println(linksWithTitles)
        return linksWithTitles
    }

    private fun displayConvertedLinks(convertedLinks: List<LinkWithTitle>) {
        val links = convertedLinks.map { it.link }
        val titles = convertedLinks.map { it.title }

        runOnUiThread {
            if (!::convertedLinksAdapter.isInitialized) {
                convertedLinksAdapter = ConvertedLinksAdapter(links, titles)
                convertedLinksRecyclerView.layoutManager = LinearLayoutManager(this)
                convertedLinksRecyclerView.adapter = convertedLinksAdapter
            } else {
                convertedLinksAdapter.updateData(links, titles)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.d("Location", "Permission not granted, requesting...")
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // 成功獲取位置！
                    val inputEditText = findViewById<TextInputEditText>(R.id.inputText)
                    inputEditText.setText("${location.latitude}, ${location.longitude}")
                    Log.d("Location", "Location found: ${location.latitude}, ${location.longitude}")
                } else {
                    // 如果仍然是 null，通常是模擬器位置未設定或系統定位服務關閉
                    Log.d("Location", "Location is null. Check emulator location settings or device GPS.")
                    Toast.makeText(this, "Could not get location. Ensure location service is on.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                // 處理獲取位置時可能發生的異常
                Log.e("Location", "Error getting location", e)
                Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 使用者同意了權限，再次呼叫 getLocation()
                getLocation()
            } else {
                // 使用者拒絕了權限
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        // 您的 request code 是 100，我們保持不變
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun addToHistory(locationInfo: LocationInfo) {
        GlobalScope.launch {
            val gson = Gson()
            val historyJson = sharedPreferences.getString("history", "[]") ?: "[]"
            Log.d("HistoryActivity", "History JSON: $historyJson")
            val historyListType = object : TypeToken<ArrayList<LocationInfo>>() {}.type
            val historyList: ArrayList<LocationInfo> = gson.fromJson(historyJson, historyListType)

            val existingItemIndex = historyList.indexOfFirst { it.source == locationInfo.source }

            if (existingItemIndex != -1) {
                historyList.removeAt(existingItemIndex)
            }

            historyList.add(locationInfo)

            val updatedHistoryJson = gson.toJson(historyList)
            Log.d("HistoryActivity", "Updated History JSON: $updatedHistoryJson")
            sharedPreferences.edit().putString("history", updatedHistoryJson).apply()
        }
    }
}