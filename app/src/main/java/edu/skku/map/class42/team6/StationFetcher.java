package edu.skku.map.class42.team6;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class StationFetcher {

    private static StationFetcher instance = null;

    private final LinkedBlockingQueue<OnStationListFetchedListener> listeners = new LinkedBlockingQueue<>();
    private OnStationListFetchedListener.StationListResult result = null;

    private StationFetcher() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final OnStationListFetchedListener listener = listeners.poll(Long.MAX_VALUE, TimeUnit.DAYS);
                        if (result == null || !result.succeeded) {
                            fetch();
                        }
                        new Handler(listener.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onStationFetched(result);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.d(StationFetcher.this.getClass().getName(), "Restarting poll...");
                    }
                }
            }
        }.start();
    }

    public static StationFetcher getInstance() {
        return instance == null? (instance = new StationFetcher()) : instance;
    }

    public void request(OnStationListFetchedListener listFetchedListener) {
        listeners.offer(listFetchedListener);
    }

    private void fetch() {
        boolean success = false;
        int r1 = 0, r2 = 0;
        Map<Integer, Models.City> cityMap = null;
        Map<String, Models.Station> stationMap = new HashMap<>();
        while (r1 < 5 && (cityMap = fetchCities()) == null) {
            r1++;
        }
        success = cityMap != null;
        if (success) {
            Log.d(StationFetcher.this.getClass().getName(), "City list fetch succeeded");
            for (Models.City city: cityMap.values()) {
                r2 = 0;
                Map<String, Models.Station> map = null;
                while (r2 < 5 && (map = fetchStationsByCity(city.getCityCode())) == null) {
                    r2++;
                }
                if (map == null) {
                    success = false;
                    break;
                }
                Log.d(StationFetcher.this.getClass().getName(), map.size() + " stations fetched from city " + city.getCityName());
                stationMap.putAll(map);
            }
        }
        result = new OnStationListFetchedListener.StationListResult(success, cityMap, stationMap);
    }

    private Map<Integer, Models.City> fetchCities() {
        HashMap<Integer, Models.City> cityHashMap = null;
        try {
            // Get request URL
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getCtyCodeList")
                            .append("?ServiceKey=").append(Models.API_KEY)
                            .toString()
            ).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 300) {
                conn.disconnect();
                return cityHashMap;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                builder.append(buffer);
            }

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

            cityHashMap = new HashMap<>();
            int cCode = -1;
            String cName = null;

            parser.setInput(new StringReader(builder.toString()));
            int parserEvent = parser.getEventType();
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        switch (name) {
                            case "citycode":
                                cCode = Integer.parseInt(parser.nextText());
                                break;
                            case "cityname":
                                cName = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("item".equals(parser.getName())) {
                            cityHashMap.put(cCode, new Models.City(cName, cCode));
                        }
                        break;
                }
                parserEvent = parser.next();
            }

            conn.disconnect();
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
                            .append("?ServiceKey=").append(Models.API_KEY)
                            .append("&cityCode=").append(cityCode)
                            .toString()
            ).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 300) {
                conn.disconnect();
                return stationHashMap;
            }

            Log.d(StationFetcher.this.getClass().getName(), "Receiving " + conn.getContentLength() + " bytes...");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                builder.append(buffer);
            }

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

            stationHashMap = new HashMap<>();
            String sID = null;
            String sName = null;

            parser.setInput(new StringReader(builder.toString()));
            int parserEvent = parser.getEventType();
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        switch (name) {
                            case "nodeid":
                                sID = parser.nextText();
                                break;
                            case "nodename":
                                sName = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("item".equals(parser.getName())) {
                            stationHashMap.put(sID, new Models.Station(sID, sName, cityCode));
                        }
                        break;
                }
                parserEvent = parser.next();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            stationHashMap = null;
        }
        return stationHashMap;
    }

    interface OnStationListFetchedListener {
        void onStationFetched(StationListResult result);

        @NonNull
        Looper getMainLooper();

        class StationListResult {
            final boolean succeeded;
            final Map<String, Models.Station> stations;
            final Map<Integer, Models.City> cities;

            StationListResult(boolean succeeded, Map<Integer, Models.City> cities, Map<String, Models.Station> stations) {
                this.succeeded = succeeded;
                this.cities = cities;
                this.stations = stations;
            }
        }
    }
}