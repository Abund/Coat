package com.example.coat.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.annotations.NotNull;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {
    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        Calendar calendar = Calendar.getInstance();
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int min=calendar.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(),(TimePickerDialog.OnTimeSetListener)getActivity(),hour,min, DateFormat.is24HourFormat(getActivity()));
    }
}
