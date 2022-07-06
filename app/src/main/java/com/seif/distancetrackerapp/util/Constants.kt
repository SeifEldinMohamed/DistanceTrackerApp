package com.seif.distancetrackerapp.util

object Constants {
    const val PERMISSION_LOCATION_REQUEST_CODE = 1
    const val PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 2

    // those constants will be used to send a command from our maps fragment to our tracker service
    const val ACTION_SERVICE_START = "ACTION_SERVICE_START"
    const val ACTION_SERVICE_END = "ACTION_SERVICE_END"
    const val ACTION_NAVIGATE_TO_MAPS_FRAGMENT= "ACTION_NAVIGATE_TO_MAPS_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
    const val NOTIFICATION_CHANNEL_NAME = "notification_channel"
    const val NOTIFICATION_ID = 1

    const val PENDING_INTENT_REQUEST_CODE = 99
}