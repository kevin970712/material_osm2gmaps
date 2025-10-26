package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createGeoLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("geo:$latitude,$longitude")
    return "geo:$latitude,$longitude"
}