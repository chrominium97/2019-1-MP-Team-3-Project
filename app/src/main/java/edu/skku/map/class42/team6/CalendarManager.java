package edu.skku.map.class42.team6;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class CalendarManager {

    private static final String SHARED_PREFS_NAME = "calendar";
    private static final String SHARED_PREFS_UID_KEY = "uid";

    private final Context context;

    CalendarManager(Context context) {
        this.context = context;
    }

    public void initCalendar() {
        SharedPreferences manager = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        int uid = manager.getInt(SHARED_PREFS_UID_KEY, -1);
        if (uid == -1) {
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
            Cursor cursor = context.getContentResolver().query(
                    context.getContentResolver().insert(builder.build(), values),
                    new String[]{Calendars._ID},
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                manager.edit()
                        .putInt(SHARED_PREFS_UID_KEY, cursor.getInt(0))
                        .commit();
            }
            cursor.close();
        }
    }

    public void deleteCalendar() {
        SharedPreferences manager = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        int uid = manager.getInt(SHARED_PREFS_UID_KEY, -1);
        int result;
        if (uid != -1) {
            result = context.getContentResolver().
                    delete(Calendars.CONTENT_URI, CalendarContract.Events.ACCOUNT_NAME + "=?", new String[]{"Korail"});
            Log.d("CAL", String.valueOf(result));
        }
        manager.edit().remove(SHARED_PREFS_UID_KEY).commit();
    }

    public void clearCalendar() {
        deleteCalendar();
        initCalendar();
    }

    public void insertScheduleToCalendar(Models.TrainSchedule schedule) {
        SharedPreferences manager = context.getSharedPreferences("calendar", MODE_PRIVATE);
        int uid = manager.getInt(SHARED_PREFS_UID_KEY, -1);
        String from = schedule.getDepartureTime();
        String to = schedule.getArrivalTime();
        try {
            if (uid != -1) {
                ContentValues values = new ContentValues();
                values.put(Events.DTSTART, new SimpleDateFormat("yyyyMMddHHmmss").parse(from).getTime());
                values.put(Events.DTEND, new SimpleDateFormat("yyyyMMddHHmmss").parse(to).getTime());
//        values.put(Events.RRULE,
//                "FREQ=DAILY;COUNT=20;BYDAY=MO,TU,WE,TH,FR;WKST=MO");
                values.put(Events.TITLE, schedule.getDepartingStation() + "→" + schedule.getArrivingStation() + "(" + schedule.getTrainType() + ")");
                values.put(Events.EVENT_LOCATION, schedule.getDepartingStation() + "역");
                values.put(Events.CALENDAR_ID, uid);
                values.put(Events.EVENT_TIMEZONE, "Asia/Seoul");
//        values.put(Events.DESCRIPTION,
//                "The agenda or some description of the event");
// reasonable defaults exist:
                values.put(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE);
                values.put(Events.SELF_ATTENDEE_STATUS,
                        Events.STATUS_CONFIRMED);
                values.put(Events.ALL_DAY, 0);
//        values.put(Events.ORGANIZER, "some.mail@some.address.com");
                values.put(Events.GUESTS_CAN_INVITE_OTHERS, 1);
                values.put(Events.GUESTS_CAN_MODIFY, 1);
                values.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);

                Log.d("CAL", context.getContentResolver()
                        .insert(Events.CONTENT_URI, values).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            Cursor cursor = context.getContentResolver().query(
//                    Events.CONTENT_URI,
//                    new String[]{Calendars._ID},
//                    Calendars.ACCOUNT_NAME + "=? AND " + Calendars.NAME + "=?",
//                    new String[]{"Korail", "Train Calendar"},
//                    null);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void insertSchedule(Models.TrainSchedule schedule) {
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

    public void refreshSchedulesOnce(final ScheduleChangeListener listener) {
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

    public void refreshSchedules(final ScheduleChangeListener listener) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(FirebaseAuth.getInstance().getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Map<String, String>> out = (Map<String, Map<String, String>>) dataSnapshot.getValue();
                listener.onScheduleChanged(out == null ? new HashMap<String, Map<String, String>>() : out);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    interface ScheduleChangeListener {
        void onScheduleChanged(Map<String, Map<String, String>> newSchedule);
    }
}
