package net.retiolus.osm2gmaps.utils.maps.converters

import net.retiolus.osm2gmaps.utils.maps.extractHttpsLink
import net.retiolus.osm2gmaps.utils.maps.handleNoValidLocationData
import java.math.BigDecimal
import java.math.BigInteger

fun extractAppleMapsCoordinates(link: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val llIndex = link.indexOf("ll=")

    if (llIndex != -1) {
        val substring = link.substring(llIndex + 3)

        val parts = substring.split("&")

        if (parts.isNotEmpty()) {
            val latitudeLongitudePart = parts[0]

            val coordinates = latitudeLongitudePart.split("%2C")

            if (coordinates.size == 2) {
                try {
                    val latitude = coordinates[0].toBigDecimal()
                    val longitude = coordinates[1].toBigDecimal()
                    return Triple(latitude, longitude, BigInteger.ZERO)
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
            }
        }
    }
    return handleNoValidLocationData(extractHttpsLink(link))
}

fun createAppleMapsLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://maps.apple.com/place?ll=$latitude%2C$longitude")
    return "https://maps.apple.com/place?ll=$latitude%2C$longitude"
}