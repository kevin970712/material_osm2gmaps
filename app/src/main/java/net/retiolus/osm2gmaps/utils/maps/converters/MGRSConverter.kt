package net.retiolus.osm2gmaps.utils.maps.converters

import java.math.BigDecimal
import java.math.BigInteger
import java.util.Locale
import kotlin.math.*

/**
 * Converts an MGRS string like "31U DQ 52069 15404" into lat, lon, zoom.
 */
fun convertMGRSToLatLng(mgrsString: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    // 1) Clean up input
    val cleaned = mgrsString
        .replace("MGRS:", "", ignoreCase = true)
        .trim()
        .uppercase(Locale.US)
        .replace("\\s+".toRegex(), "")   // strip spaces
    require(cleaned.length >= 7) { "Invalid MGRS format" }

    // 2) Zone and latitude‐band
    val zone = cleaned.substring(0, 2).toIntOrNull()
        ?: error("Invalid zone in MGRS string")
    val band = cleaned[2]
    val northernHemisphere = when (band) {
        in 'N'..'X' -> true
        in 'C'..'M' -> false
        else -> error("Invalid latitude band '$band'")
    }

    // 3) 100 km grid letters
    val e100k = cleaned[3]
    val n100k = cleaned[4]

    // 4) Numeric precision
    val nums = cleaned.substring(5)
    require(nums.length % 2 == 0) { "Invalid numeric precision" }
    val prec = nums.length / 2
    val eDigits = nums.substring(0, prec).toDouble()
    val nDigits = nums.substring(prec).toDouble()
    val scale = 10.0.pow(5 - prec)    // to get metres within square

    // 5) Which 100 km column?
    val eCols = if (zone % 2 == 1) "ABCDEFGH" else "JKLMNPQR"  // no I/O
    val eIdx = eCols.indexOf(e100k).takeIf { it >= 0 }
        ?: error("Invalid easting letter '$e100k'")
    val baseEasting = (eIdx + 1) * 100_000.0 + eDigits * scale

    // 6) Which 100 km row?
    val nRows = "ABCDEFGHJKLMNPQRSTUV"  // no I/O
    val nIdx = nRows.indexOf(n100k).takeIf { it >= 0 }
        ?: error("Invalid northing letter '$n100k'")
    val rawNorthing = nIdx * 100_000.0 + nDigits * scale

    // 7) Find true meridional‐arc bounds for this band (in metres)
    val (bandSouthDeg, bandNorthDeg) = bandBoundsDeg(band)
    val lowerNorthing = meridionalArc(rad(bandSouthDeg))
    val upperNorthing = meridionalArc(rad(bandNorthDeg))

    // 8) Wrap rawNorthing into the correct 2 000 000 m band
    var totalNorthing = rawNorthing
    if (totalNorthing < lowerNorthing) {
        // add enough 2 000 000 m wraps to exceed the lower bound
        val wraps = ceil((lowerNorthing - totalNorthing) / 2_000_000.0).toInt()
        totalNorthing += wraps * 2_000_000.0
    }
    // (we assume it never exceeds upperNorthing by more than one wrap)

    // 9) Now hand off to your existing UTM→latlon routine
    return convertUTMCoordinates(
        zone,
        baseEasting,
        totalNorthing,
        northernHemisphere
    )
}

/** Returns (southEdgeDeg, northEdgeDeg) of the given MGRS band. */
private fun bandBoundsDeg(band: Char): Pair<Double, Double> {
    val bands = listOf(
        'C' to -80.0, 'D' to -72.0, 'E' to -64.0, 'F' to -56.0,
        'G' to -48.0, 'H' to -40.0, 'J' to -32.0, 'K' to -24.0,
        'L' to -16.0, 'M' to  -8.0, 'N' to   0.0, 'P' to   8.0,
        'Q' to  16.0, 'R' to  24.0, 'S' to  32.0, 'T' to  40.0,
        'U' to  48.0, 'V' to  56.0, 'W' to  64.0, 'X' to  72.0
    )
    val south = bands.firstOrNull { it.first == band }
        ?.second ?: error("Unknown band '$band'")
    val north = if (band == 'X') 84.0 else (
            bands.firstOrNull { it.first == band + 1 }
                ?: error("No upper edge for band '$band'")
            ).second
    return Pair(south, north)
}

/** Computes the meridional arc from equator to φ (rad) on WGS-84. */
private fun meridionalArc(latRad: Double): Double {
    val a = 6378137.0
    val e2 = 0.00669437999014
    val A0 = 1 - e2/4 - 3*e2*e2/64 - 5*e2*e2*e2/256
    val A2 = 3.0/8   * (e2 + e2*e2/4 + 15*e2*e2*e2/128)
    val A4 = 15.0/256* (e2*e2 + 3*e2*e2*e2/4)
    val A6 = 35.0/3072* e2*e2*e2
    return a * ( A0*latRad - A2*sin(2*latRad) + A4*sin(4*latRad) - A6*sin(6*latRad) )
}

private fun rad(deg: Double): Double = Math.toRadians(deg)
