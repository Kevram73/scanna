package com.example.scanna;

import androidx.annotation.NonNull;

import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends FlutterActivity implements IAsynchronousMessage {

    private static final String METHOD_CHANNEL = "com.example.scanna/method";
    private static final String EVENT_CHANNEL = "com.example.scanna/events";

    private EventChannel.EventSink eventSink;
    private ExecutorService executor;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        // MethodChannel for RFID operations
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
                        case "setPower":
                            int power = call.argument("power");
                            result.success(setAntennaPower(power));
                            break;
                        case "getPower":
                            result.success(getAntennaPower());
                            break;
                        default:
                            result.notImplemented();
                            break;
                    }
                });

        // EventChannel for streaming tag data
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

        executor = Executors.newSingleThreadExecutor();
    }

    // Initialize the RFID reader
    private boolean initializeReader() {
        try {
            return UHFReader.getUHFInstance().OpenConnect(this);
        } catch (Exception e) {
            return false;
        }
    }

    // Start reading tags
    private void startReading() {
        if (UHFReader.getUHFInstance() != null) {
            UHFReader._Tag6C.GetEPC(1, 1);
        }
    }

    // Stop reading tags
    private boolean stopReading() {
        try {
            return UHFReader.getUHFInstance().Stop().equals("0");
        } catch (Exception e) {
            return false;
        }
    }

    // Set antenna power
    private boolean setAntennaPower(int powerValue) {
        return UHFReader._Config.SetANTPowerParam(1, powerValue) == 0;
    }

    // Get current antenna power
    private int getAntennaPower() {
        return UHFReader._Config.GetANTPowerParam();
    }

    // Handle tag data callback
    @Override
    public void OutPutEPC(EPCModel model) {
        if (eventSink != null) {
            executor.execute(() -> {
                String tagData = String.format("EPC: %s, RSSI: %s", model._EPC, model._RSSI);
                eventSink.success(tagData);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
        stopReading();
        UHFReader.getUHFInstance().CloseConnect();
    }
}
