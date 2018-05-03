package com.als.ado.demomap;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAccessibilityDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import module.Road;
import module.RoadJSONParser;
import module.node;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    public static Place road;

    private ArrayList<LatLng> arr;
    private BottomSheetBehavior bottomSheetBehavior;

    public List<Road> listRoad;
    private TextView txt_name;
    private TextView txt_distance;

    private RecyclerView recyclerView;
    public static List<Road> listWayRel;
    private RoadAdapter roadAdapter;

    private RecyclerView recyclerViewDistrict;
    public static List<String> districts;
    private DistrictAdapter districtAdapter;

    private FloatingActionButton floatingActionButton;
    private android.support.v7.widget.CardView cV;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

        PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        placeAutocompleteFragment.setHint("Nhâp tên con đường");

        placeAutocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(10.3798, 106.3449),
                new LatLng(11.1528, 106.9780)));


        AutocompleteFilter filter = new AutocompleteFilter.Builder().
                setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        placeAutocompleteFragment.setFilter(filter);

        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                floatingActionButton.setVisibility(View.GONE);
                cV.setVisibility(View.INVISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mMap.clear();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 14));
               // MarkerOptions marker = new MarkerOptions().title((String) place.getName()).snippet(place.toString()).position(place.getLatLng());
               // mMap.addMarker(marker);
                road = place;
                List<Integer> type = place.getPlaceTypes();
                if (type.contains(Place.TYPE_ROUTE)){
                    Toast.makeText(getApplicationContext(), road.getName(), Toast.LENGTH_SHORT).show();
                    String URL = getRoadsURL(road.getAddress()+ "");
                    LatLngBounds bounds = place.getViewport();
                    Double S = bounds.southwest.latitude;
                    Double W = bounds.southwest.longitude;
                    Double N = bounds.northeast.latitude;
                    Double E = bounds.northeast.longitude;

                    txt_name.setText(place.getName());

                    //Log.e("bounds", S + ","+ W + "," + N +"," + E);
                    Log.e("url", URL);
                    new fetchDataURL().execute(URL);
                }else{
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn một con đường", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), "Place selection failed: " + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cV.getVisibility() == View.VISIBLE){
                    cV.setVisibility(View.INVISIBLE);
                }else {
                    cV.setVisibility(View.VISIBLE);
                }
            }
        });

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment.setHint("Kiểm Tra");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.e("type", place.getPlaceTypes()+ "");
                if (place.getPlaceTypes().contains(Place.TYPE_ROUTE)){
                    boolean completed = true;
                    Log.e("ssasa", place.getName() + "");
                    for (Road road1: listWayRel) {
                        Log.e("ss", road1.getRoad());
                        if (road1.getRoad().trim().equalsIgnoreCase((place.getName() + "").trim())){
                            Toast.makeText(getApplicationContext(), place.getName() + " có giao với "
                                    + road.getName(), Toast.LENGTH_SHORT).show();
                            completed = false;
                            break;
                        }
                    }
                    if (completed){
                        Toast.makeText(getApplicationContext(), place.getName() + " k giao với "
                                + road.getName(), Toast.LENGTH_SHORT).show();
                    }
                }else if (place.getPlaceTypes().contains(Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2) &&
                        place.getPlaceTypes().contains(Place.TYPE_POLITICAL)){
                                String name = place.getName() + "";
                                name = name.replace("District", "Quận");

                                if (districts.contains(name.trim())){
                                    Toast.makeText(getApplicationContext(), name +
                                            " có giao với " + road.getName(), Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getApplicationContext(), name
                                            + " k giao với " + road.getName(), Toast.LENGTH_SHORT).show();
                                }
                }else {
                    Toast.makeText(getApplicationContext(), "Hãy nhập vào một đường hoặc một quận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {

            }
        });

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        listWayRel = new ArrayList<>();
        roadAdapter = new RoadAdapter(this, listWayRel);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration();
        recyclerView.setAdapter(roadAdapter);

        districts = new ArrayList<>();
        districtAdapter = new DistrictAdapter(districts);
        RecyclerView.LayoutManager manager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDistrict.setLayoutManager(manager1);
        recyclerViewDistrict.setItemAnimator(new DefaultItemAnimator());
        recyclerViewDistrict.setAdapter(districtAdapter);

    }

    private void init(){
        View view = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        txt_name = findViewById(R.id.tv_road_name);
        txt_distance = findViewById(R.id.tv_distance);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerViewDistrict = findViewById(R.id.district);
        floatingActionButton = findViewById(R.id.floating_button);
        cV = findViewById(R.id.cardView2);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng HCM = new LatLng(10.824861, 106.627803);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HCM, 10));

        mMap.addMarker(new MarkerOptions()
                .title("HCM City")
                .snippet("The most populous city in VietNam.")
                .position(HCM));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }

    /*@Override
    public void onPlaceSelected(Place place) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 14));
        MarkerOptions marker = new MarkerOptions().title((String) place.getName()).snippet(place.toString()).position(place.getLatLng());
        mMap.addMarker(marker);
        road = place;
        List<Integer> type = place.getPlaceTypes();
        if (type.contains(Place.TYPE_ROUTE)){
            Toast.makeText(getApplicationContext(), road.getName(), Toast.LENGTH_SHORT).show();
            String URL = getRoadsURL(road.getAddress()+ "");
            LatLngBounds bounds = place.getViewport();
            Double S = bounds.southwest.latitude;
            Double W = bounds.southwest.longitude;
            Double N = bounds.northeast.latitude;
            Double E = bounds.northeast.longitude;

            txt_name.setText(place.getName());

            //Log.e("bounds", S + ","+ W + "," + N +"," + E);
            Log.e("url", URL);
            new fetchDataURL().execute(URL);
        }else{
            Toast.makeText(getApplicationContext(), "Vui lòng chọn một con đường", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }*/

    private void addPolyline(ArrayList<LatLng> coordinates){
        mMap.addPolyline(new PolylineOptions().geodesic(true).addAll(coordinates)
        ).setColor(R.color.colorPolyline);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Tao http connection giao tiep voi url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting den url
            urlConnection.connect();

            boolean isError = urlConnection.getResponseCode() >= 400;
            //In HTTP error cases, HttpURLConnection only gives you the input stream via #getErrorStream().
            iStream = isError ? urlConnection.getErrorStream() : urlConnection.getInputStream();


            // Reading data tu url
            //iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downl", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters + "&key="
                + getString(R.string.maps_key);

        return url;
    }

    private String getRoadsURL(String andress){

        String URL = "https://nominatim.openstreetmap.org/search/" + andress.trim()
                +"?format=json&addressdetails=1&limit=100";
        return  URL;
    }

    public static String getARoadURL(String wayID){
        return "http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(way(" +
                wayID +
                "););out%20body;%3E;out%20skel%20qt;";
    }

    public static String getWayForNodeURL(String nodeID){
        return "http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(node(" +
                nodeID +
                ");way(bn););out%20body;%3E;out%20skel%20qt;";
    }

    private String getDistrictURL(LatLng latLng){
        return "https://maps.googleapis.com/maps/api/geocode/json?&latlng=" +
                latLng.latitude +
                "," +
                latLng.longitude;
    }

    /*private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }*/

    /** A class to parse the Google Places in JSON format */
   /* private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

                ArrayList<LatLng> points = null;
                PolylineOptions lineOptions = null;
                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);

                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {

                        HashMap<String, String> point = path.get(j);

                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));

                        Log.e("ss", lat + "," + lng);
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    lineOptions.addAll(points);
                    lineOptions.color(Color.RED);
                }
            // Adding all the points in the route to LineOptions

            arr = points;
            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }*/

    private class fetchDataURL extends AsyncTask<String, Void, String>{
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();
        @Override
        protected String doInBackground(String... strings) {
            Request.Builder builder = new Request.Builder();
            builder.url(strings[0]);

            Request request = builder.build();

            try {
                Response  response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s == ""){
                Toast.makeText(getApplicationContext(), "URL not found!", Toast.LENGTH_SHORT).show();
            }else {
               new ParserRoad().execute(s);
            }

        }
    }

    private class ParserRoad extends AsyncTask<String, Void, List<Road>>{

        @Override
        protected List<Road> doInBackground(String... strings) {

            List<Road> roads = new ArrayList<>();
            roads.clear();

            try {
                JSONArray jsonArray = new JSONArray(strings[0]);

                RoadJSONParser roadJSONParser = new RoadJSONParser();

                roads = roadJSONParser.parseRoad(jsonArray);

                //Log.e("So luong cac con duong", roads.get(0).toString() + "");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return roads;
        }

        @Override
        protected void onPostExecute(List<Road> roads) {
            super.onPostExecute(roads);

            if (roads.size() > 0){
                listRoad = roads;
                for (Road road: roads) {
                    ArrayList<LatLng> latLngs = new ArrayList<>();
                    PolylineOptions polylineOptions = new PolylineOptions();
                    List<node> nodes = road.getListNodes();

                    for (node node1 : nodes) {
                        latLngs.add(node1.getCoordinate());
                    }

                    polylineOptions.addAll(latLngs);
                    polylineOptions.color(Color.RED);

                    if (polylineOptions != null) {
                        mMap.addPolyline(polylineOptions);
                    }
                }

                new parseWaysForNode().execute(roads);
            }
        }
    }

    private class parseWaysForNode extends AsyncTask<List<Road>, Void, List<Road>>{

        @Override
        protected List<Road> doInBackground(List<Road>... lists) {
            List<Road> list = lists[0];

            return new RoadJSONParser().waysForNode(list);
        }

        @Override
        protected void onPostExecute(List<Road> roads) {
            super.onPostExecute(roads);
            if (roads.size() > 0) {
                listRoad = roads;
                List<String> districtss = new ArrayList<>();
                List<Road> listWayForNode = new ArrayList<>();
                for (Road road : roads) {
                    List<node> nodes = road.getListNodes();
                    if (!districtss.contains(road.getSuburb())){
                        districtss.add(road.getSuburb());
                    }
                    for (node node1 : nodes) {
                        for (int i = 0; i < node1.getWaysFornode().size(); i++) {
                            String name = node1.getWaysFornode().get(i).getRoad().trim();
                            if (!name.equalsIgnoreCase(MapsActivity.road.getName() + "")) {
                                listWayForNode.add(node1.getWaysFornode().get(i));
                            }
                        }
                    }
                }
                districts.clear();
                for (String dis:districtss) {
                    if (!districts.contains(dis.trim())){
                        districts.add(dis);
                    }
                }
                /*districts.addAll(districtss);
                Log.e("sss", districts.size() + "");*/
                districtAdapter.notifyDataSetChanged();

                listWayRel.clear();
                listWayRel.addAll(listWayForNode);
                roadAdapter.notifyDataSetChanged();

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setPeekHeight(160);
                floatingActionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public class RoadAdapter extends RecyclerView.Adapter<RoadAdapter.RoadHolder>{

        public Context context;
        public List<Road> list;

        public RoadAdapter(Context context, List<Road> list) {
            this.context = context;
            this.list = list;
        }

        @NonNull
        @Override
        public RoadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new RoadHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RoadHolder holder, int position) {
            holder.name.setText(list.get(position).getRoad());
            holder.idWay = list.get(position).getId();

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
        public class RoadHolder extends RecyclerView.ViewHolder{
            public ImageView image;
            public TextView name;
            public String idWay;

            public RoadHolder(final View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.thumbnail);
                name = itemView.findViewById(R.id.name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(),  idWay, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder> {
        List<String> data;

        public DistrictAdapter(List<String> data){
            this.data = data;
        }

        @NonNull
        @Override
        public DistrictViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.district, parent, false);
            return new DistrictViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DistrictViewHolder holder, int position) {
           holder.name.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class DistrictViewHolder extends RecyclerView.ViewHolder{
            public TextView name;
            public DistrictViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.districtName);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }
    }
}
