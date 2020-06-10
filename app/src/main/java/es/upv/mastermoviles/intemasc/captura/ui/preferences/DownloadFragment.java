package es.upv.mastermoviles.intemasc.captura.ui.preferences;

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

import es.upv.mastermoviles.intemasc.captura.R;
import es.upv.mastermoviles.intemasc.captura.data.DataBase;
import static es.upv.mastermoviles.intemasc.captura.data.DataBase.DISMISS_DIALOG;
import static es.upv.mastermoviles.intemasc.captura.data.DataBase.saveLabelledPhotosInCSVFile;
import static es.upv.mastermoviles.intemasc.captura.ui.MainActivity.prefs;
import static java.lang.String.format;

public class DownloadFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private int valor = 3, numOptions;
    private ListPreference listDownloadPhotos, listDownloadLabels, listdownload_label_and_photo;
    private DatePreference inicialDate, finalDate;
    private static final int SOLICITUD_PERMISO_WRITE_EXTERNAL_STORAGE = 0;
    private EditTextPreference register_last_download;
    private final long HOUR_MS = 3600000;
    private Date timeDownload, currentDate;
    private Preference timePickerPreferenceInitial, timePickerPreferenceFinal;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_download);
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = new Date(System.currentTimeMillis());
        String dateString = formatoFecha.format(currentDate);

        listDownloadPhotos= (ListPreference) findPreference("download_photo");
        listDownloadPhotos.setOnPreferenceChangeListener(this);
        listDownloadLabels= (ListPreference) findPreference("download_label");
        listDownloadLabels.setOnPreferenceChangeListener(this);
        listDownloadLabels.setPositiveButtonText("Ok");
        listdownload_label_and_photo = (ListPreference) findPreference("download_label_and_photo");
        listdownload_label_and_photo.setOnPreferenceChangeListener(this);
        inicialDate = (DatePreference) findPreference("pref_inicial_date");
        inicialDate.setOnPreferenceChangeListener(this);
        inicialDate.setSummary(dateString);
        inicialDate.onSetInitialValue(true, dateString);
        finalDate = (DatePreference) findPreference("pref_final_date");
        finalDate.setOnPreferenceChangeListener(this);
        finalDate.setSummary(dateString);
        finalDate.onSetInitialValue(true, dateString);


        register_last_download = (EditTextPreference) findPreference("register_download");
        register_last_download.setSelectable(false);
        registerLastDownload();

        timePickerPreferenceInitial = findPreference("set_time_ini");
        SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
        timePickerPreferenceInitial.setSummary(formatoHora.format(currentDate));
        timePickerPreferenceFinal = findPreference("set_time_fin");
        timePickerPreferenceFinal.setSummary(formatoHora.format(currentDate));
        configListener();

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
                getDateForDownload(numOptions, true, false);
                res = true;
                break;
            case "download_label":
                numOptions = Integer.parseInt((String)newValue);
                getDateForDownload(numOptions, false, true);
                res = true;
                break;
            case "pref_inicial_date":
                inicialDate.setSummary((String) newValue);
                break;
            case "pref_final_date":
                finalDate.setSummary((String) newValue);
                break;
            case "download_label_and_photo":
                numOptions = Integer.parseInt((String)newValue);
                getDateAndHourForDownload(numOptions);
                break;
        }
        return res;
    }

    private void downloadPhotos(long initialDateSearch,long finalDateSearch){
        DataBase.searchAllURLPhotos(initialDateSearch,finalDateSearch,
                new DataBase.LoadLabelledPhotosListener() {
                    ArrayList<String> list_id_photos = new ArrayList<String>();
                    List<String> labels;
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

    private void downloadLabelledphoto(long initialDateSearch,long finalDateSearch,
                                       final boolean withPhoto, final boolean withLabel){
        DataBase.searchLabelledPhoto(initialDateSearch,finalDateSearch,
                new DataBase.LoadLabelledPhotosListener() {
                    List<List<String>>  listLabelledPhotosString = new ArrayList<List<String>>();
                    ArrayList<String> list_id_photos = new ArrayList<String>();
                    List<String> labels;
                    @Override
                    public void onLoadPhotos(List<Map<String, Object>> listLabelledPhotos) {
                        Activity activity = getActivity();
                        if(!listLabelledPhotos.isEmpty()){
                            for(Map<String, Object> label: listLabelledPhotos){
                                String url = label.get("uriPhoto").toString();
                                String id = label.get("idPhoto").toString();
                                System.out.println("uriPhoto: "+url+ ", idPhoto: " +id);
                                list_id_photos.add(id);
                                if(withLabel){
                                    labels = new ArrayList<String>();
                                    double creation_date = (double) label.get("creation_date");
                                    labels.add(id);
                                    labels.add(dateFormatLongToString((long)creation_date));
                                    labels.add(url);
                                    List<String> listLabel = (List<String>) label.get("label");
                                    List<Double> x =(List<Double>) label.get("x");
                                    List<Double> y =(List<Double>) label.get("y");
                                    for(int i=0; i < listLabel.size(); i++){
                                        labels.add(listLabel.get(0));
                                        labels.add(x.get(i).toString());
                                        labels.add(y.get(i).toString());
                                    }
                                }
                                listLabelledPhotosString.add(labels);

                            }
                            if(withPhoto)
                            DataBase.downloadPhotosById(activity,list_id_photos,
                                    0, new ProgressDialog(activity));
                            if(withLabel)
                            saveLabelledPhotosInCSVFile(activity, listLabelledPhotosString);

                            saveInPreferenceLastDownload(withPhoto, withLabel);
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

    private void getDateForDownload(int numOptions, final boolean withPhoto, final boolean withLabel){
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
                showDatePickerDialog(withPhoto,withLabel);
                dateSearch = 0;
                break;
        }
        if(dateSearch > 0){
            if(withPhoto && !withLabel)
                downloadPhotos(dateSearch, realTime);
            if(!withPhoto && withLabel)
            downloadLabelledphoto(dateSearch, realTime,withPhoto, withLabel);
        }
        Toast.makeText(getActivity(), "No existen descargas de fotos", Toast.LENGTH_LONG);

    }

    private void getDateAndHourForDownload(int numOptions){
        //Función me permite obtener la fecha y hora para descargar etiquetas y/o fotos
        long iniDate = dateFormatStringToLong(inicialDate.getSummary().toString(),
                timePickerPreferenceInitial.getSummary().toString());
        long finDate = dateFormatStringToLong(finalDate.getSummary().toString(),
                timePickerPreferenceFinal.getSummary().toString());

        if(iniDate > finDate){
            //Me aseguro que la fecha inicial siempre sea menor que la fecha final,
            // caso contrario entra al if y retorna sin niguna acción
            String title = getActivity().getResources().getString(R.string.title_mostrar_dialogo_no_descargas);
            String mensaje = "Fecha Inicial: "+dateFormatLongToString(iniDate)+
                    " debe ser menor a la Fecha Final: "+dateFormatLongToString(finDate);
            DataBase.showDialogFireStorage(getActivity(), title, mensaje, DISMISS_DIALOG);
            return;
        }

       switch (numOptions){
            case 0:
                //Solo etiquetas
                downloadLabelledphoto(iniDate,finDate, false, true);
                break;
            case 1:
                //Solo fotos
                downloadLabelledphoto(iniDate,finDate, true, false);
                break;
            case 2:
                //Etiquetas y fotos
                downloadLabelledphoto(iniDate,finDate, true, true);
                break;
        }
    }


    public String dateFormatLongToString(long date){
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatoFecha.format(date);
    }

    public long dateFormatStringToLong(String date , String hour){
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        String hhmm[] = hour.split(":");
        try{
            Date d = formatoFecha.parse(date);
            long milisecondsDate = d.getTime() + Integer.valueOf(hhmm[0])*HOUR_MS
                    + Integer.valueOf(hhmm[1])*HOUR_MS/60;
            return milisecondsDate;
        }catch (Exception ex){
            ex.printStackTrace();
            return  0;
        }
    }


    private void showDatePickerDialog(final boolean withPhoto, final boolean withLabel) {
        DatePreference.DatePickerFragment newFragment = new
                DatePreference.DatePickerFragment(new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar  calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                timeDownload = calendar.getTime();
                long time = timeDownload.getTime();
                long realTime = System.currentTimeMillis();
                if(withPhoto && !withLabel)
                    downloadPhotos(time, realTime);
                if(!withPhoto && withLabel)
                    downloadLabelledphoto(time, realTime,withPhoto, withLabel);
            }
        });
        newFragment.show(PreferencesActivity.fragmentManager, "datePicker");
    }


    final int TYPE_TIMER_INI = 1;
    final int TYPE_TIMER_FIN = 2;
    private void showTimePickerDialog(Preference preference, final int type) {
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
                    if(type == TYPE_TIMER_INI){
                        sharedPreferences.edit().putString("set_time_init", time).apply();
                        timePickerPreferenceInitial.setSummary(time);
                    }
                    if(type == TYPE_TIMER_FIN){
                        sharedPreferences.edit().putString("set_time_fin", time).apply();
                        timePickerPreferenceFinal.setSummary(time);
                    }
                }
            }, hours, minutes)
                    .show(PreferencesActivity.fragmentManager, getString(R.string.tag_time_picker));
        }
    }

    private void configListener() {
        if (timePickerPreferenceInitial != null){
            timePickerPreferenceInitial.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimePickerDialog(preference, TYPE_TIMER_INI);
                    return true;
                }
            });
        }

        if (timePickerPreferenceFinal != null){
            timePickerPreferenceFinal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimePickerDialog(preference,TYPE_TIMER_FIN);
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
            editor.putLong("lastDownloadLabel", date);
        editor.commit();
    }
    private void registerLastDownload(){
        String msgLastDownload = "No existen descargas";
        long photos = prefs.getLong("lastDownloadPhoto", 0);
        long labels = prefs.getLong("lastDownloadLabel", 0);
        if(photos>0 && labels >0) {
            msgLastDownload = "Fotos: "+ dateFormatLongToString(photos) +"\n"+
                    "Etiquetas: " + dateFormatLongToString(labels);
        }
        else if(photos>0){
            msgLastDownload = "Fotos: "+ dateFormatLongToString(photos);
        }
        else if(labels > 0){
            msgLastDownload = "Etiquetas: "+ dateFormatLongToString(labels);
        }
        register_last_download.setDefaultValue(msgLastDownload);
        register_last_download.setSummary(msgLastDownload);
    }


}
