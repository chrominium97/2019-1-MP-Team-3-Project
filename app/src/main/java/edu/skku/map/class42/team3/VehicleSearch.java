package edu.skku.map.class42.team3;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.util.Map;

class VehicleSearch{

    interface OnVehicleListFetchedListener {
        int i =0;
        final String[] vehiclekndid = null;
        final String[] vehiclekndnm = null;
        void onStationFetched(StationListFetcher.OnStationListFetchedListener.StationListResult result);
    }
    private void fetchVehicle(){


        try{
            StringBuilder urlBuilder = new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getVhcleKndList"
                    + "?ServiceKey=mCayx%2FW%2FW%2FvhZrAo7PNumFfNOjrs2Lepqx2BwnVCo8xXwmMZfjG9n8Ney5eTvI82bEuzUlAD2GGRKmv1%2BDE%2Fgw%3D%3D"); /*Service Key*/
            //urlBuilder.append("&" + URLEncoder.encode("파라미터영문명","UTF-8") + "=" + URLEncoder.encode("파라미터기본값", "UTF-8")); /*파라미터설명*/

            URL url = new URL(urlBuilder.toString());
            //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setRequestMethod("GET");
            //conn.setRequestProperty("Content-type", "application/json");
            //System.out.println("Response code: " + conn.getResponseCode());


            boolean initem = false;
            boolean invehiclekndid = false, invehiclekndnm = false;




            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            //parser.setInput(url.openStream(), null);
            parser.setInput(url.openStream(),null);
            int parserEvent = parser.getEventType();
            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals("vehiclekndid")) {
                            invehiclekndid = true;
                        }
                        if (parser.getName().equals("vehiclekndnm")) {
                            invehiclekndnm = true;
                        }
                        if(parser.getName().equals("message")){
                            System.out.print("kaka");
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (invehiclekndid) {
                            vehiclekndid[i] = parser.getText();
                            invehiclekndid = false;
                        }
                        if (invehiclekndnm) {
                            vehiclekndnm[i] = parser.getText();
                            invehiclekndnm = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //if (parser.getName().equals("item")) {
                          //  System.out.printf("%s:%s", vehiclekndid, vehiclekndnm);
                            //result.setText(result.getText()+"id : "+ vehiclekndid +
                              //      "name : " + vehiclekndnm);
                           // initem = false;
                        //}
                        break;
                }
                parserEvent = parser.next();
            }
        }
        catch(Exception e){
            System.out.print("에러");
        }


    }
    protected int FetchVehicletoint(String vehicle){
        int a = 0;
        for(int j =0; j < i; i++){
            if(vehiclekndnm[j].equals(vehicle)){
                a = Integer.parseInt(vehiclekndnm[j]);
            }
        }
        return a;
    }


}

