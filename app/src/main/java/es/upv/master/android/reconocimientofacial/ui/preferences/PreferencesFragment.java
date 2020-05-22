package es.upv.master.android.reconocimientofacial.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import es.upv.master.android.reconocimientofacial.R;

import static es.upv.master.android.reconocimientofacial.ui.MainActivity.prefs;

public class PreferencesFragment extends PreferenceFragment {
    private SwitchPreference passwordSwitch;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        passwordSwitch = (SwitchPreference) findPreference("passwordSwitch");
        if(!prefs.getBoolean("password", false)){
            passwordSwitch.setChecked(false);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("password", true);
            editor.commit();
        }

    }
}
