package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createGoogleMapsLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://www.google.com/maps?q=$latitude,$longitude")
    return "https://www.google.com/maps?q=$latitude,$longitude"
}