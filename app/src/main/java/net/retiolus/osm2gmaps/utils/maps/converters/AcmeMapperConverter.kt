package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createAcmeMapperLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://mapper.acme.com/?ll=$latitude,$longitude")
    return "https://mapper.acme.com/?ll=$latitude,$longitude"
}