package com.example.coat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coat.R;

public class ViewPsychologist extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_viewpsychologist, container, false);
        super.onCreate(savedInstanceState);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
