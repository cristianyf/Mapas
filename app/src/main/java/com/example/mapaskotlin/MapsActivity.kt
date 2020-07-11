package com.example.mapaskotlin

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {

    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var callback: LocationCallback? = null
    private var mapa: Mapa? = null
    private var markerListener = this
    private var dragListener = this

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                if (mapa != null) {

                    mapa?.configurarMiUbicacion()

                    for (ubicacion in locationResult?.locations!!) {
                        /*Toast.makeText(applicationContext,ubicacion.latitude.toString() + " , " + ubicacion.longitude.toString(),
                            Toast.LENGTH_SHORT).show()*/
                        mapa?.miPosicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mapa?.anadirMarcadorMiPosicion()

                    }
                }

            }
        }
    }

    private fun inicializarLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mapa = Mapa(googleMap, applicationContext, markerListener, dragListener)

        mapa?.cambiarEstiloMapa()

        mapa?.marcadoresEstaticos()

        mapa?.crearListener()

        mapa?.prepararMarcadores()

        mapa?.dibujarLineas()

        mapa?.dibujarPoligono()

        mapa?.dibujarCirculo()
    }

    override fun onMarkerDragEnd(marcador: Marker?) {
        Toast.makeText(
            this,
            "Termino el de mover el marcador ",
            Toast.LENGTH_SHORT
        ).show()
        Log.d("Marcador FINAL ", marcador?.position?.latitude.toString())
    }

    override fun onMarkerDragStart(marcador: Marker?) {
        Toast.makeText(
            this,
            "Empezando a mover el marcador ",
            Toast.LENGTH_SHORT
        ).show()
        Log.d("Marcador INICIAL ", marcador?.position?.latitude.toString())
    }

    override fun onMarkerDrag(marcador: Marker?) {
        title =
            marcador?.position?.latitude.toString() + " - " + marcador?.position?.longitude.toString()
    }

    override fun onMarkerClick(marcador: Marker?): Boolean {
        var numeroClicks = marcador?.tag as? Int

        if (numeroClicks != null) {
            numeroClicks++
            marcador?.tag = numeroClicks

            Toast.makeText(
                this,
                "Se han dado " + numeroClicks.toString() + " click",
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(
            this,
            permisoFineLocation
        ) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(
            this,
            permisoCoarseLocation
        ) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun pedirPermisos() {
        val deboProveerContexto =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if (deboProveerContexto) {
            //Mandar mensaje con explicacion adicional
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    private fun solicitudPermiso() {
        requestPermissions(
            arrayOf(permisoFineLocation, permisoCoarseLocation),
            CODIGO_SOLICITUD_PERMISO
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CODIGO_SOLICITUD_PERMISO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //obtener ubicacion
                    obtenerUbicacion()
                } else {
                    Toast.makeText(
                        this,
                        "No diste permiso para acceder a la ubicacion",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun detenerActualizacion() {
        fusedLocationClient?.removeLocationUpdates(callback)
    }


    /*private fun decodePoly(encoded:String): List<GeoPoin>{
        val poly = ArrayList<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len){
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift +=5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = GeoPoint((lat.toDouble() / 1E5 * 1E6).toInt().toDouble(),
                (lng.toDouble()/ 1E5 * 1E6).toInt().toDouble())
            poly.add(p)
        }
        return poly
    }*/

    override fun onStart() {
        super.onStart()
        if (validarPermisosUbicacion()) {
            obtenerUbicacion()
        } else {
            pedirPermisos()
        }
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacion()
    }
}