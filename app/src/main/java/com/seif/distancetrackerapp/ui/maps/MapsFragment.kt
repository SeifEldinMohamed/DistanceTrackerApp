package com.seif.distancetrackerapp.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.seif.distancetrackerapp.R
import com.seif.distancetrackerapp.databinding.FragmentMapsBinding
import com.seif.distancetrackerapp.service.TrackerService
import com.seif.distancetrackerapp.util.Constants.ACTION_SERVICE_START
import com.seif.distancetrackerapp.util.ExtensionsFunctions.disable
import com.seif.distancetrackerapp.util.ExtensionsFunctions.hide
import com.seif.distancetrackerapp.util.ExtensionsFunctions.show
import com.seif.distancetrackerapp.util.Permissions.hasBackgroundLocationPermission
import com.seif.distancetrackerapp.util.Permissions.requestBackgroundLocationPermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    EasyPermissions.PermissionCallbacks {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    lateinit var map: GoogleMap

    private var locationList = mutableListOf<LatLng>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.btnStart.setOnClickListener {
            onStartButtonClicked()

        }
        binding.btnStop.setOnClickListener {

        }
        binding.btnReset.setOnClickListener {

        }
    }

    private fun onStartButtonClicked() {
        if (hasBackgroundLocationPermission(requireContext())) {
            startCountDown()
            binding.btnStart.disable()
            binding.btnStart.hide()
            binding.btnStop.show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun startCountDown() {
        binding.txtCountdown.show()
        binding.btnStop.disable()

        val timer: CountDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) { // will be called every one second
                val currentSecond = millisUntilFinished / 1000
                if (currentSecond.toString() == "0") {
                    binding.txtCountdown.text = getString(R.string.go)
                    binding.txtCountdown.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                } else {
                    binding.txtCountdown.text = currentSecond.toString()
                    binding.txtCountdown.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }
            }

            override fun onFinish() {
                sendActionCommandToService(ACTION_SERVICE_START)
                binding.txtCountdown.hide()
            }
        }
        timer.start()
    }

    private fun sendActionCommandToService(action: String) {
        val intent = Intent(
            requireContext(),
            TrackerService::class.java
        )
        intent.action = action
        requireContext().startService(intent)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.permissionPermanentlyDenied(this, perms[0])) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)

        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isTiltGesturesEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isCompassEnabled = false
        }
        observeTrackerService()
    }

    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                drawPolyline()
                followPolyline()
            }
        }
    }

    private fun drawPolyline() {
        map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.GREEN)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
    }

    private fun followPolyline() {
        map.animateCamera((
                    CameraUpdateFactory.newCameraPosition(
                        MapUtil.setCameraPosition(locationList.last()) // always it will be a new position
                    )
                    ), 1000, null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.txtHint.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500L)
            binding.txtHint.hide()
            binding.btnStart.show()
        }
        return false
    }

}

// information about background location permission:
// if the application needs to share user location continually even in the background when the app is not active
// then we must declare this permission in manifest file along with other location permission as coarse or fine location (android 10(api 29) or higher)
// on lower android api levels we didn't need to include this background permission at all bec when our app receives foreground location access like coarse or fine location then we get automatically this background permission
//
// onStartCommand(): the system invokes this method by calling startService() when another component (such as activity) requests that the service be started, i have to stop the service after it's work is completed by calling stopSelf() or stopService()
//
// onBind(): the system invokes this method by calling bindService() when another component wants to bind with a service
//
// onCreate(): the system invokes this method to perform one-time setup procedures when the service is initially created ( before it either onStartCommand() or onBind() )
//
// The system invokes  this method when the service when the service is no longer used and is being destroyed my service should implement this to clean up any resources such as threads, registered listeners or receivers