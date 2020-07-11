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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {

    private lateinit var mMap: GoogleMap
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100
    var fusedLocationClient: FusedLocationProviderClient? = null
    var locationRequest: LocationRequest? = null
    var callback: LocationCallback? = null

    private var listaMarcadores: ArrayList<Marker>? = null

    private var marcador1: Marker? = null
    private var marcador2: Marker? = null


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

                if (mMap != null) {
                    //Boton de ubicacion
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true

                    for (ubicacion in locationResult?.locations!!) {
                        Toast.makeText(
                            applicationContext,
                            ubicacion.latitude.toString() + " , " + ubicacion.longitude.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        val miPosicion = LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miPosicion).title("Aqui estoy"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
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
        mMap = googleMap

        cambiarEstiloMapa()

        marcadoresEstaticos()

        crearListener()

        prepararMarcadores()

        dibujarLineas()

        dibujarPoligono()

        dibujarCirculo()

    }

    private fun dibujarCirculo(){
        val coordenadas = CircleOptions()
            .center(LatLng(4.4280905852962285, -75.1748514175415))
            .radius(70.0)
        mMap.addCircle(coordenadas)
    }

    private fun dibujarPoligono() {
        val coordenadas = PolygonOptions()
            .add(LatLng(4.421335518525363, -75.18070299178362))
            .add(LatLng(4.427511285915592, -75.18064599484204))
            .add(LatLng(4.4280905852962285, -75.1748514175415))
            .add(LatLng(4.423592228456237, -75.17236769199371))

        mMap.addPolygon(coordenadas)
    }

    private fun dibujarLineas() {
        val coordenadas = PolylineOptions()
            .add(LatLng(4.421335518525363, -75.18070299178362))
            .add(LatLng(4.427511285915592, -75.18064599484204))
            .add(LatLng(4.4280905852962285, -75.1748514175415))
            .add(LatLng(4.423592228456237, -75.17236769199371))

        mMap.addPolyline(coordenadas)
    }

    private fun cambiarEstiloMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        //Personalizar mapa https://mapstyle.withgoogle.com/
        /*val exitoCambioMapa= mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapa_json))
        if(!exitoCambioMapa){
        }*/
    }

    private fun crearListener() {
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
    }

    private fun marcadoresEstaticos() {
        val punto1 = LatLng(4.422379, -75.181451)
        val punto2 = LatLng(4.420357, -75.177556)

        marcador1 = mMap.addMarker(
            MarkerOptions()
                .position(punto1)
                .snippet("Descripcion del punto")
                // .icon(BitmapDescriptorFactory.fromResource(R.drawable.avion))//Poner imagen en el icono
                .alpha(1f)
                .title("PUNTO 1")
        )
        marcador1?.tag = 0

        marcador2 = mMap.addMarker(
            MarkerOptions()
                .position(punto2)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))//Color icono
                .alpha(0.6f)//transparencia en el color del icono
                .title("PUNTO 2")
        )
        marcador2?.tag = 0
    }

    //Agregar marcadores en el mapa
    private fun prepararMarcadores() {
        listaMarcadores = ArrayList()

        mMap.setOnMapLongClickListener { location: LatLng? ->

            listaMarcadores?.add(
                mMap.addMarker(
                    MarkerOptions()
                        .position(location!!)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))//Color icono
                        .snippet("Descripcion del punto")
                        .alpha(0.6f)//transparencia en el color del icono
                        .title("PUNTO 2")
                )
            )
            //mueve el marcador
            listaMarcadores?.last()!!.isDraggable = true
        }
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
        val index = listaMarcadores?.indexOf(marcador!!)
        Log.d(
            "Marcador INICIAL ", listaMarcadores?.get(index!!)!!.position?.latitude.toString() +
                    listaMarcadores?.get(index!!)!!.position?.longitude.toString()
        )
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