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
import kotlinx.android.synthetic.main.activity_map.*
import kotlin.math.log


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var permit_s =1
    var repeat : Int = 0
    lateinit var tm : TelephonyManager
    private var db: AppDatabase? = null
    var handler: Handler = Handler()
    val delayer = 500
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var latitude : Double = 35.0
    var longitude : Double = 40.0
    var record_size :Int =0
    lateinit var cm: ConnectivityManager
    lateinit var spinner: Spinner
    var list_position: Int = 0
    var isNewLocation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissiongranter()
        tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        db = AppDatabase.getAppDataBase(context = this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val Actionbutton : Button = findViewById(R.id.info_button)
        val Mapbutton : Button = findViewById(R.id.map_btn)
        val text2 : TextView = findViewById(R.id.text2)
        Actionbutton.setOnClickListener{
            if (repeat==0){
                getLastLocation()
                repeat=1
                text2.text = "Grabing Data ..."
            }else{
                repeat=0
                text2.text = "Process paused."
            }

        }
        spinner = findViewById<View>(R.id.areaspinner) as Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.map_filters,
            android.R.layout.simple_spinner_dropdown_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = this


        Mapbutton.setOnClickListener{
            var intent = Intent(this,MapActivity::class.java)
            intent.putExtra("index", list_position)
            startActivity(intent)
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (repeat == 1 && isNewLocation){
                    getinfo()
                }
                handler.postDelayed(this, delayer.toLong())
            }
        }, delayer.toLong())

    }


    override fun onPause() {
        super.onPause()
        repeat =0
    }

    override fun onStop() {
        super.onStop()
        repeat=0
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
            var lastLoc = Location("A")
            lastLoc.latitude = latitude
            lastLoc.longitude = longitude
            val distance = mLastLocation.distanceTo(lastLoc)
            Log.i("distance", "distance is $distance")
            isNewLocation = distance > 10
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
        records.text = "Number of records : " + (record_size).toString()
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
    private fun getNetworkClass(): String {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissiongranter()
        }
        val networkType = tm.getDataNetworkType()
        when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS(2G)"
            TelephonyManager.NETWORK_TYPE_GSM -> return "GSM(2G)"
            TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE(2G)"
            TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA(2G)"
            TelephonyManager.NETWORK_TYPE_1xRTT -> return "1XRTT(2G)"
            TelephonyManager.NETWORK_TYPE_IDEN -> return "IDEN(2G)"
            TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS(3G)"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "TD_SCDMA(3G)"
            TelephonyManager.NETWORK_TYPE_EVDO_0-> return "EVDO_0(3G)"
            TelephonyManager.NETWORK_TYPE_EVDO_A-> return "EVDO_A(3G)"
            TelephonyManager.NETWORK_TYPE_HSDPA-> return "HSDPA(3G)"
            TelephonyManager.NETWORK_TYPE_HSUPA-> return "HSUPA(3G)"
            TelephonyManager.NETWORK_TYPE_HSPA-> return "HSPA(3G)"
            TelephonyManager.NETWORK_TYPE_EVDO_B-> return "EVDO_B(3G)"
            TelephonyManager.NETWORK_TYPE_EHRPD-> return "EHRPD(3G)"
            TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSPAP(3G)"
            TelephonyManager.NETWORK_TYPE_LTE -> return "LTE(4G)"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "Unknown"
            else -> return "No network"
        }
    }
    private fun getCellInfo(cellInfo: CellInfo): HashMap<Any? ,String?> {
        var netclass = getNetworkClass()
        Log.d("MyActivity",netclass)
        var map = hashMapOf<Any?, String?>()
        if (cellInfo is CellInfoGsm) {
            val cellIdentityGsm = cellInfo.cellIdentity
            val cellSignalGsm = cellInfo.cellSignalStrength
            map["cell_identity"]=cellIdentityGsm.cid.toString()
            map["MCC"]=cellIdentityGsm.mcc.toString()
            map["MNC"]=cellIdentityGsm.mnc.toString()
            map["LAC"]=cellIdentityGsm.lac.toString()
            map["RSSI"]=cellSignalGsm.dbm.toString()
            map["RxLev"]=cellSignalGsm.asuLevel.toString()
            map["Level_of_strength"]=cellSignalGsm.level.toString()
            map["type"]="2"
        } else if (cellInfo is CellInfoLte) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val cellIdentityLte = cellInfo.cellIdentity
                val cellSignalLte = cellInfo.cellSignalStrength
                map["cell_identity"] = cellIdentityLte.ci.toString()
                map["MCC"] = cellIdentityLte.mcc.toString()
                map["MNC"] = cellIdentityLte.mnc.toString()
                map["TAC"] = cellIdentityLte.tac.toString()
                map["RSRP"] = cellSignalLte.rsrp.toString()
                map["RSRQ"] = cellSignalLte.rsrq.toString()
                map["CINR"] = cellSignalLte.rssnr.toString()
                map["Level_of_strength"] = cellSignalLte.level.toString()
                map["type"] = "4"
            }
        } else if (cellInfo is CellInfoWcdma) {
            val cellIdentityWcdma = cellInfo.cellIdentity
            val cellSignalWcdma = cellInfo.cellSignalStrength
            map["cell_identity"]=cellIdentityWcdma.cid.toString()
            map["MCC"]=cellIdentityWcdma.mcc.toString()
            map["MNC"]=cellIdentityWcdma.mnc.toString()
            map["LAC"]=cellIdentityWcdma.lac.toString()
            map["RSCP"]=cellSignalWcdma.dbm.toString()
            map["Level_of_strength"]=cellSignalWcdma.level.toString()
            map["type"]="3"
        }
        map["net_type"]=netclass
        map["plmn"] = tm.getNetworkOperator().toString()
        return map
    }



    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mapButton : Button = findViewById(R.id.map_btn)
        if (position == 0){
            list_position = 0
            mapButton.setText("Map\n by \n cell\n filter")
        }
        else if (position == 1){
            list_position = 1
            mapButton.setText("Map\n by \n TAC\n filter")
        }
        else if(position == 2) {
            list_position = 2
            mapButton.setText("Map\n by \n Gen\n filter")
        }
        else{
            list_position = 3
            mapButton.setText("Map\n by \n PLMN\n filter")
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}