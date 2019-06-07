package edu.skku.map.class42.team6;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

public class Models {

//    public static final String API_KEY = "mCayx%2FW%2FW%2FvhZrAo7PNumFfNOjrs2Lepqx2BwnVCo8xXwmMZfjG9n8Ney5eTvI82bEuzUlAD2GGRKmv1%2BDE%2Fgw%3D%3D";
    public static final String API_KEY = "F8csb9A9pHefkBCGavCVP%2BF%2BeiMb1i3vLsGV9hi1sWCcmOtNmtD5oXWN%2BGbSPxp%2B2ZORejIa9PY%2FaIlPM%2FH1vA%3D%3D";

    static class City implements Serializable {
        private final String cityName;
        private final int cityCode;

        City(String name, int code) {
            this.cityName = name;
            this.cityCode = code;
        }

        public String getCityName() {
            return cityName;
        }

        public int getCityCode() {
            return cityCode;
        }
    }

    static class Station implements Serializable {
        private final String stationID;
        private final String stationName;
        private final int stationCityCode;

        Station(String id, String name, int cityCode) {
            this.stationID = id;
            this.stationName = name;
            this.stationCityCode = cityCode;
        }

        public String getStationID() {
            return stationID;
        }

        public String getStationName() {
            return stationName;
        }

        public int getStationCityCode() {
            return stationCityCode;
        }
    }

    static class SearchOptions implements Serializable {

        private Calendar dateTime;
        private SearchMode mode;
        private Station origin;
        private Station destination;
        SearchOptions(Calendar dateTime, SearchMode mode) {
            this.dateTime = dateTime;
            this.mode = mode;
        }

        SearchOptions setMode(SearchMode mode) {
            this.mode = mode;
            return this;
        }

        SearchOptions setDateTimeNow() {
            dateTime = Calendar.getInstance();
            return this;
        }

        SearchOptions setDate(int year, int month, int dayOfMonth) {
            dateTime.set(year, month, dayOfMonth);
            return this;
        }

        SearchOptions setTime(int hour, int minute) {
            dateTime.set(Calendar.HOUR_OF_DAY, hour);
            dateTime.set(Calendar.MINUTE, minute);
            return this;
        }

        public void setDeparture(Station origin) {
            this.origin = origin;
        }

        public Station getOrigin() {
            return origin;
        }

        public void setArrival(Station destination) {
            this.destination = destination;
        }

        public Station getDestination() {
            return destination;
        }

        Calendar getDateTime() {
            return dateTime;
        }

        boolean checkValidity() {
            return mode != null && origin != null && destination != null;
        }

        enum SearchMode {
            BY_DEPARTURE, BY_ARRIVAL
        }
    }

    static class Vehicle {
        private final int vehicleID;
        private final String vehicleName;

        Vehicle(int vid, String vname) {
            this.vehicleID = vid;
            this.vehicleName = vname;
        }

        public int getVehicleID() {
            return vehicleID;
        }

        public String getVehicleName() {
            return vehicleName;
        }
    }

    static class TrainSchedule {
        private final String departureTIme;
        private final String arrivalTime;

        private final int trainType;

        TrainSchedule(String dep, String arr, int trainType) {
            this.departureTIme = dep;
            this.arrivalTime = arr;
            this.trainType = trainType;
        }

        public int getTrainType() {
            return trainType;
        }

        public String getArrivalTime() {
            return arrivalTime;
        }

        public String getDepartureTIme() {
            return departureTIme;
        }

        @NonNull
        @Override
        public String toString() {
            return new StringBuilder(departureTIme)
                    .append("~")
                    .append(arrivalTime).toString();
        }
    }
}
