package com.ellesue.airquality

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
                //위치가져옴
            }else{
                Toast.makeText(this, "권한이 거부되었습니다. 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}