package com.dqsmart.example.zigbee;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dqsmart.zigbee.DqsZbNwkManager;
import com.dqsmart.zigbee.ZigbeeConnectionCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UsbService extends Service {
    private static final String TAG = UsbService.class.getSimpleName();
    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    private static final String ACTION_USB_PERMISSION = "com.android.smart.USB_PERMISSION";

    public static final String ACTION_INIT_USB_DEVICE = "action_find_usb_device";

    public static boolean SERVICE_CONNECTED = false;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static Handler mainHandler;
    private Handler mServiceHandler;

    private UsbManager usbManager;
    private UsbDevice mUsbDevice;
    private boolean serialPortConnected;
    private static boolean notify;
    public static final Boolean isAndroidO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

    private IBinder iBinder = new UsbBinder();
    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     * About BroadcastReceiver: http://developer.android.com/reference/android/content/BroadcastReceiver.html
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
        if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
            boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
            if (granted) // User accepted our USB connection. Try to open the device as a serial port
            {
                Log.d(TAG, "USB Permission granted, connect to usb");
                connectUsb();
            } else
            {
                Log.d(TAG, "USB Permission not granted, connect to usb");
            }
        } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
            if (!serialPortConnected)
                findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
        } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
            disconnect();
        }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        notify = true;
        mServiceHandler = new Handler(Looper.getMainLooper());
        serialPortConnected = false;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (isAndroidO && notify) {
            createNotificationChannel();
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText("start")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
            notify = false;
        }

        handleServiceIntent(intent);
        return Service.START_STICKY;
    }

    private void handleServiceIntent(Intent intent) {
        Log.d(TAG, "Handle service intent: " + intent);
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_INIT_USB_DEVICE:
                        findSerialPortDevice();
                        break;
                    default:
                        Log.d(TAG, "Unknown action: " + action);
                        break;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null)
            manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;
        mServiceHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(usbReceiver);
    }


    private ConcurrentMap<Integer, Integer> mCheckedDevicePidMap = new ConcurrentHashMap<>();
    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                mUsbDevice = entry.getValue();
                int deviceVID = mUsbDevice.getVendorId();
                int devicePID = mUsbDevice.getProductId();
                Log.d(TAG, String.format("deviceVID: 0x%04X (%d), devicePID: 0x%04X (%d)", deviceVID, deviceVID, devicePID, devicePID));

                if (deviceVID != 0x1d6b && (devicePID != 0x0001 && devicePID != 0x0002 && devicePID != 0x0003)) {
                    requestUserPermission();
                    keep = false;
                } else {
                    Log.d(TAG, "Device VID and PID not matched");
                    mUsbDevice = null;
                }

                if (!keep)
                    break;
            }
        } else {
            Log.d(TAG, "Find serial port, no USB found");
        }
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }

    /*
     * Request user permission. The response will be received in the BroadcastReceiver
     */
    private void requestUserPermission() {
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        usbManager.requestPermission(mUsbDevice, mPendingIntent);
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private void connectUsb() {
        Log.d(TAG, "Start connect to USB device");
        DqsZbNwkManager.getInstance().connect(mUsbDevice, new ZigbeeConnectionCallback() {
            @Override
            public void onConnected() {
                Log.d(TAG, "On zigbee connected");
            }

            @Override
            public void onError(Exception error) {
                Log.d(TAG, "On zigbee connect error: " + error.getMessage());
            }
        });
    }

    private void disconnect() {
        serialPortConnected = false;
        long macUsb = DqsZbNwkManager.getInstance().getZbMacAddress();
        String data =  DqsZbNwkManager.getInstance().exportZigBeeDevice(macUsb);
        Log.d(TAG, "data: " + data);
        DqsZbNwkManager.getInstance().disconnect();
    }
}
