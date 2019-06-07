package edu.skku.map.class42.team6;

import android.os.Bundle;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartArrivalActivity extends AppCompatActivity {

    ListView mListView;
    ArrayList<Models.TrainSchedule> all = new ArrayList<>();
    XmlPullParser xpp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_arrival);
        mListView = findViewById(R.id.listview);

        final Models.SearchOptions options = (Models.SearchOptions) getIntent().getSerializableExtra("options");
        final ArrayAdapter<Models.TrainSchedule> adapter
                = new ArrayAdapter<Models.TrainSchedule>(this, android.R.layout.simple_list_item_1, android.R.id.text1, all);
        mListView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Models.TrainSchedule> data = getXmlData(options.getOrigin().getStationID(), options.getDestination().getStationID());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        all.clear();
                        all.addAll(data);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

        VehicleFetcher.getInstance().request(new VehicleFetcher.OnVehicleListFetchedListener() {
            @Override
            public void onVehicleFetched(VehicleListResult result) {
                ChipGroup group = findViewById(R.id.chip_group);
                for (Models.Vehicle vehicle : result.vehicles.values()) {
                    Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip, group, false);
                    chip.setText(vehicle.getVehicleName());
                    group.addView(chip);
                }
            }

            @NonNull
            @Override
            public Looper getMainLooper() {
                return StartArrivalActivity.this.getMainLooper();
            }
        });
    }

    List<Models.TrainSchedule> getXmlData(String dep, String arr) {

        ArrayList<Models.TrainSchedule> out = new ArrayList<>();
        try {
            HttpURLConnection conn
                    = (HttpURLConnection) new URL("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getStrtpntAlocFndTrainInfo?serviceKey=" +
                    Models.API_KEY + "&numOfRows=10&pageNo=1&" + "depPlaceId=" + dep + "&arrPlaceId=" + arr + "&depPlandTime=20190601").openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                builder.append(buffer);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(builder.toString()));

            String tag;

            xpp.next();
            //parser.setInput(url.openStream(), null);

            int eventType = xpp.getEventType();

            String depTIme = null, arrTIme = null;
            int trainGrade = 0;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        switch (tag) {
                            case "arrplandtime":
                                xpp.next();
                                arrTIme = xpp.getText();
                                break;
                            case "depplandtime":
                                xpp.next();
                                depTIme = xpp.getText();
                                break;
                            case "traingradename":
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();

                        if (tag.equals("item")) {
                            out.add(new Models.TrainSchedule(depTIme, arrTIme, 0));
                        }
                        break;
                }
                eventType = xpp.next();
            }


        } catch (Exception e) {
            e.printStackTrace();
            // result.setText("Error!");
        }
        //buffer.append("파싱 끝!");
        return out;
    }
}
