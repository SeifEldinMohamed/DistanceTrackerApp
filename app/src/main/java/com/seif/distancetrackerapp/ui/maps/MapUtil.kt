package com.seif.distancetrackerapp.ui.maps

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

object MapUtil {
    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = stopTime - startTime

        val seconds = (elapsedTime/1000).toInt() % 60 // the modelus operator will return the remaining from this division so we can get elapsed time
        val minutes = (elapsedTime/1000*60).toInt() % 60
        val hours = (elapsedTime/1000*60*60).toInt() % 24

        return "$hours:$minutes:$seconds"
    }
}