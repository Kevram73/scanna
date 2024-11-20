package com.example.scanna;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity implements IAsynchronousMessage {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_READ_PHONE_STATE = 1;

    private static final String METHOD_CHANNEL = "com.example.scanna/method";
    private static final String EVENT_CHANNEL = "com.example.scanna/events";

    private boolean isReaderInitialized = false;
    private boolean isReading = false;
    private EventChannel.EventSink eventSink;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        // Configure MethodChannel for invoking native functions from Flutter
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), METHOD_CHANNEL)
                .setMethodCallHandler((call, result) -> {
                    switch (call.method) {
                        case "initializeReader":
                            result.success(initializeReader());
                            break;
                        case "startReading":
                            startReading();
                            result.success(null);
                            break;
                        case "stopReading":
                            result.success(stopReading());
                            break;
                        case "setAntennaPower":
                            int power = call.argument("power");
                            result.success(setAntennaPower(power));
                            break;
                        case "getAntennaPower":
                            result.success(getAntennaPower());
                            break;
                        default:
                            result.notImplemented();
                            break;
                    }
                });

        // Configure EventChannel for streaming tag data to Flutter
        new EventChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), EVENT_CHANNEL)
                .setStreamHandler(new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object arguments, EventChannel.EventSink events) {
                        eventSink = events;
                    }

                    @Override
                    public void onCancel(Object arguments) {
                        eventSink = null;
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_READ_PHONE_STATE
            );
        } else {
            initializeReader();
        }
    }

    private boolean initializeReader() {
        try {
            isReaderInitialized = UHFReader.getUHFInstance().OpenConnect(this);
            if (!isReaderInitialized) {
                Log.e(TAG, "Failed to initialize UHF Reader.");
                return false;
            }
            UHFReader._Config.SetEPCBaseBandParam(255, 1, 1, 0); // Default baseband parameters
            UHFReader._Config.SetANTPowerParam(1, 28); // Default antenna power
            Log.d(TAG, "UHF Reader initialized successfully.");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during UHF Reader initialization: " + e.getMessage(), e);
            return false;
        }
    }

    private void startReading() {
        if (!isReaderInitialized) {
            Log.e(TAG, "UHF Reader is not initialized.");
            return;
        }
        if (isReading) {
            Log.w(TAG, "Reader is already in reading mode.");
            return;
        }
        int result = UHFReader._Tag6C.GetEPC(1, 1); // Start reading EPC
        if (result == 0) {
            isReading = true;
            Log.d(TAG, "Started reading tags.");
        } else {
            Log.e(TAG, "Failed to start reading tags.");
        }
    }

    private boolean stopReading() {
        if (!isReaderInitialized || !isReading) {
            Log.w(TAG, "Cannot stop reading. Reader is either not initialized or not reading.");
            return false;
        }
        String result = UHFReader.getUHFInstance().Stop();
        if ("0".equals(result)) {
            isReading = false;
            Log.d(TAG, "Stopped reading tags.");
            return true;
        } else {
            Log.e(TAG, "Failed to stop reading tags.");
            return false;
        }
    }

    private boolean setAntennaPower(int power) {
        try {
            int result = UHFReader._Config.SetANTPowerParam(1, power);
            return result == 0;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set antenna power: " + e.getMessage(), e);
            return false;
        }
    }

    private int getAntennaPower() {
        try {
            return UHFReader._Config.GetANTPowerParam();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get antenna power: " + e.getMessage(), e);
            return -1;
        }
    }

    @Override
    public void OutPutEPC(EPCModel model) {
        if (eventSink != null) {
            String tagData = String.format("EPC: %s, RSSI: %s", model._EPC, model._RSSI);
            eventSink.success(tagData);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReaderInitialized) {
            UHFReader.getUHFInstance().CloseConnect();
            Log.d(TAG, "UHF Reader connection closed.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeReader();
        } else {
            Log.e(TAG, "Permission denied. Cannot initialize UHF Reader.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
