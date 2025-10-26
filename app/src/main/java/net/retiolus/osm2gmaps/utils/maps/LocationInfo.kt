package net.retiolus.osm2gmaps.utils.maps

data class LocationInfo(
    val timestamp: Long = System.currentTimeMillis(),
    val source: String,
    val latitude: Number = 0.0,
    val longitude: Number = 0.0,
    val zoom: Number? = null,
    val dms: DMS? = null,
    val decimal: Decimal? = null,
    val geoUri: String? = null,
    val utm: UTM? = null,
    val links: MutableMap<String, String> = mutableMapOf(),
) {
    data class DMS(val latitude: String, val longitude: String)
    data class Decimal(val latitude: String, val longitude: String)
    data class UTM(val zone: String, val easting: Long, val northing: Long)

    fun addMapLink(type: String, link: String) {
        links[type] = link
    }

    fun deleteMapLink(type: String) {
        links.remove(type)
    }

    fun modifyMapLink(type: String, newLink: String) {
        if (links.containsKey(type)) {
            links[type] = newLink
        }
    }

    fun getLinksList(): List<Pair<String, String>> {
        return links.toList()
    }
}
