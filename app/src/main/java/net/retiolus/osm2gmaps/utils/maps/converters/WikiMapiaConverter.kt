package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createWikiMapiaConverterLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://wikimapia.org/#lang=en&lat=$latitude&lon=$longitude&z=15&m=w")
    return "https://wikimapia.org/#lang=en&lat=$latitude&lon=$longitude&z=15&m=w"
}