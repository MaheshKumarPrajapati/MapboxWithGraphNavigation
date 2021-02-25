package com.mahesh_prajapati.mopboxexamples.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMapOptions
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_simple_map.view.*


class MapWithPolygons: Fragment() , OnMapReadyCallback {
    var BLUE_COLOR = Color.parseColor("#3bb2d0")
    var RED_COLOR = Color.parseColor("#AF0000")
    private var rootView: View? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key));
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_mapwith_polygons, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapboxMapOptions = MapboxMapOptions.createFromAttributes(activity!!, null)
        mapboxMapOptions
                .camera(CameraPosition.Builder()
                        .zoom(13.0)
                        .target(LatLng(25.255377, 55.3089185))
                        .build())
                .attributionTintColor(RED_COLOR)
                .compassFadesWhenFacingNorth(true)
        view.mapView!!.onCreate(savedInstanceState)
        view.mapView.getMapAsync(this);
    }

    var POLYGON_COORDINATES: ArrayList<Point?> = object : ArrayList<Point?>() {
        init {
            add(Point.fromLngLat(55.30122473231012, 25.26476622289597))
            add(Point.fromLngLat(55.29743486255916, 25.25827212207261))
            add(Point.fromLngLat(55.28978863411328, 25.251356725509737))
            add(Point.fromLngLat(55.300027931336984, 25.246425506635504))
            add(Point.fromLngLat(55.307474692951274, 25.244200378933655))
            add(Point.fromLngLat(55.31212891895635, 25.256408010450187))
            add(Point.fromLngLat(55.30774064871093, 25.26266169122738))
            add(Point.fromLngLat(55.301357710197806, 25.264946609615492))
            add(Point.fromLngLat(55.30122473231012, 25.26476622289597))
        }
    }

    var HOLE_COORDINATES: ArrayList<List<Point?>?> = object : ArrayList<List<Point?>?>() {
        init {
            add(ArrayList(object : ArrayList<Point?>() {
                init {
                    add(Point.fromLngLat(55.30084858315658, 25.256531695820797))
                    add(Point.fromLngLat(55.298280197635705, 25.252243254705405))
                    add(Point.fromLngLat(55.30163885563897, 25.250501032248863))
                    add(Point.fromLngLat(55.304059065092645, 25.254700192612702))
                    add(Point.fromLngLat(55.30084858315658, 25.256531695820797))
                }
            }))
            add(ArrayList(object : ArrayList<Point?>() {
                init {
                    add(Point.fromLngLat(55.30173763969924, 25.262517391695198))
                    add(Point.fromLngLat(55.301095543307355, 25.26122200491396))
                    add(Point.fromLngLat(55.30396028103232, 25.259479911263526))
                    add(Point.fromLngLat(55.30489872958182, 25.261132667394975))
                    add(Point.fromLngLat(55.30173763969924, 25.262517391695198))
                }
            }))
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style->
            val outerLineString = LineString.fromLngLats(POLYGON_COORDINATES)
            val innerLineString = LineString.fromLngLats(HOLE_COORDINATES[0]!!)
            val secondInnerLineString = LineString.fromLngLats(HOLE_COORDINATES[1]!!)

            val innerList: MutableList<LineString> = ArrayList()
            innerList.add(innerLineString)
            innerList.add(secondInnerLineString)

            style.addSource(GeoJsonSource("source-id",
                    Feature.fromGeometry(Polygon.fromOuterInner(outerLineString, innerList))))

            val polygonFillLayer = FillLayer("layer-id", "source-id")
                    .withProperties(fillColor(RED_COLOR))

            if (style.getLayer("road-number-shield") != null) {
                style.addLayerBelow(polygonFillLayer, "road-number-shield")
            } else {
                style.addLayer(polygonFillLayer)
            }
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