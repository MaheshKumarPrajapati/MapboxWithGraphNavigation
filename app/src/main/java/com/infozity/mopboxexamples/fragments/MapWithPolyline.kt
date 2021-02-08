package com.infozity.mopboxexamples.fragments

import android.R.style
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.infozity.mopboxexamples.R
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*


class MapWithPolyline : Fragment(), OnMapReadyCallback {
    private val mapView: MapView? = null
    private var routeCoordinates: ArrayList<Point>?= null
    lateinit var mapboxMap: MapboxMap
    lateinit var rootView: View
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragment_mapwith_polyline, container, false)
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
            initRouteCoordinates()
            // Create the LineString from the list of coordinates and then make a GeoJSON
            // FeatureCollection so we can add the line to our map as a layer
            // Create the LineString from the list of coordinates and then make a GeoJSON
            // FeatureCollection so we can add the line to our map as a layer
            it.addSource(GeoJsonSource("line-source",
                    FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates!!)
                    )))))

            // The layer properties for our line

            // The layer properties for our line
            it.addLayer(LineLayer("linelayer", "line-source").withProperties(
                    PropertyFactory.lineWidth(3f),
                    PropertyFactory.lineColor(Color.parseColor("#FF0000"))
            ))
            val loc= LatLng(
                    38.89399,
                    -77.03659
            )
            val cameraPosition=CameraPosition.Builder().target(loc).zoom(15.0)
            mapboxMap.cameraPosition = cameraPosition.build()
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
    private fun initRouteCoordinates() {
        routeCoordinates = ArrayList()
        routeCoordinates!!.add(Point.fromLngLat(-77.0381498336792, 38.893596444352134))
        routeCoordinates!!.add(Point.fromLngLat(-77.03792452812195, 38.89337933372204))
        routeCoordinates!!.add(Point.fromLngLat(-77.03761339187622, 38.89316222242831))
        routeCoordinates!!.add(Point.fromLngLat(-77.03731298446655, 38.893028615148424))
        routeCoordinates!!.add(Point.fromLngLat(-77.03691601753235, 38.892920059048464))
        routeCoordinates!!.add(Point.fromLngLat(-77.03637957572937, 38.892903358095296))
        routeCoordinates!!.add(Point.fromLngLat(-77.03592896461487, 38.89301191422077))
        routeCoordinates!!.add(Point.fromLngLat(-77.03549981117249, 38.89316222242831))
        routeCoordinates!!.add(Point.fromLngLat(-77.03514575958252, 38.89340438498248))
        routeCoordinates!!.add(Point.fromLngLat(-77.0349633693695, 38.893596444352134))
    }
}