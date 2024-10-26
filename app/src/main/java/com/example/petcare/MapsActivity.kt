package com.example.petcare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val client = OkHttpClient()
    private val esp32Ip = "http://192.168.4.1" // Cambiado a la IP fija del AP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.map_fragment_layout)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Habilita la ubicación
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        mMap.isMyLocationEnabled = true

        // Obtener la ubicación del ESP32
        obtenerUbicacionESP32()
    }

    private fun obtenerUbicacionESP32() {
        val request = Request.Builder()
            .url(esp32Ip)
            .build()

        Thread {
            try {
                val response: Response = client.newCall(request).execute()
                val responseData = response.body?.string()
                // Parsear la respuesta
                responseData?.let {
                    val latLng = it.split(", ")
                    val latitude = latLng[0].split(": ")[1].toDouble()
                    val longitude = latLng[1].split(": ")[1].toDouble()
                    runOnUiThread {
                        val location = LatLng(latitude, longitude)
                        mMap.addMarker(MarkerOptions().position(location).title("Ubicación del ESP32"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
