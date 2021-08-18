package com.mahesh_prajapati.mopboxexamples.utils

import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.locationtech.jts.geom.Polygon
import java.util.ArrayList

data class MergeModel(
        val source: GeoJsonSource,
        val fillLayerPointList: ArrayList<Point> =
                ArrayList<Point>(),
        var lineLayerPointList: ArrayList<Point> =
                ArrayList<Point>(),
        var circleLayerFeatureList: ArrayList<Feature> = ArrayList<Feature>(),
        var polygons: Polygon,
        val layerId: String
) {
}