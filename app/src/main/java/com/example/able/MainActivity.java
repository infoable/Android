package com.example.able;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    final static int CODE = 1000;


    Intent i;
    SpeechRecognizer mRecognizer;

    ImageView btn;
    TextView listen, talk, result;

    boolean nowListening = false;

    private WebView web;
    private WebSettings webSet;

    private void permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            ArrayList<String> arrayPermission = new ArrayList<>();

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                arrayPermission.add(Manifest.permission.RECORD_AUDIO);
            }
            //any permission? add here

            if (arrayPermission.size() > 0) {
                String strArray[] = new String[arrayPermission.size()];
                strArray = arrayPermission.toArray(strArray);
                ActivityCompat.requestPermissions(this, strArray, CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CODE: {
                if (grantResults.length > 1) {
                    Toast.makeText(this, "권한 획득에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "권한 획득에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionCheck();

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);

        btn = findViewById(R.id.btn);
        listen = findViewById(R.id.listen_text);
        talk = findViewById(R.id.talk);
        result = findViewById(R.id.result);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (nowListening) {
                        talk.setVisibility(View.VISIBLE);
                        result.setVisibility(View.VISIBLE);
                        listen.setVisibility(View.GONE);
                        mRecognizer.stopListening();
                        nowListening = false;
                    } else {
                        TapTargetView.showFor(MainActivity.this,
                                TapTarget.forView(findViewById(R.id.btn), "")
                                        .outerCircleAlpha(0)
                                        .transparentTarget(true)
                                        .targetCircleColor(R.color.colorPrimary)
                                        .targetRadius(32),
                                new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                    }
                                });
                        talk.setVisibility(View.GONE);
                        result.setVisibility(View.GONE);
                        listen.setVisibility(View.VISIBLE);
                        mRecognizer.startListening(i);
                        nowListening = true;
                    }

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });


        //Log.d("test", post("https://api.infoable.xyz/recongnize", ));

        web = findViewById(R.id.webview);
        web.setWebViewClient(new WebViewClient());
        webSet = web.getSettings();
        webSet.setJavaScriptEnabled(true);
        web.loadUrl("https://www.naver.com");
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            //System.out.println("onReadyForSpeech.........................");
        }

        @Override
        public void onBeginningOfSpeech() {
            //Toast.makeText(MainActivity.this, "지금부터 말을 해주세요!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            //System.out.println("onRmsChanged.........................");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            //System.out.println("onBufferReceived.........................");
        }

        @Override
        public void onEndOfSpeech() {
            result.setVisibility(View.VISIBLE);
            talk.setVisibility(View.VISIBLE);
            listen.setVisibility(View.GONE);
            nowListening = false;
            //System.out.println("onEndOfSpeech.........................");
        }

        @Override
        public void onError(int error) {
            result.setVisibility(View.VISIBLE);
            talk.setVisibility(View.VISIBLE);
            listen.setVisibility(View.GONE);
            nowListening = false;
            Toast.makeText(MainActivity.this, "천천히 다시 말해주세요.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            //System.out.println("onPartialResults.........................");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            //System.out.println("onEvent.........................");
        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            talk.setText(rs[0]);


            try {
                post("https://api.infoable.xyz/req", rs[0], new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        //result.setText("인터넷 연결을 확인하세요");
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(MainActivity.this, response.body().string(), Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
//                        final Handler handler = new Handler(Looper.getMainLooper()) {
//                            public void handleMessage(Message msg) {
//                                try {
//                                    //JSONObject jsonObject = new JSONObject();
//
//                                    result.setText(response.body().string());
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        };
//                        new Thread() {
//                            public void run() {
//                                Message msg = handler.obtainMessage();
//                                handler.sendMessage(msg);
//                            }
//                        }.start();


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

//            try {
//                result.setText(post("https://api.infoable.xyz/req", "query:".concat(rs[0])));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //Toast.makeText(MainActivity.this, rs[0], Toast.LENGTH_SHORT).show();
            //mRecognizer.startListening(i); //음성인식이 계속 되는 구문이니 필요에 맞게 쓰시길 바람
        }
    };

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();


    Call post(String url, String json, Callback callback) throws IOException {

        //String text = "{\"query\": \"test\"}";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("query", json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("test", jsonObject.toString());

        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(tts != null){
//            tts.stop();
//            tts.shutdown();
//            tts = null;
//        }

        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }
}
