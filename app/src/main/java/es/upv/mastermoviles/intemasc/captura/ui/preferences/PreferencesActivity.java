package es.upv.mastermoviles.intemasc.captura.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import es.upv.mastermoviles.intemasc.captura.R;

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

}
