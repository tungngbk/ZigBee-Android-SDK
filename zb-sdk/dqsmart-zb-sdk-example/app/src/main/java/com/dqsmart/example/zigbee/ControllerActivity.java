package com.dqsmart.example.zigbee;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ControllerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_EXAMPLE = 0x9345;
    private static final int REQUEST_CODE_EXAMPLE2 = 0x9349;
//    Button switchBtn;
    SwitchCompat switchCompat;
    TextView deviceNameText;
    Button mBtnChangeName, mBtnAcceptRename;
    Boolean isOn;
    String newDeviceName;
    Dialog mPopUpRename;
    EditText textRename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        SharedPreferences statusMap = getSharedPreferences("statusMap", Context.MODE_PRIVATE);
        SharedPreferences devName = getSharedPreferences("devName", Context.MODE_PRIVATE);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menuHome:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menuVoice:
                        startActivity(new Intent(getApplicationContext(), VoiceActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        isOn = getIntent().getBooleanExtra("isOn", false);
        String deviceName = getIntent().getStringExtra("deviceName");
//        Toast.makeText(this, "day: " + deviceName, Toast.LENGTH_SHORT).show();
        deviceNameText = findViewById(R.id.deviceName);
        deviceNameText.setText(deviceName);
        final int position = getIntent().getIntExtra("position",5);
//        switchBtn = (Button) findViewById(R.id.switchBtn);
        switchCompat = (SwitchCompat) findViewById(R.id.switchBtn);
        switchCompat.setChecked(isOn);
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchCompat.isChecked()){
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    intent.putExtra("newStatus", true);
                    intent.putExtra("position", position);
//                    switchCompat.setChecked(true);
                    startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
                }
                else {
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    intent.putExtra("newStatus", false);
                    intent.putExtra("position", position);
//                    switchCompat.setChecked(false);
                    startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
                }
            }
        });

        mPopUpRename = new Dialog(this);
        mBtnChangeName = findViewById(R.id.buttonRename);
        mBtnChangeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopUpRename.setContentView(R.layout.rename_popup);
                mPopUpRename.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                textRename = mPopUpRename.findViewById(R.id.textRename);
                mBtnAcceptRename = mPopUpRename.findViewById(R.id.acceptRename);
                mPopUpRename.show();

                mBtnAcceptRename.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        newDeviceName = textRename.getText().toString();
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        intent.putExtra("newDeviceName", newDeviceName);
                        intent.putExtra("position", position);
                        startActivityForResult(intent, REQUEST_CODE_EXAMPLE2);
                        mPopUpRename.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_EXAMPLE){
            if(resultCode == Activity.RESULT_OK){
                final String result = data.getStringExtra(MainActivity.EXTRA_DATA);
//                Toast.makeText(this, "Result: " + result, Toast.LENGTH_SHORT).show();
            }
            else{

            }
        }
        if(requestCode == REQUEST_CODE_EXAMPLE2){
            if(resultCode == Activity.RESULT_OK){
                final String result = data.getStringExtra(MainActivity.EXTRA_DATA);
//                Toast.makeText(this, "Result: " + result, Toast.LENGTH_SHORT).show();
            }
            else{

            }
        }
    }

}