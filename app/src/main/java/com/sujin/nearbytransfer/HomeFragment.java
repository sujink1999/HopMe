package com.sujin.nearbytransfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    EditText editText;
    TextView textView;
    Button sendButton;
    String myName;
    ArrayList<SentTable> sentTables;
    Uri imageuri;
    String string;
    ImageView imageView;

    HomeFragment(String myName, ArrayList<SentTable> sentTables)
    {
        this.myName = myName;
        this.sentTables = sentTables;
    }

    OnSendClickListener mCallback;

    // OnImageClickListener interface, calls a method in the host activity named onImageSelected
    public interface OnSendClickListener {
        void broadcast(Uri uri,String fileMessage,String value);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            mCallback = (OnSendClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnImageClickListener");
        }
    }




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home,container,false);

        editText = view.findViewById(R.id.info);
        textView = view.findViewById(R.id.choosePhoto);
        sendButton = view.findViewById(R.id.sendButton);
        imageView = view.findViewById(R.id.imageView);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), 2);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCallback.broadcast(imageuri,string,"");
            }
        });





        return view;



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2
                && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Im here", Toast.LENGTH_SHORT).show();
            // The URI of the file selected by the user.
            Uri uri = data.getData();

            RandomString randomString = new RandomString();
            String id = randomString.generateRandomString(10);
            String userData = editText.getText().toString();
            String fileNameMessage = id + myName + ".jpg\n" + userData;//# is to differentiate if a image is present

            sentTables.add(new SentTable(id, myName, userData, uri));
            imageuri = uri;
            string = fileNameMessage;
            Picasso.get().load(uri).into(imageView);
            //broadcast(uri, fileNameMessage, "");

        }
    }

}