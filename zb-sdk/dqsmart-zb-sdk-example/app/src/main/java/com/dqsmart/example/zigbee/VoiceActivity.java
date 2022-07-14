package com.dqsmart.example.zigbee;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class VoiceActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_EXAMPLE = 0x9345;
    SharedPreferences sharedPreferences;
    ImageButton imageButton;
    TextView editText;
    SpeechRecognizer speechRecognizer;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        sharedPreferences = getSharedPreferences("devName", Context.MODE_PRIVATE);
        Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();


        imageButton = findViewById(R.id.testbtn);

        editText = findViewById(R.id.testedit);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},1);
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(count == 0){
                     imageButton.setImageDrawable(getDrawable(R.drawable.ic_voice_on ));
                     speechRecognizer.startListening(speechRecognizerIntent);
                     count = 1;
                 }
                 else {
                     imageButton.setImageDrawable(getDrawable(R.drawable.ic_voice_off ));
                     speechRecognizer.stopListening();
                     count = 0;
                 }
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);

                Set<String> set = map.keySet();
                String voiceResult = data.get(0).toLowerCase();
                for(String key : set){
                    if(voiceResult.contains(map.get(key).toLowerCase())){
                        if(voiceResult.contains("bật") || voiceResult.contains("bậc") || voiceResult.contains("mở")){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("newStatus", true);
                            intent.putExtra("voiceKey", key);
                            startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
                        }
                        else if(voiceResult.contains("tắt")  || voiceResult.contains("ngắt") || voiceResult.contains("ngắc")){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("newStatus", false);
                            intent.putExtra("voiceKey", key);
                            startActivityForResult(intent, REQUEST_CODE_EXAMPLE);
                        }
                    }

                }
                editText.setText(data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.menuVoice);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menuHome:
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.menuVoice:
                        return true;
                }
                return false;
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}