package com.sujin.nearbytransfer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Connections;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnSendClickListener, InternetConnectivityListener {

    Button advertiseButton, discoverButton;
    Handler mHandler;
    Runnable mHandlerTask;
    ConnectionLifecycleCallback mConnectionLifecycleCallback;
    EndpointDiscoveryCallback endpointDiscoveryCallback;
    PayloadCallback payloadCallback;
    ListView devicesList, connectedList;
    ArrayList<String> availableDevices = new ArrayList<>();
    ArrayList<String> connectedDevices = new ArrayList<>();
    ArrayList<SentTable> sentTables = new ArrayList<>();
    ArrayList<ReceivedTable> receivedTables = new ArrayList<>();
    ArrayAdapter availableArrayAdapter, connectedArrayAdapter;
    ArrayList<String> connectedNames = new ArrayList<>();
    String connected;
    Button sendButton;
    String myName = "myName";
    boolean discovered = false;
    RandomString randomString;
    Payload incomingPayload;
    boolean sender = false;
    String discovery = "", advertise = "";
    TextView discoveryTV, advertiseTV;
    ImageView imageView;
    private static final int RC_PHOTO_PICKER =  2;
    SectionsPageAdapter mSectionsPageAdapter;
    ViewPager mViewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

        }

        InternetAvailabilityChecker.init(this);

        myName = getIntent().getStringExtra("name");

        initialize();
        Log.i("path",getFilesDir().toString());
        //Toast.makeText(this, Integer.toString(Connections.MAX_BYTES_DATA_SIZE), Toast.LENGTH_SHORT).show();

        //discoveryTV = findViewById(R.id.discovery);
        //advertiseTV = findViewById(R.id.advertise);
        imageView = findViewById(R.id.imageView);

        startAdvertising();
        startDiscovery();


        InternetAvailabilityChecker internetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        internetAvailabilityChecker.addInternetConnectivityListener(this);






        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        setupViewPager(mViewPager, "home");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.home)
                {
                    //Toast.makeText(MainActivity.this, "Bookmarks", Toast.LENGTH_SHORT).show();
                    setupViewPager(mViewPager,"home");
                }else if(item.getItemId()==R.id.people)
                {
                    //Toast.makeText(MainActivity.this, "Repositories", Toast.LENGTH_SHORT).show();
                    setupViewPager(mViewPager,"people");
                }else if(item.getItemId()==R.id.logger)
                {
                    //Toast.makeText(MainActivity.this, "Repositories", Toast.LENGTH_SHORT).show();
                    setupViewPager(mViewPager,"logger");
                }

                return true;
            }

        });



    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if(isConnected)
        {

            /*for(int i=0; i<receivedTables.size();i++)
            {
                File file = new File(receivedTables.get(i).getPath());
                RequestBody requestFile =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file);

                String fName = receivedTables.get(i).getId()+receivedTables.get(i).getName()+".jpg";
// MultipartBody.Part is used to send also the actual file name
                MultipartBody.Part body =
                        MultipartBody.Part.createFormData("upload", fName, requestFile);

// add another part within the multipart request
                RequestBody fullName =
                        RequestBody.create(MediaType.parse("multipart/form-data"), "Your Name");
                multipartImageUpload(receivedTables.get(i).getPath(),fName);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Api.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Api service = retrofit.create(Api.class);
                Call<String> daily = service.sendMessage(new Message(fName,receivedTables.get(i).getMessage()));
                daily.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("Repository",t.getMessage());
                    }
                });
            }*/

            for(int i=0; i<sentTables.size();i++)
            {
                String fName = sentTables.get(i).getId()+sentTables.get(i).getName()+".jpg";
                multipartImageUploadUri(sentTables.get(i).getUri(),fName);

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Api.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Api service = retrofit.create(Api.class);
                Call<String> daily = service.sendMessage(new Message(fName,sentTables.get(i).getMessage()));
                daily.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d("Repository",t.getMessage());
                    }
                });
            }

        }else
        {
            Log.i("status","no");
        }



    }

    void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }



    private void setupViewPager(ViewPager viewPager, String nav)
    {
        mSectionsPageAdapter.removeFragments();

        if(nav.equals("home")) {

            mSectionsPageAdapter.addFragment(new HomeFragment(myName,sentTables));
        }else if(nav.equals("people"))
        {
            mSectionsPageAdapter.addFragment(new PeopleFragment(connectedNames,connectedDevices));

        }else if(nav.equals("logger"))
        {
            mSectionsPageAdapter.addFragment(new LoggerFragment(receivedTables));

        }
        viewPager.setAdapter(mSectionsPageAdapter);
    }









    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
        Nearby.getConnectionsClient(getApplicationContext())
                .startAdvertising(
                        myName, "com.sujin.nearbymessages", mConnectionLifecycleCallback, advertisingOptions)
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
        //advertise+=" on";
        //advertiseTV.setText(advertise);
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
       // discovery+=" on";
        //discoveryTV.setText(discovery);
    }

    private void stopDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        Toast.makeText(this, "Discovery Stopped.", Toast.LENGTH_SHORT).show();
        //discovery+=" off";
        //discoveryTV.setText(discovery);
    }

    private void stopAdvertising() {
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
        Toast.makeText(this, "Advertising Stopped.", Toast.LENGTH_SHORT).show();
        //advertise+=" off";
        //advertiseTV.setText(advertise);
    }


    private void initialize() {
        //
        //sendButton = findViewById(R.id.sendButton);
        //devicesList = findViewById(R.id.devicesList);
        //connectedList = findViewById(R.id.connectedDevicesList);
        availableArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, availableDevices);
        connectedArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, connectedDevices);
        randomString = new RandomString();

        //
        //devicesList.setAdapter(availableArrayAdapter);
        //connectedList.setAdapter(connectedArrayAdapter);


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
                    connected = discoveredEndpointInfo.getEndpointName();
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
                stopAdvertising();
                stopDiscovery();
                connected = connectionInfo.getEndpointName();
                Nearby.getConnectionsClient(getApplicationContext()).acceptConnection(s, payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution result) {

                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        connectedDevices.add(s);
                        connectedArrayAdapter.notifyDataSetChanged();
                        connectedNames.add(connected);
                        sendAvailableData(s);
                        if(connectedDevices.size()<2) {
                            startAdvertising();
                            startDiscovery();
                        }
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        Toast.makeText(MainActivity.this, "Connection Rejected", Toast.LENGTH_SHORT).show();
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                        startDiscovery();
                        startAdvertising();
                        availableDevices.remove(s);
                        availableArrayAdapter.notifyDataSetChanged();
                        Log.i("status","status failure");
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onDisconnected(@NonNull String s) {
                connectedNames.remove(connectedDevices.indexOf(s));
                connectedDevices.remove(s);
                connectedArrayAdapter.notifyDataSetChanged();

                if(connectedDevices.size()<2) {
                    startAdvertising();
                    startDiscovery();
                }
            }
        };

        payloadCallback = new PayloadCallback() {
            boolean isFilenameReceived = false;
            boolean isFileReceived = false;
            Payload incomingPayload = null;
            String payloadFilenameMessage = "";
            int r = -1;

            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                if (payload.getType() == Payload.Type.BYTES) {
                    payloadFilenameMessage = new String(payload.asBytes());
                    isFilenameReceived = true;
                } else if (payload.getType() == Payload.Type.FILE) {
                    // Add this to our tracking map, so that we can retrieve the payload later.
                    incomingPayload = payload;
                }
            }


            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {


                if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {

                        if (r == -1) {
                            if (incomingPayload == null && isFilenameReceived && payloadFilenameMessage.charAt(0) == '#') {
                                String filename = payloadFilenameMessage.substring(1, payloadFilenameMessage.indexOf("\n"));
                                String id = filename.substring(0, 10);
                                String name = filename.substring(10, filename.lastIndexOf("."));
                                String extension = filename.substring(filename.lastIndexOf("."));
                                String data = payloadFilenameMessage.substring(payloadFilenameMessage.indexOf("\n") + 1);

                                if (!(checkReceivedTable(id, name) || checkSentTable(id, name))) {
                                    receivedTables.add(new ReceivedTable(id, name, data, null));
                                    broadcastReceived(null, payloadFilenameMessage,endpointId);
                                }
                                startDiscovery();
                                startAdvertising();
                                isFilenameReceived = false;
                                isFileReceived = false;
                                incomingPayload = null;
                                payloadFilenameMessage = "";
                                Toast.makeText(MainActivity.this, "only message received", Toast.LENGTH_SHORT).show();
                            } else if (incomingPayload == null && isFilenameReceived && payloadFilenameMessage.charAt(0) != '#') {
                                r = 1;
                            } else {
                                r = 2;
                                isFileReceived = true;
                            }
                        } else if (r == 1) {
                            if(incomingPayload!=null) {
                                File file = incomingPayload.asFile().asJavaFile();
                                String filename = payloadFilenameMessage.substring(0, payloadFilenameMessage.indexOf("\n"));
                                String id = filename.substring(0, 10);
                                String name = filename.substring(10, filename.lastIndexOf("."));
                                String extension = filename.substring(filename.lastIndexOf("."));
                                String data = payloadFilenameMessage.substring(payloadFilenameMessage.indexOf("\n") + 1);
                                if (!(checkReceivedTable(id, name) || checkSentTable(id, name))) {
                                    receivedTables.add(new ReceivedTable(id, name, data, file.getAbsolutePath()));
                                    //Picasso.get().load(file).into(imageView);
                                    broadcastReceived(file.getAbsolutePath(), payloadFilenameMessage,endpointId);

                                }
                                startDiscovery();
                                startAdvertising();
                                isFilenameReceived = false;
                                isFileReceived = false;
                                incomingPayload = null;
                                payloadFilenameMessage = "";
                                r = -1;
                                Toast.makeText(MainActivity.this, "message then file", Toast.LENGTH_SHORT).show();
                            }

                        } else if (r == 2) {
                            if(incomingPayload!=null) {
                                File file = incomingPayload.asFile().asJavaFile();
                                String filename = payloadFilenameMessage.substring(0, payloadFilenameMessage.indexOf("\n"));
                                String id = filename.substring(0, 10);
                                String name = filename.substring(10, filename.lastIndexOf("."));
                                String extension = filename.substring(filename.lastIndexOf("."));
                                String data = payloadFilenameMessage.substring(payloadFilenameMessage.indexOf("\n") + 1);
                                if (!(checkReceivedTable(id, name) || checkSentTable(id, name))) {
                                    receivedTables.add(new ReceivedTable(id, name, data, file.getAbsolutePath()));
                                    //Picasso.get().load(file).into(imageView);
                                    broadcastReceived(file.getAbsolutePath(), payloadFilenameMessage,endpointId);

                                }
                                startDiscovery();
                                startAdvertising();
                                isFilenameReceived = false;
                                isFileReceived = false;
                                incomingPayload = null;
                                payloadFilenameMessage = "";
                                r = -1;
                                Toast.makeText(MainActivity.this, "file then message", Toast.LENGTH_SHORT).show();
                            }else {
                                isFilenameReceived = false;
                                isFileReceived = false;
                                incomingPayload = null;
                                payloadFilenameMessage = "";
                                r = -1;
                            }

                        }
                }

            }

            /*@Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                    startAdvertising();
                    startDiscovery();



                   *//* if (incomingPayload != null) {
                        File incomingFile = incomingPayload.asFile().asJavaFile();
                        byte[] receivedByteArray = new byte[(int) incomingFile.length()];
                        try {


                            FileInputStream fis = new FileInputStream(incomingFile);
                            fis.read(receivedByteArray); //read file into bytes[]
                            fis.close();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        //String string = new String(receivedByteArray);

                        //receivedByteArray = Base64.decode(string,Base64.DEFAULT);

                        File outputFile = null;
                        int index = decode(receivedByteArray);
                        byte[] fileByteArray = getFile(index, receivedByteArray);
                        String fileName = getFileName(index, receivedByteArray);
                        String id = fileName.substring(0, 10);
                        String name = fileName.substring(10);
                        String extension = fileName.substring(fileName.lastIndexOf("."));

                        if (!(checkSentTable(id, name) || checkReceivedTable(id, name))) {
                            outputFile = new File(getFilesDir(), fileName);
                            receivedTables.add(new Table(id, name, outputFile.getAbsolutePath()));
                            Log.i("receivedtable", "added "+fileName);
                            //broadcast(receivedByteArray,s);
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                fos.write(fileByteArray);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        Log.i("receivedtable", outputFile.getAbsolutePath());
                        Picasso.get().load(outputFile).into(imageView);
                        incomingPayload = null;
                    }*//*

                }

            }*/
        };

        //Click Listeners

        /*devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //Toast.makeText(MainActivity.this, "post request connection", Toast.LENGTH_SHORT).show();
            }
        });*/

        /*sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                *//*String id = randomString.generateRandomString(10);
                String fName = id+myName+".jpg";*//*
                *//*File file = new File(getFilesDir(),fName);*//*
                *//*try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data.getBytes());
                    fos.close();

                }catch (Exception e)
                {
                    e.printStackTrace();
                }*//*
                //byte[] encodedData = encode(file.getAbsolutePath(),fName);
                //sentTables.add(new Table(id,myName+".txt",file.getAbsolutePath()));
                //Log.i("path",file.getAbsolutePath());

                //broadcast(encodedData,"");
                *//*Toast.makeText(MainActivity.this, fName, Toast.LENGTH_SHORT).show();
                Log.i("senttable", "added "+fName);*//*


                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);


            }
        });*/


    }

    private void requestConnection(final String endpoint) {

        stopDiscovery();
        stopAdvertising();
        Nearby.getConnectionsClient(getApplicationContext())
                .requestConnection(myName, endpoint, mConnectionLifecycleCallback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "request success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "request failed", Toast.LENGTH_SHORT).show();
                                availableDevices.remove(endpoint);
                                availableArrayAdapter.notifyDataSetChanged();
                                startAdvertising();
                                startDiscovery();
                            }
                        });
    }







    private byte[] encodeUsingUri(Uri uri, String fileName)
    {
        byte[] transferDataByteArray = null;
        byte[] fileByteArray = null;
        byte[] fileNameByteArray = new byte[fileName.length()];
        fileNameByteArray = fileName.getBytes();

        try {
            InputStream iStream = getContentResolver().openInputStream(uri);
            fileByteArray = getBytes(iStream);
        }catch (Exception e)
        {
            Toast.makeText(this,e.getMessage() , Toast.LENGTH_SHORT).show();
        }

        String delimiter = "#kaipulla#";
        byte[] delimiterByteArray = delimiter.getBytes();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            outputStream.write(fileByteArray);
            outputStream.write(delimiterByteArray);
            outputStream.write(fileNameByteArray);

            transferDataByteArray = outputStream.toByteArray();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return transferDataByteArray;

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


    private void broadcastReceived(String path,String fileNameMessage,String endpoint) {

        stopAdvertising();
        stopDiscovery();
        Payload filenameBytesPayload = Payload.fromBytes(fileNameMessage.getBytes());
        // Open the ParcelFileDescriptor for this URI with read access.
        File file = new File(path);

        for (int i = 0; i < connectedDevices.size(); i++) {
            if(!connectedDevices.get(i).equals(endpoint)) {
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filenameBytesPayload);

                if (path != null) {
                    Payload filePayload = null;
                    try {
                        filePayload = Payload.fromFile(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filePayload);
                }
            }

        }
    }


    public void broadcast(Uri uri,String fileNameMessage,String endpoint)
    {

        stopAdvertising();
        stopDiscovery();
        Payload filenameBytesPayload = Payload.fromBytes(fileNameMessage.getBytes());
        ParcelFileDescriptor pfd;
        try {
            // Open the ParcelFileDescriptor for this URI with read access.
            pfd = getContentResolver().openFileDescriptor(uri, "r");

        } catch (FileNotFoundException e) {
            Log.e("MyApp", "File not found", e);
            return;
        }
        for(int i=0; i<connectedDevices.size();i++)
        {
            if(!connectedDevices.get(i).equals(endpoint)) {
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filenameBytesPayload);

                if (uri != null) {
                    Payload filePayload = Payload.fromFile(pfd);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filePayload);
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            },4000);

        }



        /*// Send the filename message as a bytes payload.
        Payload filenameBytesPayload =
                Payload.fromBytes(filenameMessage.getBytes(StandardCharsets.UTF_8));
        Nearby.getConnectionsClient(context).sendPayload(endpointId, filenameBytesPayload);

        // Finally, send the file payload.
        Nearby.getConnectionsClient(context).sendPayload(endpointId, filePayload);



        //String encodedString = Base64.encodeToString(encodedData, Base64.DEFAULT);
        File outputFile = new File(getFilesDir(),"sample.txt");
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(encodedData);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        stopAdvertising();
        stopDiscovery();

        for(int i=0; i<connectedDevices.size();i++)
        {
            if(!connectedDevices.get(i).equals(endpoint)) {
                try {
                    Payload filePayload = Payload.fromFile(outputFile);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filePayload);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if(outputFile.delete())
        {
            Log.i("delete","success");
        }*/
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

    private void sendAvailableData(String endpoint) {
        stopAdvertising();
        stopDiscovery();

        //for received table
        for (int i = 0; i < receivedTables.size(); i++) {
            String filenameMessage = receivedTables.get(i).getId() + receivedTables.get(i).getName() +"\n"+ receivedTables.get(i).getMessage();


            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, Payload.fromBytes(filenameMessage.getBytes()));
            try {
                if(receivedTables.get(i).getPath()!=null) {
                    File file = new File(receivedTables.get(i).getPath());
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, Payload.fromFile(file));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        //for sent table
        for (int i = 0; i < sentTables.size(); i++) {
            String filenameMessage = sentTables.get(i).getId() + sentTables.get(i).getName() +".jpg\n"+ sentTables.get(i).getMessage();


            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, Payload.fromBytes(filenameMessage.getBytes()));
            ParcelFileDescriptor pfd;
            try {
                // Open the ParcelFileDescriptor for this URI with read access.
                if(sentTables.get(i).getUri()!=null) {
                    pfd = getContentResolver().openFileDescriptor(sentTables.get(i).getUri(), "r");
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(endpoint, Payload.fromFile(pfd));
                }

            } catch (FileNotFoundException e) {
                Log.e("MyApp", "File not found", e);
                return;
            }

        }


    }












        /*Payload filenameBytesPayload = Payload.fromBytes(fileNameMessage.getBytes());
        ParcelFileDescriptor pfd;
        try {
            // Open the ParcelFileDescriptor for this URI with read access.
            pfd = getContentResolver().openFileDescriptor(uri, "r");

        } catch (FileNotFoundException e) {
            Log.e("MyApp", "File not found", e);
            return;
        }
        for(int i=0; i<connectedDevices.size();i++)
        {
            if(!connectedDevices.get(i).equals(endpoint)) {
                Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filenameBytesPayload);

                if (uri != null) {
                    Payload filePayload = Payload.fromFile(pfd);
                    Nearby.getConnectionsClient(getApplicationContext()).sendPayload(connectedDevices.get(i), filePayload);
                }
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            },4000);
    }*/


    /*protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER
                && resultCode == Activity.RESULT_OK) {
            // The URI of the file selected by the user.
            Toast.makeText(this, "IM in main activity", Toast.LENGTH_SHORT).show();
            Uri uri = data.getData();

            String id = randomString.generateRandomString(10);
            String userData = "data";
            String fileNameMessage = id+myName+".jpg\n"+userData;//# is to differentiate if a image is present

            sentTables.add(new SentTable(id,myName,userData,uri));
            broadcast(uri,fileNameMessage,"");

        }*/




        /*if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            byte[] sendByteArray = null;

            //File file = new File(selectedImageUri.getPath());
            //Toast.makeText(this, file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            //Picasso.get().load(R.drawable.pexels).into(imageView);
            String id = randomString.generateRandomString(10);
            String fName = id+myName+".jpg";
            try {
                sendByteArray = encodeUsingUri(selectedImageUri, fName);
            }catch (Exception e)
            {

            }
            broadcast(sendByteArray,"");

            *//*if(file.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);

            }else
            {
                //Toast.makeText(this, "poda punda", Toast.LENGTH_SHORT).show();
            }*//*

        }*/


        public byte[] getBytes (InputStream inputStream) throws IOException {
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }

        @Override
        public void onRequestPermissionsResult ( final int requestCode,
        @NonNull final String[] permissions, @NonNull final int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


        private void multipartImageUpload (String path, String fname){
            try {
                File filesDir = getApplicationContext().getFilesDir();
                File file = new File(getFilesDir(), fname);

                Bitmap mBitmap = null;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mBitmap = BitmapFactory.decodeFile(path);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                Uri uri = null;
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");

                file.setReadable(true);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                Log.i("status", "yes");
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Api.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                Api service = retrofit.create(Api.class);


                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
                RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload");

                Call<ResponseBody> req = service.postImage(body, name);
                req.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    private void multipartImageUploadUri (Uri uri, String fname){
        try {
            File filesDir = getApplicationContext().getFilesDir();
            File file = new File(getFilesDir(), fname);

            Bitmap mBitmap = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),uri);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            file.setReadable(true);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            Log.i("status", "yes");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Api.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Api service = retrofit.create(Api.class);


            RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("upload", file.getName(), reqFile);
            RequestBody name = RequestBody.create(MediaType.parse("text/plain"), "upload");

            Call<ResponseBody> req = service.postImage(body, name);
            req.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






/*        File filesDir = getApplicationContext().getFilesDir();
        File file = new File(path);

        Bitmap mBitmap = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();


        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        Log.i("status","yes");
        Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(Api.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
        Api service = retrofit.create(Api.class);
        Call<String> daily = service.sendMessage(new Message("this","that"));
        daily.enqueue(new Callback<String>() {
@Override
public void onResponse(Call<String> call, Response<String> response) {

        }

@Override
public void onFailure(Call<String> call, Throwable t) {
        Log.d("Repository",t.getMessage());
        }
        });*/


    }
