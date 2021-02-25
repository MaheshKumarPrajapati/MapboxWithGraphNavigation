package com.mahesh_prajapati.mopboxexamples.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import java.net.URI
import java.net.URISyntaxException
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.literal


class FragmentMultipleGeometry : Fragment() , OnMapReadyCallback{
    private val GEOJSON_SOURCE_ID = "GEOJSONFILE"
    lateinit var mapboxMap: MapboxMap
    lateinit var rootView: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragment_multiple_geometry, container, false)
        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.mapView!!.onCreate(savedInstanceState)
        view.mapView.getMapAsync(this);
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.LIGHT) { style ->
            createGeoJsonSource(style)
            addPolygonLayer(style)
            addPointsLayer(style)
        }
    }

    private fun createGeoJsonSource(loadedMapStyle: Style) {
        try {
// Load data from GeoJSON file in the assets folder
            loadedMapStyle.addSource(
                GeoJsonSource(
                    GEOJSON_SOURCE_ID,
                    URI("asset://fake_norway_campsites.geojson")
                )
            )
        } catch (exception: URISyntaxException) {

        }
    }

    private fun addPolygonLayer(loadedMapStyle: Style) {
// Create and style a FillLayer that uses the Polygon Feature's coordinates in the GeoJSON data
        val countryPolygonFillLayer = FillLayer("polygon", GEOJSON_SOURCE_ID)
        countryPolygonFillLayer.setProperties(
            PropertyFactory.fillColor(Color.RED),
            PropertyFactory.fillOpacity(.4f)
        )
        countryPolygonFillLayer.setFilter(eq(literal("\$type"), literal("Polygon")))
        loadedMapStyle.addLayer(countryPolygonFillLayer)
    }

    private fun addPointsLayer(loadedMapStyle: Style) {
// Create and style a CircleLayer that uses the Point Features' coordinates in the GeoJSON data
        val individualCirclesLayer = CircleLayer("points", GEOJSON_SOURCE_ID)
        individualCirclesLayer.setProperties(
            PropertyFactory.circleColor(Color.YELLOW),
            PropertyFactory.circleRadius(3f)
        )
        individualCirclesLayer.setFilter(eq(literal("\$type"), literal("Point")))
        loadedMapStyle.addLayer(individualCirclesLayer)
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