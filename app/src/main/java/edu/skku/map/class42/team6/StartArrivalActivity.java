package edu.skku.map.class42.team6;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class StartArrivalActivity extends AppCompatActivity {

    private CalendarManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Models.SearchOptions options = (Models.SearchOptions) getIntent().getSerializableExtra("options");
        manager = new CalendarManager(StartArrivalActivity.this);

        setContentView(R.layout.activity_start_arrival);

        setTitle(options.getOrigin().getStationName() + "→" + options.getDestination().getStationName());


        final RecyclerView list = findViewById(R.id.listview);
        list.setLayoutManager(new LinearLayoutManager(this));

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Models.TrainSchedule> data = getXmlData(options);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (data == null) {
                            Toast.makeText(StartArrivalActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        }
                        final RecyclerView.Adapter adapter = data == null
                                ? new SearchListAdapter(new ArrayList<Models.TrainSchedule>(), options)
                                : new SearchListAdapter(data, options);
                        list.setAdapter(adapter);
                        manager.refreshSchedulesOnce(new CalendarManager.ScheduleChangeListener() {
                            @Override
                            public void onScheduleChanged(Map<String, Map<String, String>> newSchedule) {

                                manager.refreshSchedules(new CalendarManager.ScheduleChangeListener() {
                                    @Override
                                    public void onScheduleChanged(Map<String, Map<String, String>> newSchedule) {
                                        manager.initCalendar();
                                        manager.clearCalendar();
                                        List<Models.TrainSchedule> schedules = new ArrayList<>();
                                        for (String s : newSchedule.keySet()) {
                                            Map<String, String> k = newSchedule.get(s);
                                            Models.TrainSchedule schedule = new Models.TrainSchedule(
                                                    s,
                                                    k.get("arrTime"),
                                                    k.get("type"),
                                                    k.get("dep"),
                                                    k.get("arr")
                                            );
                                            schedules.add(schedule);
                                            manager.insertScheduleToCalendar(schedule);
                                        }
                                        ((SearchListAdapter) adapter).setButtonFilters(schedules);
                                    }
                                });

                                VehicleFetcher.getInstance().request(new VehicleFetcher.OnVehicleListFetchedListener() {
                                    @Override
                                    public void onVehicleFetched(VehicleListResult result) {
                                        ChipGroup group = findViewById(R.id.chip_group);
                                        List<Models.Vehicle> vehicleList = new ArrayList<>(result.vehicles.values());
                                        Collections.sort(vehicleList, new Comparator<Models.Vehicle>() {
                                            @Override
                                            public int compare(Models.Vehicle o1, Models.Vehicle o2) {
                                                return o1.getVehicleName().compareTo(o2.getVehicleName());
                                            }
                                        });
                                        for (Models.Vehicle vehicle : vehicleList) {
                                            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip, group, false);
                                            chip.setText(vehicle.getVehicleName());
                                            chip.setChecked(true);
                                            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                    if (isChecked) {
                                                        ((SearchListAdapter) adapter).removeFilter(buttonView.getText().toString());
                                                    } else {
                                                        ((SearchListAdapter) adapter).addFilter(buttonView.getText().toString());
                                                    }
                                                }
                                            });
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
                        });
                    }
                });
            }
        }).start();
    }

    private List<Models.TrainSchedule> getXmlData(Models.SearchOptions options) {

        ArrayList<Models.TrainSchedule> out = new ArrayList<>();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(
                    new StringBuilder("http://openapi.tago.go.kr/openapi/service/TrainInfoService/getStrtpntAlocFndTrainInfo")
                            .append("?serviceKey=").append(Models.API_KEY)
                            .append("&numOfRows=100&pageNo=1")
                            .append("&depPlaceId=").append(options.getOrigin().getStationID())
                            .append("&arrPlaceId=").append(options.getDestination().getStationID())
                            .append("&depPlandTime=").append(new SimpleDateFormat("yyyyMMdd").format(options.getDateTime().getTime()))
                            .toString()
            ).openConnection();
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

            int eventType = xpp.getEventType();

            String depTIme = null, arrTIme = null, trainType = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName();
                        switch (tag) {
                            case "arrplandtime":
                                arrTIme = xpp.nextText();
                                break;
                            case "depplandtime":
                                depTIme = xpp.nextText();
                                break;
                            case "traingradename":
                                trainType = xpp.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();

                        if (tag.equals("item")) {
                            out.add(new Models.TrainSchedule(
                                    depTIme,
                                    arrTIme,
                                    trainType,
                                    options.getOrigin().getStationName(),
                                    options.getDestination().getStationName())
                            );
                        }
                        break;
                }
                eventType = xpp.next();
            }


        } catch (Exception e) {
            e.printStackTrace();
            out = null;
            // result.setText("Error!");
        }
        //buffer.append("파싱 끝!");
        return out;
    }

    @SuppressWarnings("WeakerAccess")
    class SearchListAdapter extends RecyclerView.Adapter<SearchListViewHolder> {

        final List<Models.TrainSchedule> schedules;
        final List<Models.TrainSchedule> originalSchedules;
        final Models.SearchOptions options;
        final List<String> filters = new ArrayList<>();
        List<Models.TrainSchedule> alreadyAddedSchedules = new ArrayList<>();

        SearchListAdapter(final List<Models.TrainSchedule> s, Models.SearchOptions displayOptions) {
            this.originalSchedules = new ArrayList<>();
            this.options = displayOptions;
            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(displayOptions.getDateTime().getTime());
            if (options.getMode() == Models.SearchOptions.SearchMode.BY_ARRIVAL) {
                for (Models.TrainSchedule schedule : s) {
                    if (schedule.getArrivalTime().compareTo(date) <= 0) {
                        originalSchedules.add(schedule);
                    }
                }
                Collections.sort(originalSchedules, new Comparator<Models.TrainSchedule>() {
                    @Override
                    public int compare(Models.TrainSchedule o1, Models.TrainSchedule o2) {
                        return o2.getDepartureTime().compareTo(o1.getDepartureTime());
                    }
                });
            } else {
                for (Models.TrainSchedule schedule : s) {
                    if (schedule.getDepartureTime().compareTo(date) >= 0) {
                        originalSchedules.add(schedule);
                    }
                }
                Collections.sort(originalSchedules, new Comparator<Models.TrainSchedule>() {
                    @Override
                    public int compare(Models.TrainSchedule o1, Models.TrainSchedule o2) {
                        return o1.getArrivalTime().compareTo(o2.getArrivalTime());
                    }
                });
            }
            this.schedules = new ArrayList<>(originalSchedules);
        }

        @NonNull
        @Override
        public SearchListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_layout, parent, false);
            return new SearchListViewHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchListViewHolder holder, int position) {
            Models.TrainSchedule schedule = schedules.get(position);
            holder.trainType.setText(schedule.getTrainType());
            holder.trainTime.setText(schedule.getDepartureTime().substring(8, 12) + " ~ " + schedule.getArrivalTime().substring(8, 12));
            holder.schedule = schedule;
            holder.isAdded = alreadyAddedSchedules.contains(schedule);
            holder.button.setText(holder.isAdded ? "Added" : "Add");
        }

        @Override
        public int getItemCount() {
            return schedules.size();
        }

        public void setButtonFilters(List<Models.TrainSchedule> newSchedule) {
            this.alreadyAddedSchedules = newSchedule;
            notifyDataSetChanged();
        }

        public void addFilter(String s) {
            filters.add(s);
            applyFilter();
        }

        public void removeFilter(String s) {
            filters.remove(s);
            applyFilter();
        }

        public void setFilters(List<String> newFilters) {
            filters.clear();
            filters.addAll(newFilters);
            applyFilter();
        }

        private void applyFilter() {
            schedules.clear();
            for (Models.TrainSchedule schedule : originalSchedules) {
                if (!filters.contains(schedule.getTrainType())) {
                    schedules.add(schedule);
                }
            }
            this.notifyDataSetChanged();
        }
    }

    class SearchListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView trainType;
        public final TextView trainTime;
        public final Button button;

        public boolean isAdded = false;

        public Models.TrainSchedule schedule;

        public SearchListViewHolder(View v) {
            super(v);
            trainType = v.findViewById(R.id.trainType);
            trainTime = v.findViewById(R.id.trainTime);
            button = v.findViewById(R.id.addButton);

            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isAdded) {
                manager.removeSchedule(schedule);
            } else {
                manager.insertSchedule(schedule);
            }
        }
    }
}
