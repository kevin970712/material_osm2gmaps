package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class DMS(
    val latitudeDegrees: Int,
    val latitudeMinutes: Int,
    val latitudeSeconds: Int,
    val latitudeDirection: String,
    val longitudeDegrees: Int,
    val longitudeMinutes: Int,
    val longitudeSeconds: Int,
    val longitudeDirection: String
)

fun convertToDMS(latitude: BigDecimal, longitude: BigDecimal): DMS {
    val latitudeDirection = if (latitude >= BigDecimal.ZERO) "N" else "S"
    val longitudeDirection = if (longitude >= BigDecimal.ZERO) "E" else "W"

    val latitudeAbs = latitude.abs()
    val longitudeAbs = longitude.abs()

    val latitudeDegrees = latitudeAbs.toInt()
    val latitudeMinutes = ((latitudeAbs.subtract(BigDecimal(latitudeDegrees))).multiply(BigDecimal(60))).toInt()
    val latitudeSeconds = ((latitudeAbs.subtract(BigDecimal(latitudeDegrees))
        .subtract(BigDecimal(latitudeMinutes).divide(BigDecimal(60), 10, RoundingMode.HALF_UP))
        .multiply(BigDecimal(3600)))).setScale(0, RoundingMode.HALF_UP).toInt()

    val longitudeDegrees = longitudeAbs.toInt()
    val longitudeMinutes = ((longitudeAbs.subtract(BigDecimal(longitudeDegrees))).multiply(BigDecimal(60))).toInt()
    val longitudeSeconds = ((longitudeAbs.subtract(BigDecimal(longitudeDegrees))
        .subtract(BigDecimal(longitudeMinutes).divide(BigDecimal(60), 10, RoundingMode.HALF_UP))
        .multiply(BigDecimal(3600)))).setScale(0, RoundingMode.HALF_UP).toInt()

    return DMS(
        latitudeDegrees, latitudeMinutes, latitudeSeconds, latitudeDirection,
        longitudeDegrees, longitudeMinutes, longitudeSeconds, longitudeDirection
    )
}

fun createDMSLatitude(dms: DMS): String{
    return "${dms.latitudeDegrees}° ${dms.latitudeMinutes}' ${dms.latitudeSeconds}\" ${dms.latitudeDirection}"
}

fun createDMSLongitude(dms: DMS): String{
    return "${dms.longitudeDegrees}° ${dms.longitudeMinutes}' ${dms.longitudeSeconds}\" ${dms.longitudeDirection}"
}

fun createDMSLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    val dms = convertToDMS(latitude, longitude)
    return "${dms.latitudeDegrees}° ${dms.latitudeMinutes}' ${dms.latitudeSeconds}\" ${dms.latitudeDirection}, " +
            "${dms.longitudeDegrees}° ${dms.longitudeMinutes}' ${dms.longitudeSeconds}\" ${dms.longitudeDirection}"
}
