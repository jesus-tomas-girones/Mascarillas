package es.upv.mastermoviles.intemasc.captura.ui.preferences;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePreference extends DialogPreference {
    private int lastDate = 0;
    private int lastMonth = 0;
    private int lastYear = 0;
    private String dateval;
    private CharSequence mSummary;
    private DatePicker picker = null;


    public static int getYear(String dateval) {
        String[] pieces = dateval.split("-");
        return (Integer.parseInt(pieces[0]));
    }

    public static int getMonth(String dateval) {
        String[] pieces = dateval.split("-");
        return (Integer.parseInt(pieces[1]));
    }

    public static int getDate(String dateval) {
        String[] pieces = dateval.split("-");
        return (Integer.parseInt(pieces[2]));
    }

    public DatePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Aceptar");
        setNegativeButtonText("Cancelar");
    }

    @Override
    protected View onCreateDialogView() {
            picker = new DatePicker(getContext());
            // setCalendarViewShown(false) attribute is only available from API level 11
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                picker.setCalendarViewShown(false);
            return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.updateDate(lastYear, lastMonth , lastDate);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastYear = picker.getYear();
            lastMonth = picker.getMonth();
            lastDate = picker.getDayOfMonth();
            String dateval = String.valueOf(lastYear) + "-"
                    + String.valueOf(lastMonth+1) + "-"
                    + String.valueOf(lastDate);

            if (callChangeListener(dateval)) {
                persistString(dateval);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        dateval = null;
        if (restoreValue) {
            if (defaultValue == null) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                String formatted = format1.format(cal.getTime());
                dateval = formatted;
            } else {
                dateval = defaultValue.toString();
            }
        } else {
            dateval = defaultValue.toString();
        }
        lastYear = getYear(dateval);
        lastMonth = getMonth(dateval)-1;
        lastDate = getDate(dateval);
    }

    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        dateval = text;
        persistString(text);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getText() {
        return dateval;
    }

    public CharSequence getSummary() {
        return mSummary;
    }

    public void setSummary(CharSequence summary) {
        if (summary == null && mSummary != null || summary != null
                && !summary.equals(mSummary)) {
            mSummary = summary;
            notifyChanged();
        }
    }
    /***********************************************************************************************************/
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
/***********************************************************************************************************/
    public static class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

        private TimePickerDialog.OnTimeSetListener onTimeSetListener;
        private int hours;
        private int minutes;

        TimePickerFragment(TimePickerDialog.OnTimeSetListener onTimeSetListener, int hours, int minutes) {
            this.onTimeSetListener = onTimeSetListener;
            this.hours = hours;
            this.minutes = minutes;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new TimePickerDialog(getActivity(),
                    onTimeSetListener, hours, minutes, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) { }

    }

}
