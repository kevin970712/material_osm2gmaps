package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun extract2GisCoordinates(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val decodedLink = java.net.URLDecoder.decode(link, "UTF-8")
    val coordinatesRegex = """([-+]?\d*\.?\d+),([-+]?\d*\.?\d+)""".toRegex()
    val zoomRegex = """(?<=/zoom/)([\d.]+)""".toRegex()

    val coordinatesMatch = coordinatesRegex.find(decodedLink)
    println(coordinatesMatch)
    val zoomMatch = zoomRegex.find(decodedLink)

    if (coordinatesMatch != null) {
        val latitude = coordinatesMatch.groupValues[2].toBigDecimal()
        val longitude = coordinatesMatch.groupValues[1].toBigDecimal()
        /*val zoom = zoomMatch?.groupValues?.get(1)?.toInt() ?: 0*/
        val zoom = BigInteger.ZERO

        return Triple(latitude, longitude, zoom)
    }

    return Triple(BigDecimal.ZERO, BigDecimal.ZERO, BigInteger.ZERO)
}

fun create2GisLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://2gis.ru/geo/$longitude%2C$latitude")
    return "https://2gis.ru/geo/$longitude%2C$latitude"
}