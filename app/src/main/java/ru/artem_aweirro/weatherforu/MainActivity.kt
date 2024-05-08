package ru.artem_aweirro.weatherforu

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import ru.artem_aweirro.weatherforu.ui.theme.WeatherForUTheme


const val API_KEY = "de97550f3d7623b2925e8c443201f9cf"

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) { // определяет интерфейс при запуске проекта
        super.onCreate(savedInstanceState)
        setContent { // метод, определяющий, что выводить
            val weatherInfoList = remember { mutableStateListOf<WeatherModel>() }

            var locationText by remember { mutableStateOf("No location obtained :(") }
            var coordinateLocation by remember { mutableStateOf(Pair(55.7522, 37.6156)) } // первоначально координаты Москвы
            var boolPermissionResult by remember { mutableStateOf(false) }
            var boolPermissionDenied by remember { mutableStateOf(false) }
            var boolPermissionRevoked by remember { mutableStateOf(false) }
            var permissionResultText by remember { mutableStateOf("Permission Granted...") }
            RequestLocationPermission(
                onPermissionGranted = { // все разрешения получены
                    boolPermissionResult = true
                    getLastUserLocation(
                        onGetLastLocationSuccess = {
                            coordinateLocation = it
                            //getWeatherInfo(coordinateLocation.first, coordinateLocation.second, weatherInfoList)
                            Log.d("MyLog", "LastLocate: " + it.first.toString() + " " + it.second.toString())
                        },
                        onGetLastLocationFailed = {exception ->
                            locationText = exception.localizedMessage ?: "Error Getting Last Location"
                        },
                        onGetLastLocationIsNull = {
                            getCurrentLocation( // получим текущее положение
                                onGetCurrentLocationSuccess = {
                                    coordinateLocation = it
                                    getFullInforamion(
                                        coordinateLocation.first,
                                        coordinateLocation.second,
                                        weatherInfoList,
                                        this
                                    )
                                    Log.d("MyLog", "CurrentLocate: " + it.first.toString() + " " + it.second.toString())
                                },
                                onGetCurrentLocationFailed = {
                                    locationText = it.localizedMessage?: "Error Getting Current Location"
                                    Log.d("MyLog", "Error Getting Location")
                                }
                            )
                        }
                    )
                },
                onPermissionDenied = { // при отклонении разрешений
                    permissionResultText = "Permission Denied :("
                    Log.d("MyLog", "Permission Denied")
                    boolPermissionDenied = true
                },
                onPermissionsRevoked = {
                    permissionResultText = "Permission Revoked :("
                    Log.d("MyLog", "Permission Revoked")
                    boolPermissionRevoked = true
                }
            )


            WeatherForUTheme {
                if (weatherInfoList.size == 41) { // 1 - инфо сейчас, 40 - по три часа последующие дни
                    //Log.d("MyLog", coordinateLocation.first.toString() + " " + coordinateLocation.second.toString())
                    Navigation(weatherInfoList, this)
                } else { // иначе - индикатор загрузки
                    if (boolPermissionRevoked)
                        MessagePermissionRevoked()
                    else
                        if (boolPermissionDenied)
                            MessagePermissionDenied()
                        else
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .width(64.dp)
                                    .align(Alignment.Center),
                                strokeWidth = 6.dp,
                                color = Color(66,170,255)
                            )
                    }
                }
            }
        }
    }

    /**
     * Запрашивает разрешение на местоположение и идет по сценарию
     *
     * @param onPermissionGranted Обратный вызов, который выполняется при предоставлении всех разрешений
     * @param onPermissionDenied Обратный вызов, который выполняется при отклонении любого запрошенного разрешения
     * @param onPermissionsRevoked Обратный вызов, который выполняется при отзыве всех разрешений
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestLocationPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onPermissionsRevoked: () -> Unit
    ) {
        // Инициализация состояний для управления разрешениями
        val permissionState = rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        // асинхронный запуск
        LaunchedEffect(key1 = permissionState) {
            // Проверка - отозваны ли все ранее предоставленные разрешения
            val allPermissionsRevoked =
                permissionState.permissions.size == permissionState.revokedPermissions.size

            // Фильтр разрешений, которые необходимо запросить
            val permissionsToRequest = permissionState.permissions.filter {
                !it.status.isGranted
            }

            // Если необходимо разрешение, делается запрос
            if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

            // Выполнение обратного вызова на основе статуса разрешения
            if (allPermissionsRevoked) {
                onPermissionsRevoked()
            } else {
                if (permissionState.allPermissionsGranted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }

    /**
     * Получает последнее местоположение устройства асинхронно
     *
     * @param onGetLastLocationSuccess Обратный вызов при успешном получении данных.
     * Возврат пары широта и долгота
     * @param onGetLastLocationFailed Обратный вызов при ошибке получения данных.
     * Возврат этого исключения
     */
    @SuppressLint("MissingPermission") // для подавления предупреждений об отсутсвующих разрешениях
    private fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit,
        onGetLastLocationIsNull: () -> Unit
    ) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Проверка на предоставление разрешений
        if (areLocationPermissionsGranted()) {
            // Получение последнего местоположения
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // Если location != null, происходит обратынй вызов-успех
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }?.run {
                        onGetLastLocationIsNull()
                    }
                }
                .addOnFailureListener { exception ->
                    // Если возникает ошибка, происходит обратный вызов-фэйлд
                    onGetLastLocationFailed(exception)
                }
        }
    }


    /**
     * Получает текущее местоположение пользователя асинхронно
     *
     * @param onGetCurrentLocationSuccess Обратный вызов запускается при успешном получении текущего местоположения
     * Представляет собой пару широту и долготу
     * @param onGetCurrentLocationFailed Обрытный вызов выполняется, когда возникает ошибка при получении текущего местоположения
     * Отображает возникшее исключение
     * @param priority Указывает желаемую точность поиска местоположения. По умолчанию используется высокая точность
     * Если установлено значение false, используется точность сбалансированной мощности.
     */
    @SuppressLint("MissingPermission") // для подавления предупреждений об отсутсвующих разрешениях
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        // Определение точности на основании параметра (по умолчанию - true)
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Проверка на предоставление разрешения местоположения
        if (areLocationPermissionsGranted()) {
            // Получение местоположения асинхронно
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    /**
     * Проверка на предоставление разрешений
     *
     * @return true - если ACCESS_FINE_LOCATION и ACCESS_COARSE_LOCATION разрешения предоставлены; false - иначе. */
    private fun areLocationPermissionsGranted (): Boolean {
        return (
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                )
    }

}

@Composable
fun Navigation(weatherInfo: MutableList<WeatherModel>, context: Context) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "screenHome" // начальный экран
    ) {
        composable(route = "screenHome") {
            ScreenHome(context, navController, weatherInfo)
        }
        composable(
            route = "screenInfoWeather" + "/{selectWeatherModel}",
            arguments = listOf(navArgument("selectWeatherModel") { type = NavType.StringType})
        ) {
            navBackStack ->
            val restoredWeatherModel = deserializeWeatherModel(navBackStack.arguments?.getString("selectWeatherModel") ?: "")
            ScreenInfoWeather(weatherInfo = restoredWeatherModel, context = context)
        }
        // не успел реализовать, но в планах было
        //        composable(route = "screenSettings") {
        //            ScreenSettings {
        //                navController.navigate("screenHome") {
        //                    popUpTo("screenHome"){
        //                        inclusive = true // чтобы стек очищался и оставался только screenHome
        //                    }
        //                }
        //            }
        //        }
    }
}


@Composable
fun MessagePermissionRevoked() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Местоположение не было получено :(\nРазрешите доступ к местоположению и перезапустите приложение",
            modifier = Modifier
                .padding(30.dp)
                .shadow(2.dp),
            textAlign = TextAlign.Center)
    }
}

@Composable
fun MessagePermissionDenied() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Точное местоположение\nне было получено :(\nРазрешите доступ к местоположению и перезапустите приложение",
            modifier = Modifier
                .padding(40.dp)
                .shadow(2.dp),
            textAlign = TextAlign.Center)
    }
}
