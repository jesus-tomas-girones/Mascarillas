package es.upv.master.android.reconocimientofacial.ui.preferences;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.upv.master.android.reconocimientofacial.R;
import es.upv.master.android.reconocimientofacial.data.DataBase;
import static es.upv.master.android.reconocimientofacial.data.DataBase.DISMISS_DIALOG;
import static es.upv.master.android.reconocimientofacial.ui.MainActivity.prefs;
import static java.lang.String.format;

public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private int valor = 3, numOptions;
    private ListPreference listDownloadPhotos, listDownloadLabels;
    private DatePreference inicialDate, finalDate;
    private static final int SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE = 0;
    private EditTextPreference register_last_download;
    private final long HOUR_MS = 3600000;
    private Date timeDownload, currentDate;
    private Preference timePickerPreference;
    ArrayList<String> list_id_photos = new ArrayList<String>();
    private SwitchPreference passwordSwitch;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);

        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = new Date(System.currentTimeMillis());
        String dateString = formatoFecha.format(currentDate);
/*        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format1.format(cal.getTime());*/

        listDownloadPhotos= (ListPreference) findPreference("download_photo");
        listDownloadPhotos.setOnPreferenceChangeListener(this);
        listDownloadLabels= (ListPreference) findPreference("download_label");
        listDownloadLabels.setOnPreferenceChangeListener(this);

        inicialDate = (DatePreference) findPreference("pref_inicial_date");
        inicialDate.setOnPreferenceChangeListener(this);
        inicialDate.setSummary(dateString);
        inicialDate.onSetInitialValue(true, dateString);
        finalDate = (DatePreference) findPreference("pref_final_date");
        finalDate.setOnPreferenceChangeListener(this);
        finalDate.setSummary(dateString);
        inicialDate.onSetInitialValue(true, dateString);


        register_last_download = (EditTextPreference) findPreference("register_download");
        register_last_download.setSelectable(false);
        registerLastDownload();

        timePickerPreference = findPreference("set_time_ini");
        configListener();

        boolean switchPass = prefs.getBoolean("password", false);
/*        passwordSwitch = (SwitchPreference) findPreference("passwordSwitch");
        passwordSwitch.setOnPreferenceChangeListener(this);
        //password.setChecked(switchPass);
        passwordSwitch.setSelectable(true);*/

        list_id_photos = new ArrayList<String>();

        almacenamientoEnMemoria();
        if(!almacenamientoEnMemoria()){
            hayPermisoAlmacenamientoExterno();
        }
    }

    public boolean almacenamientoEnMemoria(){
        if(!hayPermisoAlmacenamientoExterno()){
            solicitarPermiso(Manifest.permission.WRITE_EXTERNAL_STORAGE, (String) getResources().getText(R.string.permission_extenal_storage),
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

/*    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { almacenamientoEnMemoria();} else {
                Toast.makeText(getActivity(), getResources().getText(R.string.permission_extenal_storage), Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(!hayPermisoAlmacenamientoExterno()){
            return false;
        }
        boolean res = false;
        switch (preference.getKey()){
            case "download_photo":
                numOptions = Integer.parseInt((String)newValue);
                getDateForDownload(numOptions);
                res = true;
                break;
            case "download_label":
                break;
            case "pref_inicial_date":
                inicialDate.setSummary((String) newValue);
                break;
            case "passwordSwitch":
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("password", (Boolean) newValue);
                //password.setChecked((Boolean) newValue);
                break;
        }
        return res;
    }


    private void download_photo(long dateSearch ){
        DataBase.searchLabelledPhoto(dateSearch,0,true,
                false, new DataBase.LoadLabelledPhotosListener() {
                    @Override
                    public void onLoadPhotos(List<Map<String, Object>> listLabelledPhotos) {
                        Activity activity = getActivity();
                        if(!listLabelledPhotos.isEmpty()){
                            for(Map<String, Object> label: listLabelledPhotos){
                                String url = label.get("uriPhoto").toString();
                                String id = label.get("idPhoto").toString();
                                System.out.println("uriPhoto: "+url+ ", idPhoto: " +id);
                                list_id_photos.add(id);
                            }
                            DataBase.downloadPhotosById(activity,list_id_photos,
                                    0, new ProgressDialog(activity));
                            saveInPreferenceLastDownload(true, false);
                            registerLastDownload();
                        }
                        else{
                            String title = activity.getResources().getString(R.string.title_mostrar_dialogo_no_descargas);
                            String mensaje = activity.getResources().getString(R.string.message_mostrar_dialogo_no_descargas);
                            DataBase.showDialogFireStorage(activity, title, mensaje, DISMISS_DIALOG);
                        }
                    }
                });
    }

    private void getDateForDownload(int numOptions){
        long dateSearch = 0;
        long realTime = System.currentTimeMillis();
        switch (numOptions){
            case 0:
                dateSearch = prefs.getLong("lastDownloadPhoto", 0);
                break;
            case 1:
                dateSearch = realTime - 24*HOUR_MS;
                break;
            case 2:
                dateSearch = realTime - 3*24*HOUR_MS;
                break;
            case 3:
                dateSearch = realTime - 7*24*HOUR_MS;
                break;
            case 4:
                showDatePickerDialog();
                dateSearch = 0;
                break;
        }
        if(dateSearch > 0){
            download_photo(dateSearch);
        }
        Toast.makeText(getActivity(), "No existen descargas de fotos", Toast.LENGTH_LONG);

    }


    private String dateFormat(long date){
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatoFecha.format(date);
    }


    private void showDatePickerDialog() {
        DatePreference.DatePickerFragment newFragment = new
                DatePreference.DatePickerFragment(new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar  calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                timeDownload = calendar.getTime();
                long time = timeDownload.getTime();
                download_photo(time);
            }
        });
        newFragment.show(PreferencesActivity.fragmentManager, "datePicker");
    }

    private void showTimePickerDialog(Preference preference) {
        String value = preference.getSharedPreferences().getString("set_time", "12:00");
        String[] time = value.split(":");
        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);
        if (getFragmentManager() != null) {
            new DatePreference.TimePickerFragment(new TimePickerDialog.OnTimeSetListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    String time = format(Locale.getDefault(),"%02d", hourOfDay) + ":" + format(Locale.getDefault(), "%02d", minute);
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getContext());
                    sharedPreferences.edit().putString("set_time", time).apply();
                    // if you use setOnPreferenceChangeListener on it, use setTime.callChangeListener(time);
                }
            }, hours, minutes)
                    .show(PreferencesActivity.fragmentManager, getString(R.string.tag_time_picker));
        }
    }
    private void configListener() {
        if (timePickerPreference != null){
            timePickerPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimePickerDialog(preference);
                    return true;
                }
            });

        }
    }

    private void saveInPreferenceLastDownload(boolean photos, boolean labels){
        long date = System.currentTimeMillis();
        SharedPreferences.Editor editor = prefs.edit();
        if(photos)
            editor.putLong("lastDownloadPhoto", date);
        if(labels)
            editor.putLong("lastDownloadLavel", date);
        editor.commit();
    }
    private void registerLastDownload(){
        String msgLastDownload = "No existen descargas";
        long photos = prefs.getLong("lastDownloadPhoto", 0);
        long labels = prefs.getLong("lastDownloadLavel", 0);
        if(photos>0 && labels >0) {
            msgLastDownload = "Fotos: "+ dateFormat(photos) +"\n"+
                    "Etiquetas: " + dateFormat(labels);
        }
        else if(photos>0){
            msgLastDownload = "Fotos: "+ dateFormat(photos);
        }
        if(labels > 0){
            msgLastDownload = "Etiquetas: "+ dateFormat(labels);
        }
        register_last_download.setSummary(msgLastDownload);
    }


}
