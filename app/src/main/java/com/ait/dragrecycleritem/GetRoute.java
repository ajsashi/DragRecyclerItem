package com.ait.dragrecycleritem;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetRoute {
    Context context;
    private MapListener listener;
    private ArrayList<Model> datalist;

    public void getDirection(final Context context, MapListener listener) {
        this.datalist= (ArrayList<Model>)Constants.datalist.clone();
        String startLat = String.valueOf(datalist.get(0).latLng.latitude);
        String startLng = String.valueOf(datalist.get(0).latLng.longitude);
        String endLat = String.valueOf(datalist.get(datalist.size()-1).latLng.latitude);
        String endLng = String.valueOf(datalist.get(datalist.size()-1).latLng.longitude);
        datalist.remove(0);
        datalist.remove(datalist.size()-1);
        StringBuilder waPoint=new StringBuilder();
        if(datalist.size()!=0){

            for (int i = 0; i<datalist.size(); i++){
                waPoint.append(datalist.get(i).getLatLng().latitude);
                waPoint.append(",");
                waPoint.append(datalist.get(i).getLatLng().longitude);
                if(i!=datalist.size()-1){
                    waPoint.append("|");
                }

            }
        }
        this.context = context;
        this.listener = listener;
        Log.d("GetDirection", "call");

        String requestApi = null;
        try {
            if(waPoint.length() == 0){
                requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "mode=driving&" + "transit_routing_preference=less_driving&" +
                        "origin=" + startLat + "," + startLng + "&" +
                        /*"waypoints=optimize:true|"+waPoint + "&" +*/
                        "destination=" + endLat + "," +endLng + "&" +
                        "key=AIzaSyCS7UEhyUGySSmxjjzAZXFDbZByx3C7yVw";
            }else {
                requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "mode=driving&" + "transit_routing_preference=less_driving&" +
                        "origin=" + startLat + "," + startLng + "&" +
                        "waypoints=optimize:true|"+waPoint + "&" +
                        "destination=" + endLat + "," + endLng + "&" +
                        "key=AIzaSyCS7UEhyUGySSmxjjzAZXFDbZByx3C7yVw";
            }

            Log.d("Home Activity url", requestApi);
            ApiInterface mService = GoogleMapAPI.getClient("https://maps.googleapis.com").create(ApiInterface.class);
            mService.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {

                       new ParserTask().execute(response.body().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(context, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {

                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++) {

                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.rgb(91, 59, 1));
                polylineOptions.geodesic(true);
            }
            listener.onRouteObtained(polylineOptions);

        }
    }

    public class DirectionJSONParser {
        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }

            return routes;
        }

        /**
         * Method to decode polyline points
         * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List decodePoly(String encoded) {

            List poly = new ArrayList();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }

    }
}
