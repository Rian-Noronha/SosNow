package com.rn.sosnow
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object RouteHttp {
    private const val key = "AIzaSyAbE0ADJpvOT2YHY4GnF4sqlyXqc0bNSBc"
    fun searchRoute(orig: LatLng, dest: LatLng): List<LatLng>? {
        try {

            val urlRoute = "https://maps.googleapis.com/maps/api/directions/json?origin=${orig.latitude},${orig.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=true&mode=driving&key=$key"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(urlRoute)
                .build()

            val response = client.newCall(request).execute()
            val result = response.body?.string()
            val json = result?.let { JSONObject(it) }
            val jsonRoute = json!!.getJSONArray("routes").getJSONObject(0)
            val leg = jsonRoute.getJSONArray("legs").getJSONObject(0)
            val steps = leg.getJSONArray("steps")
            val numSteps = steps.length()
            var step: JSONObject

            val latLngList = mutableListOf<LatLng>()
            for (i in 0 until numSteps) {
                step = steps.getJSONObject(i)
                val points = step.getJSONObject("polyline").getString("points")
                latLngList.addAll(PolyUtil.decode(points))
            }
            return latLngList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}