package net.retiolus.osm2gmaps.utils.maps.converters

import net.retiolus.osm2gmaps.utils.maps.extractHttpsLink
import net.retiolus.osm2gmaps.utils.maps.handleNoValidLocationData
import java.math.BigDecimal
import java.math.BigInteger

fun extractBingMapsCoordinates(url: String): Triple<BigDecimal, BigDecimal, BigInteger> {
    val regex = Regex("""cp=([-+]?\d+\.\d+)~([-+]?\d+\.\d+)""")
    val matchResult = regex.find(url)

    return matchResult?.let {
        val (latitude, longitude) = it.destructured
        Triple(latitude.toBigDecimal(), longitude.toBigDecimal(), BigInteger.ZERO)
    } ?: return handleNoValidLocationData(extractHttpsLink(url))

}

fun createBingMapsLink(latitude: BigDecimal, longitude: BigDecimal, zoomLevel: BigInteger): String {
    println("https://www.bing.com/maps/?v=2&cp=$latitude~$longitude&style=r&lvl=15&rtp=pos.$latitude%5F$longitude%5F%5F%5F%5F")
    return "https://www.bing.com/maps/?v=2&cp=$latitude~$longitude&style=r&lvl=15&rtp=pos.$latitude%5F$longitude%5F%5F%5F%5F"
}

