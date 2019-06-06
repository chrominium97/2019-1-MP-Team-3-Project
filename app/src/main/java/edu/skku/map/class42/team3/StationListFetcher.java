package edu.skku.map.class42.team3;

import android.content.ContextWrapper;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class StationListFetcher {

    interface OnStationListFetchedListener {
        class StationListResult {
            final boolean succeeded;
            final Map<String, Models.Station> stations;
            final Map<Integer, Models.City> cities;

            StationListResult (boolean succeeded, Map<Integer, Models.City> cities, Map<String, Models.Station> stations) {
                this.succeeded = succeeded;
                this.cities = cities;
                this.stations = stations;
            }
        }
        void onStationFetched(StationListResult result);
    }

    ContextWrapper contextWrapper;

    StationListFetcher(ContextWrapper wrapper) {
        this.contextWrapper = wrapper;
    }

    void fetch(final OnStationListFetchedListener listener) {

        new Thread() {
            @Override
            public void run() {
                boolean succeeded = false;
                Map<Integer, Models.City> cities;
                Map<String, Models.Station> stations = new HashMap<>();

                cities = fetchCities();
                if (cities != null) {
                    succeeded = true;
                    for (Integer cityCode : cities.keySet()) {
                        int retries = 0;
                        Map<String, Models.Station> stationMap;
                        do {
                            retries++;
                            stationMap = fetchStationsByCity(cityCode);
                        } while (stationMap == null && retries < 5);
                        if (stationMap == null) {
                            succeeded = false;
                            break;
                        }
                        stations.putAll(stationMap);
                    }
                }

                final OnStationListFetchedListener.StationListResult result = new OnStationListFetchedListener.StationListResult(succeeded, cities, stations);
                new Handler(contextWrapper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStationFetched(result);
                    }
                });
            }
        }.start();
    }

    private Map<Integer, Models.City> fetchCities() {
        HashMap<Integer, Models.City> cityHashMap = null;
        try {
            // Get request URL
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getCtyCodeList")
                            .append("?ServiceKey=F8csb9A9pHefkBCGavCVP%2BF%2BeiMb1i3vLsGV9hi1sWCcmOtNmtD5oXWN%2BGbSPxp%2B2ZORejIa9PY%2FaIlPM%2FH1vA%3D%3D")
                            .toString()
            ).openConnection();
            conn.setRequestMethod("GET");

            // Fetch from URL if succeeded
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                // Parse city list
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(conn.getInputStream(), "utf-8");
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                int eventType = parser.getEventType();

                cityHashMap = new HashMap<>();
                int cCode = -1;
                String cName = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            switch (name) {
                                case "item":
                                    cCode = -1;
                                    cName = null;
                                    break;
                                case "citycode":
                                    cCode = Integer.parseInt(parser.nextText());
                                    break;
                                case "cityname":
                                    cName = parser.nextText();
                                    break;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if ("item".equals(name))
                                cityHashMap.put(cCode, new Models.City(cName, cCode));
                            break;
                    }
                    parser.next();
                    eventType = parser.getEventType();
                }
            }

            // Finish connection
            conn.disconnect();
            Log.d(StationListFetcher.this.getClass().getName(), "Succeessfully retrieved city list!");
        } catch (Exception e) {
            e.printStackTrace();
            cityHashMap = null;
        }

        return cityHashMap;
    }

    private Map<String, Models.Station> fetchStationsByCity(@NonNull Integer cityCode) {
        HashMap<String, Models.Station> stationHashMap = null;
        try {
            // Get request URL
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getCtyAcctoTrainSttnList")
                            .append("?ServiceKey=F8csb9A9pHefkBCGavCVP%2BF%2BeiMb1i3vLsGV9hi1sWCcmOtNmtD5oXWN%2BGbSPxp%2B2ZORejIa9PY%2FaIlPM%2FH1vA%3D%3D")
                            .append("&").append(URLEncoder.encode("cityCode","UTF-8")).append("=").append(cityCode)
                            .toString()
            ).openConnection();
            conn.setRequestMethod("GET");

            // Fetch from URL if succeeded
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                // Parse city list
                XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setInput(conn.getInputStream(), null);
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                int eventType = parser.getEventType();

                stationHashMap = new HashMap<>();
                String sID = null;
                String sName = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String name = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            switch (name) {
                                case "item":
                                    sID = null;
                                    sName = null;
                                    break;
                                case "nodeid":
                                    sID = parser.nextText();
                                    break;
                                case "nodename":
                                    sName = parser.nextText();
                                    break;
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            if ("item".equals(name))
                                stationHashMap.put(sID, new Models.Station(sID, sName, cityCode));
                            break;
                    }
                    parser.next();
                    eventType = parser.getEventType();
                }
            }

            // Finish connection
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            stationHashMap = null;
        }
        return stationHashMap;
    }
}