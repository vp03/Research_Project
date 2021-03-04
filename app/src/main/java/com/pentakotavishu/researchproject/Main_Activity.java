package com.pentakotavishu.researchproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class Main_Activity extends AppCompatActivity {
    private Button relocate, test;
    private TextView textView;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private SensorManager mSensorManager;
    private long lastUpdate;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    public List<String> input;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private StorageReference mStorageRef;
    AppCompatActivity act;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Intent intent;
    private MediaPlayer mediaPlayer1;

    //hello

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment
        refresh(ft);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        act = this;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);
        relocate = findViewById(R.id.relocate);
        test = findViewById(R.id.test);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        mediaPlayer1 = new MediaPlayer();


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }

        });
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        lastUpdate = 0;
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }
            @Override
            public void onBeginningOfSpeech() {
            }
            @Override
            public void onRmsChanged(float rmsdB) {
            }
            @Override
            public void onBufferReceived(byte[] buffer) {
            }
            @Override
            public void onEndOfSpeech() {
            }
            @Override
            public void onError(int error) {
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                textView.setText("");
                if (matches != null){
                    string = matches.get(0);
                    textView.setText(string);
                    if (string.equals("instructions")) {
                        textToSpeech.speak("To make a new recording. Shake. Wait for the first beep, then say new recording", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else if (string.equals("new recording")) {
                        Intent intent = new Intent(Main_Activity.this, My_Post.class);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onPartialResults(Bundle partialResults) {
            }
            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }
    public void refresh(FragmentTransaction ft) {
        File f = new File(Environment.getExternalStorageDirectory(), "Download");
        // Get all the names of the files present
        // in the given directory
        File[] files = f.listFiles();
        input = new ArrayList<>();
        for(int x=0; x < files.length; x++) {
            if(files[x].getName().contains(".3gp")) {
                input.add(files[x].getName());
                System.out.println("ARRAY LIST SIZE: " + input.size());
            }
        }
        ft.replace(R.id.your_placeholder, new Refresh_Fragment(input));
        //ft.add(R.id.your_placeholder, new Refresh_Fragment(input));
        // Complete the changes added above
        ft.commit();
    }
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 300) {
                lastUpdate = curTime;
                if (mAccel > 15) {
                    Toast.makeText(getApplicationContext(), "Shake event detected", Toast.LENGTH_SHORT).show();
                    textToSpeech.speak("Say instructions to get assistance.", TextToSpeech.QUEUE_FLUSH, null, null);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    speechRecognizer.startListening(intent);
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }
    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    public void make_recording (View view){
        Intent intent = new Intent(Main_Activity.this, My_Post.class);
        startActivity(intent);
    }

    public void test (View view)
    {
        /*
        StorageReference riversRef = mStorageRef.child("audio/1614107369410.3gp");
        File localFile = null;
        try {
            localFile = File.createTempFile("audio", "3gp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        riversRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Successfully downloaded data to local file
                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle failed download
                // ...
            }
        });
        */
        downloadFile();
    }
    private void downloadFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://research-project-98fa1.appspot.com/audio");
        StorageReference islandRef = storageRef.child("1614879745143.3gp");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Download");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }
        String name = "Be_Heard/"; // +fireabse name;
        final File localFile = new File(rootPath,"tester_file.3gp");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }

    public void play_recording(View view) {
        TextView audio = (TextView) view;
        audio.getText();

        File audio_file = new File(Environment.getExternalStorageDirectory(), "Download");; // initialize Uri here

        String filename = "Filename is: " + Environment.getExternalStorageDirectory() + "/Download";
        System.out.println(audio.getText());
        System.out.println(audio_file.getName());

        mediaPlayer1.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        //Uri uri = FileProvider.getUriForFile(this.getApplicationContext(), BuildConfig.APPLICATION_ID, audio_file);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Download/" + audio.getText());
        try {
            mediaPlayer1.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer1.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer1.start();
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
     */

}
