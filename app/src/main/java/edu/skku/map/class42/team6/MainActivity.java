package edu.skku.map.class42.team6;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_DEP = 1;
    public static final int REQUEST_ARR = 2;
    Models.SearchOptions searchOptions;
    TextInputEditText input_arrival;
    TextInputEditText input_departure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchOptions = new Models.SearchOptions(Calendar.getInstance(), Models.SearchOptions.SearchMode.BY_DEPARTURE);

        final TextView by = findViewById(R.id.tv_search_type);

        TabLayout layout = findViewById(R.id.by_option);
        layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        searchOptions.setMode(Models.SearchOptions.SearchMode.BY_DEPARTURE);
                        by.setText("부터");
                        break;
                    case 1:
                        searchOptions.setMode(Models.SearchOptions.SearchMode.BY_ARRIVAL);
                        by.setText("까지");
                        break;
                }
                refreshOptions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        input_arrival = findViewById(R.id.select_arrival);
        input_arrival.setOnClickListener(this);
        input_departure = findViewById(R.id.select_departure);
        input_departure.setOnClickListener(this);

        refreshOptions();

        StationFetcher.getInstance().request(new StationFetcher.OnStationListFetchedListener() {
            @Override
            public void onStationFetched(StationListResult result) {
                Log.i(MainActivity.this.getClass().getName(), "Station pre-fetched.");
            }

            @NonNull
            @Override
            public Looper getMainLooper() {
                return MainActivity.this.getMainLooper();
            }
        });
        VehicleFetcher.getInstance().request(new VehicleFetcher.OnVehicleListFetchedListener() {
            @Override
            public void onVehicleFetched(VehicleListResult result) {
                Log.i(MainActivity.this.getClass().getName(), "Vehicles pre-fetched.");
            }

            @NonNull
            @Override
            public Looper getMainLooper() {
                return MainActivity.this.getMainLooper();
            }
        });

        CalendarManager manager = new CalendarManager(new CalendarManager.ScheduleChangeListener() {
            @Override
            public void onScheduleChanged(Map<String, Map<String, String>> newSchedule) {

            }

            @NonNull
            @Override
            public Context getContext() {
                return MainActivity.this;
            }
        });
        manager.deleteCalendar();
        manager.initCalendar();
        manager.refreshSchedules();
    }

    public void selectDate(View v) {
        final Calendar calendar = searchOptions.getDateTime();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                searchOptions.setDate(year, month, dayOfMonth);
                refreshOptions();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void selectTime(View v) {
        final Calendar calendar = searchOptions.getDateTime();
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                searchOptions.setTime(hourOfDay, minute);
                refreshOptions();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    void refreshOptions() {
        TextView date = findViewById(R.id.date);
        TextView time = findViewById(R.id.time);
        TextView mode = findViewById(R.id.tv_search_type);
        TabLayout tabs = findViewById(R.id.by_option);

        date.setText(SimpleDateFormat.getDateInstance().format(searchOptions.getDateTime().getTime()));
        time.setText((new SimpleDateFormat("HH:mm")).format(searchOptions.getDateTime().getTime()));
        mode.setText(tabs.getSelectedTabPosition() == 0 ? getString(R.string.from) : getString(R.string.until));
    }

    @Override
    public void onClick(View v) {
        int reqCode;
        switch (v.getId()) {
            case R.id.select_departure:
                reqCode = REQUEST_DEP;
                break;
            case R.id.select_arrival:
                reqCode = REQUEST_ARR;
                break;
            default:
                return;
        }

        TextInputEditText editText = (TextInputEditText) v;
        CharSequence hint = editText.getHint();
        Editable text = editText.getText();
        Intent intent = new Intent(this, StationSearchActivity.class)
                .putExtra("hint", hint.toString())
                .putExtra("text", text.toString());
        startActivityForResult(intent, reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Models.Station station;
        if (data == null || (station = (Models.Station) data.getSerializableExtra("station")) == null) {
            return;
        }
        switch (requestCode) {
            case REQUEST_DEP:
                input_departure.setText(station.getStationName());
                searchOptions.setDeparture(station);
                break;
            case REQUEST_ARR:
                input_arrival.setText(station.getStationName());
                searchOptions.setArrival(station);
                break;
        }
        Log.e(this.getLocalClassName(), station.toString() + requestCode);
    }

    public void startSearch(View v) {
        if (searchOptions.checkValidity()) {
            Intent intent = new Intent(MainActivity.this, StartArrivalActivity.class)
                    .putExtra("options", searchOptions);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Some required fields are not filled!", Toast.LENGTH_SHORT).show();
        }
    }
}
