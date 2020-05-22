package es.upv.mastermoviles.intemasc.captura.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import es.upv.mastermoviles.intemasc.captura.R;

public class PreferencesActivity extends AppCompatActivity {
    public static FragmentManager fragmentManager;
    private String type_preferences_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        PreferenceFragment preferenceFragment;
        Bundle extras = getIntent().getExtras();
        type_preferences_fragment = extras.getString("type_preferences");
        if(type_preferences_fragment.equals("download")){
            preferenceFragment = new DownloadFragment();
            String title = getString(R.string.menu_download);
            this.setTitle(title);
        }else{
            preferenceFragment = new PreferencesFragment();
        }
          getFragmentManager().beginTransaction()
                .replace(android.R.id.content, preferenceFragment)
                .commit();
        fragmentManager = getSupportFragmentManager();

    }

    @Override
    public void onStop() { //Stop
        super.onStop();
        this.finish();
    }

}
