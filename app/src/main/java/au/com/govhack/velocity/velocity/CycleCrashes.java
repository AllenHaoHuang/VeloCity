package au.com.govhack.velocity.velocity;

import java.util.ArrayList;
import java.util.List;

public class CycleCrashes {
    public CycleCrashes() {
    }

    public static class CycleCrash {
        public CycleCrash(){};

        private String date;
        private String type;
        private String severity;
        private double latitude;
        private double longitude;

        public CycleCrash(String date, String type, String severity, double latitude, double longitude) {
            this.date = date;
            this.type = type;
            this.severity = severity;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public String getSeverity() {
            return severity;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return type + ", " + severity;
        }
    }

    private List<CycleCrash> crashes = new ArrayList<>();

    public List<CycleCrash> getCrashes() {
        return crashes;
    }

    public void setCrashes(List<CycleCrash> crashes) {
        this.crashes = crashes;
    }

    public void addCrash(String date, String type, String severity, double latitude, double longitude) {
        crashes.add(new CycleCrash(date, type, severity, latitude, longitude));
    }
}
