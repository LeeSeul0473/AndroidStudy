package com.ellesue.airquality

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ellesue.airquality.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val PERMISSIONS_REQUESET_CODE = 100

    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAllPermissions()
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
        TODO("Not yet implemented")
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
                //위치가져옴
            }else{
                Toast.makeText(this, "권한이 거부되었습니다. 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}