package au.com.govhack.velocity.velocity;

/**
 * Created by allen on 29/07/2017.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by manalmohania on 29/7/17.
 */
public class ScenicSpots {

    public static class ScenicSpot{

        String name;
        double lat;
        double longi;

        public ScenicSpot(){}

        ScenicSpot(String name, double lat, double longi){
            this.name = name;
            this.lat = lat;
            this.longi = longi;
        }

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return lat;
        }

        public double getLongitude() {
            return longi;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLatitude(double lat) {
            this.lat = lat;
        }

        public void setLongitude(double longi) {
            this.longi = longi;
        }
    }

}
