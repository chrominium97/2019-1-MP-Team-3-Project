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

class VehicleFetcher {

    private static VehicleFetcher instance = null;

    private final LinkedBlockingQueue<OnVehicleListFetchedListener> listeners = new LinkedBlockingQueue<>();
    private OnVehicleListFetchedListener.VehicleListResult result = null;

    private VehicleFetcher() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final OnVehicleListFetchedListener listener = listeners.poll(Long.MAX_VALUE, TimeUnit.DAYS);
                        if (result == null || !result.succeeded) {
                            fetch();
                        }
                        new Handler(listener.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onVehicleFetched(result);
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.d(VehicleFetcher.this.getClass().getName(), "Restarting poll...");
                    }
                }
            }
        }.start();
    }

    public static VehicleFetcher getInstance() {
        return instance == null ? (instance = new VehicleFetcher()) : instance;
    }

    public void request(OnVehicleListFetchedListener listener) {
        listeners.offer(listener);
    }

    private void fetch() {
        Map<Integer, Models.Vehicle> vehicleMap = fetchVehicle();
        int r1 = 0;
        while (r1 < 5 && (vehicleMap= fetchVehicle()) == null) {
            r1++;
        }
        result = new OnVehicleListFetchedListener.VehicleListResult(vehicleMap != null, vehicleMap);
    }

    private HashMap<Integer, Models.Vehicle> fetchVehicle() {
        HashMap<Integer, Models.Vehicle> vehicleHashMap = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getVhcleKndList")
                            .append("?ServiceKey=").append(Models.API_KEY)
                            .toString()
            ).openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() < 200 || conn.getResponseCode() > 300) {
                conn.disconnect();
                return vehicleHashMap;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                builder.append(buffer);
            }

            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

            vehicleHashMap = new HashMap<>();
            int vID = -1;
            String vName = null;

            parser.setInput(new StringReader(builder.toString()));
            int parserEvent = parser.getEventType();
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        switch (name) {
                            case "vehiclekndid":
                                vID = Integer.valueOf(parser.nextText());
                                break;
                            case "vehiclekndnm":
                                vName = parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("item".equals(parser.getName())) {
                            vehicleHashMap.put(vID, new Models.Vehicle(vID, vName));
                        }
                        break;
                }
                parserEvent = parser.next();
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            vehicleHashMap = null;
        }
        return vehicleHashMap;
    }

    interface OnVehicleListFetchedListener {
        void onVehicleFetched(VehicleListResult result);

        @NonNull
        Looper getMainLooper();

        class VehicleListResult {
            final boolean succeeded;
            final Map<Integer, Models.Vehicle> vehicles;

            VehicleListResult(boolean succeeded, Map<Integer, Models.Vehicle> vehicles) {
                this.succeeded = succeeded;
                this.vehicles = vehicles;
            }
        }
    }
}

