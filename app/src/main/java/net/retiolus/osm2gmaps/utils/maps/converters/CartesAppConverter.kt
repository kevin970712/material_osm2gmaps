package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createCartesAppLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://cartes.app/#12/$latitude/$longitude")
    return "https://cartes.app/#12/$latitude/$longitude"
}