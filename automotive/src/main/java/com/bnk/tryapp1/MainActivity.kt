package com.bnk.tryapp1

import android.car.Car
import android.car.VehicleAreaSeat
import android.car.hardware.CarSensorManager
import android.car.hardware.hvac.CarHvacManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var car : Car
    internal lateinit var carSpeedTextView: TextView
    internal var carSpeed=0

    //private val permissions = arrayOf(Car.PERMISSION_CONTROL_CAR_CLIMATE)
    private val permissions = arrayOf(Car.PERMISSION_SPEED)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("MainActivity", "created ")

        carSpeedTextView = findViewById<TextView>(R.id.car_speed_text_view)
        //Initialize defualt speed
        carSpeedTextView.text = getString(R.string.car_speed_text,carSpeed.toString())
        EstablishConnection()
    }

    override fun onResume() {
        super.onResume()
        //Log.d("MainActivity", "GOING TO RESUME")

        if(checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "RESUME PERMISSION IS GRANTED")
            if (!car.isConnected && !car.isConnecting) {
                car.connect()
            }
        } else {
            Log.d("MainActivity", "RESUME PERMISSION IS NOT GRANTED ?????")

            requestPermissions(permissions, 0)
        }

    }

    override fun onPause() {
        if(car.isConnected) {
           //Resume should disconnect??
            car.disconnect()
        }
        //Log.d("MainActivity", "GOING TO PAUSE")
        super.onPause()
    }


    private fun EstablishConnection() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            Log.d("MainActivity", "AUTOMOTIVE FEATURE NOT ENABLED")
            return
        }
        if(::car.isInitialized) {
            Log.d("MainActivity", "CAR ALREADY INITIALIZED")
            return
        }
        Log.d("MainActivity", "GOING TO CREATE")
        car = Car.createCar(this,object:ServiceConnection {

            override fun onServiceDisconnected(p0: ComponentName?) {
                Log.d("MainActivity", "GOING TO onServicedDisConnected")
            }

            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                Log.d("MainActivity", "RECEIVED onServiceConnected")
                onCarServiceConnected()
            }
        }
        )
    }

    private fun onCarServiceConnected() {
        Log.d("MainActivity", "GOING TO onCarServiceConnected")
        InitializeManagers()
    }

    private fun InitializeManagers() {

        //carHvacManager.registerCallback(
        //    arrayOf(arrayOf(CarHvacManager.CarHvacEventCallback))
       /// )

        val sensorManager = car.getCarManager(Car.SENSOR_SERVICE) as CarSensorManager

        sensorManager.registerListener(
            { carSensorEvent ->
                Log.d("MainActivity", "Speed: ${carSensorEvent.floatValues[0]}")
                carSpeed = carSensorEvent.floatValues[0].toInt()
                carSpeedTextView.text = getString(R.string.car_speed_text,carSpeed.toString())
            },
            CarSensorManager.SENSOR_TYPE_CAR_SPEED,
            CarSensorManager.SENSOR_RATE_NORMAL
        )

        Toast.makeText(this,"sensorManager is connected",Toast.LENGTH_LONG)
        Log.d("MainActivity", "InitializeManagers exit")
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (permissions[0] == Car.PERMISSION_SPEED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //if (permissions[0] == Car.PERMISSION_CONTROL_CAR_CLIMATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Log.d("MainActivity", "GOING TO PERMISSION")
            if (!car.isConnected && !car.isConnecting) {
                car.connect()
            }
        }
    }
}