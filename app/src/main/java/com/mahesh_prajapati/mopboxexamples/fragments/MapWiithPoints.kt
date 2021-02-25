package com.mahesh_prajapati.mopboxexamples.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*


class MapWiithPoints : Fragment() , OnMapReadyCallback {


    private val SOURCE_ID = "SOURCE_ID"
    private val ICON_ID = "ICON_ID"
    private val LAYER_ID = "LAYER_ID"
    private val mapView: MapView? = null
    private var rootView: View? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragmemt_with_point, container, false)
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
        val symbolLayerIconFeatureList: MutableList<Feature> = ArrayList()
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-57.225365, -33.213144)
            )
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-54.14164, -33.981818)
            )
        )
        symbolLayerIconFeatureList.add(
            Feature.fromGeometry(
                Point.fromLngLat(-56.990533, -30.583266)
            )
        )

        mapboxMap.setStyle(
            Style.Builder()
                .fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41") // Add the SymbolLayer icon image to the map style
                .withImage(
                    ICON_ID, BitmapFactory.decodeResource(
                        activity!!.getResources(),
                        R.drawable.mapbox_marker_icon_default
                    )
                ) // Adding a GeoJson source for the SymbolLayer icons.
                .withSource(
                    GeoJsonSource(
                        SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
                    )
                ) // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
                // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
                // the coordinate point. This is offset is not always needed and is dependent on the image
                // that you use for the SymbolLayer icon.
                .withLayer(
                    SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(
                            iconImage(ICON_ID),
                            iconAllowOverlap(true),
                            iconIgnorePlacement(true)
                        )
                )
        ) {
            // Map is set up and the style has loaded. Now you can add additional data or make other map adjustments.
        }
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    public override fun onResume() {
        super.onResume()
        rootView!!.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        rootView!!.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        rootView!!.mapView.onStop()
    }

    public override fun onPause() {
        super.onPause()
        rootView!!.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        rootView!!.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView!!.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        rootView!!.mapView.onSaveInstanceState(outState)
    }

}