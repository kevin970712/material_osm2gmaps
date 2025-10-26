package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createWazeLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://waze.com/ul?ll$latitude%2C$longitude&navigate=yes")
    return "https://waze.com/ul?ll$latitude%2C$longitude&navigate=yes"
}