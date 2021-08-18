package com.mahesh_prajapati.mopboxexamples.fragments


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahesh_prajapati.mopboxexamples.R
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_create_polygon.view.*

import java.util.*


class PolygonCreaterFragment : Fragment() , OnMapReadyCallback{
    private val CIRCLE_SOURCE_ID = "circle-source-id"
    private val FILL_SOURCE_ID = "fill-source-id"
    private val LINE_SOURCE_ID = "line-source-id"
    private val CIRCLE_LAYER_ID = "circle-layer-id"
    private val FILL_LAYER_ID = "fill-layer-polygon-id"
    private val LINE_LAYER_ID = "line-layer-id"
    private var fillLayerPointList: ArrayList<Point> =
        ArrayList<Point>()
    private var lineLayerPointList: ArrayList<Point> =
        ArrayList<Point>()
    private var circleLayerFeatureList: ArrayList<Feature> = ArrayList<Feature>()
    private var listOfList: ArrayList<ArrayList<Point>>? = null
    private val mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private var circleSource: GeoJsonSource? = null
    private var fillSource: GeoJsonSource? = null
    private var lineSource: GeoJsonSource? = null
    private var firstPointOfPolygon: Point? = null
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView=inflater.inflate(R.layout.fragment_create_polygon, container, false)
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

        mapboxMap.setStyle(Style.MAPBOX_STREETS) { style ->
            mapboxMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .zoom(16.0)
                        .build()
                ), 4000
            )

            // Add sources to the map
            circleSource = initCircleSource(style)
            fillSource = initFillSource(style)
            lineSource = initLineSource(style)

            // Add layers to the map
            initCircleLayer(style)
            initLineLayer(style)
            initFillLayer(style)
            initFloatingActionButtonClickListeners()

        }
    }

    /**
     * Set the button click listeners
     */
    private fun initFloatingActionButtonClickListeners() {
        val clearBoundariesFab: Button = rootView!!.findViewById(R.id.clear_button)
        val undoBoundariesFab: Button = rootView!!.findViewById(R.id.undo_button)
        clearBoundariesFab.setOnClickListener(View.OnClickListener { clearEntireMap() })
        undoBoundariesFab.setOnClickListener(View.OnClickListener { undoLastEntryMap() })
        val dropPinFab: ImageView = rootView!!.findViewById(R.id.drop_pin_button)
        dropPinFab.setOnClickListener(View.OnClickListener { // Use the map click location to create a Point object
            val mapTargetPoint = Point.fromLngLat(
                mapboxMap!!.cameraPosition.target.longitude,
                mapboxMap!!.cameraPosition.target.latitude
            )

            // Make note of the first map click location so that it can be used to create a closed polygon later on
            if (circleLayerFeatureList.size == 0) {
                firstPointOfPolygon = mapTargetPoint
            }

            // Add the click point to the circle layer and update the display of the circle layer data
            circleLayerFeatureList.add(Feature.fromGeometry(mapTargetPoint))
            if (circleSource != null) {
                circleSource!!.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList))
            }

            // Add the click point to the line layer and update the display of the line layer data
            if (circleLayerFeatureList.size < 3) {
                lineLayerPointList.add(mapTargetPoint)
            } else if (circleLayerFeatureList.size == 3) {
                lineLayerPointList.add(mapTargetPoint)
                lineLayerPointList.add(firstPointOfPolygon!!)
            } else {
                lineLayerPointList.removeAt(circleLayerFeatureList.size - 1)
                lineLayerPointList.add(mapTargetPoint)
                lineLayerPointList.add(firstPointOfPolygon!!)
            }
            if (lineSource != null) {
                lineSource!!.setGeoJson(
                    FeatureCollection.fromFeatures(
                        arrayOf(
                            Feature.fromGeometry(
                                LineString.fromLngLats(lineLayerPointList)
                            )
                        )
                    )
                )
            }

            // Add the click point to the fill layer and update the display of the fill layer data
            if (circleLayerFeatureList.size < 3) {
                fillLayerPointList.add(mapTargetPoint)
            } else if (circleLayerFeatureList.size == 3) {
                fillLayerPointList.add(mapTargetPoint)
                fillLayerPointList.add(firstPointOfPolygon!!)
            } else {
                fillLayerPointList.removeAt(fillLayerPointList.size - 1)
                fillLayerPointList.add(mapTargetPoint)
                fillLayerPointList.add(firstPointOfPolygon!!)
            }
            listOfList = ArrayList()
            listOfList!!.add(fillLayerPointList)
            val finalFeatureList: ArrayList<Feature> = ArrayList()
            finalFeatureList.add(Feature.fromGeometry(Polygon.fromLngLats(listOfList!! as List<MutableList<Point>>)))
            val newFeatureCollection: FeatureCollection =
                FeatureCollection.fromFeatures(finalFeatureList)
            if (fillSource != null) {
                fillSource!!.setGeoJson(newFeatureCollection)
            }
        })
    }

    private fun undoLastEntryMap() {
        if(circleLayerFeatureList.size<=1 || lineLayerPointList.size<=1 ||
            fillLayerPointList.size<=1) {
            firstPointOfPolygon= null
            clearEntireMap()
        }else {
            circleLayerFeatureList.removeAt(circleLayerFeatureList.size-1)
            if (circleSource != null) {
                circleSource!!.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList))
            }


            if (circleLayerFeatureList.size < 3) {
                lineLayerPointList.removeAt(lineLayerPointList.size -1)
            } else if (circleLayerFeatureList.size == 3) {
                lineLayerPointList.removeAt(lineLayerPointList.size -2)
            } else {
                lineLayerPointList.removeAt(lineLayerPointList.size -2)
            }
            if (lineSource != null) {
                lineSource!!.setGeoJson(
                    FeatureCollection.fromFeatures(
                        arrayOf(
                            Feature.fromGeometry(
                                LineString.fromLngLats(lineLayerPointList)
                            )
                        )
                    )
                )
            }

            // Add the click point to the fill layer and update the display of the fill layer data
            fillLayerPointList.clear()
            if(listOfList!=null){
                listOfList!!.clear()
            }

            if (fillSource != null) {
                fillSource!!.setGeoJson(FeatureCollection.fromFeatures(arrayOf<Feature>()))
            }

           fillLayerPointList.addAll(lineLayerPointList)
            listOfList = ArrayList()
            listOfList!!.add(fillLayerPointList)
            val finalFeatureList: ArrayList<Feature> = ArrayList()
            finalFeatureList.add(Feature.fromGeometry(Polygon.fromLngLats(listOfList!! as List<MutableList<Point>>)))
            val newFeatureCollection: FeatureCollection =
                FeatureCollection.fromFeatures(finalFeatureList)
            if (fillSource != null) {
                fillSource!!.setGeoJson(newFeatureCollection)
            }
        }
    }

    /**
     * Remove the drawn area from the map by resetting the FeatureCollections used by the layers' sources
     */

    private fun clearEntireMap() {
        fillLayerPointList = ArrayList()
        circleLayerFeatureList = ArrayList()
        lineLayerPointList = ArrayList()
        if (circleSource != null) {
            circleSource!!.setGeoJson(FeatureCollection.fromFeatures(arrayOf<Feature>()))
        }
        if (lineSource != null) {
            lineSource!!.setGeoJson(FeatureCollection.fromFeatures(arrayOf<Feature>()))
        }
        if (fillSource != null) {
            fillSource!!.setGeoJson(FeatureCollection.fromFeatures(arrayOf<Feature>()))
        }
    }

    /**
     * Set up the CircleLayer source for showing map click points
     */
    private fun initCircleSource(loadedMapStyle: Style): GeoJsonSource? {
        val circleFeatureCollection =
            FeatureCollection.fromFeatures(arrayOf())
        val circleGeoJsonSource =
            GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection)
        loadedMapStyle.addSource(circleGeoJsonSource)
        return circleGeoJsonSource
    }

    /**
     * Set up the CircleLayer for showing polygon click points
     */
    private fun initCircleLayer(loadedMapStyle: Style) {
        val circleLayer = CircleLayer(
            CIRCLE_LAYER_ID,
            CIRCLE_SOURCE_ID
        )
        circleLayer.setProperties(
            circleRadius(7f),
            circleColor(Color.RED)
        )
        loadedMapStyle.addLayer(circleLayer)
    }

    /**
     * Set up the FillLayer source for showing map click points
     */
    private fun initFillSource(loadedMapStyle: Style): GeoJsonSource? {
        val fillFeatureCollection =
            FeatureCollection.fromFeatures(arrayOf())
        val fillGeoJsonSource = GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection)
        loadedMapStyle.addSource(fillGeoJsonSource)
        return fillGeoJsonSource
    }

    /**
     * Set up the FillLayer for showing the set boundaries' polygons
     */
    private fun initFillLayer(loadedMapStyle: Style) {
        val fillLayer = FillLayer(
            FILL_LAYER_ID,
            FILL_SOURCE_ID
        )
        fillLayer.setProperties(
            fillOpacity(.6f),
            fillColor(Color.parseColor("#00e9ff"))
        )
        loadedMapStyle.addLayerBelow(fillLayer, LINE_LAYER_ID)
    }

    /**
     * Set up the LineLayer source for showing map click points
     */
    private fun initLineSource(loadedMapStyle: Style): GeoJsonSource? {
        val lineFeatureCollection =
            FeatureCollection.fromFeatures(arrayOf())
        val lineGeoJsonSource = GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection)
        loadedMapStyle.addSource(lineGeoJsonSource)
        return lineGeoJsonSource
    }

    /**
     * Set up the LineLayer for showing the set boundaries' polygons
     */
    private fun initLineLayer(loadedMapStyle: Style) {
        val lineLayer = LineLayer(
            LINE_LAYER_ID,
            LINE_SOURCE_ID
        )
        lineLayer.setProperties(
            lineColor(Color.BLUE),
            lineWidth(5f)
        )
        loadedMapStyle.addLayerBelow(lineLayer, CIRCLE_LAYER_ID)
    }


    override fun onResume() {
        super.onResume()
        rootView!!.mapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        rootView!!.mapView!!.onStart()
    }

     override fun onStop() {
        super.onStop()
         rootView!!.mapView!!.onStop()
    }

    override fun onPause() {
        super.onPause()
        rootView!!.mapView!!.onPause()
    }



  override fun onDestroy() {
        super.onDestroy()
      rootView!!.mapView!!.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        rootView!!.mapView!!.onLowMemory()
    }

}