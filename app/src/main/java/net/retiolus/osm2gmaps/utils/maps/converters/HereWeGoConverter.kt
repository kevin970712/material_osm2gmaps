package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createHereWeGoLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://wego.here.com/location/?map=$latitude,$longitude,15,normal")
    return "https://wego.here.com/location/?map=$latitude,$longitude,15,normal"
}