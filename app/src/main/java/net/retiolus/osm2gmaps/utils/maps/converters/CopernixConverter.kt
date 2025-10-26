package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createCopernixLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://copernix.io/#?where=$longitude,$latitude,15")
    return "https://copernix.io/#?where=$longitude,$latitude,15"
}