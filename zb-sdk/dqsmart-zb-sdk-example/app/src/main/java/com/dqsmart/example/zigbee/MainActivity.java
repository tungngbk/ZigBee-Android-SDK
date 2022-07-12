package com.dqsmart.example.zigbee;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dqsmart.zigbee.AddDeviceCallback;
import com.dqsmart.zigbee.DqsZbNwkManager;
import com.dqsmart.zigbee.LogManager;
import com.dqsmart.zigbee.ZigbeeCallback;
import com.dqsmart.zigbee.cache.CacheManager;
import com.dqsmart.zigbee.core.device.DeviceModel;
import com.dqsmart.zigbee.core.device.ZigbeeDevice;
import com.dqsmart.zigbee.core.device.ZigbeeDeviceInfo;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_DATA = "EXTRA_DATA";

    private static final String TAG = "MainActivity";
    private Dialog dialog;
    private LinearLayout mGetDevInfo;
    private LinearLayout mStartPairing;
    private LinearLayout mResetFactory;
    private LinearLayout mListDevices;

    private TextView mDevIdText;
    Dialog mPopUpInfo;
    Dialog mPopUpModelList;
    Dialog mPopUpDeviceList;

    private Handler mHandler;
    private Spinner mModelList;
    private Button modelSelectedButton;

    private List<ZigbeeDevice> mZigbeeDevices = new ArrayList<>();
    private DeviceListAdapter mDeviceAdapter;
    private RecyclerView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // New
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menuHome);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menuHome:
                        return true;
                    case R.id.menuVoice:
                        startActivity(new Intent(getApplicationContext(), VoiceActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });


        //
        mHandler = new Handler(Looper.getMainLooper());
        DqsZbNwkManager.getInstance().setLogLevel(LogManager.LOG_DEBUG);
        DqsZbNwkManager.getInstance().registerCallback(mZbCallback);
        startUsbService();


        mDeviceAdapter = new DeviceListAdapter(getApplicationContext());
        mDeviceAdapter.setHasStableIds(true);
        mDeviceAdapter.setOnDeviceListClickListener(mOnDeviceListClickListener);


//
//        mDeviceAdapter.setZigbeeDevices(CacheManager.getInstance().getAllDevices());

        // Get ZigBee USB ID
        mPopUpInfo = new Dialog(this);
        mGetDevInfo = findViewById(R.id.layoutInfo);
        mGetDevInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceId = DqsZbNwkManager.getInstance().getDeviceId();
                mPopUpInfo.setContentView(R.layout.info_popup);
                mPopUpInfo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDevIdText = mPopUpInfo.findViewById(R.id.text_device_id);
                mDevIdText.setText(deviceId);
                mPopUpInfo.show();
                Log.d(TAG, "Test get device ID: " + deviceId);
            }
        });

        // Start paring with Model
        List<String> list = new ArrayList<String>();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("SMART SOCKET", DeviceModel.ZB_SMART_SOCKET);
        hashMap.put("DOOR LOCK", DeviceModel.ZB_DOOR_LOCK);
        hashMap.put("RE6 SWITCH", DeviceModel.ZB_RE6_SWITCH);
        hashMap.put("SMOKE SENSOR", DeviceModel.ZB_SMOKE_SENSOR);
//        list.add(DeviceModel.ZB_SMART_SOCKET);
//        list.add(DeviceModel.ZB_DOOR_LOCK);
//        list.add(DeviceModel.ZB_RE6_SWITCH);
//        list.add(DeviceModel.ZB_SMOKE_SENSOR);
        list.add("SMART SOCKET");
        list.add("DOOR LOCK");
        list.add("RE6 SWITCH");
        list.add("SMOKE SENSOR");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mPopUpModelList = new Dialog(this);
        mStartPairing = findViewById(R.id.layoutPair);
        mStartPairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopUpModelList.setContentView(R.layout.model_list_popup);
                mPopUpModelList.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mModelList = mPopUpModelList.findViewById(R.id.spinnerModel);
                mModelList.setAdapter(dataAdapter);
                modelSelectedButton = mPopUpModelList.findViewById(R.id.modelSelBtn);
                mPopUpModelList.show();

                modelSelectedButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String modelId = (String) hashMap.get(mModelList.getSelectedItem());
                        Log.d(TAG, "get model id: " + modelId);
                        Toast.makeText(MainActivity.this, "Select model " + modelId, Toast.LENGTH_SHORT).show();
                        DqsZbNwkManager.getInstance().addDeviceByModel(modelId, 60, mAddDeviceCallBack);
                    }
                });
            }
        });

        mDeviceAdapter.setZigbeeDevices(CacheManager.getInstance().getAllDevices());

        // List ZigBee devices paired
        mPopUpDeviceList = new Dialog(this);
        mListDevices = findViewById(R.id.layoutList);
        mListDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopUpDeviceList.setContentView(R.layout.devices_list_popup);
                mPopUpDeviceList.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDeviceListView = mPopUpDeviceList.findViewById(R.id.rcv_device_list);
                LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
                mDeviceListView.setLayoutManager(layoutManager);
                mDeviceListView.setAdapter(mDeviceAdapter);
                mPopUpDeviceList.show();
            }
        });

        if(getIntent().hasExtra("newDeviceName")){
            String newDeviceName = getIntent().getStringExtra("newDeviceName");
            int position = getIntent().getIntExtra("position",0);
            if(!mDeviceAdapter.getmZigbeeDevices().isEmpty()){
                ZigbeeDevice zigbeeDevice = mDeviceAdapter.getmZigbeeDevices().get(position);
                mDeviceAdapter.setCheckPosition(position);
                mDeviceAdapter.setNewDevName(newDeviceName);
            }
//            Intent data = new Intent();
//            data.putExtra(EXTRA_DATA, "nhan r he");
//            setResult(Activity.RESULT_OK, data);
//            finish();
        }

        if(getIntent().hasExtra("newStatus")){
            int newStatus = getIntent().getBooleanExtra("newStatus", false) ? 1 : 0;
            mDeviceAdapter.getmDevStatusMap().put(String.format("%04X", R.id.text_dev_src_address) + "." + R.id.text_dev_endpoint, newStatus);
            int position = getIntent().getIntExtra("position",0);
            if(!mDeviceAdapter.getmZigbeeDevices().isEmpty()){
                ZigbeeDevice zigbeeDevice = mDeviceAdapter.getmZigbeeDevices().get(position);
                if(mDeviceAdapter.getmListener() != null){
                    mDeviceAdapter.getmListener().onDeviceStatusClick(zigbeeDevice, newStatus);
                }
            }
            Intent data = new Intent();
            data.putExtra(EXTRA_DATA, "nhan r ha");
            setResult(Activity.RESULT_OK, data);
            finish();
        }

        // Reset Factory
        mResetFactory = findViewById(R.id.layoutReset);
        mResetFactory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DqsZbNwkManager.getInstance().resetFactory();
                Toast.makeText(MainActivity.this, "Reset successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private DeviceListAdapter.OnDeviceListClickListener mOnDeviceListClickListener = new DeviceListAdapter.OnDeviceListClickListener() {
        @Override
        public void onDeviceStatusClick(ZigbeeDevice zigbeeDevice, int newStatus) {
            Log.d(TAG, "On device status click, srcMac: " + zigbeeDevice.getSrcAddress() +
                    ", devEndpoint: " + zigbeeDevice.getEndpoint() +
                    ", newStatus: " + newStatus);
            if (zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_LIGHT || zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_SWITCH) {
                boolean success = DqsZbNwkManager.getInstance().switchControl(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint(), newStatus);
                Log.d(TAG, "Send switch control command success? " + success);
            } else if(zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_LOCK){
                boolean success = DqsZbNwkManager.getInstance().lockUnlockControl(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint(), newStatus);
                Log.d(TAG, "Send lock_unlock control command success? " + success);
            } else if(zigbeeDevice.getDeviceType() == ZigbeeDeviceInfo.DEVICE_TYPE_COVER){
//                final Integer value = (Integer) mModeCover.getSelectedItem();
//                boolean success = DqsZbNwkManager.getInstance().coverControl(zigbeeDevice.getSrcAddress(), zigbeeDevice.getEndpoint(), value);
//                boolean success1 = DqsZbNwkManager.getInstance().deviceVersionRequest(zigbeeDevice.getSrcAddress());
//                Log.d(TAG, "Send lock_unlock control command success? " + success + String.format("value: %d", value) + "enpoint: " + zigbeeDevice.getEndpoint());
            }
        }
    };


    private void startUsbService() {
        Intent intent = new Intent(this, UsbService.class);
        intent.setAction(UsbService.ACTION_INIT_USB_DEVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DqsZbNwkManager.getInstance().unregisterCallback(mZbCallback);
    }

    private ZigbeeCallback mZbCallback = new ZigbeeCallback() {
        @Override
        public void onUpdateStatus(int shortMac, int subDeviceId, int messageType, int value) {
            Log.d(TAG, "On attribute update: " + String.format("ShortMac %04X, subDevId %d, msgType %d, data %s",
                    shortMac, subDeviceId, messageType, value));
            mDeviceAdapter.updateDevStatus(shortMac, subDeviceId, value);
        }
    };

    private AddDeviceCallback mAddDeviceCallBack = new AddDeviceCallback() {
        @Override
        public void onResult(List<ZigbeeDevice> list) {
            Log.d(TAG, "On add device result: " + list);
            List<ZigbeeDevice> newDevices = new ArrayList<>(list);
            mZigbeeDevices.addAll(newDevices);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDeviceAdapter.setZigbeeDevices(mZigbeeDevices);
                    Toast.makeText(MainActivity.this, "Add Zigbee device successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onTimeout() {
            Log.d(TAG, "On add device timeout");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Add Zigbee device timeout", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    public void onBackPressed() {

        // đặt resultCode là Activity.RESULT_CANCELED thể hiện
        // đã thất bại khi người dùng click vào nút Back.
        // Khi này sẽ không trả về data.
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }
}
