package com.sujin.nearbytransfer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class LoggerFragment extends Fragment {

    ListView loggerListView;

    ArrayList<ReceivedTable> receivedTables;

    LoggerFragment(ArrayList<ReceivedTable> receivedTables) {
        this.receivedTables = receivedTables;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loggerlayout, container, false);

        loggerListView = view.findViewById(R.id.loggerListView);
        LoggerAdapter loggeradapter = new LoggerAdapter(getContext(),R.layout.loggercard,receivedTables);
        loggerListView.setAdapter(loggeradapter);

        return view;

    }
}