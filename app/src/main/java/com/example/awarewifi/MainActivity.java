//
// Master in Applied Software Development Start Sept 2020
// Capstone Project - Master in Applied Software Development
// Date - 21 January 2022
// Lecturers - Graham Glanville | Mark Morrissey
// Student Name - Mariabeluz Suarez Amador
// Student Number - 2017367
//Inspired by:
// https://developer.android.com/guide/topics/connectivity/wifi-scan
// https://stackoverflow.com/questions/47962989/wifimanger-getscanresults-returns-empty-list-on-android
// https://stackoverflow.com/questions/38509419/highlight-text-inside-a-textview
// https://www.tabnine.com/code/java/methods/android.nfc.NfcAdapter/isEnabled
// https://www.titanwolf.org/Network/q/e21a39eb-671b-46e7-9ea3-bb764e0e9ffb/y
// https://github.com/romellfudi/VoIpUSSD
//

package com.example.awarewifi;

// Libraries
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
// this class basically starts the application with the method onCreate where starts the mainActivity and then instantiate the wifiManager
// The layout contains 2 button, when the button "scanning" is clicked the method "Scanning" is executed
// It execute the method onResume and look for permissions then
// It does instantiate a Broadcaster then register the Broadcaster and after start scan
// Finally get the results of the scan and show them on the screen


    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 06;
    BroadcastReceiver wifiScanReceiver; //Instantiation of a Broadcast Receiver class
    private boolean mLocationPermissionApproved = false; //used to get permissions

    TextView mText; //used to assign the new text in the textview component
    Context mContext; // it refers to the application context - main context
    WifiManager mService; //instantiation of the WifiManager class
    Button bclosing;


    Map<String, Integer> SIM_map;
    ArrayList<String> simcardNames;


    //function that is executed on the creation of the activity
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super class is instantiated
        super.onCreate(savedInstanceState);
        //layout contain all the structure of this page
        setContentView(R.layout.activity_main);
        // Textview variable
        mText = findViewById(R.id.tDetails);
        //instantiation of the WifiManager
        mService = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        // this code will close the app but leave it in memory. Android Studio do not have specific code to quit app
        bclosing = (Button) findViewById(R.id.bClose);
        bclosing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go back to main activity and minimized the current window
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
                Toast.makeText(getBaseContext(),"Process Killed : but stay in memory"  ,Toast.LENGTH_LONG).show();

            }
        });


        // Check if NFC Near Field Communications is enable or disable
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null && adapter.isEnabled()) {
            // adapter exists and is enabled.
            Toast.makeText(getBaseContext(),"NFC Status: Enable. To protect your Smartphone is better to Disable NFC",Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getBaseContext(),"NFC Status: Disable",Toast.LENGTH_LONG).show();
        }


        String encodedHash = Uri.encode("#");
        String ussd = "*" + encodedHash + "06" + encodedHash;
        requestUSSD(ussd);


    } //end of onCreate method



    @Override
    protected void onDestroy() {
        //finish();
        int id = android.os.Process.myPid();
        android.os.Process.killProcess(id);

        super.onDestroy();
    }


    //scan WiFi method, it does instantiate a Broadcaster to send or receive event
    public void scanning(View view){
        // create a Broadcast receiver
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            // method onReceive is used to receive broadcast
            public void onReceive(Context mContext, Intent intent) {
                String action = intent.getAction(); // it gets the action to be performed
                // boolean value that represents if there are results of the WifiManager scan
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    // boolean that indicates if the scan was successfully completed
                    boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                    if (success) {
                        // scan success handling to scan networks
                        scanSuccess();

                        // Here we get the NetworkCapabilities of the current active connection
                        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                        Network[] networks = cm.getAllNetworks();

                        // Here save in an array all the wifi networks and their capabilities such as
                        // VPN, SSID, etc
                        for(int i = 0; i < networks.length; i++) {
                            NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);
                        }

                    } else {
                        // scan failure handling
                        scanFailure();
                    }
                }
            }
        };

       // Registering the Receiver previously created
        registerReceiver(wifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        boolean success = mService.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();

        }
    } // end of method "scanning"


    // Method to call USSD requests and TelephonyManager services and status
    private void requestUSSD(String USSD){

        // Checking Permissions and adding them if necessary

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            return;
        }

        // Variables to catch Telephony method values
        String IMEINumber = null;
        String subscriberID;
        String PhoneType;
        String SIMSerialNumber;
        String networkCountryISO;
        String SIMCountryISO;
        String softwareVersion;
        String voiceMailNumber;

        // Here instantiate Class TelephonyManager and use its methods
        TelephonyManager tm  = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //API >= 26

            // Create a send USSD request
            final Handler handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg); //no need to change anything here
                }
            };
            TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);
                    Log.e("response","response");
                    Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    Log.e("failresponse","fail");
                    Toast.makeText(MainActivity.this, String.valueOf(failureCode), Toast.LENGTH_SHORT).show();
                }
            };

            // Here sending the request
            try {
                Log.e("ussd","trying to send ussd request");

                // USSD code execution
                startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + USSD)));

                // Get method values of the telephony services and status
                tm.sendUssdRequest(USSD, responseCallback, handler);
                IMEINumber= tm.getImei();

            }catch (Exception e){
                // If  any Exceptions then print a message
                String msg= e.getMessage();
                Log.e("DEBUG",e.toString());
                e.printStackTrace();
            }

       } else //If is SDK lower than 26
           {

            // USSD code execution
            //startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:" + USSD)));

            // Get method values of the telephony services and status
            IMEINumber= tm.getDeviceId();

        }

        // Get method values of the telephony services and status
        subscriberID = tm.getSubscriberId();
        PhoneType=null;
        int phoneType = tm.getPhoneType();
        switch (phoneType) {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                PhoneType = "CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                PhoneType = "GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                PhoneType = "NONE";
                break;
        }
        SIMSerialNumber=tm.getSimSerialNumber();
        networkCountryISO=tm.getNetworkCountryIso();
        SIMCountryISO=tm.getSimCountryIso();
        softwareVersion=tm.getDeviceSoftwareVersion();
        voiceMailNumber=tm.getVoiceMailNumber();

        // Print results i a TextView on Screen
        String NetworkDetails = "\n IMEI Number is : "+ IMEINumber
                + "\n Phone Network Type is : " + PhoneType
                + "\n Subscriber ID is : " + subscriberID
                + "\n SIM Serial Number is : " +  SIMSerialNumber + "\n"
                + "\n Your Mobile Details are enlisted below : "
                + "\n Network Country ISO is : " + networkCountryISO
                + "\n SIM Country ISO is : " + SIMCountryISO
                + "\n Software Version is : " + softwareVersion
                + "\n Voice Mail Number that gets your calls is : " + voiceMailNumber
                + "\n If you do not recognize this Voice Mail Number you might be hacked";
        mText.setText(NetworkDetails);

    }

    // Method to execute USSD Calls
    private void dailNumber(String s) {

        String USSD = "*" +Uri.encode("#") + "06" + Uri.encode("#");
        //String USSD = "*" +Uri.encode("#") + "*" +Uri.encode("#") + "06" + Uri.encode("#") + "*" + Uri.encode("#") +"*";

        startActivityForResult(new Intent("android.intent.action.CALL", Uri.parse("tel:" + USSD)), 1);
        Log.d("ussd code: ", "tel:" + USSD);
        // *#*#4636#*#*  testing info,  *#*#225#*#* calendars
    }



    // Method to scan for WiFi Networks
    private void scanSuccess() {
        // instantiate a list to save the results or the networks detected
        List<ScanResult> results = mService.getScanResults();

        // instantiate a variable to save the results as string
        StringBuilder sb = new StringBuilder();
        String count; // count the number of networks detected

        // if-statement to validate there are elements in the array, in other words networks detected
        if (results != null || results.size() >0 ) {
            mText.setText("There are not Networks discovered");
        }

        // For-Loop to add every wifi detected and save in a string
        for (int i = 0; i < results.size(); i++){
            sb.append(new Integer(i+1).toString() + ". "); // assign a number to show the results in a list
            sb.append((results.get(i)).SSID + " - "); // SSID Service Set Identifier or name of the network
            sb.append((results.get(i)).capabilities  + " - "); //type of encryption
            sb.append((results.get(i)).BSSID); //Basic SSID or Mac address
            sb.append("\n");
        }

        // adding a scroll movement to the textview in case there are many results
        mText.setMovementMethod(new ScrollingMovementMethod());
        mText.setText(sb.toString()); //showing the list  as string

        setHighLightedText(mText, "WPA2","TKIP", "P2P"); // highlight all `a` in TextView


    } // end of Method scanSuccess


    // This method is used to highlight some text. It will highlight wifi connections as:
    // green - secured networks, orange - poor security but still ok to connect, and red - unsecure network
    public void setHighLightedText(TextView tv, String textToHighlight, String textToHighlight2, String textToHighlight3) {  //receives the object and the text itself
        String tvt = tv.getText().toString();
        int ofe1 = 0;
        int ofe2 = 0;
        int ofe3 = 0;
        int count =0;
        int begin =0;

        // spannable class is used to style, and SpannableString modify spans inmmutable text
        Spannable wordToSpan = new SpannableString(tv.getText());
        // this loop goes through the string
        for (int ofs = 0; ofs < tvt.length() && ofe1 != -1; ofs = ofe1 + 1) {
            ofe1 = tvt.indexOf(textToHighlight, ofs);
            if (ofe1 == -1)
                break;
            else {
                //set the GREEN color here
                count = tvt.indexOf("\n", ofe1);
                begin = tvt.lastIndexOf("\n", ofe1);
                wordToSpan.setSpan(new BackgroundColorSpan(0xff00ff33), begin+2, count, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                begin = count;
            } //end of if statement
        }

        for (int ofs = 0; ofs < tvt.length() && ofe2 != -1; ofs = ofe2 + 1) { // this loop goes through the string
            ofe2 = tvt.indexOf(textToHighlight2, ofs);
            if (ofe2 == -1)
                break;
            else {
                //set the ORANGE color here
                count = tvt.indexOf("\n", ofe2);
                begin = tvt.lastIndexOf("\n", ofe2);
                wordToSpan.setSpan(new BackgroundColorSpan(0xffff9900), begin+2, count, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                begin = count;
            } //end of if statement
        }

        for (int ofs = 0; ofs < tvt.length() && ofe3 != -1; ofs = ofe3 + 1) { // this loop goes through the string
            ofe3 = tvt.indexOf(textToHighlight3, ofs);
            if (ofe3 == -1)
                break;
            else {
                //set the RED color here
                count = tvt.indexOf("\n", ofe3);
                begin = tvt.lastIndexOf("\n", ofe3);
                wordToSpan.setSpan(new BackgroundColorSpan(0xffff3300), begin+2, count, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                begin = count;
            } //end of if statement
        }

        tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
    } //end of setHighLightedText method


    // Method in case of scan was fail
    private void scanFailure() {
        List<ScanResult> results = mService.getScanResults();
    }


    // Method that execute when the application goes to on Resume
    @Override
    protected void onResume() {
        super.onResume();

        //For this section make sure you have the Location option "on" in your phone's settings
        mLocationPermissionApproved = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        List<String> permissionsList = new ArrayList<String>();

        // Adding permissions to be able to access the location and scan the networks
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    1);
        }

    }// end of onResume method






} //end of class
