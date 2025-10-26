package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger

fun createOpenHistoricalMapLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://www.openhistoricalmap.org/?mlat=$latitude&mlon=$longitude&zoom=15&layers=O")
    return "https://www.openhistoricalmap.org/?mlat=$latitude&mlon=$longitude&zoom=15&layers=O"
}