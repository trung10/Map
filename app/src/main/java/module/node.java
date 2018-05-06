package module;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import module.Road;

public class node {//class các điểm trên một con đường
    private String id;//id
    private LatLng coordinate;//tọa đồ của điểm đó
    private List<Road> waysFornode;//List các con đường trên điểm đó


    public node(String id, LatLng coordinate, List<Road> waysFornode) {
        this.id = id;
        this.coordinate = coordinate;
        this.waysFornode = waysFornode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public List<Road> getWaysFornode() {
        return waysFornode;
    }

    public void setWaysFornode(List<Road> waysFornode) {
        this.waysFornode = waysFornode;
    }

    @Override
    public String toString() {
        return "node{" +
                "id=" + id +
                ", coordinate= (" + coordinate.latitude + "," + coordinate.longitude +
                "), waysFornode=" + waysFornode +
                '}';
    }
}
