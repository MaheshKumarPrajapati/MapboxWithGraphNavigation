package com.mahesh_prajapati.mopboxexamples.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.fragment_simple_map.view.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SimpleMapFragment : Fragment() , OnMapReadyCallback, PermissionsListener {

    lateinit var permissionsManager: PermissionsManager
    lateinit var mapboxMap: MapboxMap
    lateinit var rootView:View
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragment_simple_map, container, false)
        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        view.mapView!!.onCreate(savedInstanceState)
        view.mapView.getMapAsync(this);
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { it->
            enableLocationComponent(it)
// Map is set up and the style has loaded. Now you can add data or make other map adjustments.
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    public override fun onResume() {
        super.onResume()
        rootView.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        rootView.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        rootView.mapView.onStop()
    }

    public override fun onPause() {
        super.onPause()
        rootView.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        rootView.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rootView.mapView.onSaveInstanceState(outState)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(activity!!, "user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted) {
            mapboxMap.getStyle(Style.OnStyleLoaded { style -> enableLocationComponent(style); })
        }else {
            Toast.makeText(activity!!, "user_location_permission_explanation", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    fun enableLocationComponent(style: Style){
        if (PermissionsManager.areLocationPermissionsGranted(activity!!)) {
            // Enable the most basic pulsing styling by ONLY using
// the `.pulseEnabled()` method

            // Enable the most basic pulsing styling by ONLY using
// the `.pulseEnabled()` method
            val customLocationComponentOptions: LocationComponentOptions =
                LocationComponentOptions.builder(activity!!)
                    // .pulseEnabled(true)
                    .build()

// Get an instance of the component

// Get an instance of the component
            val locationComponent = mapboxMap.locationComponent

// Activate with options

// Activate with options
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(activity!!, style)
                    .locationComponentOptions(customLocationComponentOptions)
                    .build()
            )

// Enable to make component visible

// Enable to make component visible
            locationComponent.isLocationComponentEnabled = true

// Set the component's camera mode

// Set the component's camera mode
            locationComponent.cameraMode = CameraMode.TRACKING

// Set the component's render mode

// Set the component's render mode
            locationComponent.renderMode = RenderMode.COMPASS
        }else {
            permissionsManager = PermissionsManager(this);
            permissionsManager.requestLocationPermissions(activity);
        }
    }
}