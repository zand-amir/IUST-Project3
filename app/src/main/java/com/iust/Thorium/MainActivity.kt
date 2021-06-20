package com.iust.Thorium

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.*
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.iust.Thorium.data.AppDatabase
import com.iust.Thorium.data.model.CellPower
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var permit_s =1
    var repeat : Int = 0
    lateinit var tm : TelephonyManager
    private var db: AppDatabase? = null
    var handler: Handler = Handler()
    val delayer = 10000
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var latitude : Double = 35.0
    var longitude : Double = 40.0
    var record_size :Int =0

    var latency : Long =0
    var content_latency :Long =0
    var jitter  =0
    var jitter_counter= 0
    var downSpeed: Int =0
    var upSpeed: Int =0
    lateinit var cm: ConnectivityManager

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
//            Toast.makeText(this, "infos ...", Toast.LENGTH_SHORT).show()
            if (repeat==0){
                getLastLocation()
                repeat=1
                text2.text = "Grabing Data ..."
            }else{
                repeat=0
                text2.text = "Process paused."
            }

        }
        Mapbutton.setOnClickListener{
            var intent = Intent(this,map_act::class.java)
            startActivity(intent)
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (repeat == 1){
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
        mLocationRequest.interval = 9000
        mLocationRequest.fastestInterval = 5000
//        mLocationRequest.numUpdates = 1

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
        speedmeter()
        latency = latencycal("8.8.8.8")
        content_latency = contlatencycal("cdn.filimo.com")
        Log.d("content ",content_latency.toString())
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissiongranter()
        }
        var ci1 = tm.allCellInfo
        Log.d("MyActivity", ci1.toString())
        val out = getCellInfo(ci1.get(0))
        Log.d("MyActivity",out.toString())
        var my_info : CellPower
        if (out["type"]=="2"){
            my_info = CellPower(type=2,latitude = latitude,longitude = longitude,Level_of_strength = out["Level_of_strength"],MCC = out["MCC"],MNC = out["MNC"],plmn = out["plmn"],cell_identity = out["cell_identity"],net_type = out["net_type"],LAC = out["LAC"],RSSI = out["RSSI"] ,RxLev = out["RxLev"] ,downspeed = downSpeed , upspeed = upSpeed ,latency = latency.toString() ,jitter = jitter.toString() ,content_latency = content_latency.toString())
        }else if (out["type"]=="3"){
            my_info = CellPower(type =3 , latitude = latitude,longitude = longitude,Level_of_strength = out["Level_of_strength"],MCC = out["MCC"],MNC = out["MNC"],plmn = out["plmn"],cell_identity = out["cell_identity"],net_type = out["net_type"],LAC = out["LAC"],RSCP = out["RSCP"] ,downspeed = downSpeed , upspeed = upSpeed ,latency = latency.toString() ,jitter = jitter.toString() ,content_latency = content_latency.toString())
        }else{
            my_info = CellPower(type = 4,latitude = latitude,longitude = longitude,Level_of_strength = out["Level_of_strength"],MCC = out["MCC"],MNC = out["MNC"],plmn = out["plmn"],cell_identity = out["cell_identity"],net_type = out["net_type"],TAC = out["TAC"],RSRP = out["RSRP"],RSRQ = out["RSRQ"],CINR = out["CINR"] ,downspeed = downSpeed , upspeed = upSpeed ,latency = latency.toString() ,jitter = jitter.toString() ,content_latency = content_latency.toString())
        }
//        val INFOtext : TextView = findViewById(R.id.info_text)
        val records : TextView = findViewById(R.id.record_size_text)
        db?.cellPowerDao()?.insert(my_info)
        var my_info2 = db?.cellPowerDao()?.getAll()
        record_size = my_info2!!.size
        records.text = "Number of records : " + record_size.toString()
//        INFOtext.text = my_info2.toString() + "\n"
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
//            TelephonyManager.NETWORK_TYPE_NR -> return "5G"
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
    private fun speedmeter() {
        var netInfo: NetworkInfo = cm.getActiveNetworkInfo()
        if (netInfo.isConnected){
            val nc: NetworkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            downSpeed = nc.getLinkDownstreamBandwidthKbps()
            upSpeed= nc.getLinkUpstreamBandwidthKbps()
        }
    }
    private fun latencycal(addr: String) :Long {
        val runtime = Runtime.getRuntime()
        var pingg : Long =999999999
        try {
            var a : Long = (System.currentTimeMillis() %100000)
            val IpProcess = runtime.exec("/system/bin/ping -c 1 "+addr)
            val mExitValue  = IpProcess.waitFor(2,TimeUnit.SECONDS)
            if (mExitValue ){
                var b : Long = (System.currentTimeMillis() %100000)
                if(b<=a){
                    pingg = (100000 - a) + b
                }else{
                    pingg = b - a
                }
            }else{
                pingg = 999999999
            }
            if (jitter_counter != 0 && pingg != 999999999.toLong() && latency != 999999999.toLong()){
                var pingg2 = pingg.toInt()
                var pingg3 = kotlin.math.abs((pingg2 - latency).toInt())
                jitter = (((jitter_counter-1)*jitter)+(pingg3)) / jitter_counter
                jitter_counter++
            }else if (jitter_counter == 0){
                jitter_counter++
            }
//            latency = pingg
            Log.d("jitter :",jitter.toString())
            Log.d("ping :",pingg.toString())
        } catch (ignore: InterruptedException) {
            ignore.printStackTrace()
            Log.d(""," Exception:$ignore")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("", " Exception:$e")
        }
        return pingg
    }
    private fun contlatencycal(addr: String) :Long {
        val runtime = Runtime.getRuntime()
        var pingg : Long =999999999
        try {
            var a : Long = (System.currentTimeMillis() %100000)
            val IpProcess = runtime.exec("/system/bin/ping -c 1 "+addr)
            val mExitValue  = IpProcess.waitFor(2,TimeUnit.SECONDS)
            if (mExitValue ){
                var b : Long = (System.currentTimeMillis() %100000)
                if(b<=a){
                    pingg = (100000 - a) + b
                }else{
                    pingg = b - a
                }
            }else{
                pingg = 999999999
            }
        } catch (ignore: InterruptedException) {
            ignore.printStackTrace()
            Log.d(""," Exception:$ignore")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("", " Exception:$e")
        }
        return pingg
    }
}