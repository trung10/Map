package module;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.als.ado.demomap.MapsActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import module.Road;
import module.node;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RoadJSONParser {
   // public static double distance = 0;

    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).retryOnConnectionFailure(true).build();

    public List<Road> parseRoad(JSONArray jsonObject){
        List<Road> roads= new ArrayList<>();

        try {
            //get array
            for (int i = 0; i < jsonObject.length(); i++) {
                List<node> nodes = new ArrayList<>();
                JSONObject info = (JSONObject) jsonObject.get(i);
                //if (info.get("osm_type").toString().trim() == "way") {
                    String id = (String) info.get("osm_id");
                    Double lat = Double.parseDouble((String) info.get("lat"));
                    Double lon = Double.parseDouble((String) info.get("lon"));
                    LatLng coordinate = new LatLng(lat, lon);

                    JSONObject address = (JSONObject) info.get("address");
                    String road = null;
                    if (address.has("road")){
                        road = (String) address.get("road");
                    }else if(address.has("address27")){
                        road = (String) address.get("address27");
                    }

                    String city = (String) address.get("city");
                    String suburb = null;
                    if (address.has("suburb")) {
                        suburb = (String) address.get("suburb");
                    }else if(address.has("county")){
                        suburb = (String) address.get("county");
                    }
                    String country = (String) address.get("country");

                    Request.Builder  builder = new Request.Builder();
                    String URL = MapsActivity.getARoadURL(id.trim());
                    builder.url(URL);

                    Request request = builder.build();

                    try {
                        Response response = client.newCall(request).execute();

                        String data =  response.body().string();

                        JSONObject object = new JSONObject(data);

                        nodes = parseNode(object);

                       // Log.e("So node cua 1 duong", nodes.size() + "");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Road road1 = new Road(id, nodes, coordinate, nodes.get(0).getCoordinate(), nodes.get(nodes.size() - 1).getCoordinate(), road,suburb, city, country);

                    roads.add(road1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

       // Log.e("ssss", roads.get(0).toString());
        return  roads;
    }

    public double getDistance(){

        return 0;
    }

    public synchronized List<node> parseNode(JSONObject jsonObject){
        List<node> nodes = new ArrayList<>();
        nodes.clear();

        try {
            JSONArray array = jsonObject.getJSONArray("elements");
            ArrayList<String> listID = new ArrayList<>();
            listID.clear();
            JSONObject listNode = ((JSONObject)array.get(0));
            JSONArray arrayNodeID = listNode.getJSONArray("nodes");
            for (int i = 0; i < arrayNodeID.length(); i++) {
                listID.add(String.valueOf(arrayNodeID.get(i)));
                //Log.e("ssss", arrayNodeID.get(i) + "");
            }
            for (int i = 0; i < listID.size(); i++) {
                for (int j = 1; j < array.length(); j++) {
                    JSONObject node = (JSONObject) array.get(j);

                    if (listID.get(i).equalsIgnoreCase(String.valueOf(node.get("id")))){

                        Double lat = node.isNull("lat") ? null : node.getDouble("lat");
                        Double lon = node.isNull("lon") ? null : node.getDouble("lon");
                        LatLng latLng = null;
                        if (lat != null && lon != null){
                            latLng = new LatLng(lat, lon);
                        }
                        //dang co loi
                        //int id = listID.get(i).equals("") ? null : Integer.valueOf(listID.get(i).trim());
                        String id = node.isNull("id") ? null : node.get("id").toString();
                        //Log.e("ID", id + "");

                        //Road way = new Road();


                        node node1 = new node(id.trim(), latLng, new ArrayList<Road>());
                        nodes.add(node1);
                        break;
                    }
                }
               //Log.e("ID node", listID.get(i));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return  nodes;
    }

    public List<Road> waysForNode(List<Road> roads){

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < roads.size(); i++) {
            ids.add(roads.get(i).getId());
        }

        for (int i = 0; i < roads.size(); i++) {
            List<node> nodes = new ArrayList<>();
            nodes = roads.get(i).getListNodes();

            for (int j = 0; j < nodes.size(); j++) {
                List<Road> r = new ArrayList<>();

                Request.Builder  builder = new Request.Builder();
                String URL = wayOfNodeURL(nodes.get(j).getId());
                builder.url(URL);

                Request request = builder.build();

                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();

                    JSONObject object = new JSONObject(res);

                    //listWay = waysForNode(object);
                    JSONArray jsonArray = object.getJSONArray("elements");

                    for (int k = 1; k < jsonArray.length(); k++) {
                        JSONObject wayJson = jsonArray.getJSONObject(k);
                        String type = wayJson.isNull("type") ? null : wayJson.getString("type");
                        if (type.trim().equalsIgnoreCase("way")){
                            JSONObject tags = wayJson.getJSONObject("tags");
                            String name = tags.isNull("name") ? null : tags.getString("name");

                            if (name!= null){
                                String id = wayJson.getString("id");
                                String city = tags.isNull("addr:city") ? null : tags.getString("addr:city");
                                String suburb = tags.isNull("addr:district") ? null : tags.getString("addr:district");

                                Road way = new Road(id, null, null,null, null,
                                        name, suburb, city, null);
                                //r.add(way);
                                roads.get(i).getListNodes().get(j).getWaysFornode().add(way);

                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //roads.get(i).getListNodes().get(j).getWaysFornode().addAll(r);
            }
        }

        return roads;
    }
    private String wayOfNodeURL(String nodeID){
        return "http://overpass-api.de/api/interpreter?data=[out:json][timeout:25];(node(" +
                nodeID +
                "); way(bn);); out body;";
    }
}
