package com.MDmde.mobile;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

/**
 * MDme Android application
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 1/21/15
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
public class DatePreference extends DialogPreference implements
        DatePicker.OnDateChangedListener {

    private String mDateString;
    private String mChangedValueCanBeNull;
    private DatePicker mDatePicker;

    public DatePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DatePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //get a DatePicker to set date from getDate()
    @Override
    protected View onCreateDialogView() {
        mDatePicker = new DatePicker(getContext());
        Calendar cal = getDate();
        mDatePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), this);
        return mDatePicker;
    }

    //gets date from datepicker or default value from xml
    public Calendar getDate() {
        try {
            Date date = formatter().parse(defaultValue());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }
        catch (java.text.ParseException e) {
            return defaultCalendar();
        }
    }

    public void setDate(String dateString) {
        mDateString = dateString;
    }

    //internal storage date format
    public static SimpleDateFormat formatter() {
        return new SimpleDateFormat("yyyy.MM.dd");
    }

    //format of date displayed
    public static SimpleDateFormat summaryFormatter() {
        return new SimpleDateFormat("MMMM dd, yyyy");
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    //called when picker shown or restored
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object def) {
        if (restoreValue) {
            mDateString = getPersistedString(defaultValue());
            setTheDate(mDateString);
        }
        else {
            boolean wasNull = mDateString == null;
            setDate((String) def);
            if (!wasNull) {
                persistDate(mDateString);
            }
        }
    }

    //called on onPause()
    @Override
    protected Parcelable onSaveInstanceState() {
        if (isPersistent()) {
            return super.onSaveInstanceState();
        }
        else {
            return new SavedState(super.onSaveInstanceState());
        }
    }

    //called on onResume()
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            setTheDate(((SavedState) state).dateValue);
        }
        else {
            SavedState s = (SavedState) state;
            super.onRestoreInstanceState(s.getSuperState());
            setTheDate(s.dateValue);
        }
    }

    //called when date changed
    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        Calendar selected = new GregorianCalendar(year, month , day);
        mChangedValueCanBeNull = formatter().format(selected.getTime());
    }

    //called when dialog closed and saves on 'OK'
    @Override
    protected void onDialogClosed(boolean shouldSave) {
        if (shouldSave && mChangedValueCanBeNull != null) {
            setTheDate(this.mChangedValueCanBeNull);
            mChangedValueCanBeNull = null;
        }
    }

    private void setTheDate(String s) {
        setDate(s);
        persistDate(s);
    }

    private void persistDate(String s) {
        persistString(s);
        setSummary(summaryFormatter().format(getDate().getTime()));
    }

    //default when no defualt set in xml
    public static Calendar defaultCalendar() {
        return new GregorianCalendar(1970, 0, 1);
    }

    public static String defaultCalendarString() {
        return formatter().format(defaultCalendar().getTime());
    }

    private String defaultValue() {
        if (mDateString == null) {
            setDate(defaultCalendarString());
        }
        return mDateString;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        mDatePicker.clearFocus();
        onDateChanged(mDatePicker, mDatePicker.getYear(), mDatePicker.getMonth(),
                mDatePicker.getDayOfMonth());
        onDialogClosed(which == DialogInterface.BUTTON1);
    }

    public static Calendar getDateFor(SharedPreferences preferences, String field) {
        Date date = stringToDate(preferences.getString(field, defaultCalendarString()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private static Date stringToDate(String dateString) {
        try {
            return formatter().parse(dateString);
        }
        catch (ParseException e) {
            return defaultCalendar().getTime();
        }
    }

    private static class SavedState extends BaseSavedState {
        String dateValue;

        public SavedState(Parcel p) {
            super(p);
            dateValue = p.readString();
        }

        public SavedState(Parcelable p) {
            super(p);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(dateValue);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }
}
