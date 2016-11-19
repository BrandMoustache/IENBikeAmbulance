package mainbrain.tech.ienbikeambulance.track;

/**
 * Created by iammike on 03/08/16.
 */

public class Service
{
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    String id;
    String name;
    String number;
    double distance;
}
