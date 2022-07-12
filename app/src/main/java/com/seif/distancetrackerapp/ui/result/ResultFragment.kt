package com.seif.distancetrackerapp.ui.result

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.seif.distancetrackerapp.R
import com.seif.distancetrackerapp.databinding.FragmentResultBinding

class ResultFragment : BottomSheetDialogFragment() {

    private val args: ResultFragmentArgs by navArgs()

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtDistanceValue.text = getString(R.string.result, args.result.distance) // distance + km
        binding.txtTimeValue.text = args.result.time

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}