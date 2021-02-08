package com.infozity.mopboxexamples.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.infozity.mopboxexamples.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property.NONE
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.VectorSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*
import kotlinx.android.synthetic.main.show_hide_layers.*


/**
 * Toggle visibility of a dataset with a Button.
 */
class ShowAndHideLayers : Fragment(), OnMapReadyCallback{

    lateinit var mapboxMap: MapboxMap
    lateinit var rootView: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.show_hide_layers, container, false)
        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.mapView!!.onCreate(savedInstanceState)
        view.mapView.getMapAsync(this);
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
         mapboxMap.setStyle(Style.MAPBOX_STREETS) { it->

             it.addSource(
                     VectorSource("museums_source", "mapbox://mapbox.2opop9hr")
             )

             val museumsLayer = CircleLayer("museums", "museums_source")
             museumsLayer.setSourceLayer("museum-cusco")
             museumsLayer.setProperties(
                     visibility(VISIBLE),
                     circleRadius(8f),
                     circleColor(Color.argb(255, 55, 148, 179))
             )
             it.addLayer(museumsLayer)

             fab_layer_toggle.setOnClickListener {
                 toggleLayer()
             }
        }
    }

    private fun toggleLayer() {
        mapboxMap.getStyle { style ->
            val layer: Layer? = style.getLayer("museums")
            if (layer != null) {
                if (VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE))
                } else {
                    layer.setProperties(visibility(VISIBLE))
                }
            }
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

}