package com.iust.Thorium

//import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
//import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iust.Thorium.data.AppDatabase
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class map_act : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_map_act)
//    }
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var map: MapView? = null
    private val mLocationOverlay: MyLocationNewOverlay? = null
    private val mRotationGestureOverlay: RotationGestureOverlay? = null

    private var db: AppDatabase? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        db = AppDatabase.getAppDataBase(context = this)

        val ctx = applicationContext
        Configuration.getInstance()
            .load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map_act)
        requestPermissionsIfNecessary(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
        )
        val map = findViewById(R.id.map) as MapView
        map.canZoomIn()
        map.canZoomOut()
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        if (Build.VERSION.SDK_INT >= 16) map.setHasTransientState(true)
        val controller = map.getController()
        controller.setZoom(11.0)
//        val mLocationOverlay = MyLocationNewOverlay(map)
//        mLocationOverlay.enableFollowLocation()
//        mLocationOverlay.enableMyLocation()
//        map.getOverlays().add(mLocationOverlay)
        // mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
//        mRotationGestureOverlay.setEnabled(true);
        val startPoint = GeoPoint(35.715298, 51.404343)
        controller.setCenter(startPoint)
        // map.getOverlays().add(this.mRotationGestureOverlay);
        val mCompassOverlay =
            CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), map)
        mCompassOverlay.enableCompass()
        map.getOverlays().add(mCompassOverlay)
        var my_info2 = db?.cellPowerDao()?.getAll()
        if (my_info2 != null) {
            for (i in my_info2) {
                val cellMarker = Marker(map)

                cellMarker.position = GeoPoint(i.latitude, i.longitude)
                if(i.type==2)
                {
                    var discription = "cell identity " + i.cell_identity + "\n"+ "MCC " + i.MCC + "\n"+ "MNC " + i.MNC + "\n"+ "LAC " + i.LAC + "\n"+ "RSSI " + i.RSSI + "\n"+ "RXlex  " + i.RxLev + "\n" +"downspeed " +i.downspeed+"kbps"+"\n" +"upspeed " +i.upspeed+"kbps"+"\n"+"latency " +i.latency+"\n"+"jitter " +i.jitter+"\n" + "content_ping " +i.content_latency+"\n";
                    if(i.Level_of_strength == "1")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmveryweakicon));
                    }
                    else if(i.Level_of_strength == "2")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmokicon));
                    }
                    else if(i.Level_of_strength == "3")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmweakicon));
                    }
                    else
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmstrongicon));
                    }
//                    cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmveryweakicon));
                    cellMarker.setTitle(discription);
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)

                }
                if(i.type==4)
                {
                    var discription =  "cell identity " + i.cell_identity + "\n"+ "MCC " + i.MCC + "\n"+ "MNC " + i.MNC + "\n"+ "TAC " + i.TAC + "\n"+ "RSRP " + i.RSRP + "\n"+ "RSRQ " + i.RSRQ + "\n"+ "CINR " + i.CINR + "\n"+"downspeed " +i.downspeed+" kbps"+"\n" +"upspeed " +i.upspeed+" kbps"+"\n"+"latency " +i.latency+"\n"+"jitter " +i.jitter+"\n" + "content_ping " +i.content_latency+"\n";
                    if(i.Level_of_strength == "1")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.lteveryweakicon));
                    }
                    else if(i.Level_of_strength == "2")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.lteokicon));
                    }
                    else if(i.Level_of_strength == "3")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.lteweakicon));
                    }
                    else
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.ltestrongicon));
                    }
                        cellMarker.setTitle(discription);
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)

                }
                if(i.type==3)
                {
                    var discription ="cell identity " + i.cell_identity + "\n"+ "MCC " + i.MCC + "\n"+ "MNC " + i.MNC + "\n"+ "LAC" + i.LAC + "\n"+ "RSCP" + i.RSCP + "\n" +"downspeed " +i.downspeed+"kbps"+"\n" +"upspeed " +i.upspeed+"kbps"+"\n"+"latency " +i.latency+"\n"+"jitter " +i.jitter+"\n" + "content_ping " +i.content_latency+"\n";
                    if(i.Level_of_strength == "1")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.wcdmaveryweakicon));
                    }
                    else if(i.Level_of_strength == "2")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.wcdmaokicon));
                    }
                    else if(i.Level_of_strength == "3")
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.wcdmaweakicon));
                    }
                    else
                    {
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.wcdmastrongicon));
                    }
                    cellMarker.title = discription;
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)
                }
            }
        }


    }

//    public override fun onResume() {
//        super.onResume()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        map!!.onResume() //needed for compass, my location overlays, v6.0.0 and up
//    }

//    public override fun onPause() {
//        super.onPause()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().save(this, prefs);
//        map!!.onPause() //needed for compass, my location overlays, v6.0.0 and up
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest =
            ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest =
            ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}