package com.seif.distancetrackerapp.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.seif.distancetrackerapp.util.Constants.ACTION_SERVICE_END
import com.seif.distancetrackerapp.util.Constants.ACTION_SERVICE_START

class TrackerService : LifecycleService() {

    companion object {
        val started = MutableLiveData<Boolean>()
    }

    private fun setInitialValue(){
        started.postValue(false)
    }

    override fun onCreate() { // will be called when service created for the first time
        setInitialValue()
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
                }
                ACTION_SERVICE_END -> {
                    started.postValue(false)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}