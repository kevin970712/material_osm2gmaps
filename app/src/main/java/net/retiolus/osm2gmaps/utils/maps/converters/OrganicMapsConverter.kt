package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun getOmapsAppShortCode(url: String): String {
    val pattern = Regex("""https?://[^/]+/([^/]+)/?""")
    val matchResult = pattern.find(url)
    if (matchResult != null) {
        return matchResult.groupValues[1]
    }
    return ""
}

fun decodeOmapsAppShortCode(encodedLatLonZoom: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    println("Encoded: $encodedLatLonZoom")
    val geoMaxPointBytes = 10
    val geoMaxCoordBits = geoMaxPointBytes * 3

    val zoomChar = encodedLatLonZoom[0]
    val zoom = base64Reverse[zoomChar]
        ?: throw IllegalArgumentException("Invalid zoom level: the url was not encoded properly")
    if (zoom > 63) throw IllegalArgumentException("Invalid zoom level: the url was not encoded properly")
    val roundedZoom = Math.round(zoom.toDouble() / 4 + 4).toInt()

    val latLonStr = encodedLatLonZoom.substring(1)
    val latLonBytes = latLonStr.length

    var lat = 0
    var lon = 0

    var shift = geoMaxCoordBits - 3
    for (i in 0 until latLonBytes) {
        val a = base64Reverse[latLonStr[i]]
            ?: throw IllegalArgumentException("Invalid character in encoded string")
        val lat1 = (((a shr 5) and 1) shl 2) or (((a shr 3) and 1) shl 1) or ((a shr 1) and 1)
        val lon1 = (((a shr 4) and 1) shl 2) or (((a shr 2) and 1) shl 1) or (a and 1)
        lat = lat or (lat1 shl shift)
        lon = lon or (lon1 shl shift)
        shift -= 3
    }

    val middleOfSquare = 1 shl (3 * (geoMaxPointBytes - latLonBytes) - 1)
    lat += middleOfSquare
    lon += middleOfSquare

    var latDouble = lat.toDouble() / ((1 shl geoMaxCoordBits) - 1) * 180.0 - 90.0
    var lonDouble = lon.toDouble() / (1 shl geoMaxCoordBits) * 360.0 - 180.0

    latDouble = Math.round(latDouble * 1e5) / 1e5
    lonDouble = Math.round(lonDouble * 1e5) / 1e5

    if (latDouble <= -90.0 || latDouble >= 90.0 || lonDouble <= -180.0 || lonDouble >= 180.0)
        throw IllegalArgumentException("Invalid coordinates $encodedLatLonZoom, the url was not encoded properly")

    println("Decoded latitude: $latDouble")
    println("Decoded longitude: $lonDouble")
    println("Decoded Zoom: $roundedZoom")
    return Triple(BigDecimal.valueOf(latDouble), BigDecimal.valueOf(lonDouble), BigInteger.valueOf(
        roundedZoom.toLong()
    ))
}

private val base64Reverse: Map<Char, Int> = mapOf(
    'A' to 0,
    'B' to 1,
    'C' to 2,
    'D' to 3,
    'E' to 4,
    'F' to 5,
    'G' to 6,
    'H' to 7,
    'I' to 8,
    'J' to 9,
    'K' to 10,
    'L' to 11,
    'M' to 12,
    'N' to 13,
    'O' to 14,
    'P' to 15,
    'Q' to 16,
    'R' to 17,
    'S' to 18,
    'T' to 19,
    'U' to 20,
    'V' to 21,
    'W' to 22,
    'X' to 23,
    'Y' to 24,
    'Z' to 25,
    'a' to 26,
    'b' to 27,
    'c' to 28,
    'd' to 29,
    'e' to 30,
    'f' to 31,
    'g' to 32,
    'h' to 33,
    'i' to 34,
    'j' to 35,
    'k' to 36,
    'l' to 37,
    'm' to 38,
    'n' to 39,
    'o' to 40,
    'p' to 41,
    'q' to 42,
    'r' to 43,
    's' to 44,
    't' to 45,
    'u' to 46,
    'v' to 47,
    'w' to 48,
    'x' to 49,
    'y' to 50,
    'z' to 51,
    '0' to 52,
    '1' to 53,
    '2' to 54,
    '3' to 55,
    '4' to 56,
    '5' to 57,
    '6' to 58,
    '7' to 59,
    '8' to 60,
    '9' to 61,
    '-' to 62,
    '_' to 63
)

fun createOmapsAppLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://omaps.app/map?v=1&ll=$latitude,$longitude")
    return "https://omaps.app/map?v=1&ll=$latitude,$longitude"
}

// need to encode the link before
fun createOrganicMapsLink(latitude: Double, longitude: Double, zoomLevel: Int): String {
    println("om://$latitude,$longitude")
    return "om://$latitude,$longitude"
}