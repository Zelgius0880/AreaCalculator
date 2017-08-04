package florian.com.areacalculator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var moveMarker = false
    var marker: Marker? = null
    val list = mutableListOf<Marker>()
    var line: Polygon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            8965 -> if (grantResults.size == 2
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                enablePosition()
        }
    }

    private fun enablePosition() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    8965)
        } else {
            mMap.isMyLocationEnabled = true
            val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.lastLocation
                    .addOnSuccessListener(this, { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13f))

                            val cameraPosition = CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))      // Sets the center of the map to location user
                                    .zoom(17f)                   // Sets the zoom
                                    .build()                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                        }
                    })

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        enablePosition()

        googleMap.setOnMapLongClickListener { latLng ->
            list.add(googleMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .draggable(true))
            )

            if (list.size > 2) cardView.visibility = View.VISIBLE

            drawLine()
            computeArea()
        }

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker?): View? {
                return null
            }

            override fun getInfoWindow(p0: Marker?): View {
                val view = View.inflate(this@MapsActivity, R.layout.info_window, null)

                return view
            }

        })

        googleMap.setOnInfoWindowClickListener { marker ->
            list.remove(marker)
            marker.remove()

            drawLine()
            computeArea()
        }

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                return true
            }

            override fun onMarkerDragEnd(p0: Marker?) {
                drawLine()
                computeArea()
            }

            override fun onMarkerDragStart(p0: Marker?) {
                drawLine()
                computeArea()
            }

            override fun onMarkerDrag(p0: Marker?) {
                drawLine()
                computeArea()
            }

        })
    }

    private fun drawLine() {
        line?.remove()
        val options = PolygonOptions()
                .strokeWidth(5f)
                .strokeColor(Color.BLUE)

        for (m: Marker in list)
            options.add(LatLng(m.position.latitude, m.position.longitude))

        //if(list.size > 2) options.add(LatLng(list.last().position.latitude, list.last().position.longitude))

        line = mMap.addPolygon(options)
    }

    private fun computeArea() {
        if (list.size > 2) {
            cardView.visibility = View.VISIBLE

            val locations = mutableListOf<LatLng>()
            list.mapTo(locations) { LatLng(it.position.latitude, it.position.longitude) }
            textView.text = String.format("%.3f m²", GeoUtils.computeArea(ArrayList(locations)))
        } else cardView.visibility = View.GONE

    }
}
