package es.upv.master.android.reconocimientofacial.ui.preferences;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class PreferencesActivity extends AppCompatActivity {
    public static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new DownloadFragment())
                .commit();
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onStop() { //Stop
        super.onStop();
        this.finish();
    }

}
