package edu.skku.map.class42.team6;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class CalendarManager {

    final ScheduleChangeListener listener;

    CalendarManager(ScheduleChangeListener listener) {
        this.listener = listener;
    }

    public void initCalendar() {
        SharedPreferences manager = listener.getContext().getSharedPreferences("calendar", MODE_PRIVATE);
        String uri = manager.getString("uri", "");
        if ("".equals(uri)) {

            ContentValues values = new ContentValues();
            values.put(
                    Calendars.ACCOUNT_NAME,
                    "Korail");
            values.put(
                    Calendars.ACCOUNT_TYPE,
                    CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(
                    Calendars.NAME,
                    "Train Calendar");
            values.put(
                    Calendars.CALENDAR_DISPLAY_NAME,
                    "Train Calendar");
            values.put(
                    Calendars.CALENDAR_COLOR,
                    0xffff0000);
            values.put(
                    Calendars.CALENDAR_ACCESS_LEVEL,
                    Calendars.CAL_ACCESS_OWNER);
            values.put(
                    Calendars.CALENDAR_TIME_ZONE,
                    "Asia/Seoul");
            values.put(
                    Calendars.SYNC_EVENTS,
                    1);
            Uri.Builder builder =
                    Calendars.CONTENT_URI.buildUpon();
            builder.appendQueryParameter(
                    Calendars.ACCOUNT_NAME,
                    "edu.skku.class42.team6");
            builder.appendQueryParameter(
                    Calendars.ACCOUNT_TYPE,
                    CalendarContract.ACCOUNT_TYPE_LOCAL);
            builder.appendQueryParameter(
                    CalendarContract.CALLER_IS_SYNCADAPTER,
                    "true");
            manager.edit()
                    .putString("uri", listener.getContext().getContentResolver().insert(builder.build(), values).toString())
                    .commit();
        }
    }

    public void deleteCalendar() {
        SharedPreferences manager = listener.getContext().getSharedPreferences("calendar", MODE_PRIVATE);
        String uri = manager.getString("uri", "");
        if (!"".equals(uri)) {
            listener.getContext().getContentResolver().
                    delete(Uri.parse(uri), "", new String[]{});
        }
        manager.edit().remove("uri").commit();
    }

    public void addSchedule(Models.TrainSchedule schedule) {
        Map<String, String> s = new HashMap<>();
        s.put("arr", schedule.getArrivingStation());
        s.put("dep", schedule.getDepartingStation());
        s.put("arrTime", schedule.getArrivalTime());
        s.put("type", schedule.getTrainType());

        FirebaseDatabase.getInstance().getReference(
                FirebaseAuth.getInstance().getUid()
        ).child(schedule.getDepartureTime())
                .setValue(s);
    }

    public void removeSchedule(Models.TrainSchedule schedule) {
        FirebaseDatabase.getInstance().getReference(
                FirebaseAuth.getInstance().getUid()
        ).child(schedule.getDepartureTime())
                .removeValue();
    }

    public void refreshSchedules() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(FirebaseAuth.getInstance().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Map<String, String>> out = (Map<String, Map<String, String>>) dataSnapshot.getValue();
                listener.onScheduleChanged(out);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    interface ScheduleChangeListener {
        void onScheduleChanged(Map<String, Map<String, String>> newSchedule);

        @NonNull
        Context getContext();
    }
}
