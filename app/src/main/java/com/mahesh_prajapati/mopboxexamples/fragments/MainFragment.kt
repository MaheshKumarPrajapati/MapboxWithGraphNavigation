package com.mahesh_prajapati.mopboxexamples.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mahesh_prajapati.mopboxexamples.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_first.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        button_supportMapBox.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SupportMapBoxFragment)
        }
        button_map_point.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MapWithPointFragment)
        }
        button_map_polyline.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MapWithPolyline)
        }
        button_map_polygons.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MapWithPolygons)
        }
        show_hide_map_layers.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_ShowAndHideLayers)
        }
        click_layers.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_FragmentLayerClick)
        }
        location_picker.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_LocationPicker)
        }
        show_multiple_geometrie.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MultipleGeometry)
        }
    }
}