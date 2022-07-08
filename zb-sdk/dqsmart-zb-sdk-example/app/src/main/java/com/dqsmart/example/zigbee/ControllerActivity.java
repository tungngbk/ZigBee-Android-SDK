package com.dqsmart.example.zigbee;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ControllerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_EXAMPLE = 0x9345;
    Button switchBtn;
    Boolean isOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        isOn = getIntent().getBooleanExtra("isOn", false);
        final int position = getIntent().getIntExtra("position",5);
        switchBtn = (Button) findViewById(R.id.switchBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean newStatus = (isOn) ? false : true;
                isOn = newStatus;
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                intent.putExtra("newStatus", newStatus);
                intent.putExtra("position", position);
//                view.getContext().startActivity(intent);
                startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
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
    }

}