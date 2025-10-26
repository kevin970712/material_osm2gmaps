package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun extractMagicEarhCoordinates(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val latIndex = link.indexOf("lat")
    val lonIndex = link.indexOf("lon")

    if (latIndex != -1 && lonIndex != -1) {
        val latSubstring = link.substring(latIndex + 4)
        val lonSubstring = link.substring(lonIndex + 4)

        val latitude = latSubstring.split("&")[0]
        val longitude = lonSubstring.split("&")[0]

        return Triple(latitude.toBigDecimal(), longitude.toBigDecimal(), BigInteger.ZERO)
    }

    return Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigInteger.ZERO)
}

fun createMagicEarthLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://magicearth.com/?show_on_map&lat=$latitude&lon=$longitude")
    return "https://magicearth.com/?show_on_map&lat=$latitude&lon=$longitude"
}