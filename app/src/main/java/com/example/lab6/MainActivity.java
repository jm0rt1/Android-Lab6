package com.example.lab6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "MainActivity";
    public String mLastAddress = "";
    CustomBroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        br = new CustomBroadcastReceiver();

        IntentFilter filter = new IntentFilter("com.example.lab6.new_location");
        registerReceiver(br, filter);

    }

    public void updateLocation(View v){
        try{
            Log.i(TAG, "Updating Location");
            Intent intent = new Intent(getApplicationContext(),aService.class);
            intent.setAction(Constants.GET_LOCATION);
            startService(intent);
        } catch (Exception e){
            Log.e(TAG, e.toString());
        }


    }

    public void displayAddress(String address){
        Log.i(TAG, "clicked optionsButton");
        DialogFragment newFragment = MyAlertDialog.newInstance(R.string.dialog_title,address);
        newFragment.show(getSupportFragmentManager(),"alert");

    }

    public void saveAddress(View v) throws IOException {
        String filename = "data.txt";

        File file = new File(getApplicationContext().getFilesDir(), filename);

        file.createNewFile();
        String fileContents = mLastAddress;
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        }

    }

    public void displayLastAddress(View v) throws FileNotFoundException {

        String filename = "data.txt";

        FileInputStream fis = getApplicationContext().openFileInput(filename);
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            String contents = stringBuilder.toString();
            TextView textView = findViewById(R.id.textView);
            textView.setText(contents);
        }


    }


    public static class MyAlertDialog extends DialogFragment {
        public static MyAlertDialog newInstance(int title, String address) {
            MyAlertDialog frag = new MyAlertDialog();
            Bundle args = new Bundle();
            args.putInt("title", title);
            args.putString("address", address);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            //arguments should not be null, normally should use an assert
            int title = getArguments().getInt("title"); //id of the string resource for the title
            String address = getArguments().getString("address"); //id of the string-array for the elements
            builder.setTitle(title);
            builder.setMessage(address);
            //set the multi choice listener


            //set the positive button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                }
            });
            return builder.create();
        }
    }

    public class CustomBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra("location");
            Geocoder geocoder = new Geocoder(context.getApplicationContext(), Locale.getDefault());

            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }


            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL


            Log.i(TAG, "clicked optionsButton");
            displayAddress(address);
            mLastAddress = address;
        }
    }
}