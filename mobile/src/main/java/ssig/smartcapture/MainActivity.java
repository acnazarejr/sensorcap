package ssig.smartcapture;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btWatchManager;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btWatchManager = findViewById(R.id.btWatchManager);

        this.buttonListeners();
    }

    private void buttonListeners() {
        this.btWatchManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent watchIntent = new Intent(MainActivity.this, WatchManagerActivity.class);
                startActivity(watchIntent);
            }
        });
    }



}
