package com.ellesue.airquality

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ellesue.airquality.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var binding : ActivityMapBinding

    private var mMap : GoogleMap? = null
    var currentLat : Double = 0.0
    var currentLng : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLat = intent.getDoubleExtra("currentLat", 0.0)
        currentLng = intent.getDoubleExtra("currentLon", 0.0)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setButton()

        // Log
        logCheck()

        if (mapFragment == null) {
            Log.e("MapActivity", "mapFragment is null! 지도 프래그먼트를 찾을 수 없음")
        } else {
            Log.d("MapActivity", "mapFragment 초기화 완료!")
            mapFragment.getMapAsync(this)
        }

        mMap?.setOnMapLoadedCallback {
            Log.d("MapActivity", "Google Map이 정상적으로 로드되었습니다!")
        }
        mMap?.setOnCameraIdleListener {
            Log.d("MapActivity", "카메라 위치: ${mMap?.cameraPosition?.target}")
        }

    }

    private fun logCheck() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (status != ConnectionResult.SUCCESS) {
            Log.e("MapActivity", "Google Play 서비스가 필요합니다.")
            googleApiAvailability.getErrorDialog(this, status, 9000)?.show()
        } else {
            Log.d("MapActivity", "Google Play 서비스가 정상적으로 사용 가능합니다.")
        }
    }

    private fun setButton() {
        binding.btnCheckHere.setOnClickListener{
            mMap?.let{
                val intent = Intent()
                intent.putExtra("latitude", it.cameraPosition.target.latitude)
                intent.putExtra("longitude", it.cameraPosition.target.longitude)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        binding.fabCurrentLocation.setOnClickListener {
            val locationProvider = LocationProvider(this@MapActivity)
            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()

            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude!!,longitude!!), 16f))
            setMarker()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapActivity", "Google Map is ready!") // 로그 출력

        mMap = googleMap

        mMap?.let{
            val currentLocation = LatLng(currentLat, currentLng)
            it.setMaxZoomPreference(20.0f)
            it.setMinZoomPreference(12.0f)
            it.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
            setMarker()
    }
}

    private fun setMarker() {
        mMap?.let{
            it.clear()
            val markerOption = MarkerOptions()
            markerOption.position(it.cameraPosition.target)
            markerOption.title("마커 위치")
            val marker = it.addMarker(markerOption)

            it.setOnCameraMoveListener {
                marker?.let { marker->
                    marker.position = it.cameraPosition.target
                }
            }
        }
    }

}