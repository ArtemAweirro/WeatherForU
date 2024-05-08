package ru.artem_aweirro.weatherforu

import kotlinx.serialization.Serializable

@Serializable
data class WeatherModel (
    val city: String, // название города
    val time: Long, // время в UNIX UTC
    val timeSunrise: Long, // время восхода солнца в UNIX UTC
    val timeSunset: Long, // время захода солнца в UNIX UTC
    var dtTimeUTC: String, // дата и время в строке
    var partDay: String, // d - day, n - night
    val idCondition: Int, // id погоды
    val description: String, // описание погоды
    val currentTemp: Double, // текущая температура
    val feelTemp: Double, // ощущаемая температура
    val minTemp: Double,
    val maxTemp: Double,
    val rain1h: Double, // кол-во дождя в мм за час
    val rain3h: Double, // кол-во дождя в мм за три часа
    val snow1h: Double, // кол-во снега в мм за час
    val snow3h: Double, // кол-во снега в мм за три часа
    val pressure: Int, // давление
    val humidity: Int, // влажность
    val clouds: Int, // облачность в %
    val windSpeed: Double, // скорость ветра в м/c
    val windDeg: Int, // направление ветра в градусах
    val windGust: Double, // порывы ветра в м/c
    val pop: Int // вероятность выпадения осадков
)