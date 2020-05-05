package es.upv.master.android.reconocimientofacial;

import androidx.appcompat.app.AppCompatActivity;
import es.upv.master.android.reconocimientofacial.label.LabelActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View v){
        //Intent i = new Intent(this, RecognitionActivity.class);
        Intent i = new Intent(this, LabelActivity.class);
        startActivity(i);
    }
}
