package com.sujin.nearbytransfer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PeopleFragment extends Fragment {

    ListView listView;
    ArrayList<String> connectedDevices;
    ArrayList<String> endpoints;


    PeopleFragment(ArrayList<String> connectedDevices, ArrayList<String> endpoints)
    {
        this.connectedDevices = connectedDevices;
        this.endpoints = endpoints;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.peoplefragment,container,false);


        listView = view.findViewById(R.id.listView);
        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,connectedDevices);
        PeopleAdapter peopleAdapter = new PeopleAdapter(getContext(),R.layout.peoplecard,connectedDevices,endpoints);
        listView.setAdapter(peopleAdapter);
        return view;



    }

}
