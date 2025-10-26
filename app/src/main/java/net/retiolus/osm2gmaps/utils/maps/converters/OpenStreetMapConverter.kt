package net.retiolus.osm2gmaps.utils.maps.converters

import net.retiolus.osm2gmaps.utils.maps.handleNoValidLocationData
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URLDecoder

fun getOsmGoShortCode(link: String): String {
    val goIndex = link.indexOf("/go/")
    if (goIndex != -1) {
        val subString = link.substring(goIndex + 4)
        val questionIndex = subString.indexOf("?")
        return if (questionIndex != -1) {
            URLDecoder.decode(subString.substring(0, questionIndex), "UTF-8")
        } else {
            URLDecoder.decode(subString, "UTF-8")
        }
    }
    throw IllegalArgumentException("Invalid OSM link: $link")
}

fun decodeOsmGoShortCode(s: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    println("Shortcode: $s")
    val array = ('A'..'Z').toList() + ('a'..'z').toList() + ('0'..'9').toList() + listOf('_', '~')
    var x = 0L
    var y = 0L
    var z = 0
    var zOffset = 0

    val modifiedStr = s.replace("@", "~")

    modifiedStr.forEach { c ->
        var t = array.indexOf(c)
        if (t == -1) {
            zOffset -= 1
        } else {
            repeat(3) {
                x = x shl 1
                x = x or (if ((t and 32) != 0) 1 else 0)
                t = t shl 1

                y = y shl 1
                y = y or (if ((t and 32) != 0) 1 else 0)
                t = t shl 1
            }
            z += 3
        }
    }
    x = x shl (32 - z)
    y = y shl (32 - z)

    val lon = (x * 360.0 / (1L shl 32)) - 180.0
    val lat = (y * 180.0 / (1L shl 32)) - 90.0

    z = z - 8 - (zOffset % 3)
    println("Decoded latitude: $lat")
    println("Decoded longitude: $lon")
    println("Decoded Zoom: $z")
    return Triple(BigDecimal.valueOf(lat), BigDecimal.valueOf(lon), BigInteger.valueOf(z.toLong()))
}

fun extractOpenStreetMapOrgCoordinates(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val regex = Regex("""/(\d+\.\d+)/(\d+\.\d+)""")
    val matchResult = regex.find(link)
    return matchResult?.let {
        val (latitude, longitude) = it.destructured
        Triple(latitude.toBigDecimal(), longitude.toBigDecimal(), BigInteger.ZERO)
    } ?: return handleNoValidLocationData(link)
}

fun createOpenStreetMapLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://openstreetmap.org/?mlat=$latitude&mlon=$longitude")
    return "https://openstreetmap.org/?mlat=$latitude&mlon=$longitude"
}