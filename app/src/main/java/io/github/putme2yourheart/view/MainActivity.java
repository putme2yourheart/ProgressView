package io.github.putme2yourheart.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ProgressView mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressView = (ProgressView) findViewById(R.id.progress);
        mProgressView.setOnFinishListener(new OnFinishListener() {
            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, "finish", Toast.LENGTH_SHORT).show();
            }
        });
        mProgressView.setMax(100);

        new Thread() {
            int p = 0;
            @Override
            public void run() {
                while (p <= 100) {
                    mProgressView.setProgress(p++);
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
