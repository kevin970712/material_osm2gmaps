package net.retiolus.osm2gmaps.utils.maps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

data class LinkWithTitle(val link: String, val title: String)

suspend fun getMapuLink(shortLink: String, host: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("$host/unshortener?link=$shortLink")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                response.toString()
            } else {
                "Error: HTTP $responseCode"
            }
        } catch (e: MalformedURLException) {
            "Error: Malformed URL"
        } catch (e: IOException) {
            "Error: IOException - ${e.message}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

fun extractHttpsLink(text: Any): String {
    val pattern = Regex("""https://\S+""")
    val matchResult = pattern.find(text.toString())
    return matchResult?.value ?: ""
}

fun handleNoValidLocationData(sharedText: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val coordinates = extractCoordinates(sharedText)
    if (coordinates != null) {
        return Triple(coordinates.first.toBigDecimal(), coordinates.second.toBigDecimal(), BigInteger.ZERO)
    }
    return Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigInteger.ZERO)
}

fun extractCoordinates(text: String): Pair<Double, Double>? {
    println("HERE: $text")
    val coordinateFormats = listOf(
        Regex("""!3d(-?\d+\.\d+)!4d(-?\d+\.\d+)"""),
        Regex("""q=(-?\d+\.\d+)%2C(-?\d+\.\d+)"""),
        Regex("""to=ll\.(-?\d+\.\d+)%2C(-?\d+\.\d+)"""),
        Regex("""ll=(-?\d+\.\d+)%2C(-?\d+\.\d+)"""),
        Regex("""(\d+\.?\d*)[°˚]\s*(\d+\.?\d*)['′]\s*(\d+\.?\d*)["″]\s*([NSEW])"""),
        Regex("""/([-+]?\d+\.\d+)\s*,\s*([-+]?\d+\.\d+)\?"""),
        Regex("""([-+]?\d+\.?\d*),\s*([-+]?\d+\.?\d*)"""),
        Regex("""([0-9A-Z]+)\s*([0-9A-Z]+)\s*([0-9]+)\s*([0-9]+)\s*([0-9]+)\s*([0-9]+)""")
    )

    for (format in coordinateFormats) {
        println(format)
        val matchResult = format.find(text)
        if (matchResult != null) {
            val (latitude, longitude) = matchResult.destructured
            println(latitude)
            println(longitude)
            return Pair(parseCoordinate(latitude), parseCoordinate(longitude))
        }
    }
    return null
}

fun parseCoordinate(coordinate: String): Double {
    return when {
        coordinate.matches(Regex("""[-+]?\d+\.?\d*""")) -> coordinate.toDouble()
        coordinate.matches(Regex("""\d+""")) -> coordinate.toDouble()
        coordinate.matches(Regex("""[0-9A-Z]+""")) -> coordinate.toDouble()
        else -> 0.0
    }
}