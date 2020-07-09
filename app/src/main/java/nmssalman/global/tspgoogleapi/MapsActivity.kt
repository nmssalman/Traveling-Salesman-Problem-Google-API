package nmssalman.global.tspgoogleapi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var jsonObject = JSONObject()
    var jsonArray = JSONArray()
    var polylineLat = ArrayList<LatLng>()
    val sorted_Locations = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun rawData(){
        jsonObject.put("latitude", 53.5144554)
        jsonObject.put("longitude", -113.5907162)
        jsonObject.put("title", "DHL Office")
        jsonObject.put("description", "Digital Fractal Office")
        jsonObject.put("isDelivery", true)
        jsonObject.put("reference", "#111111")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", true)
        jsonArray.put(jsonObject)

        jsonObject = JSONObject()


        jsonObject.put("latitude", 53.5692783)
        jsonObject.put("longitude",-113.4185204)
        jsonObject.put("title", "Beverly")
        jsonObject.put("description", "Alberta, Canada")
        jsonObject.put("isDelivery", false)
        jsonObject.put("reference", "#222222")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", false)
        jsonArray.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put("latitude", 53.5268217)
        jsonObject.put("longitude", -113.4997403)
        jsonObject.put("title", "Strathcona")
        jsonObject.put("description", "Alberta Canada")
        jsonObject.put("isDelivery", true)
        jsonObject.put("reference", "#333333")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", false)
        jsonArray.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put("latitude", 53.5413792)
        jsonObject.put("longitude", -113.5052135)
        jsonObject.put("title", "Downtown")
        jsonObject.put("description", "Alberta Canada")
        jsonObject.put("isDelivery", true)
        jsonObject.put("reference", "#444444")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", false)
        jsonArray.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put("latitude", 53.5532782)
        jsonObject.put("longitude", -113.5284191)
        jsonObject.put("title", "Queen Mary Park")
        jsonObject.put("description", "Alberta Canada")
        jsonObject.put("isDelivery", true)
        jsonObject.put("reference", "#55555555555555")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", false)
        jsonArray.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put("latitude", 53.5759177)
        jsonObject.put("longitude", -113.5073226)
        jsonObject.put("title", "Westwood")
        jsonObject.put("description", "Alberta Canada")
        jsonObject.put("isDelivery", true)
        jsonObject.put("reference", "#666666ABC-AA")
        jsonObject.put("isVisible", true)
        jsonObject.put("isStartPoint", false)
        jsonArray.put(jsonObject)

        generateUrl(jsonArray)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        rawData()
        var startPoint = jsonArray.getJSONObject(1)
        var startLatitude = startPoint.getDouble("latitude")
        var startLongitude = startPoint.getDouble("longitude")
        mMap = googleMap
        mMap.setMinZoomPreference(6.0f)
        mMap.setMaxZoomPreference(14.0f)

    }
    fun generateUrl(locationArray: JSONArray)
    {
        var location = locationArray.getJSONObject(0)
        var origin : String = ""
                var destination : String = "destination=New York, United States"

//        var destination : String = "destination=${locationArray.getJSONObject(locationArray.length()-1).getString("latitude")},${locationArray.getJSONObject(locationArray.length()-1).getString("longitude")}"
        var waypoints: String = "waypoints=optimize:true"
        var baseURL: String = "https://maps.googleapis.com/maps/api/directions/json?"

        origin = "origin=${location.getString("latitude")},${location.getString("longitude")}"

        for (x in 1 until locationArray.length() step 1)
        {
            var single_location = locationArray.getJSONObject(x)
            waypoints += "|${single_location.getString("latitude")},${single_location.getString("longitude")}"

        }
        var url = "${baseURL}${origin}&${destination}&${waypoints}&key=AIzaSyDecyJi_LpaQjqP_mc1AJ0k4tXaRmw0ec4"
        Log.i("CheckInfo", url)
        http_maps().execute(url)

    }

    fun generateMap(locations: JSONObject){
        if(locations.getBoolean("isVisible")) {
            var icon =
                BitmapDescriptorFactory.fromResource(R.drawable.ping_current) as BitmapDescriptor
            if (!locations.getBoolean("isStartPoint")) {
                if (locations.getBoolean("isDelivery").equals(false)) {
                    icon = BitmapDescriptorFactory.fromBitmap(generatePing(locations.getString("reference"), R.drawable.ping_blue))
                } else {
                    icon = BitmapDescriptorFactory.fromBitmap(generatePing(locations.getString("reference"), R.drawable.ping_red))
                }
            }
            val location_info = LatLng(locations.getDouble("latitude"), locations.getDouble("longitude"))
            var markerOptions = MarkerOptions().position(location_info)
                .title(locations.getString("title"))
                .position(location_info)
                .snippet(locations.getString("description"))
                .icon(icon)

            mMap.addMarker(markerOptions)
            polylineLat.add(location_info)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location_info))
        }
    }

    fun generatePing(referece: String, isDelivery: Int): Bitmap{

        var tv = layoutInflater.inflate(R.layout.custom_ping, null, false)
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        tv.layout(0,0,tv.measuredWidth, tv.measuredHeight)
        var image = tv.findViewById<ImageView>(R.id.main_image)
        var text = tv.findViewById<TextView>(R.id.main_text)
        text.text = referece
        image.setImageResource(isDelivery)
        tv.isDrawingCacheEnabled =  true
        tv.buildDrawingCache()
        var bm = tv.getDrawingCache()
        return bm
    }

    inner class http_maps(): AsyncTask<String, Void, Void>(){
        override fun doInBackground(vararg params: String): Void? {
            try
            {
                Log.i("URL", params[0])
                var response = URL(params[0]).readText();
                var waypoint_order = JSONObject(response).getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order")

                for (x in 0 until jsonArray.length() step 1)
                {
                    val sorted_object = jsonArray.getJSONObject(waypoint_order[x] as Int + 1) as JSONObject
                    sorted_Locations.put(sorted_object)
                    Log.i("Arrays",   x.toString()+" - " + sorted_Locations.getJSONObject(x).toString() )
                }
            }
            catch (ex: Exception){ }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Log.i("sortedarraycheck", sorted_Locations.length().toString())
            generateMap(jsonArray.getJSONObject(0))
            for (x in 0 until sorted_Locations.length() step 1)
            {
                generateMap(sorted_Locations.getJSONObject(x))
            }

            mMap.addPolyline(PolylineOptions().addAll(polylineLat).color(Color.RED).width(4f))
        }
    }

}


