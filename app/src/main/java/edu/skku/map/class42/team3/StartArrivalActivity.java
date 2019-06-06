package edu.skku.map.class42.team3;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class StartArrivalActivity extends AppCompatActivity {

    String key =  "mCayx%2FW%2FW%2FvhZrAo7PNumFfNOjrs2Lepqx2BwnVCo8xXwmMZfjG9n8Ney5eTvI82bEuzUlAD2GGRKmv1%2BDE%2Fgw%3D%3D";
    String startplace = null;
    String finalplace = null;
    EditText edit;
    EditText edit2;
    TextView text;
    XmlPullParser xpp;
    String data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_arrival);

        StrictMode.enableDefaults();

        Models.SearchOptions options = (Models.SearchOptions) getIntent().getSerializableExtra("options");

        getXmlData(options.getOrigin().getStationID(), options.getDestination().getStationID());

        //text = (TextView)findViewById(R.id.result);
        //edit = (EditText)findViewById(R.id.edit);
        //edit2 = (EditText)findViewById(R.id.edit2);
        //String key =  "mCayx%2FW%2FW%2FvhZrAo7PNumFfNOjrs2Lepqx2BwnVCo8xXwmMZfjG9n8Ney5eTvI82bEuzUlAD2GGRKmv1%2BDE%2Fgw%3D%3D";

    }

    public void mOnClick(View v){
        switch (v.getId()){
            case R.id.button:

            new Thread(new Runnable() {
                @Override
                public void run() {
                    data = getXmlData();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(data);
                        }
                    });
                }
            }).start();
            break;
        }
    }

    String getXmlData() {
        return getXmlData("NAT010000", "NAT011668");
    }

    String getXmlData(String dep, String arr){

        /*boolean initem = false, inadultcharge = false, inarrplacename = false;
        boolean inarrplandtime =  false, indepplacename = false, indepplandtime = false;
        boolean intraingradename = false;

        String adultcharge = null, arrplacename = null, arrplandtime = null;
        String depplandtime = null, depplacename = null, traingradename = null;*/
        StringBuffer buffer = new StringBuffer();
        startplace = edit.getText().toString();
        finalplace = edit2.getText().toString();
        String realStart = URLEncoder.encode(startplace);
        String realarrive = URLEncoder.encode(finalplace);
        try{
            URL url = new URL("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getStrtpntAlocFndTrainInfo?serviceKey="+
                    key+"&numOfRows=10&pageNo=1&"+"depPlaceId=" + dep + "&arrPlaceId=" + arr + "&depPlandTime=20190601&trainGradeCode=00");

            InputStream is = url.openStream();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;

            xpp.next();
            //parser.setInput(url.openStream(), null);

            int eventType = xpp.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 start!\n\n");
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        if(tag.equals("item"));
                        else if(tag.equals("adultcharge")){
                            buffer.append("요금 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        else if(tag.equals("arrplacename")){
                            buffer.append("도착지 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        else if(tag.equals("arrplandtime")){
                            buffer.append("도착시각 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        else if(tag.equals("depplacename")){
                            buffer.append("출발지: ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        else if(tag.equals("depplandtime")){
                            buffer.append("출발시각 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        if(tag.equals("traingradename")){
                            buffer.append("열차종류 : ");
                            xpp.next();
                            buffer.append(xpp.getText());
                            buffer.append("\n");
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();

                        if(tag.equals("item")) buffer.append("\n");
                        break;
                }
               eventType = xpp.next();
            }


        } catch (Exception e){
            e.printStackTrace();
           // result.setText("Error!");
        }
        //buffer.append("파싱 끝!");
        return buffer.toString();
    }
}
