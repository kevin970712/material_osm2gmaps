package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createOpenTopoMapLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://opentopomap.org/#marker=15/$latitude/$longitude")
    return "https://opentopomap.org/#marker=15/$latitude/$longitude"
}