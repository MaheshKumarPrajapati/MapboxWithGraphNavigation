package com.infozity.mopboxexamples.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import com.infozity.mopboxexamples.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.SupportMapFragment


class SupportMapBoxFragment: Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_support_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.bt_next).setOnClickListener {
            findNavController().navigate(R.id.action_SupportMapBoxFragment_to_SimpleMapFragment)
        }

        // Create supportMapFragment
        // Create supportMapFragment
        val mapFragment: SupportMapFragment
        if (savedInstanceState == null) {

            // Create fragment
            val transaction: FragmentTransaction = activity!!.getSupportFragmentManager().beginTransaction()

            // Build mapboxMap
            val options = MapboxMapOptions.createFromAttributes(activity!!, null)
            options.camera(CameraPosition.Builder()
                    .target(LatLng(-52.6885, -70.1395))
                    .zoom(9.0)
                    .build())

            // Create map fragment
            mapFragment = SupportMapFragment.newInstance(options)

             // Add map fragment to parent container
            transaction.add(R.id.container, mapFragment, "com.mapbox.map")
            transaction.commit()
        } else {
            mapFragment = activity!!.getSupportFragmentManager().findFragmentByTag("com.mapbox.map") as SupportMapFragment
        }

        mapFragment?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) { it->

// Map is set up and the style has loaded. Now you can add data or make other map adjustments.
            }
        }
    }
}