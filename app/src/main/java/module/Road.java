package module;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Road {//class con đường
    private String id;
    //private String address;
    private List<node> listNodes;//List các điểm trên con đường
    private LatLng coordinate;//tọa độ một điểm bất kỳ trên con đường
    private LatLng coordinateStart;//tọa dộ bắt đầu của con đường
    private LatLng coordinateEnd;//tọa độ kết thúc của con đường
    private String road;//tên con đường
    private String suburb;//tên quận chứa con đường đó
    private String city;//thành phố của con đường đó
    private String country;//nc

    public Road(String id, List<node> listNodes, LatLng coordinate, LatLng coordinateStart, LatLng coordinateEnd, String road, String suburb, String city, String country) {
        this.id = id;
        this.listNodes = listNodes;
        this.coordinate = coordinate;
        this.coordinateStart = coordinateStart;
        this.coordinateEnd = coordinateEnd;
        this.road = road;
        this.suburb = suburb;
        this.city = city;
        this.country = country;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    private Road(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public List<node> getListNodes() {
        return listNodes;
    }

    public void setListNodes(List<node> listNodes) {
        this.listNodes = listNodes;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public LatLng getCoordinateStart() {
        return coordinateStart;
    }

    public void setCoordinateStart(LatLng coordinateStart) {
        this.coordinateStart = coordinateStart;
    }

    public LatLng getCoordinateEnd() {
        return coordinateEnd;
    }

    public void setCoordinateEnd(LatLng coordinateEnd) {
        this.coordinateEnd = coordinateEnd;
    }

    @Override
    public String toString() {
        return  listNodes != null ?
                "Road{" +
                "id=" + id +
                ", listNodes=" + listNodes.size() +
                ", coordinate=" + coordinate +
                ", coordinateStart=" + coordinateStart +
                ", coordinateEnd=" + coordinateEnd +
                ", road='" + road + '\'' +
                ", suburb='" + suburb + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}'
                : "Road{" +
                "id=" + id +
                ", listNodes=" + "null" +
                ", coordinate=" + coordinate +
                ", coordinateStart=" + coordinateStart +
                ", coordinateEnd=" + coordinateEnd +
                ", road='" + road + '\'' +
                ", suburb='" + suburb + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
