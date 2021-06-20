package com.iust.thorium

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.iust.thorium.data.AppDatabase
import com.iust.thorium.data.model.CellInformation
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var permit_s =1
    var repeat : Int = 0
    lateinit var tm : TelephonyManager
    private var db: AppDatabase? = null
    var handler: Handler = Handler()
    val delayer = 700
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var latitude : Double = 35.0
    var longitude : Double = 40.0
    var record_size :Int =0
    lateinit var cm: ConnectivityManager
    lateinit var spinner: Spinner
    var list_position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (isLocationEnabled()) {

            mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                var location: Location? = task.result
                requestNewLocationData()
            }
        } else {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1500
        mLocationRequest.fastestInterval = 500
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude= mLastLocation.longitude
        }
    }

    private fun getinfo(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissiongranter()
        }
        var ci1 = tm.allCellInfo
        val out = getCellInfo(ci1.get(0))
        var my_info : CellInformation
        if (out["type"]=="2"){
            my_info = CellInformation(
                latitude = latitude,
                longitude = longitude,
                cell_identity = out["cell_identity"],
                MCC = out["MCC"],
                MNC = out["MNC"],
                plmn = out["plmn"],
                net_type = out["net_type"],
                LAC = out["LAC"],
                type=2
            )
        }else if (out["type"]=="3"){
            my_info = CellInformation(
                latitude = latitude,
                longitude = longitude,
                cell_identity = out["cell_identity"],
                MCC = out["MCC"],
                MNC = out["MNC"],
                plmn = out["plmn"],
                net_type = out["net_type"],
                LAC = out["LAC"],
                type =3
            )
        }else{
            my_info = CellInformation(
                latitude = latitude,
                longitude = longitude,
                cell_identity = out["cell_identity"],
                MCC = out["MCC"],
                MNC = out["MNC"],
                plmn = out["plmn"],
                net_type = out["net_type"],
                TAC = out["TAC"],
                type = 4
            )
        }
        val records : TextView = findViewById(R.id.record_size_text)
        db?.cellPowerDao()?.insert(my_info)
        var my_info2 = db?.cellPowerDao()?.getAll()
        record_size = my_info2!!.size
        records.text = "Number of records : " + record_size.toString()
    }

    private fun permissiongranter() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    Toast.makeText(this@MainActivity, "permission granted", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), permit_s
                    )


                }

            }).check()
    }
}