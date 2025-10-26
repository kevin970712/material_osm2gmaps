package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

fun createGeoHackLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    val absLatitude = latitude.abs().setScale(6, RoundingMode.HALF_UP)
    val absLongitude = longitude.abs().setScale(6, RoundingMode.HALF_UP)
    val latitudeLetter = if (latitude >= BigDecimal.ZERO) "N" else "S"
    val longitudeLetter = if (longitude >= BigDecimal.ZERO) "E" else "W"

    val geoHackUrl = "https://geohack.toolforge.org/geohack.php?params=${absLatitude}_${latitudeLetter}_${absLongitude}_${longitudeLetter}&zoom=${zoomLevel}"
    println(geoHackUrl)
    return geoHackUrl
}

