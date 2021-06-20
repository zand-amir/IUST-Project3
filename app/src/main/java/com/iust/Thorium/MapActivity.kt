package com.iust.thorium

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.iust.thorium.data.AppDatabase
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


class MapActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private var map: MapView? = null
    private val mLocationOverlay: MyLocationNewOverlay? = null
    private val mRotationGestureOverlay: RotationGestureOverlay? = null

    private var db: AppDatabase? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        db = AppDatabase.getAppDataBase(context = this)

        val ctx = applicationContext
        Configuration.getInstance()
            .load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_map)
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
        val startPoint = GeoPoint(35.715298, 51.404343)
        controller.setCenter(startPoint)
        val mCompassOverlay =
            CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), map)
        mCompassOverlay.enableCompass()
        map.getOverlays().add(mCompassOverlay)
        var my_info2 = db?.cellPowerDao()?.getAll()
        val list_position = intent.getIntExtra("index", 0)
        if (my_info2 != null) {
            if (list_position == 0){ //for cell changes
                var cellMap = mutableMapOf<String?, Int>()
                var color_index = 0
                var use_new_color = false
                var old_color_index = 0
                var prevInfoCell = my_info2[0].cell_identity
                for (i in my_info2) {
                    val cellMarker = Marker(map)
                    cellMarker.position = GeoPoint(i.latitude, i.longitude)
                    var discription = "PLMN-ID " + i.plmn + "\n"
                            if(i.type == 2){
                        discription += "RAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    else if(i.type == 3){
                        discription += "LAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    else{
                        discription += "TAC " + i.TAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    if (cellMap.contains(i.cell_identity)) {
                        color_index = cellMap[i.cell_identity]!!
                        if (prevInfoCell != i.cell_identity) {
                            use_new_color = true
                            old_color_index = color_index
                        }
                    }
                    else {
                        if (!cellMap.isEmpty()) {
                            if (use_new_color) {
                                color_index = (old_color_index + 1) % 10
                                use_new_color = false
                            } else {
                                color_index = (color_index + 1) % 10
                            }
                        }
                        cellMap.put(i.cell_identity, color_index)
                    }
                    cellMarker.setIcon(getIconFromColorIndex(color_index));
                    cellMarker.setTitle(discription);
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)
                    prevInfoCell = i.cell_identity
                }
            }
            else if (list_position == 1) {  //for tac changes
                var tacMap = mutableMapOf<String?, Int>()
                var color_index = 0
                var use_new_color = false
                var old_color_index = 0
                var prev_info_TAC = my_info2[0].TAC
                for (i in my_info2) {
                    val cellMarker = Marker(map)
                    cellMarker.position = GeoPoint(i.latitude, i.longitude)
                    if (i.type == 4) {
                        var discription = "PLMN-ID " + i.plmn + "\n"
                        discription += "TAC " + i.TAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                        if (tacMap.contains(i.TAC)) {
                            color_index = tacMap[i.TAC]!!
                            if (prev_info_TAC != i.TAC) {
                                use_new_color = true
                                old_color_index = color_index
                            }
                        }
                        else {
                            if (!tacMap.isEmpty()) {
                                if (use_new_color) {
                                    color_index = (old_color_index + 1) % 10
                                    use_new_color = false
                                } else {
                                    color_index = (color_index + 1) % 10
                                }
                            }
                            tacMap.put(i.TAC, color_index)
                        }
                        cellMarker.setIcon(getIconFromColorIndex(color_index));
                        cellMarker.setTitle(discription);
                        cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        map.overlays.add(cellMarker)
                        prev_info_TAC = i.TAC
                    }
                }
            }
            else if (list_position == 2) { //for generation changes
                for (i in my_info2) {
                    val cellMarker = Marker(map)

                    cellMarker.position = GeoPoint(i.latitude, i.longitude)
                    var discription = "PLMN-ID " + i.plmn + "\n"
                    if (i.type == 2) {
                        discription += "RAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.gsmveryweakicon))
                    }
                    if (i.type == 3) {
                        discription += "LAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.umts_icon))
                    }
                    if (i.type == 4) {
                        discription += "TAC " + i.TAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                        cellMarker.setIcon(getResources().getDrawable(R.drawable.lte_icon))
                    }
                    cellMarker.setTitle(discription);
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)
                }
            }
            else {  //for plmn_id changes
                var plmnMap = mutableMapOf<String?, Int>()
                var color_index = 0
                var use_new_color = false
                var old_color_index = 0
                var prevInfoPlmn = my_info2[0].plmn
                for (i in my_info2) {
                    val cellMarker = Marker(map)
                    cellMarker.position = GeoPoint(i.latitude, i.longitude)
                    var discription = "PLMN-ID " + i.plmn + "\n"
                    if(i.type == 2){
                        discription += "RAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    else if(i.type == 3){
                        discription += "LAC " + i.LAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    else{
                        discription += "TAC " + i.TAC + "\n" + "Cell-ID " + i.cell_identity + "\n"
                    }
                    if (plmnMap.contains(i.plmn)) {
                        color_index = plmnMap[i.plmn]!!
                        if (prevInfoPlmn != i.plmn) {
                            use_new_color = true
                            old_color_index = color_index
                        }
                    } else {
                        if (!plmnMap.isEmpty()) {
                            if (use_new_color) {
                                color_index = (old_color_index + 1) % 10
                                use_new_color = false
                            } else {
                                color_index = (color_index + 1) % 10
                            }
                        }
                        plmnMap.put(i.plmn, color_index)
                    }
                    cellMarker.setIcon(getIconFromColorIndex(color_index));
                    cellMarker.setTitle(discription);
                    cellMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(cellMarker)
                    prevInfoPlmn = i.plmn
                }
            }
        }


    }

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

    private fun getIconFromColorIndex(colorIndex:Int): Drawable? {
        when(colorIndex){
            0 -> return getResources().getDrawable(R.drawable.blue_icon)
            1 -> return getResources().getDrawable(R.drawable.red_icon)
            2 -> return getResources().getDrawable(R.drawable.black_icon)
            3 -> return getResources().getDrawable(R.drawable.green_icon)
            4 -> return getResources().getDrawable(R.drawable.pinc_icon)
            5 -> return getResources().getDrawable(R.drawable.brown_icon)
            6 -> return getResources().getDrawable(R.drawable.light_blue_icon)
            7 -> return getResources().getDrawable(R.drawable.yellow_icon)
            8 -> return getResources().getDrawable(R.drawable.purple_icon)
            else -> return getResources().getDrawable(R.drawable.orange_icon)
        }
    }
}