package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createMapQuestLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://www.mapquest.com/latlng/$latitude,$longitude?zoom=15&maptype=map")
    return "https://www.mapquest.com/latlng/$latitude,$longitude?zoom=15&maptype=map"
}