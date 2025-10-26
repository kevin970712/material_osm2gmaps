package net.retiolus.osm2gmaps.utils.maps.converters

import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.* 

/**
 * Parses a UTM string in the format "UTM: <zone><hemisphere> <easting> <northing>" 
 * (for example, "UTM: 33N 500000 4649776")
 * and converts it to latitude, longitude and zoom.
 */
fun convertUTMToLatLng(utmString: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    // Remove prefix if present
    val cleaned = utmString.replace("UTM:", "", ignoreCase = true).trim()
    val parts = cleaned.split("\\s+".toRegex())
    if(parts.size < 3) {
        throw IllegalArgumentException("Invalid UTM input format")
    }
    // Extract zone and hemisphere from the first token (e.g., "33N")
    val zoneHem = parts[0].trim()
    val zone = zoneHem.dropLast(1).toInt()
    val band = zoneHem.last().uppercaseChar()
    val northernHemisphere = when (band) {
        in 'N'..'X' -> true
        in 'C'..'M' -> false
        else -> throw IllegalArgumentException("Invalid UTM latitude band: $band")
    }
    // Parse easting and northing
    val easting = parts[1].toDouble()
    val northing = parts[2].toDouble()
    return convertUTMCoordinates(zone, easting, northing, northernHemisphere)
}

/**
 * Converts UTM coordinates (zone, easting, northing, hemisphere flag)
 * to latitude and longitude using the standard formulas.
 */
fun convertUTMCoordinates(
    zone: Int,
    easting: Double,
    northing: Double,
    northernHemisphere: Boolean
): Triple<BigDecimal, BigDecimal, BigInteger> {
    // WGS84 ellipsoid parameters
    val a = 6378137.0                                    // semi-major axis
    val f = 1 / 298.257223563                           // flattening
    val k0 = 0.9996                                     // scale factor
    val e2 = f * (2 - f)                                // eccentricity squared
    val n = f / (2 - f)                                 // third flattening

    // False easting & northing
    val x = (easting - 500_000.0) / k0
    var y = northing
    if (!northernHemisphere) {
        y -= 10_000_000.0
    }
    y /= k0

    // Compute A = a / (1 + n) * (1 + n²/4 + n⁴/64)
    val A = a / (1 + n) * (1 + n.pow(2) / 4 + n.pow(4) / 64)

    // Footpoint coordinates
    val xi = y / A
    val eta = x / A

    // β series coefficients (for the inverse conversion)
    val beta1 =  0.5 * n - 2.0 / 3 * n.pow(2) + 5.0 / 16 * n.pow(3)
    val beta2 =          1.0 / 48 * n.pow(2) + 1.0 / 15 * n.pow(3)
    val beta3 =          17.0 / 480 * n.pow(3)

    // Remove the series terms
    val xiPrime = xi -
            beta1 * sin(2 * xi) * cosh(2 * eta) -
            beta2 * sin(4 * xi) * cosh(4 * eta) -
            beta3 * sin(6 * xi) * cosh(6 * eta)

    val etaPrime = eta -
            beta1 * cos(2 * xi) * sinh(2 * eta) -
            beta2 * cos(4 * xi) * sinh(4 * eta) -
            beta3 * cos(6 * xi) * sinh(6 * eta)

    // Compute footpoint latitude χ
    val chi = asin(sin(xiPrime) / cosh(etaPrime))

    // δ series coefficients
    val delta1 = 2 * n - 2.0 / 3 * n.pow(2) - 2 * n.pow(3)
    val delta2 = 7.0 / 3 * n.pow(2) - 8.0 / 5 * n.pow(3)
    val delta3 = 56.0 / 15 * n.pow(3)

    // Latitude φ
    val latRad = chi +
            delta1 * sin(2 * chi) +
            delta2 * sin(4 * chi) +
            delta3 * sin(6 * chi)

    // Longitude λ
    val lonOrigin = (zone - 1) * 6 - 180 + 3            // central meridian of zone
    val lonRad = toRadians(lonOrigin.toDouble()) +
            atan2(sinh(etaPrime), cos(xiPrime))

    val latitude  = toDegrees(latRad)
    val longitude = toDegrees(lonRad)

    // We still return a fixed zoom of 15
    return Triple(BigDecimal(latitude), BigDecimal(longitude), BigInteger.valueOf(15))
}
