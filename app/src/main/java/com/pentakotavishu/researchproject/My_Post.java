package com.pentakotavishu.researchproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class My_Post extends AppCompatActivity {
    private Button startbtn, playbtn, upload;
    private TextView textView;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private boolean active;
    private boolean playing;
    private int counter1;
    private int counter2;
    private static final String LOG_TAG = "AudioRecording";
    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private StorageReference mStorageRef;
    AppCompatActivity act;
    private SensorManager mSensorManager;
    private long lastUpdate;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        startbtn = findViewById(R.id.btnRecord);
        //stopbtn = findViewById(R.id.btnRecord);
        playbtn = findViewById(R.id.btnPlay);
        upload = findViewById(R.id.btnUpload);
        textView = findViewById(R.id.textView);
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";
        mStorageRef = FirebaseStorage.getInstance().getReference();
        act = this;
        active = false;
        playing = false;
        counter1 = 0;
        counter2 = 0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
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
                //  textToSpeech.speak("Ready for speech", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            @Override
            public void onBeginningOfSpeech() {
                // textToSpeech.speak("beginning of speech", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            @Override
            public void onRmsChanged(float rmsdB) {
                //  textToSpeech.speak("rms changed", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            @Override
            public void onBufferReceived(byte[] buffer) {
                // textToSpeech.speak("buffer received", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            @Override
            public void onEndOfSpeech() {
                // textToSpeech.speak("end of speech", TextToSpeech.QUEUE_FLUSH, null, null);
            }
            @Override
            public void onError(int error) {
            }
            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matches = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                String string = "";
                textView.setText("");
                if (matches != null) {
                    string = matches.get(0);
                    textView.setText(string);
                    if (string.equals("instructions")) {
                        textToSpeech.speak("To start a new recording. Shake. Wait for the first beep, then say start recording. Just shake to stop recording. Say play recording to hear what you recorded. Just shake to pause the play back. And say upload to upload your recording. ", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else if (string.equals("start recording")) {
                        counter1++;
                        if (CheckPermissions()) {
                            active = true;
                            startbtn.setText("Stop Recording");
                            //startbtn.setVisibility(View.INVISIBLE);
                            //playbtn.setVisibility(View.INVISIBLE);
                            //upload.setVisibility(View.INVISIBLE);
                            mRecorder = new MediaRecorder();
                            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                            mRecorder.setOutputFile(mFileName);
                            try {
                                mRecorder.prepare();

                            } catch (IOException e) {
                                Log.e(LOG_TAG, "prepare() failed");
                            }
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //Do something after 100ms
                                    mRecorder.start();
                                    Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
                                }
                            }, 1500);
                        }
                        else {
                            RequestPermissions();
                        }
                    }
                    else if (string.equals("play recording")) {
                        playing = true;
                        //playbtn.setVisibility(View.VISIBLE);
                        //playbtn.setVisibility(View.INVISIBLE);
                        playbtn.setText("Stop Playing");
                        //upload.setVisibility(View.VISIBLE);
                        // startbtn.setVisibility(View.VISIBLE);
                        mPlayer = new MediaPlayer();
                        try {
                            mPlayer.setDataSource(mFileName);
                            mPlayer.prepare();
                            mPlayer.start();
                            Toast.makeText(getApplicationContext(), "Recording Started Playing", Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                    }
                    else if (string.equals("upload")) {
                        upload();
                        Toast.makeText(getApplicationContext(), "Audio has been uploaded", Toast.LENGTH_SHORT).show();
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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter1 = counter1 + 1;
                if ((counter1 % 2) == 1) {
                    if (CheckPermissions()) {
                        active = true;
                        startbtn.setText("Stop Recording");
                        //startbtn.setVisibility(View.INVISIBLE);
                        //playbtn.setVisibility(View.INVISIBLE);
                        //upload.setVisibility(View.INVISIBLE);
                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mRecorder.setOutputFile(mFileName);
                        try {
                            mRecorder.prepare();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                        mRecorder.start();
                        Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
                    }
                    else {
                        RequestPermissions();
                    }
                }
                else if ((counter1 % 2) == 0){
                    active = false;
                    startbtn.setText("Start Recording");
//                        startbtn.setVisibility(View.VISIBLE);
//                        playbtn.setVisibility(View.VISIBLE);
//                        upload.setVisibility(View.VISIBLE);
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                    Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
                }
            }

        });
       /*
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = false;
                startbtn.setText("Start Recording");
                startbtn.setVisibility(View.VISIBLE);
                playbtn.setVisibility(View.VISIBLE);
                upload.setVisibility(View.VISIBLE);
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
            }
        });
        */


        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter2 = counter2 + 1;
                if ((counter2 % 2) == 1) {
                    playing = true;
                    //playbtn.setVisibility(View.VISIBLE);
                    //playbtn.setVisibility(View.INVISIBLE);
                    playbtn.setText("Stop Playing");
                    //upload.setVisibility(View.VISIBLE);
                    // startbtn.setVisibility(View.VISIBLE);
                    mPlayer = new MediaPlayer();
                    try {
                        mPlayer.setDataSource(mFileName);
                        mPlayer.prepare();
                        mPlayer.start();
                        Toast.makeText(getApplicationContext(), "Recording Started Playing", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "prepare() failed");
                    }
                }
                else {
                    playing = false;
                    mPlayer.release();
                    mPlayer = null;
                    playbtn.setText("Start Playing");
                    startbtn.setVisibility(View.VISIBLE);
                    playbtn.setVisibility(View.VISIBLE);
                    upload.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Playing Audio Stopped", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /*
        stopplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playing = false;
                mPlayer.release();
                mPlayer = null;
                playbtn.setText("Start Playing");
                startbtn.setVisibility(View.VISIBLE);
                playbtn.setVisibility(View.VISIBLE);
                upload.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Playing Audio Stopped", Toast.LENGTH_SHORT).show();
            }
        });
         */

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload to Firebase
                upload();
                Toast.makeText(getApplicationContext(),"Audio has been uploaded", Toast.LENGTH_SHORT).show();

            }
        });

        ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        textView = findViewById(R.id.textView);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }

        });
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    }

    public void upload()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            // this will request for permission when permission is not true
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/mynotes.txt"));
        Uri file = Uri.fromFile(new File(mFileName));
        String name = "" + System.currentTimeMillis();
        StorageReference notesRef = mStorageRef.child("audio/" + name + ".3gp");//could name the files based on time stamp
        System.out.println(file.toString());

        notesRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                        System.out.println("testing: "+ downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        System.out.println("testing: "+ exception);
                    }
                });
    }

/*
    public void upload()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            // this will request for permission when permission is not true
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
//        Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/mynotes.txt"));
//        StorageReference audioRef = mStorageRef.child("notes/tester.3gp");
//
//        audioRef.putFile(file);
//                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                // Get a URL to the uploaded content
//                                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
//                                System.out.println("testing: "+ downloadUrl);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Handle unsuccessful uploads
//                                System.out.println("testing: "+ exception);
//                            }
//                        });
        //Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/mynotes.txt"));
        Uri file = Uri.fromFile(new File(mFileName));
        StorageReference notesRef = mStorageRef.child("audio/tester.3gp");//could name the files based on time stamp
        System.out.println(file.toString());
        notesRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                        System.out.println("testing: "+ downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        System.out.println("testing: "+ exception);
                    }
                });
    }
 */

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
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(My_Post.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
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
                    if(active == true)
                    {
                        Log.i("stopped at: ", "1");
                        active = false;
                        startbtn.setText("Start Recording");
                        mRecorder.stop();
                        mRecorder.release();
                        mRecorder = null;
                        Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
                    }
                    else if (playing == true)
                    {
                        playing = false;
                        Log.i("stopped at: ", "2");
                        mPlayer.release();
                        mPlayer = null;
                        playbtn.setText("Start Playing");
                        Toast.makeText(getApplicationContext(),"Playing Audio Stopped", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.i("stopped at: ", "3");
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
}
