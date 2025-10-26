import android.content.Context
import com.what3words.javawrapper.What3WordsV3
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.ConvertTo3WA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

suspend fun createWhat3WordsLink(latitude: BigDecimal, longitude: BigDecimal, context: Context): String = withContext(Dispatchers.IO) {
    val sharedPreferences = context.getSharedPreferences("osm2gmaps", Context.MODE_PRIVATE)
    val apiKey = sharedPreferences.getString("customWhat3WordsApiKey", null)
    if (apiKey != null) {
        if (apiKey.isEmpty()) {
            return@withContext "Error: API key for What3Words is not set."
        }
    }
    val w3wClient = apiKey?.let { com.what3words.androidwrapper.What3WordsV3(it, context) }

    try {
        val coordinates = Coordinates(latitude.toDouble(), longitude.toDouble())

        val result: ConvertTo3WA = w3wClient?.convertTo3wa(coordinates)!!.execute()
        if (result.isSuccessful) {
            "https://w3w.co/${result.words}"
        } else {
            "Error: ${result.error.message}"
        }
    } catch (e: Exception) {
        "Error: ${e.message}"
    }
}
