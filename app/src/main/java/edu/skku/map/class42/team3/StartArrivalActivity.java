package edu.skku.map.class42.team3;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class StartArrivalActivity extends AppCompatActivity {

    String key =  "mCayx%2FW%2FW%2FvhZrAo7PNumFfNOjrs2Lepqx2BwnVCo8xXwmMZfjG9n8Ney5eTvI82bEuzUlAD2GGRKmv1%2BDE%2Fgw%3D%3D";
    String startplace = null;
    String finalplace = null;
    EditText edit;
    EditText edit2;
    TextView text;
    ListView mListView;
    ArrayList<Models.TrainSchedule> all = new ArrayList<>();
    XmlPullParser xpp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_arrival);
        mListView = findViewById(R.id.listview);
        StrictMode.enableDefaults();

        final Models.SearchOptions options = (Models.SearchOptions) getIntent().getSerializableExtra("options");
        final ArrayAdapter<Models.TrainSchedule> adapter
                = new ArrayAdapter<Models.TrainSchedule>(this, android.R.layout.simple_list_item_1, android.R.id.text1, all);
        mListView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Models.TrainSchedule> data = getXmlData(options.getOrigin().getStationID(), options.getDestination().getStationID());;

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
    }

    List<Models.TrainSchedule> getXmlData(String dep, String arr){

        StringBuffer buffer;
        //startplace = edit.getText().toString();
        //finalplace = edit2.getText().toString();
        //String realStart = URLEncoder.encode(startplace);
        //String realarrive = URLEncoder.encode(finalplace);
        ArrayList<Models.TrainSchedule> out = new ArrayList<>();
        try{
            URL url = new URL("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getStrtpntAlocFndTrainInfo?serviceKey="+
                    key+"&numOfRows=10&pageNo=1&"+"depPlaceId=" + dep + "&arrPlaceId=" + arr + "&depPlandTime=20190601");

            InputStream is = url.openStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;

            xpp.next();
            //parser.setInput(url.openStream(), null);

            int eventType = xpp.getEventType();

            String depTIme = null, arrTIme = null;
            int trainGrade = 0;
            while(eventType != XmlPullParser.END_DOCUMENT){

                buffer = new StringBuffer();
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 start!\n\n");
                        break;
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

                        if(tag.equals("item")) {
                            out.add(new Models.TrainSchedule(depTIme, arrTIme, 0));
                        }
                        break;
                }
               eventType = xpp.next();
            }


        } catch (Exception e){
            e.printStackTrace();
           // result.setText("Error!");
        }
        //buffer.append("파싱 끝!");
        return out;
    }
}
