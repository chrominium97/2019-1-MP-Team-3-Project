package edu.skku.map.class42.team6;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class StationSearchActivity extends AppCompatActivity {

    final Results results = new Results(new HashMap<String, Models.Station>());
    RecyclerView stationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_search);

        setTitle(R.string.title_activity_station_search);

        Intent intent = getIntent();
        TextInputLayout layout = findViewById(R.id.layout);
        final TextInputEditText editText = layout.findViewById(R.id.field);

        layout.setHint(intent.getStringExtra("hint"));
        String text = intent.getStringExtra("text");
        editText.setText(text);
        results.setFilter(text);

        // Init list
        stationList = findViewById(R.id.list);
        RecyclerView.LayoutManager manager
                = new GridLayoutManager(
                this,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 4 : 2
        );
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<StationListViewHolder>() {
            @NonNull
            @Override
            public StationListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View inflate = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                return new StationListViewHolder(inflate);
            }

            @Override
            public void onBindViewHolder(@NonNull StationListViewHolder holder, int position) {
                holder.textView.setText(results.get(position).getStationName());
            }

            @Override
            public int getItemCount() {
                return results.size();
            }
        };
        stationList.setLayoutManager(manager);
        stationList.setAdapter(adapter);
        results.setAdapter(adapter);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                results.setFilter(editText.getText().toString());
            }
        });
        editText.requestFocus();

        fetchStations();
    }

    private void fetchStations() {
        // Fetch station list
        StationFetcher.getInstance().request(new StationFetcher.OnStationListFetchedListener() {
            @Override
            public void onStationFetched(StationListResult result) {
                if (result.succeeded) {
                    results.putNewResult(result.stations);
                } else {
                    Toast.makeText(StationSearchActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }
            }

            @NonNull
            @Override
            public Looper getMainLooper() {
                return StationSearchActivity.this.getMainLooper();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_statin_options, menu);

        menu.findItem(android.R.id.closeButton).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent();
                setResult(RESULT_CANCELED, i);
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class Results {
        Map<String, Models.Station> originalMap;
        ArrayList<String> originalIndex;
        ArrayList<String> stationIndex;

        String filter;

        RecyclerView.Adapter adapter;

        Results(Map<String, Models.Station> stations) {
            putNewResult(stations);
        }

        public void putNewResult(Map<String, Models.Station> stations) {
            Log.d(StationSearchActivity.this.getClass().getName(), "New set of data set");
            this.originalMap = stations;
            this.originalIndex = new ArrayList<>(this.originalMap.keySet());

            Collections.sort(this.originalIndex, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return get(o1).getStationName().compareTo(get(o2).getStationName());
                }
            });
            this.stationIndex = new ArrayList<>(this.originalIndex);

            applyFilter();
        }

        public void setAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        private void applyFilter() {
            stationIndex.clear();
            for (String index : originalIndex) {
                if (get(index).getStationName().contains(filter)) {
                    stationIndex.add(index);
                }
            }
            if (!"".equals(filter)) {
                Collections.sort(stationIndex, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        String s1 = get(o1).getStationName(), s2 = get(o2).getStationName();
                        boolean b1 = s1.startsWith(filter);
                        boolean b2 = s2.startsWith(filter);
                        if (b1 ^ b2) {
                            return b1 ? -1 : 1;
                        }
                        return s1.compareTo(s2);
                    }
                });
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        public void setFilter(String s) {
            this.filter = s;
            applyFilter();
        }

        public Models.Station get(int position) {
            return originalMap.get(stationIndex.get(position));
        }

        public Models.Station get(String key) {
            return originalMap.get(key);
        }

        public int size() {
            return stationIndex.size();
        }
    }

    class StationListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView textView;

        public StationListViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
            textView = v.findViewById(android.R.id.text1);
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            i.putExtra("station", results.get(getAdapterPosition()));
            setResult(RESULT_OK, i);
            finish();
        }
    }
}