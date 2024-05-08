package ru.artem_aweirro.weatherforu

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

@Composable
fun ScreenHome(
    context: Context,
    navController: NavController,
    weatherInfo: MutableList<WeatherModel>,
) {
    // 44-46 строки по-хорошему надо выполнить всего 1 раз
    var weatherNow by remember { mutableStateOf(weatherInfo[0]) } // по-умолчанию это элемент[0]
    if (weatherNow.timeSunrise.toInt() == 0) // если запрос с Now выполнился позднее, то он будет в конце
        weatherNow = weatherInfo[40] // что собственно и проверяется
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image( // задний фон погоды
            bitmap = ImageBitmap.imageResource(
                id = getResourceIdByName(
                    changeBackgroundImage(weatherNow),
                    context
                )
            ),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.9f) }) // затемнение
        )
        Text( // название города
            text = weatherNow.city,
            fontSize = 25.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(15.dp)
        )
        Text( // текущая температура
            text = " " + weatherNow.currentTemp.toInt().toString() + "°",
            fontSize = 100.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 110.dp)
                .fillMaxWidth()
        )
        Text( // описание погоды
            text = weatherNow.description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            },
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 220.dp)
        )
        Text( // ощущается как
            text = "Ощущается как " + weatherNow.feelTemp.toInt() + "°",
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 250.dp)
        )

        Box (modifier = Modifier
            .padding(start = 15.dp, top = 290.dp, end = 15.dp)
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
        ) {
            val fontSize = 18.sp
            Image( // minTemp
                bitmap = ImageBitmap.imageResource(id = R.drawable.thermometer_cold),
                contentDescription = "min_temp",
                modifier = Modifier
                    .size(65.dp)
            )
            Text(text = weatherNow.minTemp.toInt().toString() + "°",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 23.dp, start = 60.dp)
            )

            Image( // maxTemp
                bitmap = ImageBitmap.imageResource(id = R.drawable.thermometer_hot),
                contentDescription = "max_temp",
                modifier = Modifier
                    .padding(top = 45.dp)
                    .size(65.dp)
            )
            Text(text = weatherNow.maxTemp.toInt().toString() + "°",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 68.dp, start = 60.dp)
            )

            Image( // clouds
                bitmap = ImageBitmap.imageResource(id = R.drawable.cloud),
                contentDescription = "max_temp",
                modifier = Modifier
                    .padding(top = 95.dp)
                    .size(65.dp)
            )
            Text(text = weatherNow.clouds.toString() + "%",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 113.dp, start = 60.dp)
            )

            Image( // windSpeed
                bitmap = ImageBitmap.imageResource(id = R.drawable.wind),
                contentDescription = "windSpeed",
                modifier = Modifier
                    .padding(top = 3.dp, start = 102.dp)
                    .size(60.dp)
            )
            Text(text = weatherNow.windSpeed.toInt().toString() + " м/с",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 23.dp, start = 160.dp)
            )

            Image( // windDeg
                bitmap = ImageBitmap.imageResource(id = R.drawable.wind_vector),
                contentDescription = "windDeg",
                modifier = Modifier
                    .padding(top = 63.dp, start = 117.dp)
                    .size(30.dp)
            )
            Text(text = definingWindDirection(weatherNow.windDeg),
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 68.dp, start = 160.dp)
            )

            Image( // pressure
                bitmap = ImageBitmap.imageResource(id = R.drawable.pressure),
                contentDescription = "pressure",
                modifier = Modifier
                    .padding(top = 105.dp, start = 115.dp)
                    .size(35.dp)
            )
            Text(text = weatherNow.pressure.toString() + " гПа",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 113.dp, start = 160.dp)
            )


            Image( // sunrise
                bitmap = ImageBitmap.imageResource(id = R.drawable.sunrise),
                contentDescription = "sunrise",
                modifier = Modifier
                    .padding(top = 15.dp, start = 255.dp)
                    .size(35.dp)
            )
            Text(
                text = getTimeFromUNIX(weatherNow.timeSunrise),
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 23.dp, start = 300.dp)
            )

            Image( // sunset
                bitmap = ImageBitmap.imageResource(id = R.drawable.sunset),
                contentDescription = "sunset",
                modifier = Modifier
                    .padding(top = 60.dp, start = 252.dp)
                    .size(40.dp)
            )
            Text(text = getTimeFromUNIX(weatherNow.timeSunset),
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 68.dp, start = 300.dp)
            )

            Image( // humidity
                bitmap = ImageBitmap.imageResource(id = R.drawable.humidity),
                contentDescription = "humidity",
                modifier = Modifier
                    .padding(top = 108.dp, start = 257.dp)
                    .size(30.dp)
            )
            Text(text = weatherNow.humidity.toString() + "%",
                fontSize = fontSize,
                color = Color.White,
                modifier = Modifier.padding(top = 113.dp, start = 300.dp)
            )
        }


        val stringDates = getStringsDates(weatherInfo)
        LazyColumn(
            modifier = Modifier
                .padding(top = 465.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 0..< stringDates.size) {
                item {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, bottom = 8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        ) {
                        DateItem(stringDates[i]) // надпись с датой 08-05
                        LazyRow (
                            modifier = Modifier.padding(top = 50.dp),
                        ){
                            val modelsSelectDay = getModelsSelectDay(weatherInfo, stringDates[i])
                            for (j in 0..<modelsSelectDay.size)
                                item {
                                    WeatherInfoItem(modelsSelectDay[j], context, navController) // Box со временем, иконкой и температурой
                                }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DateItem(date: String) {
    Text(text = date.substring(0, 5), // дата по середине 08-05
        color = Color.White,
        fontSize = 22.sp,
        modifier = Modifier.padding(start = 150.dp, top = 15.dp, bottom = 15.dp)
    )
}

@Composable
fun WeatherInfoItem(weatherInfo: WeatherModel, context: Context, navController: NavController) {
    Box (
        modifier = Modifier
            .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)
            .clickable {
                Log.d("MyLog", weatherInfo.toString())
                navController.navigate(
                    "screenInfoWeather" + "/"
                            + serializeWeatherModel(weatherInfo)
                )
            },
        contentAlignment = Alignment.TopCenter
    ){
        Text( // время (пример - 12:00)
            text = getTimeFromUNIX(weatherInfo.time),
            fontSize = 20.sp,
            color = Color.White)
        Image( // микро-картинка погоды
            bitmap = ImageBitmap.imageResource(
                id = getResourceIdByName(
                    changeImage(weatherInfo),
                    context
                )
            ),
            contentDescription = weatherInfo.description,
            modifier = Modifier
                .size(90.dp)
                .padding(top = 15.dp)
        )
        Text(
            modifier = Modifier
                .padding(top = 80.dp),
            text = weatherInfo.currentTemp.toInt().toString() + "°",
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 24.sp)
    }
}

@Composable
fun ScreenInfoWeather (
    weatherInfo: WeatherModel,
    context: Context,
) {
    TemperatureAndDescription(weatherInfo, context)
}

@Composable
fun TemperatureAndDescription(weatherModel: WeatherModel, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image( // задний фон погоды
            bitmap = ImageBitmap.imageResource(
                id = getResourceIdByName(
                    changeBackgroundImage(weatherModel),
                    context
                )
            ),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.9f) }) // затемнение
        )
        val title = getDateFromUNIX(weatherModel.time).substring(0,5) + " " + getTimeFromUNIX(weatherModel.time)
        Text( // дата по середине сверху
            text = title,
            fontSize = 25.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                //.align(Alignment.TopCenter)
                .padding(top = 15.dp, bottom = 15.dp, start = 40.dp, end = 40.dp),
            textAlign = TextAlign.Center
        )
        Text( // текущая температура
            text = " " + weatherModel.currentTemp.toInt().toString() + "°",
            fontSize = 100.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 110.dp)
                .fillMaxWidth()
        )
        Text( // описание погоды
            text = weatherModel.description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            },
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 220.dp)
        )
        Text( // ощущается как
            text = "Ощущается как " + weatherModel.feelTemp.toInt() + "°",
            fontSize = 25.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 250.dp)
        )
        InformationPanel(weatherModel, context)
    }
}

@Composable
fun InformationPanel(weatherModel: WeatherModel, context: Context) {
    Box (modifier = Modifier
        .padding(start = 15.dp, top = 290.dp, end = 15.dp)
        .fillMaxWidth()
        .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(20.dp))
    ) {
        val fontSize = 18.sp
        Image( // minTemp
            bitmap = ImageBitmap.imageResource(id = R.drawable.thermometer_cold),
            contentDescription = "min_temp",
            modifier = Modifier
                .size(65.dp)
        )
        Text(text = weatherModel.minTemp.toInt().toString() + "°",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 23.dp, start = 60.dp)
        )

        Image( // maxTemp
            bitmap = ImageBitmap.imageResource(id = R.drawable.thermometer_hot),
            contentDescription = "max_temp",
            modifier = Modifier
                .padding(top = 45.dp)
                .size(65.dp)
        )
        Text(text = weatherModel.maxTemp.toInt().toString() + "°",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 68.dp, start = 60.dp)
        )

        Image( // clouds
            bitmap = ImageBitmap.imageResource(id = R.drawable.cloud),
            contentDescription = "max_temp",
            modifier = Modifier
                .padding(top = 95.dp)
                .size(65.dp)
        )
        Text(text = weatherModel.clouds.toString() + "%",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 113.dp, start = 60.dp)
        )

        Image( // windSpeed
            bitmap = ImageBitmap.imageResource(id = R.drawable.wind),
            contentDescription = "windSpeed",
            modifier = Modifier
                .padding(top = 3.dp, start = 102.dp)
                .size(60.dp)
        )
        Text(text = weatherModel.windSpeed.toInt().toString() + " м/с",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 23.dp, start = 160.dp)
        )

        Image( // windDeg
            bitmap = ImageBitmap.imageResource(id = R.drawable.wind_vector),
            contentDescription = "windDeg",
            modifier = Modifier
                .padding(top = 63.dp, start = 117.dp)
                .size(30.dp)
        )
        Text(text = definingWindDirection(weatherModel.windDeg),
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 68.dp, start = 160.dp)
        )

        Image( // pressure
            bitmap = ImageBitmap.imageResource(id = R.drawable.pressure),
            contentDescription = "pressure",
            modifier = Modifier
                .padding(top = 105.dp, start = 115.dp)
                .size(35.dp)
        )
        Text(text = weatherModel.pressure.toString() + " гПа",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 113.dp, start = 160.dp)
        )


        Image( // humidity
            bitmap = ImageBitmap.imageResource(id = R.drawable.humidity),
            contentDescription = "humidity",
            modifier = Modifier
                .padding(top = 18.dp, start = 253.dp)
                .size(30.dp)
        )
        Text(text = weatherModel.humidity.toString() + "%",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 23.dp, start = 290.dp)
        )

        Image( // Вероятность выпадения осадков
            bitmap = ImageBitmap.imageResource(id = getResourceIdByName(
                changeImagePrecipitation(weatherModel),
                context
            )),
            contentDescription = "precipitation",
            modifier = Modifier
                .padding(top = 60.dp, start = 250.dp)
                .size(35.dp)
        )
        val precipitation = if (weatherModel.snow3h == 0.0) { weatherModel.rain3h }
                            else {weatherModel.snow3h}
        Text(
            text = "$precipitation мм",
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.padding(top = 68.dp, start = 290.dp)
        )
    }
}


// не успел реализовать, но в планах было
//@Composable
//fun ScreenSettings(onNavigateToScreenHome: (String) -> Unit) {
//    Text(text = "Настройки")
//}
