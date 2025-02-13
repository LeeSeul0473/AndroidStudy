package com.ellesue.airquality

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ellesue.airquality.databinding.ActivityMainBinding
import com.ellesue.airquality.retrofit.AirQualityResponse
import com.ellesue.airquality.retrofit.AirQualityService
import com.ellesue.airquality.retrofit.RetrofitConnection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var locationProvider: LocationProvider

    private val PERMISSIONS_REQUESET_CODE = 100

    var latitude : Double? = 0.0
    var longitude : Double? = 0.0

    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    val startMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult>{
            override fun onActivityResult(result: ActivityResult) {
                if(result?.resultCode?: 0 == Activity.RESULT_OK){
                    latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                    longitude = result?.data?.getDoubleExtra("longtitude", 0.0) ?: 0.0
                    updateUI()
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
        updateUI()
        setRefreshButton()
        setFab()
    }

    private fun setFab() {
        binding.fab.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("currentLat", latitude)
            intent.putExtra("currentLon", longitude)
            startMapActivityResult.launch(intent)
        }
    }

    private fun setRefreshButton() {
        binding.btnRefresh.setOnClickListener{
            updateUI()
        }
    }

    private fun updateUI() {
        locationProvider = LocationProvider(this)

        if(latitude==0.0 && longitude==0.0){
            latitude = locationProvider.getLocationLatitude()
            longitude = locationProvider.getLocationLongitude()
        }

        if(latitude != null && longitude != null){

            //Update Location
            val address = getCurrentAddress(latitude!!,longitude!!)

            address?.let {
                binding.tvLocationTitle.text = "${it.thoroughfare}"
                binding.tvLocationSubtitle.text = "${it.countryName} ${it.adminArea}"
                //Toast.makeText(this, "위치를 갱신했습니다.", Toast.LENGTH_SHORT).show()
            }

            //Update Air Quality
            getAirQualityData(latitude!!, longitude!!)

        }else{
            Toast.makeText(this, "위도, 경도 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getAirQualityData(latitude: Double, longitude: Double) {
        var retrofitAPI = RetrofitConnection.getInstance().create(
            AirQualityService::class.java
        )

        retrofitAPI.getAirQualityData(
            latitude.toString(),
            longitude.toString(),
            "c1277941-4b6f-4093-91e4-4efa728526f2"
        ).enqueue( object : Callback<AirQualityResponse> {
            override fun onResponse(
                call: Call<AirQualityResponse>,
                response: Response<AirQualityResponse>
            ) {
                if(response.isSuccessful()){
                    Toast.makeText(this@MainActivity, "최신 데이터로 업데이트 완료되었습니다.", Toast.LENGTH_LONG).show()
                    response.body()?.let{updateAirUI(it)}
                }else{
                    Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@MainActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_LONG).show()

            }
        }
        )
    }

    private fun updateAirUI(airQualityData: AirQualityResponse) {
        val pollutionData = airQualityData.data.current.pollution

        binding.tvCount.text = pollutionData.aqius.toString()
        val dataTime = ZonedDateTime.parse(pollutionData.ts).withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime()
        val dateFormatter = DateTimeFormatter.ofPattern("YYYY.MM.dd MM:mm")

        binding.tvCheckTime.text = dataTime.format(dateFormatter).toString()

        when(pollutionData.aqius){
            in 0..50->{
                binding.tvTitle.text="좋음"
                binding.imgBg.setImageResource(R.drawable.bg_good)
            }

            in 51..150->{
                binding.tvTitle.text="보통"
                binding.imgBg.setImageResource(R.drawable.bg_soso)
            }

            in 151..200->{
                binding.tvTitle.text="나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_bad)
            }

            else ->{
                binding.tvTitle.text="매우나쁨"
                binding.imgBg.setImageResource(R.drawable.bg_worst)
            }
        }
    }

    private fun getCurrentAddress(latitude : Double, longitude: Double) : Address?{
        val geoCoder = Geocoder(this, Locale.KOREA)

        val addresses : List<Address>?

        addresses = try {
            geoCoder.getFromLocation(latitude, longitude, 7)
        }catch (ioException : IOException){
            Toast.makeText(this, "지오코더 서비스를 이용불가합니다.", Toast.LENGTH_LONG).show()
            return null
        }catch (illegatArgumentException : java.lang.IllegalArgumentException){
            Toast.makeText(this, "주소가 발견되지 않았습니다.", Toast.LENGTH_LONG).show()
            return null
        }

        return addresses?.getOrNull(0)
    }


    private fun checkAllPermissions() {
        if(!isLocationServicesAvailable()){
            showDialogForLocationServiceSetting()
        }else{
            isRunTimePermissionsGranted()
        }
    }

    private fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCorseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission!=PackageManager.PERMISSION_GRANTED || hasCorseLocationPermission!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUESET_CODE)
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result->
            if(result.resultCode == Activity.RESULT_OK){
                if(isLocationServicesAvailable()){
                    isRunTimePermissionsGranted()
                }else{
                    Toast.makeText(this, "위치서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val builder : AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 비활성화 되어있습니다. 위치를 활성화해주세요.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener{dialogInterface, i ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener{dialogInterface, i ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            dialogInterface.cancel()
            Toast.makeText(this, "위치서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        })
        builder.create().show()
    }

    private fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==PERMISSIONS_REQUESET_CODE && grantResults.size==REQUIRED_PERMISSIONS.size){
            var checkResult = true

            for (result in grantResults){
                if(result!=PackageManager.PERMISSION_GRANTED){
                    checkResult = false
                    break;
                }
            }

            if(checkResult){
                updateUI()
            }else{
                Toast.makeText(this, "권한이 거부되었습니다. 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}