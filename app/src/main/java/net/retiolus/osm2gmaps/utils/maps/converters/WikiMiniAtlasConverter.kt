package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createWikiMiniAtlasLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    val formattedLatitude = String.format("%.5f", latitude)
    val formattedLongitude = String.format("%.5f", longitude)

    println("https://wma.wmflabs.org/iframe.html?wma=$formattedLatitude%5F$formattedLongitude%5F700_500_en_8_en&globe=Earth&lang=en&page=")
    return "https://wma.wmflabs.org/iframe.html?wma=$formattedLatitude%5F$formattedLongitude%5F700_500_en_8_en&globe=Earth&lang=en&page="
}