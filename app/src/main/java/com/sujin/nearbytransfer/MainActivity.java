package com.sujin.nearbytransfer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    Button advertiseButton, discoverButton;
    ConnectionLifecycleCallback mConnectionLifecycleCallback;
    EndpointDiscoveryCallback endpointDiscoveryCallback;
    PayloadCallback payloadCallback;
    ListView devicesList, connectedList;
    ArrayList<String> availableDevices = new ArrayList<>();
    ArrayList<String> connectedDevices = new ArrayList<>();
    ArrayList<Table> sentTables = new ArrayList<>();
    ArrayList<Table> receivedTables = new ArrayList<>();
    ArrayAdapter availableArrayAdapter, connectedArrayAdapter;
    Button sendButton;
    String myName = "myName";
    RandomString randomString;
    Payload incomingPayload;
    String discovery = "", advertise = "";
    TextView discoveryTV, advertiseTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        discoveryTV = findViewById(R.id.discovery);
        advertiseTV = findViewById(R.id.advertise);

        startAdvertising();
        startDiscovery();



    }


    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        "user2", "com.sujin.nearbymessages", mConnectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(MainActivity.this, "Advertising Started!", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                );
        advertise+=" on";
        advertiseTV.setText(advertise);
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startDiscovery("com.sujin.nearbymessages", endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Discovery Started!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        discovery+=" on";
        discoveryTV.setText(discovery);
    }

    private void stopDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        Toast.makeText(this, "Discovery Stopped.", Toast.LENGTH_SHORT).show();
        discovery+=" off";
        discoveryTV.setText(discovery);
    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
        Toast.makeText(this, "Advertising Stopped.", Toast.LENGTH_SHORT).show();
        advertise+=" off";
        advertiseTV.setText(advertise);
    }


    private void initialize() {
        //
        sendButton = findViewById(R.id.sendButton);
        devicesList = findViewById(R.id.devicesList);
        connectedList = findViewById(R.id.connectedDevicesList);
        availableArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, availableDevices);
        connectedArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, connectedDevices);
        randomString = new RandomString();

        //
        devicesList.setAdapter(availableArrayAdapter);
        connectedList.setAdapter(connectedArrayAdapter);


        //Callbacks
        endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String s, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {



                if (!connectedDevices.contains(s)) {
                    if(availableDevices.contains(s))
                    {
                        availableDevices.remove(s);
                    }
                    availableDevices.add(s);
                    availableArrayAdapter.notifyDataSetChanged();
                    requestConnection(s);
                }
            }

            @Override
            public void onEndpointLost(@NonNull String s) {
                availableDevices.remove(s);
                availableArrayAdapter.notifyDataSetChanged();

            }
        };

        mConnectionLifecycleCallback = new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {
                Toast.makeText(MainActivity.this, "onconnection", Toast.LENGTH_SHORT).show();
                stopAdvertising();
                stopDiscovery();
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(s, payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {

                Log.i("status",connectionResolution.getStatus().toString());
                if(connectionResolution.getStatus().toString().equals("Status{statusCode=SUCCESS, resolution=null}")) {
                    connectedDevices.add(s);
                    connectedArrayAdapter.notifyDataSetChanged();
                    startAdvertising();
                    startDiscovery();
                    Log.i("status","status success");

                    sendAvailableData(s);

                }else
                {
                    startDiscovery();
                    startAdvertising();
                    availableDevices.remove(s);
                    availableArrayAdapter.notifyDataSetChanged();
                    Log.i("status","status failure");
                }
            }

            @Override
            public void onDisconnected(@NonNull String s) {
                connectedDevices.remove(s);
                connectedArrayAdapter.notifyDataSetChanged();

            }
        };

        payloadCallback = new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                incomingPayload = payload;
            }

            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {

                    if (incomingPayload != null) {
                        byte[] receivedByteArray = incomingPayload.asBytes();
                        int index = decode(receivedByteArray);
                        byte[] fileByteArray = getFile(index, receivedByteArray);
                        String fileName = getFileName(index, receivedByteArray);
                        String id = fileName.substring(0, 10);
                        String name = fileName.substring(10);
                        String extension = fileName.substring(fileName.lastIndexOf("."));

                        if (!(checkSentTable(id, name) || checkReceivedTable(id, name))) {
                            File outputFile = new File(getFilesDir(), fileName);
                            receivedTables.add(new Table(id, name, outputFile.getAbsolutePath()));
                            Log.i("receivedtable", "added "+fileName);
                            broadcast(receivedByteArray,s);
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                fos.write(fileByteArray);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(MainActivity.this, "Data received!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(MainActivity.this, fileName, Toast.LENGTH_SHORT).show();
                        incomingPayload = null;
                    }

                }

            }
        };

        //Click Listeners

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //Toast.makeText(MainActivity.this, "post request connection", Toast.LENGTH_SHORT).show();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = randomString.generateRandomString(10);
                String fName = id+myName+".txt";
                File file = new File(getFilesDir(),fName);
                String data = "This is the data";

                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data.getBytes());
                    fos.close();

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                byte[] encodedData = encode(file.getAbsolutePath(),fName);
                sentTables.add(new Table(id,myName+".txt",file.getAbsolutePath()));
                broadcast(encodedData,"");
                Toast.makeText(MainActivity.this, fName, Toast.LENGTH_SHORT).show();
                Log.i("senttable", "added "+fName);
            }
        });


    }

    private void requestConnection(String endpoint) {

        Nearby.getConnectionsClient(getApplicationContext())
                .requestConnection("user1", endpoint, mConnectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
    }

    private byte[] encode(String path, String fileName) {
        byte[] transferDataByteArray = null;
        File file = new File(path);
        //init array with file length
        byte[] fileBytesArray = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileBytesArray); //read file into bytes[]
            fis.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        byte[] fileNameByteArray = new byte[fileName.length()];
        fileNameByteArray = fileName.getBytes();

        String delimiter = "#kaipulla#";
        byte[] delimiterByteArray = delimiter.getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(fileBytesArray);
            outputStream.write(delimiterByteArray);
            outputStream.write(fileNameByteArray);

            transferDataByteArray = outputStream.toByteArray();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return transferDataByteArray;


    }


    private int decode(byte[] receivedByteArray) {
        String delimiter = "#kaipulla#";
        byte[] delimiterByteArray = delimiter.getBytes();

        boolean flag = false;
        // substring match
        for (int i = 0; i < receivedByteArray.length; i++) {
            if (receivedByteArray[i] == delimiterByteArray[0]) {
                flag = true;
                int j;
                for (j = 1; j < delimiterByteArray.length; j++) {
                    if (receivedByteArray[i + j] != delimiterByteArray[j]) {
                        flag = false;
                    }
                }
                if (flag) {
                    return i; // result
                }
            }
        }
        return -1;
    }

    private byte[] getFile(int index, byte[] receivedByteArray) {
        byte[] fileByteArray = Arrays.copyOfRange(receivedByteArray, 0, index);
        return fileByteArray;
    }

    private String getFileName(int index, byte[] receivedByteArray) {
        byte[] fileNameByteArray = Arrays.copyOfRange(receivedByteArray, index + 10, receivedByteArray.length);
        return new String(fileNameByteArray);
    }


    private void broadcast(byte[] encodedData,String endpoint)
    {
        for(int i=0; i<connectedDevices.size();i++)
        {
            if(!connectedDevices.get(i).equals(endpoint)) {
                try {
                    Payload byteArrayPayload = Payload.fromBytes(encodedData);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), byteArrayPayload);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkSentTable(String id, String name)
    {
        boolean flag = false;
        for(int i=0; i<sentTables.size();i++)
        {
            if(sentTables.get(i).checkID(id,name))
            {
                flag = true;
                break;
            }
        }
        return flag;

    }

    private boolean checkReceivedTable(String id, String name)
    {
        boolean flag = false;
        for(int i=0; i<receivedTables.size();i++)
        {
            if(receivedTables.get(i).checkID(id,name))
            {
                flag = true;
                break;
            }
        }
        return flag;

    }

    private void sendAvailableData(String endpoint)
    {
        String fName;
        for(int i=0 ; i<sentTables.size();i++)
        {
            fName = sentTables.get(i).getId()+sentTables.get(i).getName();
            File file = new File(sentTables.get(i).getPath());
            byte[] encodedData = encode(file.getAbsolutePath(),fName);

            try {
                Payload byteArrayPayload = Payload.fromBytes(encodedData);
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, byteArrayPayload);

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        for(int i=0 ; i<receivedTables.size();i++)
        {
            fName = receivedTables.get(i).getId()+receivedTables.get(i).getName();
            File file = new File(receivedTables.get(i).getPath());
            byte[] encodedData = encode(file.getAbsolutePath(),fName);

            try {
                Payload byteArrayPayload = Payload.fromBytes(encodedData);
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, byteArrayPayload);

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



}



