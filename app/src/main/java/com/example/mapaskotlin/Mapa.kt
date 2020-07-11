package com.example.mapaskotlin

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import com.google.gson.Gson

class Mapa(
    mapa: GoogleMap,
    context: Context,
    var markerClickListener: GoogleMap.OnMarkerClickListener,
    var markerDragListener: GoogleMap.OnMarkerDragListener
) {

    private var mMap: GoogleMap? = null
    private var context: Context? = null
    private var listaMarcadores: ArrayList<Marker>? = null
    var miPosicion: LatLng? = null
    private var rutaMarcada: Polyline? = null
    private var marcador1: Marker? = null
    private var marcador2: Marker? = null

    init {
        this.mMap = mapa
        this.context = context
    }

    fun dibujarCirculo() {
        val coordenadas = CircleOptions()
            .center(LatLng(4.4280905852962285, -75.1748514175415))
            .radius(70.0)
            .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(10f)))
            .strokeColor(Color.WHITE)
            .fillColor(Color.YELLOW)
            .strokeWidth(15f)
        mMap?.addCircle(coordenadas)
    }

    fun dibujarPoligono() {
        val coordenadas = PolygonOptions()
            .add(LatLng(4.421335518525363, -75.18070299178362))
            .add(LatLng(4.427511285915592, -75.18064599484204))
            .add(LatLng(4.4280905852962285, -75.1748514175415))
            .add(LatLng(4.423592228456237, -75.17236769199371))
            .strokePattern(arrayListOf<PatternItem>(Dash(10f), Gap(20f)))
            .strokeColor(Color.BLUE)
            .fillColor(Color.GREEN)
            .strokeWidth(10f)

        mMap?.addPolygon(coordenadas)
    }

    fun dibujarLineas() {
        val coordenadas = PolylineOptions()
            .add(LatLng(4.421335518525363, -75.18070299178362))
            .add(LatLng(4.427511285915592, -75.18064599484204))
            .add(LatLng(4.4280905852962285, -75.1748514175415))
            .add(LatLng(4.423592228456237, -75.17236769199371))
            .color(Color.CYAN)
            .width(30f)
            //dibula la linea con puntos
            .pattern(arrayListOf<PatternItem>(Dot(), Gap(0f)))


        mMap?.addPolyline(coordenadas)
    }

    fun cambiarEstiloMapa() {
        // mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
        //Personalizar mapa https://mapstyle.withgoogle.com/
        val exitoCambioMapa =
            mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.context, R.raw.mapa_json))
        if (!exitoCambioMapa!!) {
        }
    }

    fun crearListener() {
        mMap?.setOnMarkerClickListener(markerClickListener)
        mMap?.setOnMarkerDragListener(markerDragListener)
    }

    fun marcadoresEstaticos() {
        val punto1 = LatLng(4.422379, -75.181451)
        val punto2 = LatLng(4.420357, -75.177556)

        marcador1 = mMap?.addMarker(
            MarkerOptions()
                .position(punto1)
                .snippet("Descripcion del punto")
                // .icon(BitmapDescriptorFactory.fromResource(R.drawable.avion))//Poner imagen en el icono
                .alpha(1f)
                .title("PUNTO 1")
        )
        marcador1?.tag = 0

        marcador2 = mMap?.addMarker(
            MarkerOptions()
                .position(punto2)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))//Color icono
                .alpha(0.6f)//transparencia en el color del icono
                .title("PUNTO 2")
        )
        marcador2?.tag = 0
    }

    //Agregar marcadores en el mapa
    fun prepararMarcadores() {
        listaMarcadores = ArrayList()

        mMap?.setOnMapLongClickListener { location: LatLng? ->

            listaMarcadores?.add(
                mMap?.addMarker(
                    MarkerOptions()
                        .position(location!!)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))//Color icono
                        .snippet("Descripcion del punto")
                        .alpha(0.6f)//transparencia en el color del icono
                        .title("PUNTO 2")
                )!!
            )
            //mueve el marcador
            listaMarcadores?.last()!!.isDraggable = true

            val coordenadas = LatLng(
                listaMarcadores?.last()!!.position.latitude,
                listaMarcadores?.last()!!.position.longitude
            )

            val origen = "origin=" + miPosicion?.latitude + "," + miPosicion?.longitude + "&"

            var destino = "destination=" + coordenadas.latitude + "," + coordenadas.longitude + "&"

            val parametros = origen + destino + "sensor=false&mode=driving"

            Log.d("URL", "http://maps.googleapis.com/maps/api/directions/json?" + parametros)
            cargarURL("http://maps.googleapis.com/maps/api/directions/json?" + parametros)
        }
    }

    private fun cargarURL(url: String) {
        val queue = Volley.newRequestQueue(context)

        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String> {

                response ->
            Log.d("HTTP", response)

            val coordenadas = obtenerCoordenadas(response)

            dibujarRuta(coordenadas)

        }, Response.ErrorListener { })

        queue.add(solicitud)
    }

    private fun dibujarRuta(coordenadas: PolylineOptions) {
        if (rutaMarcada != null) {
            rutaMarcada?.remove()
        }
        rutaMarcada = mMap?.addPolyline(coordenadas)
    }

    private fun obtenerCoordenadas(json: String): PolylineOptions {
        val gson = Gson()
        val objeto = gson.fromJson(json, com.example.mapaskotlin.Response::class.java)
        val puntos = objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!

        var coordenadas = PolylineOptions()

        for (punto in puntos) {
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }
        coordenadas.color(Color.CYAN)
            .width(15f)
        return coordenadas
    }

    fun configurarMiUbicacion() {
        //Boton de ubicacion
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
    }

    fun anadirMarcadorMiPosicion() {
        mMap?.addMarker(MarkerOptions().position(miPosicion!!).title("Aqui estoy"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
    }

}