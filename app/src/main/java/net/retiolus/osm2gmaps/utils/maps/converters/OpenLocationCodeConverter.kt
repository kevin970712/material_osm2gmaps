package net.retiolus.osm2gmaps.utils.maps.converters

import com.google.openlocationcode.OpenLocationCode
import java.math.BigDecimal
import java.math.BigInteger

fun createOpenLocationCodeLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    var code = OpenLocationCode(latitude.toDouble(), longitude.toDouble()).code;
    return code
}