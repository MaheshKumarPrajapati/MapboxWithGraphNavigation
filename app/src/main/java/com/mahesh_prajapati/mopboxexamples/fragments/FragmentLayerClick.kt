package com.mahesh_prajapati.mopboxexamples.fragments

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*
import java.net.URI


class FragmentLayerClick : Fragment() , OnMapReadyCallback, MapboxMap.OnMapClickListener{
    private val geoJsonSourceId = "geoJsonData"
    private val geoJsonLayerId = "polygonFillLayer"
    lateinit var mapboxMap: MapboxMap
    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragment_layer_click, container, false)
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
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(
            Style.MAPBOX_STREETS
        ) { style ->
            mapboxMap.addOnMapClickListener(this)
            addGeoJsonSourceToMap(style)

            // Create FillLayer with GeoJSON source and add the FillLayer to the map
            style!!.addLayer(
                FillLayer(geoJsonLayerId, geoJsonSourceId)
                    .withProperties(fillOpacity(0.5f))
            )
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        val pointf = mapboxMap.projection.toScreenLocation(point)
        val rectF = RectF(pointf.x - 10, pointf.y - 10, pointf.x + 10, pointf.y + 10)
        val featureList: List<Feature> =
            mapboxMap.queryRenderedFeatures(rectF, geoJsonLayerId)
        if (featureList.size > 0) {
            for (feature in featureList) {
               // Timber.d("Feature found with %1\$s", feature.toJson())
                Toast.makeText(
                    activity, "Layer click",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return true
        }
        return false
    }

    private fun addGeoJsonSourceToMap(loadedMapStyle: Style) {
        try {
// Add GeoJsonSource to map
            loadedMapStyle.addSource(
                GeoJsonSource(
                    geoJsonSourceId, URI(
                        "https://gist.githubusercontent"
                                + ".com/tobrun/cf0d689c8187d42ebe62757f6d0cf137/raw/4d8ac3c8333f1517df9d303"
                                + "d58f20f4a1d8841e8/regions.geojson"
                    )
                )
            )
        } catch (throwable: Throwable) {
            //Timber.e("Couldn't add GeoJsonSource to map - %s", throwable)
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