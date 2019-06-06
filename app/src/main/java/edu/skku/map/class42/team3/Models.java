package edu.skku.map.class42.team3;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

public class Models {
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

        enum SearchMode {
            BY_DEPARTURE, BY_ARRIVAL
        }

        SearchOptions(Calendar dateTime, SearchMode mode) {
            this.dateTime = dateTime;
            this.mode = mode;
        }

        private Calendar dateTime;
        private SearchMode mode;
        private Station origin;
        private Station destination;

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
