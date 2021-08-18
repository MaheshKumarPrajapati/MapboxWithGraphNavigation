package com.mahesh_prajapati.mopboxexamples.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.mahesh_prajapati.mopboxexamples.R
import com.mahesh_prajapati.mopboxexamples.utils.MergeModel
import com.mapbox.geojson.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_create_polygon.view.*
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.util.*
import kotlin.collections.ArrayList


class MergePolygonFragment : Fragment(), OnMapReadyCallback {
    private val mergePolygonList: ArrayList<MergeModel> = ArrayList()
    private val CIRCLE_SOURCE_ID = "circle-source-id"
    private val FILL_SOURCE_ID = "fill-source-id"
    private val LINE_SOURCE_ID = "line-source-id"
    private val CIRCLE_LAYER_ID = "circle-layer-id"
    private val FILL_LAYER_ID = "fill-layer-polygon-id"
    private val LINE_LAYER_ID = "line-layer-id"

    private var fillLayerPointList: ArrayList<Point> = ArrayList<Point>()
    private var lineLayerPointList: ArrayList<Point> = ArrayList<Point>()
    private var circleLayerFeatureList: ArrayList<Feature> = ArrayList<Feature>()
    private var listOfList: ArrayList<ArrayList<Point>>? = null

    private var mapboxMap: MapboxMap? = null
    private var circleSource: GeoJsonSource? = null
    private var fillSource: GeoJsonSource? = null
    private var lineSource: GeoJsonSource? = null
    private var firstPointOfPolygon: Point? = null
    private var rootView: View? = null
    private var clearBoundariesFab: Button? = null
    private var undoBoundariesFab: Button? = null
    private var doneBoundariesFab: Button? = null
    private var mergeBoundariesFab: Button? = null
    private var lastItem: Point? = null
    private var lastItem2: Point? = null
    private var timeStampLast: Long? = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        Mapbox.getInstance(activity!!, getString(R.string.map_box_key))
        rootView = inflater.inflate(R.layout.fragment_merge_polygon, container, false)
        // Inflate the layout for this fragment
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearBoundariesFab = view.findViewById(R.id.clear_button)
        undoBoundariesFab = view.findViewById(R.id.undo_button)
        doneBoundariesFab = view.findViewById(R.id.done_button)
        mergeBoundariesFab = view.findViewById(R.id.merge_button)
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
            initFloatingActionButtonClickListeners(style)

        }
    }


    private fun initFloatingActionButtonClickListeners(style: Style) {
        clearBoundariesFab!!.setOnClickListener(View.OnClickListener { clearEntireMap() })
        undoBoundariesFab!!.setOnClickListener(View.OnClickListener { undoLastEntryMap() })
        val dropPinFab: ImageView = rootView!!.findViewById(R.id.drop_pin_button)
        doneBoundariesFab!!.setOnClickListener {
            saveAndDrawNewPolygon(style)
            if (mergePolygonList.size > 1) {
                doneBoundariesFab!!.visibility = View.GONE
                clearBoundariesFab!!.visibility = View.GONE
                undoBoundariesFab!!.visibility = View.GONE
                dropPinFab!!.visibility = View.GONE
                mergeBoundariesFab!!.visibility = View.VISIBLE
            } else {
                doneBoundariesFab!!.visibility = View.VISIBLE
                clearBoundariesFab!!.visibility = View.VISIBLE
                undoBoundariesFab!!.visibility = View.VISIBLE
                dropPinFab!!.visibility = View.VISIBLE
                mergeBoundariesFab!!.visibility = View.GONE
            }
        }
        mergeBoundariesFab!!.setOnClickListener { mergeAllPolygon(style) }
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

    private fun mergeAllPolygon(style: Style) {

        style.removeLayer("layer-id${timeStampLast}")
        val timeStamp = Date().time
        clearEntireMap()

        val intersection: org.locationtech.jts.geom.Geometry = mergePolygonList.get(0).polygons.intersection(mergePolygonList.get(1).polygons)
        val differ: org.locationtech.jts.geom.Geometry = mergePolygonList.get(1).polygons.difference(intersection)
        val union: org.locationtech.jts.geom.Geometry = mergePolygonList.get(0).polygons.union(differ)

        if (mergePolygonList.size > 0) {
            for (item in mergePolygonList) {
                //  style.getLayer(item.source.id)!!.setProperties(PropertyFactory.fillColor(Color.BLUE),PropertyFactory.fillOpacity(0.0f))
                style.removeLayer(item.layerId)
                circleLayerFeatureList.addAll(item.circleLayerFeatureList)
                if (lastItem != null) {
                    //  lineLayerPointList.add(lastItem!!)
                }
                lastItem = item.lineLayerPointList.get(item.lineLayerPointList.size - 1)
                lineLayerPointList.addAll(item.lineLayerPointList)
                if (lastItem2 != null) {
                    //   fillLayerPointList.add(lastItem2!!)
                }
                lastItem2 = item.fillLayerPointList.get(item.fillLayerPointList.size - 1)
                fillLayerPointList.addAll(item.fillLayerPointList)
            }
        }

        val array: ArrayList<Coordinate> = union.coordinates.toList() as ArrayList<Coordinate>
        val pointList: ArrayList<Point> = ArrayList()
        for (item in array) {
            pointList.add(Point.fromLngLat(item.y, item.x))
        }
        var list: ArrayList<List<Point>> = ArrayList<List<Point>>()
        list.add(pointList)
        style.addSource(
                GeoJsonSource(
                        "source-id${timeStamp}",
                        Feature.fromGeometry(Polygon.fromLngLats(list))
                )
        )


        val polygonFillLayer = FillLayer("layer-id${timeStamp}", "source-id${timeStamp}")
        polygonFillLayer.setProperties(PropertyFactory.fillColor(Color.BLUE), PropertyFactory.fillOpacity(0.3f))

        if (style.getLayer("road-number-shield") != null) {
            style.addLayerBelow(polygonFillLayer, "road-number-shield")
        } else {
            style.addLayer(polygonFillLayer)
        }

        timeStampLast = timeStamp
    }

    private fun saveAndDrawNewPolygon(style: Style) {
        try {
            val timeStamp = Date().time
            val outerLineString = LineString.fromLngLats(fillLayerPointList)
            var polygons = Polygon.fromOuterInner(outerLineString)
            var source = GeoJsonSource(
                    "source-id${timeStamp}",
                    Feature.fromGeometry(polygons)
            )
            // create polygons
            var polylineArray: ArrayList<Coordinate> = ArrayList()
            for (points in lineLayerPointList) {
                polylineArray.add(Coordinate(points.latitude(), points.longitude()))
            }
            val p1: org.locationtech.jts.geom.Polygon = GeometryFactory().createPolygon(
                    polylineArray.toTypedArray()
            )
            val polygonFillLayer = FillLayer("layer-id${timeStamp}", "source-id${timeStamp}")
                    .withProperties(PropertyFactory.fillColor(Color.BLUE)).withProperties(PropertyFactory.fillOpacity(0.3f))

            // style.addLayerBelow(fillLayer, "road-number-shield")
            if (style.getLayer("road-number-shield") != null) {
                style.addLayerBelow(polygonFillLayer, "road-number-shield")
            } else {
                style.addLayer(polygonFillLayer)
            }
            style.addSource(source)
            mergePolygonList.add(
                    MergeModel(
                            source,
                            fillLayerPointList,
                            lineLayerPointList,
                            circleLayerFeatureList,
                            p1, "layer-id${timeStamp}"
                    )
            )

            clearEntireMap()
        } catch (e: Exception) {
            e.message
        }

    }

    private fun undoLastEntryMap() {
        if (circleLayerFeatureList.size <= 1 || lineLayerPointList.size <= 1 ||
                fillLayerPointList.size <= 1) {
            firstPointOfPolygon = null
            clearEntireMap()
        } else {
            circleLayerFeatureList.removeAt(circleLayerFeatureList.size - 1)
            if (circleSource != null) {
                circleSource!!.setGeoJson(FeatureCollection.fromFeatures(circleLayerFeatureList))
            }


            if (circleLayerFeatureList.size < 3) {
                lineLayerPointList.removeAt(lineLayerPointList.size - 1)
            } else if (circleLayerFeatureList.size == 3) {
                lineLayerPointList.removeAt(lineLayerPointList.size - 2)
            } else {
                lineLayerPointList.removeAt(lineLayerPointList.size - 2)
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
            if (listOfList != null) {
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


    private fun initCircleSource(loadedMapStyle: Style): GeoJsonSource? {
        val circleFeatureCollection =
                FeatureCollection.fromFeatures(arrayOf())
        val circleGeoJsonSource =
                GeoJsonSource(CIRCLE_SOURCE_ID, circleFeatureCollection)
        loadedMapStyle.addSource(circleGeoJsonSource)
        return circleGeoJsonSource
    }


    private fun initCircleLayer(loadedMapStyle: Style) {
        val circleLayer = CircleLayer(
                CIRCLE_LAYER_ID,
                CIRCLE_SOURCE_ID
        )
        circleLayer.setProperties(
                PropertyFactory.circleRadius(7f),
                PropertyFactory.circleColor(Color.RED)
        )
        loadedMapStyle.addLayer(circleLayer)
    }


    private fun initFillSource(loadedMapStyle: Style): GeoJsonSource? {
        val fillFeatureCollection =
                FeatureCollection.fromFeatures(arrayOf())
        val fillGeoJsonSource = GeoJsonSource(FILL_SOURCE_ID, fillFeatureCollection)
        loadedMapStyle.addSource(fillGeoJsonSource)
        return fillGeoJsonSource
    }


    private fun initFillLayer(loadedMapStyle: Style) {
        val fillLayer = FillLayer(
                FILL_LAYER_ID,
                FILL_SOURCE_ID
        )

        fillLayer.setProperties(
                PropertyFactory.fillOpacity(.6f),
                PropertyFactory.fillColor(Color.parseColor("#00e9ff"))
        )

        loadedMapStyle.addLayerBelow(fillLayer, LINE_LAYER_ID)
    }


    private fun initLineSource(loadedMapStyle: Style): GeoJsonSource? {
        val lineFeatureCollection =
                FeatureCollection.fromFeatures(arrayOf())
        val lineGeoJsonSource = GeoJsonSource(LINE_SOURCE_ID, lineFeatureCollection)
        loadedMapStyle.addSource(lineGeoJsonSource)
        return lineGeoJsonSource
    }


    private fun initLineLayer(loadedMapStyle: Style) {
        val lineLayer = LineLayer(
                LINE_LAYER_ID,
                LINE_SOURCE_ID
        )
        lineLayer.setProperties(
                PropertyFactory.lineColor(Color.BLUE),
                PropertyFactory.lineWidth(5f)
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
