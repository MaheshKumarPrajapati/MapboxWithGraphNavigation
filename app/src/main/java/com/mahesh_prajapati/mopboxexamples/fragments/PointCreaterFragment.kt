package com.mahesh_prajapati.mopboxexamples.fragments


import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.utils.BitmapUtils


open class PointCreaterFragment : Fragment(),OnMapReadyCallback  {

    private var DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var selectLocationButton: Button? = null
    private var permissionsManager: PermissionsManager? = null
    private var hoveringMarker: ImageView? = null
    private var droppedMarkerLayer: Layer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key));
        var view=inflater.inflate(R.layout.fragment_point_picker, container, false)

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.

        selectLocationButton = view.findViewById(R.id.select_location_button)
// Initialize the mapboxMap view
        mapView = view.findViewById(R.id.mapView);
        mapView!!.onCreate(savedInstanceState);
        mapView!!.getMapAsync(this);
        return view
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.MAPBOX_STREETS
        ) { style ->
            //enableLocationPlugin(style)
            mapboxMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .zoom(16.0)
                        .build()
                ), 4000
            )

            // Toast instructing user to tap on the mapboxMap
            Toast.makeText(
                activity,
                getString(R.string.move_map_instruction), Toast.LENGTH_SHORT
            ).show()

            // When user is still picking a location, we hover a marker above the mapboxMap in the center.
            // This is done by using an image view with the default marker found in the SDK. You can
            // swap out for your own marker image, just make sure it matches up with the dropped marker.
            hoveringMarker = ImageView(activity)
           // hoveringMarker!!.layoutParams.width=120
            //hoveringMarker!!.layoutParams.height=120

            hoveringMarker!!.setImageResource(R.drawable.ic_crosshair)
            val params = FrameLayout.LayoutParams(
                185,
                185, Gravity.CENTER
            )
            hoveringMarker!!.layoutParams = params
            mapView!!.addView(hoveringMarker)

            // Initialize, but don't show, a SymbolLayer for the marker icon which will represent a selected location.
            initDroppedMarker(style)

            // Button for user to drop marker or to pick marker back up.
            selectLocationButton!!.setOnClickListener {    // Switch the button appearance back to select a location.
                selectLocationButton!!.setBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.purple_500
                    )
                )
                selectLocationButton!!.visibility=View.GONE
                selectLocationButton!!.text =
                    getString(R.string.location_picker_select_location_button_select)

                // Show the red hovering ImageView marker
                hoveringMarker!!.visibility = View.VISIBLE

                // Hide the selected location SymbolLayer
                droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID)
                if (droppedMarkerLayer != null) {
                    droppedMarkerLayer!!.setProperties(visibility(NONE))
                }
            }
            hoveringMarker!!.setOnClickListener {
                if (hoveringMarker!!.visibility == View.VISIBLE) {

                    // Use the map target's coordinates to make a reverse geocoding search
                    val mapTargetLatLng = mapboxMap.cameraPosition.target

                    // Hide the hovering red hovering ImageView marker
                    hoveringMarker!!.visibility = View.INVISIBLE
                    selectLocationButton!!.visibility = View.VISIBLE
                    // Transform the appearance of the button to become the cancel button
                    selectLocationButton!!.setBackgroundColor(
                        ContextCompat.getColor(activity!!, R.color.purple_700)
                    )
                    /* selectLocationButton!!.text =
                        getString(R.string.location_picker_select_location_button_cancel)*/

                    // Show the SymbolLayer icon to represent the selected map location
                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        val source: GeoJsonSource? =
                            style.getSourceAs("dropped-marker-source-id")
                        if (source != null) {
                            source.setGeoJson(
                                Point.fromLngLat(
                                    mapTargetLatLng.longitude,
                                    mapTargetLatLng.latitude
                                )
                            )
                        }
                        droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID)
                        if (droppedMarkerLayer != null) {
                            droppedMarkerLayer!!.setProperties(visibility(VISIBLE))
                        }
                    }

                }
            }}
    }
    private fun initDroppedMarker(loadedMapStyle: Style) {
        try {
            // Add the marker image to map
            loadedMapStyle.addImage(
                "dropped-icon-image", BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.ic_person_pin))!!
            )
            loadedMapStyle.addSource(GeoJsonSource("dropped-marker-source-id"))
            loadedMapStyle.addLayer(
                SymbolLayer(
                    DROPPED_MARKER_LAYER_ID,
                    "dropped-marker-source-id"
                ).withProperties(
                    iconImage("dropped-icon-image"),
                    visibility(NONE),
                    iconAllowOverlap(true),
                    iconIgnorePlacement(true)
                )
            )
        }catch(e:Exception){
        }

    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView!!.onLowMemory()
    }

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        permissionsManager!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String?>?) {
        Toast.makeText(activity, R.string.user_location_permission_explanation, Toast.LENGTH_LONG)
            .show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted && mapboxMap != null) {
            val style = mapboxMap!!.style
            style?.let { enableLocationPlugin(it) }
        } else {
            Toast.makeText(
                activity!!,
                R.string.user_location_permission_not_granted,
                Toast.LENGTH_LONG
            )
                .show()
            activity!!.finish()
        }
    }


      @SuppressLint("MissingPermission")
      open fun enableLocationPlugin(loadedMapStyle: Style) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(activity!!)) {

// Get an instance of the component. Adding in LocationComponentOptions is also an optional
// parameter
            val locationComponent = mapboxMap!!.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    activity!!, loadedMapStyle
                ).build()
            )
            locationComponent.isLocationComponentEnabled = true

// Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.NORMAL
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager!!.requestLocationPermissions(activity!!)
        }
    }*/
}