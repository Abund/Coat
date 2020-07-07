package com.example.coat.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.annotations.NotNull;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(),(DatePickerDialog.OnDateSetListener) getActivity(),year,month,day);
    }
}
