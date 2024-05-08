package ru.artem_aweirro.weatherforu

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject
import java.sql.Date
import java.sql.Time

fun getFullInforamion(lat: Double, lon: Double, weatherInfo: MutableList<WeatherModel>, context: Context) {
    val urlNow = "https://api.openweathermap.org/data/2.5/weather?" +
            "lat=$lat" +
            "&lon=$lon" +
            "&appid=$API_KEY" + "&lang=ru"
    val urlFiveDay = "https://api.openweathermap.org/data/2.5/forecast?" +
            "lat=$lat&" +
            "lon=$lon&" +
            "appid=$API_KEY" + "&lang=ru"

    val queue = Volley.newRequestQueue(context)
    val stringRequestNow = StringRequest(
        Request.Method.GET,
        urlNow,
        {
                response ->
            parseInformationNow(response, weatherInfo)
        },
        {
            Log.d("MyLog", "Volley error: $it")
        }
    )

    val stringRequestFiveDay = StringRequest(
        Request.Method.GET,
        urlFiveDay,
        {
                response ->
            parseinformationFiveDay(response, weatherInfo)
        },
        {
            Log.d("MyLog", "Volley error: $it")
        }
    )

    queue.add(stringRequestNow)
    queue.add(stringRequestFiveDay)
}


fun parseInformationNow(data: String, weatherInfo: MutableList<WeatherModel>) {
    val gObject = JSONObject(data)
    val item = WeatherModel(
        gObject.getString("name"),
        gObject.getLong("dt"),
        gObject.getJSONObject("sys").getLong("sunrise"),
        gObject.getJSONObject("sys").getLong("sunset"),
        "",
        "",
        gObject.getJSONArray("weather").getJSONObject(0).getInt("id"),
        gObject.getJSONArray("weather").getJSONObject(0).getString("description"),
        gObject.getJSONObject("main").getDouble("temp") - 273,
        gObject.getJSONObject("main").getDouble("feels_like") - 273,
        gObject.getJSONObject("main").getDouble("temp_min") - 273,
        gObject.getJSONObject("main").getDouble("temp_max") - 273,
        try {
            gObject.getJSONObject("rain").getDouble("1h")
        } catch (e: JSONException) {
            0.0
        },
        try {
            gObject.getJSONObject("rain").getDouble("3h")
        } catch (e: JSONException) {
            0.0
        },
        try {
            gObject.getJSONObject("snow").getDouble("1h")
        } catch (e: JSONException) {
            0.0
        },
        try {
            gObject.getJSONObject("snow").getDouble("3h")
        } catch (e: JSONException) {
            0.0
        },
        gObject.getJSONObject("main").getInt("pressure"),
        gObject.getJSONObject("main").getInt("humidity"),
        gObject.getJSONObject("clouds").getInt("all"),
        gObject.getJSONObject("wind").getDouble("speed"),
        gObject.getJSONObject("wind").getInt("deg"),
        try {
            gObject.getJSONObject("wind").getDouble("gust")
        } catch (e: JSONException) {
            -1.0
        },
        -1
    )
    definingPartDay(item)
    Log.d("MyLog", item.toString())
    weatherInfo.add(item)
}

fun parseinformationFiveDay(data: String, weatherInfo: MutableList<WeatherModel>) {
    val gObject = JSONObject(data)
    val nameCity = gObject.getJSONObject("city").getString("name") // имя города
    val gArray = gObject.getJSONArray("list") // путь для каждого дня
    for (i in 0..39 ) {
        val item = WeatherModel(
            nameCity,
            gArray.getJSONObject(i).getLong("dt"),
            0,
            0,
            gArray.getJSONObject(i).getString("dt_txt"),
            gArray.getJSONObject(i).getJSONObject("sys").getString("pod"),
            gArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getInt("id"),
            gArray.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description"),
            gArray.getJSONObject(i).getJSONObject("main").getDouble("temp") - 273,
            gArray.getJSONObject(i).getJSONObject("main").getDouble("feels_like") - 273,
            gArray.getJSONObject(i).getJSONObject("main").getDouble("temp_min") - 273,
            gArray.getJSONObject(i).getJSONObject("main").getDouble("temp_max") - 273,
            0.0, // в 3-часовом запросе нет 1 часов осадков
            try {
                gArray.getJSONObject(i).getJSONObject("rain").getDouble("3h")
            } catch (e: JSONException) {
                0.0
            },
            0.0, // в 3-часовом запросе нет 1 часа осадков
            try {
                gArray.getJSONObject(i).getJSONObject("snow").getDouble("3h")
            } catch (e: JSONException) {
                0.0
            },
            gArray.getJSONObject(i).getJSONObject("main").getInt("pressure"),
            gArray.getJSONObject(i).getJSONObject("main").getInt("humidity"),
            gArray.getJSONObject(i).getJSONObject("clouds").getInt("all"),
            gArray.getJSONObject(i).getJSONObject("wind").getDouble("speed"),
            gArray.getJSONObject(i).getJSONObject("wind").getInt("deg"),
            try {
                gArray.getJSONObject(i).getJSONObject("wind").getDouble("gust")
            } catch (e: JSONException) {
                0.0
            },
            gArray.getJSONObject(i).getInt("pop")
        )
        Log.d("MyLog", item.toString())
        weatherInfo.add(item)
    }
}

fun changeImage(weatherModel: WeatherModel): String {
    if (weatherModel.partDay == "d") {
        when (weatherModel.idCondition) {
            800 -> return "sun"
            801, 802 -> return "cloud_sun"
        }
    } else {
        when (weatherModel.idCondition) {
            800 -> return "moon"
            801, 802 -> return "cloud_moon"
        }
    }
    when (weatherModel.idCondition) {
        200, 201, 202, 230, 231, 232 -> return "cloud_lightning"
        210, 211, 212, 221 -> return "lightning"
        803, 804 -> return "cloud"
        600, 601, 602, 611 -> return "snow"
        612, 613, 615, 616, 620, 621, 622 -> return "cloud_snow"
        300, 301, 302, 310, 311, 312, 313, 314, 321 -> return "cloud_rain"
        500, 501, 502, 503, 504, 511, 520, 521, 522, 531 -> return "rain_drop"
        701, 711, 721, 731, 741, 751, 761, 762, 771, 781 -> return "fog"
    }
    return "question" // в случае, если ничего не сработает (просто перестраховка)
}

fun changeBackgroundImage(weatherModel: WeatherModel): String {
    if (weatherModel.partDay == "d") {
        when (weatherModel.idCondition) {
            800 -> return "sun_background"
            801, 802 -> return "sun_clouds_background"
            803 -> return "cloud_background"
            804 -> return "gloomy_day_background"
        }
    }
    if (weatherModel.partDay == "n") {
        when (weatherModel.idCondition) {
            800, 801, 802 -> return "night_background"
            803 -> return "cloud_night_background"
            804 -> return "gloomy_night_background"
        }
    }
    when (weatherModel.idCondition) {
        200, 201, 202, 230, 231, 232 -> return "cloud_lightning_background"
        210, 211, 212, 221 -> return "lightning_background"
        600, 601, 602, 611 -> return "snow_background"
        612, 613, 615, 616, 620, 621, 622 -> return "snow_rain_background"
        300, 301, 302, 310, 311, 312, 313, 314, 321 -> return "cloud_rain_background"
        500, 501, 502, 503, 504, 511, 520, 521, 522, 531 -> return "rain_background"
        701, 711, 721, 731, 741, 751, 761, 762, 771, 781 -> return "fog_background"
    }
    return "question" // в случае, если ничего не сработает (просто перестраховка)
}

fun changeImagePrecipitation(weatherModel: WeatherModel): String {
    if (weatherModel.snow3h != 0.0)
        return "precipitation_snow"
    return "precipitation_rain"
}

fun getDateFromUNIX (dt : Long) : String {
    // На входе 2024-05-03
    // На выходе 03-05-2024
    val dateString = Date(dt*1000).toString()
    val parts = dateString.split("-")
    return parts[2]+"-"+parts[1]+"-"+parts[0]
}

fun getTimeFromUNIX (dt : Long) : String {
    val dateString = Time(dt*1000).toString()
    val parts = dateString.split(":")
    return parts[0]+ ":" +parts[1]
}

@SuppressLint("DiscouragedApi")
fun getResourceIdByName(name: String, context: Context): Int {
    return context.resources.getIdentifier(name, "drawable", context.packageName)
} // получает ресурс картинки

fun getStringsDates(weatherModel: MutableList<WeatherModel>) : MutableList<String> { // возвращает строки с пятью датами
    val result = mutableListOf<String>() // возможно нужен remember {}
    for (i in 0..40) {
        val dateI = getDateFromUNIX(weatherModel[i].time)
        if (i == 0 || dateI != getDateFromUNIX(weatherModel[i - 1].time)) {
            result.add(dateI)
        }
    }
    return result
}

// Функция для сериализации объекта WeatherModel в JSON строку
fun serializeWeatherModel(weatherModel: WeatherModel): String {
    return Json.encodeToString(WeatherModel.serializer(), weatherModel)
}

// Функция для десериализации JSON строки в объект WeatherModel
fun deserializeWeatherModel(jsonString: String): WeatherModel {
    return Json.decodeFromString(WeatherModel.serializer(), jsonString)
}

fun definingPartDay(weatherModel: WeatherModel) {
    if (weatherModel.time >= weatherModel.timeSunrise && weatherModel.time < weatherModel.timeSunset)
        weatherModel.partDay = "d"
    else
        weatherModel.partDay = "n"
}

fun getModelsSelectDay(listModels: MutableList<WeatherModel>, dateSelect: String) : MutableList<WeatherModel> {
    val result = mutableListOf<WeatherModel>()
    for (i in 0..40) {
        if (listModels[i].timeSunrise.toInt() != 0) continue // проверка на элемент Now, а не FiveDay
        if (dateSelect == getDateFromUNIX(listModels[i].time)) result.add(listModels[i])
        if (result.size == 8) return result // иногда может ускорить процесс выборки
    }
    return result
}

fun definingWindDirection(data: Int): String {
    if (data in 0..22 || data in 338..359)
        return "З"
    if (data in 23..67)
        return "СЗ"
    if (data in 68..112)
        return "С"
    if (data in 113..157)
        return "СВ"
    if (data in 158..202)
        return "В"
    if (data in 203..247)
        return "ЮВ"
    if (data in 248..292)
        return "Ю"
    if (data in 293..337)
        return "ЮЗ"
    return "-"
}

// не успел реализовать
//fun convertingFromKelvinToDegrees(data: Double): Int {
//    return (data-273.15).toInt()
//}
//
//fun convertingFromKelvinToFahrenheit(data: Double): Int {
//    return (data*1.8-459.67).toInt()
//}
