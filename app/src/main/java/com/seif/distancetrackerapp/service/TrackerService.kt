package com.seif.distancetrackerapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.seif.distancetrackerapp.ui.maps.MapUtil
import com.seif.distancetrackerapp.util.Constants.ACTION_SERVICE_END
import com.seif.distancetrackerapp.util.Constants.ACTION_SERVICE_START
import com.seif.distancetrackerapp.util.Constants.LOCATION_FASTEST_UPDATE_INTERVAL
import com.seif.distancetrackerapp.util.Constants.LOCATION_UPDATE_INTERVAL
import com.seif.distancetrackerapp.util.Constants.NOTIFICATION_CHANNEL_ID
import com.seif.distancetrackerapp.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.seif.distancetrackerapp.util.Constants.NOTIFICATION_ID
import com.seif.distancetrackerapp.util.Constants.PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val started = MutableLiveData<Boolean>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()

        val locationList = MutableLiveData<MutableList<LatLng>>()
    }

    private fun setInitialValue() {
        started.postValue(false)
        startTime.postValue(0L)
        stopTime.postValue(0L)

        locationList.postValue(mutableListOf()) // we are trying to update this locationList whenever we receive new location from onLocationResult()
    }

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(
            result: LocationResult
        ) { // will be called every view seconds when we receive new location update from our user
            super.onLocationResult(result)
            result.locations.let { locations->
                for (location in locations){ // location: mutable list
                    updateLocationList(location)
                    // when we receive a new notification from our user, we are going to update our notification with the kilometers travelled.
                    updateNotificationPeriodically()
                }
            }
        }
    }



    private fun updateLocationList(location: Location){
        val newLatLng = LatLng(location.latitude, location.longitude)
        // we will observe this location list from maps fragment so we can draw a polyline later
        locationList.value?.apply {
            add(newLatLng)
            locationList.postValue(this)
        }

    }

    private fun updateNotificationPeriodically() {
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText(locationList.value?.let { MapUtil.calculateDistance(it) } + "Km")
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    override fun onCreate() { // will be called when service created for the first time
        setInitialValue()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int { // will be triggered when we start our service
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START -> {
                    started.postValue(true)
                    startForegroundService()
                    startLocationUpdates()
                }
                ACTION_SERVICE_END -> {
                    started.postValue(false)
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(
            NOTIFICATION_ID,
            notification.build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        startTime.postValue(System.currentTimeMillis())
    }
    private fun stopForegroundService() {
        removeLocationUpdates()
        // close our notification
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        // stop our foreground
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun removeLocationUpdates() { // remove location updates from fusedLocationProviderClient
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ (Oreo) because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                importance
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}