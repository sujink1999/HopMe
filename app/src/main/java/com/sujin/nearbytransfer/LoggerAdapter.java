package com.sujin.nearbytransfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class LoggerAdapter extends ArrayAdapter {

    ArrayList<ReceivedTable> receivedTables = new ArrayList<>();

    Context mContext;


    public LoggerAdapter(Context context,int resource, ArrayList<ReceivedTable> receivedTables) {

        super(context, resource,receivedTables);
        mContext = context;
        this.receivedTables = receivedTables;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {

            view = new View(mContext);
            view = inflater.inflate(R.layout.loggercard, null);

            //convertView = ((Activity) this).getLayoutInflater().inflate(R.layout.blogs, parent, false);


            TextView name = (TextView) view.findViewById(R.id.loggername);
            TextView endpoint = (TextView) view.findViewById(R.id.message);
            ImageView imageView = view.findViewById(R.id.loggerImage);

            name.setText(receivedTables.get(position).getName());
            endpoint.setText(receivedTables.get(position).getMessage());
            Picasso.get().load(new File(receivedTables.get(position).getPath())).into(imageView);


        }else
        {
            view = (View) convertView;
        }



        return view;
    }

}

