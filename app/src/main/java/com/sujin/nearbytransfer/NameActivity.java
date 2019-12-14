package com.sujin.nearbytransfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NameActivity extends AppCompatActivity {

    CardView enter;
    EditText username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        getWindow().setStatusBarColor(getResources().getColor(R.color.darkColorLight));


        enter = findViewById(R.id.enter);
        username = findViewById(R.id.username);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!username.getText().toString().equals("")) {

                    Intent intent = new Intent(NameActivity.this, MainActivity.class);
                    intent.putExtra("name",username.getText().toString() );
                    startActivity(intent);
                }else
                {
                    Toast.makeText(NameActivity.this, "Enter a name!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
