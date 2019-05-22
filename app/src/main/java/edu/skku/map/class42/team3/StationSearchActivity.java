package edu.skku.map.class42.team3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;

public class StationSearchActivity extends AppCompatActivity {

    enum Station implements Serializable {
        SUWON
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station_search);

        Intent intent = getIntent();
        TextInputLayout layout = findViewById(R.id.layout);
        TextInputEditText editText = layout.findViewById(R.id.field);

        layout.setHint(intent.getStringExtra("hint"));
        editText.setText(intent.getStringExtra("text"));

        RecyclerView stationList = findViewById(R.id.list);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 2);
        stationList.setLayoutManager(manager);

        layout.setError("aaaa");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_statin_options, menu);

        menu.findItem(android.R.id.closeButton).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent();
                i.putExtra("station", Station.SUWON);
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
}
