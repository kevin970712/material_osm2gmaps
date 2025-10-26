package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createYandexMapsLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://maps.yandex.com/?ll=$longitude,$latitude&spn=0.01,0.01&l=map&pt=$longitude,$latitude")
    return "https://maps.yandex.com/?ll=$longitude,$latitude&spn=0.01,0.01&l=map&pt=$longitude,$latitude"
}