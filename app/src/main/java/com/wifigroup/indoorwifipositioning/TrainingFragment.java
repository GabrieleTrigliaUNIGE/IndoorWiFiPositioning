package com.wifigroup.indoorwifipositioning;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TrainingFragment extends Fragment {

    private final String TAG = "TrainingFragment";

    private TextView tvAccessPoint;
    private TextView tvCoordinates;
    private TextView tvMeasure;
    private Button bttStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.training_fragments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initViews(View view) {
        tvAccessPoint = view.findViewById(R.id.tvAccessPoint);
        tvCoordinates = view.findViewById(R.id.tvCoordinates);
        tvMeasure = view.findViewById(R.id.tvMeasure);
        bttStart = view.findViewById(R.id.bttStart);
    }
}
