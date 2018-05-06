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
import android.widget.ProgressBar;
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


    private GoogleMap mMap; //Cái map mình hiển th
    public static Place road;//dia diem khi minh nhap vao

    private BottomSheetBehavior bottomSheetBehavior;//Bottom sheet để hiển thị tên đường quận, con đường khoảng cánh,...

    public List<Road> listRoad; //list con đường mà api trả về
    private TextView txt_name;  //TV hiển thị tên con đường
    private TextView txt_distance;//TV hiển thị khoảng cánh

    private RecyclerView recyclerView;//List View hiển thị các con đường giao với X
    public static List<Road> listWayRel;//List các con đường giao với X
    private RoadAdapter roadAdapter;//Adapter để hiển thị các con đường

    private RecyclerView recyclerViewDistrict;//List View hiển thị các quận
    public static List<String> districts;//List tên các quận Giao với X
    private DistrictAdapter districtAdapter;//Adapter như để hiện thị quận

    private FloatingActionButton floatingActionButton;//Cái button màu hồng
    private android.support.v7.widget.CardView cV;//cái mình nhập tên quận và tên con đường
    private Double distance;//khoảng cánh

    private ProgressBar progressBar; //cái nay k cânf thiết



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Ánh xạ cái map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();//hàm này để ánh xạ các thành phần còn lại

        final PlaceAutocompleteFragment placeAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);//ánh xạ cái TextView mình nhập vào
        placeAutocompleteFragment.setHint("Nhâp tên con đường");

        placeAutocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(10.3798, 106.3449),
                new LatLng(11.1528, 106.9780)));//để  trỏ cái vể cái thành phố HCM

        //set chỉ lọc ra tên con đường
        AutocompleteFilter filter = new AutocompleteFilter.Builder().
                setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).build();
        placeAutocompleteFragment.setFilter(filter);

        //Bắt sự kiện khi mình nhập vào cái gì đó
        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //ẩn các view trên màn hình đi để lại map với editText AutoComplete mình nhập vào
                floatingActionButton.setVisibility(View.GONE);
                cV.setVisibility(View.INVISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mMap.clear();

                //khi nhâp vào thì set cái map hiện thị đến địa chỉ vị trí của con đường
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 14));
                road = place;

                //lấy kiểu của địa điển mình vừa nhập
                List<Integer> type = place.getPlaceTypes();
                //nếu nó là con đường thì mình sẽ làm tiếp k thì hiển thị nhập lại
                if (type.contains(Place.TYPE_ROUTE)){
                    //get URL trả về JSON
                    String URL = getRoadsURL(road.getAddress()+ "");

                    /*LatLngBounds bounds = place.getViewport();
                    Double S = bounds.southwest.latitude;
                    Double W = bounds.southwest.longitude;
                    Double N = bounds.northeast.latitude;
                    Double E = bounds.northeast.longitude;*/

                    //set tên con đường
                    txt_name.setText(place.getName());

                    //Log.e("bounds", S + ","+ W + "," + N +"," + E);
                    //
                    Log.e("url", URL);
                    //Kéo dữ liệu về để xử lý
                    new fetchDataURL().execute(URL);
                }else{
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn một con đường", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                //cái này để bắt khi có lỗi xảy ra
                Toast.makeText(getApplicationContext(), "Place selection failed: " + status.getStatusMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        //bắt sự kiện cho nút màu hồng
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cV.getVisibility() == View.VISIBLE){
                    //khi click sẽ hiện cho mình cái editText cho mình nhập vào
                    floatingActionButton.setImageResource(R.drawable.ic_compare_arrows_black_24dp);
                    cV.setVisibility(View.INVISIBLE);
                }else {
                    //bấm cái nữa để nó ẩn đi
                    cV.setVisibility(View.VISIBLE);
                    floatingActionButton.setImageResource(R.drawable.ic_clear_black_24dp);
                }
            }
        });

        //ánh xạ cái mình nhập vào để so sánh với con đường
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment.setHint("Kiểm Tra");
        //bắt sự kiện cho nó
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //Log.e("type", place.getPlaceTypes()+ "");
                //nếu nhập vaof là một con đường hoặc một quận thì làm tiếp k báo lỗi
                if (place.getPlaceTypes().contains(Place.TYPE_ROUTE)){
                    boolean completed = true;
                    //Log.e("ssasa", place.getName() + "");
                    for (Road road1: listWayRel) {
                        //hiển thị ra màn hình nếu có giao với x
                        Log.e("ss", road1.getRoad());
                        if (road1.getRoad().trim().equalsIgnoreCase((place.getName() + "").trim())){
                            Toast.makeText(getApplicationContext(), place.getName() + " có giao với "
                                    + road.getName(), Toast.LENGTH_SHORT).show();
                            completed = false;
                            break;
                        }
                    }
                    if (completed){//ngược lại thì thông báo không giao
                        Toast.makeText(getApplicationContext(), place.getName() + " k giao với "
                                + road.getName(), Toast.LENGTH_SHORT).show();
                    }
                }else if (place.getPlaceTypes().contains(Place.TYPE_ADMINISTRATIVE_AREA_LEVEL_2) &&
                        place.getPlaceTypes().contains(Place.TYPE_POLITICAL)){//nếu nó là một quận
                                String name = place.getName() + "";
                                name = name.replace("District", "Quận");
                                //hiển thị ra kết quả
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
                //sau khi nhập xong thì ẩn đi
                floatingActionButton.setImageResource(R.drawable.ic_compare_arrows_black_24dp);
                cV.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

        //cài đăt botton sheet ẩn đi
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Các bước cài đăt listView
        listWayRel = new ArrayList<>();
        roadAdapter = new RoadAdapter(this, listWayRel);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(roadAdapter);

        //Cài đặt list View
        districts = new ArrayList<>();
        districtAdapter = new DistrictAdapter(districts);
        RecyclerView.LayoutManager manager1 = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDistrict.setLayoutManager(manager1);
        recyclerViewDistrict.setItemAnimator(new DefaultItemAnimator());
        recyclerViewDistrict.setAdapter(districtAdapter);

    }

    private void init(){//Ánh xạ
        View view = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(view);
        txt_name = findViewById(R.id.tv_road_name);
        txt_distance = findViewById(R.id.tv_distance);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerViewDistrict = findViewById(R.id.district);
        floatingActionButton = findViewById(R.id.floating_button);
        cV = findViewById(R.id.cardView2);
        progressBar = findViewById(R.id.pbLoading);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //trỏ về thành phố HCM
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

    private void addPolyline(ArrayList<LatLng> coordinates){//k dùng đêns
        mMap.addPolyline(new PolylineOptions().geodesic(true).addAll(coordinates)
        ).setColor(R.color.colorPolyline);
    }

    private String downloadUrl(String strUrl) throws IOException {//k dùng đến
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

    public static String getDirectionsUrl(LatLng origin,LatLng dest){//chỉ đường giữa 2 điểm, có luôn ấy khoảng cánh

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
                + "AIzaSyCa9kyCUjoT_1X9hUnBtRmfOxAn28TAFrU";

        return url;
    }

    private String getRoadsURL(String andress){//API lấy thông tin của con đường

        String URL = "https://nominatim.openstreetmap.org/search/" + andress.trim()
                +"?format=json&addressdetails=1&limit=100";
        return  URL;
    }

    public static String getARoadURL(String wayID){//API Lấy các điểm của con đường
        return "http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(way(" +
                wayID +
                "););out%20body;%3E;out%20skel%20qt;";
    }

    public static String getWayForNodeURL(String nodeID){//Lấy các con đường trên một điểm
        return "http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(node(" +
                nodeID +
                ");way(bn););out%20body;%3E;out%20skel%20qt;";
    }

    private String getDistrictURL(LatLng latLng){//API Lấy các quận của một điểm
        return "https://maps.googleapis.com/maps/api/geocode/json?&latlng=" +
                latLng.latitude +
                "," +
                latLng.longitude;
    }

    private class fetchDataURL extends AsyncTask<String, Void, String>{//3 tham số này có nghĩa là Tham số đầu tiên là đầu vào , tham số thứ 2 là trả về trong quá trình xữ lý, tham số thứ 3 là kết quả trả về
        //kéo dữ liệu về, dùng AsyncTask để tạo luồng
        //Sử dụng thư viện OkHttp, tham khảo ở https://viblo.asia/p/okhttp-3-l0rvmxmkGyqA
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {//hàm thực hiện
            //Xây dụng respest để dùng OkHttp
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
        protected void onPostExecute(String s) {//hàm kết quả trả về dùng để hiển thị lên giao diện
            super.onPostExecute(s);
            if (s == ""){
                Toast.makeText(getApplicationContext(), "URL not found!", Toast.LENGTH_SHORT).show();
            }else {
               new ParserRoad().execute(s);//sẽ trả về một JSON sau đó gọi tiếp một asyntask để hiển thực hiện
            }

        }
    }

    private class ParserRoad extends AsyncTask<String, Void, List<Road>>{

        @Override
        protected List<Road> doInBackground(String... strings) {//dữ liệu truyền vào

            List<Road> roads = new ArrayList<>();//list để chữa dữ liệu khi xử lý xong
            roads.clear();

            try {
                JSONArray jsonArray = new JSONArray(strings[0]);//lấy dữ liệu truyền vào ra chuyển nó thành JSONOject

                RoadJSONParser roadJSONParser = new RoadJSONParser();//clas để parser dữ  liệu ra

                roads = roadJSONParser.parseRoad(jsonArray);// lấy con các con đường mà mình tìm thấy

                //Log.e("So luong cac con duong", roads.get(0).toString() + "");
                Double distances = 0.0;//độ dài
                for (Road r : roads) {
                    distances += new RoadJSONParser().distanceParser(r.getCoordinateStart(),
                            r.getCoordinateEnd());//lấy các độ dài của tất cả con đường mà mình tìm thấy
                }
                distance = distances;//lưu lại vào biến toàn cục

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
                //tộ các con đường tìm thấy
                //vẽ các đường giữa các điểm 
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
                //tìm kiếm các con đường giao với con đường đó, vì API OSM không hỗ trợ tốt cho việt nam có thể tìm ra không hết tất cả các con đường
                new parseWaysForNode().execute(roads);
            }
        }
    }

    private class parseWaysForNode extends AsyncTask<List<Road>, Void, List<Road>>{
        //
        @Override
        protected List<Road> doInBackground(List<Road>... lists) {
            List<Road> list = lists[0];

            return new RoadJSONParser().waysForNode(list);
        }

        @Override
        protected void onPostExecute(List<Road> roads) {
            super.onPostExecute(roads);
            if (roads.size() > 0) {
                listRoad = roads;//trả lại list hoàn chỉnh các con điểm trên một con đường và các đường giao với nó
                List<String> districtss = new ArrayList<>();//luu cac quan
                List<Road> listWayForNode = new ArrayList<>();//list cac con duong
                for (Road road : roads) {//forech
                    List<node> nodes = road.getListNodes();//lấy các điểm của con đường
                    if (!districtss.contains(road.getSuburb())){//lấy các quận ra
                        districtss.add(road.getSuburb());
                    }
                    for (node node1 : nodes) {//lấy các các con đường giao với x
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
                        districts.add(dis);//thêm vào biến toàn cục
                    }
                }
                districtAdapter.notifyDataSetChanged();//hiển thị lên list View
                txt_distance.setText(distance/1000 + " km");//hiển thị độ dài

                listWayRel.clear();
                for (Road r: listWayForNode) {
                    String name = r.getRoad();
                    if (!containsRoadName(name, listWayRel)){
                        listWayRel.add(r);//lưu các con đường giao với x
                    }

                }
                //listWayRel.addAll(listWayForNode);
                roadAdapter.notifyDataSetChanged();//hiện thị các con đương giao lên màn hình

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);//hiện thị buttom sheet
                bottomSheetBehavior.setPeekHeight(160);
                floatingActionButton.setVisibility(View.VISIBLE);
                //progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private boolean containsRoadName(String name, List<Road> b){//hàm này kiểm tra các con đường giao với x bị trùng tên
        for (Road r: b) {
            if (name.equalsIgnoreCase(r.getRoad())){
                return true;
            }
        }
        return  false;
    }

    public class RoadAdapter extends RecyclerView.Adapter<RoadAdapter.RoadHolder>{//tạo adapter để hiện thị lên list view

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

    public class DistrictAdapter extends RecyclerView.Adapter<DistrictAdapter.DistrictViewHolder> {//adapter để đổ dữ liệu ra màn hình
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
