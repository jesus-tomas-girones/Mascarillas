package es.upv.master.android.reconocimientofacial;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.jar.Attributes;

import es.upv.master.android.reconocimientofacial.ui.MainActivity;
import es.upv.master.android.reconocimientofacial.ui.label.ListLabelActivity;

import static java.lang.String.format;

public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private int valor = 3, numOptions;
    private ListPreference listDownloadPhotos, listDownloadLabels;
    private DatePreference inicialDate, finalDate;
    private static final int SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE = 0;
    private EditTextPreference register_last_download;
    private final long HOUR_MS = 3600000;
    private Date timeDownload, currentDate;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);

        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = new Date(System.currentTimeMillis());
        String dateString = formatoFecha.format(currentDate);

        listDownloadPhotos= (ListPreference) findPreference("download_photo");
        listDownloadPhotos.setOnPreferenceChangeListener(this);
        listDownloadLabels= (ListPreference) findPreference("download_label");
        listDownloadLabels.setOnPreferenceChangeListener(this);

        inicialDate = (DatePreference) findPreference("pref_inicial_date");
        inicialDate.setOnPreferenceChangeListener(this);
        inicialDate.setSummary(dateString);
        finalDate = (DatePreference) findPreference("pref_final_date");
        finalDate.setOnPreferenceChangeListener(this);
        finalDate.setSummary(dateString);

        register_last_download = (EditTextPreference) findPreference("register_download");
        register_last_download.setText("última descarga: ");
        register_last_download.setSelectable(false);

        almacenamientoEnMemoria();
        if(!almacenamientoEnMemoria()){
             hayPermisoAlmacenamientoExterno();
        }

    }

    public boolean almacenamientoEnMemoria(){
        if(!hayPermisoAlmacenamientoExterno()){
            solicitarPermiso(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Sin el permiso"+ " de almacenamiento externo no puede escribir o leer las puntuaciones en memoria.",
                    SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE, getActivity());
        }
        return hayPermisoAlmacenamientoExterno();
    }

    public static void solicitarPermiso(final String permiso, String justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)){
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode); }}).show();

        }else {
            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
        }
    }

    public boolean hayPermisoAlmacenamientoExterno() {
        return (ActivityCompat.checkSelfPermission(
                this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { almacenamientoEnMemoria();} else {
                Toast.makeText(getActivity(), "Sin el permiso, no puedo realizar la " + "acción", Toast.LENGTH_SHORT).show(); }
        }
    }*/

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean res = false;
        Date time;
        switch (preference.getKey()){
            case "download_photo":
                numOptions = Integer.parseInt((String)newValue);
                switch (numOptions){
                    case 0:
                        time = new Date(System.currentTimeMillis()-24*HOUR_MS);
                        break;
                    case 1:
                        time = new Date(System.currentTimeMillis()-3*24*HOUR_MS);
                        break;
                    case 2:
                        time = new Date(System.currentTimeMillis()-7*24*HOUR_MS);
                        break;
                    case 3:
                        time = new Date(System.currentTimeMillis()-15*24*HOUR_MS);
                        break;
                    case 4:
                        showDatePickerDialog();
                        time = timeDownload;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + numOptions);
                }
                if(time != null)
                register_last_download.setSummary("última descarga: "+dateFormat(time));
                res = true;
                break;
            case "download_label":
                break;
            case "pref_inicial_date":
                inicialDate.setSummary((String) newValue);
                break;
        }
        return res;
    }

    private String dateFormat(Date date){
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatoFecha.format(date);
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private Calendar calendar;
        private DatePickerDialog.OnDateSetListener onDateSetListener;

        public DatePickerFragment(DatePickerDialog.OnDateSetListener onDateSetListener) {
            this.onDateSetListener = onDateSetListener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment(new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar  calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                timeDownload = calendar.getTime();
                long time = timeDownload.getTime();
                register_last_download.setSummary("última descarga: "+calendar.getTime().toString());
            }
        });
        newFragment.show(PreferencesActivity.fragmentManager, "datePicker");
    }

}
