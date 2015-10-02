package com.fujitsu.jp.foodnote;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


//20150615_kawai implementsにTextToSpeech.OnInitListenerを追加
public class TalkActivity extends ActionBarActivity implements TextToSpeech.OnInitListener, RecognitionListener {

    // = 0 の部分は、適当な値に変更してください（とりあえず試すには問題ないですが）
    private static final int REQUEST_CODE = 0;


    //Robot MODE
    private static final int NORMAL = 0;
    private static final int TIME = 1;

    private TextToSpeech tts;

    //20150615_kawai TextToSpeach用　onInit()によるインスタンスの初期化ステータス用
    //of. https://akira-watson.com/android/tts.html
    private static final String TAG = "TestTTS";

    private Context context;

    private ProgressDialog progressBar;

    private ActionHandler act;

    private MyAsyncTask task;
    private MyAsyncTask reload;
    private String res = null;


    //プロジェクトリスト
    private CharSequence[] items;
    private  ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> pjId = new ArrayList<String>();
    AlertDialog dialog;


    private ListItemManager sListItemManager;   // 会話表示View管理クラスオブジェクト
    private ScrollView sScrollView;             // 会話表示のスクロールビューオブジェクト
    private LinearLayout mBaseLayout;           // 会話表示のベースレイアウトオブジェクト

    private  WebView wb;

    //20150617_kawai 選択プロジェクトの保持用に追加
    //画像表示を変更させるにはActionHandlerクラスに渡す必要がある。
    //ListItemManagerクラスのコンストラクタで何とかする。
    private String projectName;
    private String projectID;
    private String userID;

    HashMap<String, String> ttsparam;

    private SpeechRecognizer mSpeechRecognizer;//20150916 yokoi
    private ImageButton button;

    //タイマー
    private Timer mTimer;

    /** カメラのハードウェアを操作する {@link android.hardware.Camera} クラスです。 */
    private Camera mCamera;
    /** カメラのプレビューを表示する {@link android.view.SurfaceView} です。 */
    private SurfaceView mView;
    private CameraOverlayView mCameraOverlayView;
    private Date lasttime = null;
    private Date starttime = null;


    //20150615_kawai TextToSpeach用　onInit()でインスタンスの初期化ステータスを取得--ここから--
    //of. https://akira-watson.com/android/tts.html
    @Override
    public void onInit(int status) {
        // TTS初期化
        if (TextToSpeech.SUCCESS == status) {
            Log.d(TAG, "initialized");
        } else {
            Log.e(TAG, "faile to initialize");
        }
    }
    //20150615_kawai TextToSpeach用　onInit()でインスタンスの初期化ステータスを取得--ここまで--
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        //20150615_kawai talkwindowの取得
            setContentView(R.layout.talk_window);

         //Contextの取得
        context = getApplicationContext();


        //20150615_kawai 遷移元からの値の受け渡し-ここから-
        //of.http://seesaawiki.jp/w/moonlight_aska/d/%B2%E8%CC%CC%B4%D6%A4%C7%A5%C7%A1%BC%A5%BF%A4%F2%BC%F5%A4%B1%C5%CF%A4%B7%A4%B9%A4%EB

        Intent intent = getIntent();
        // インテントに保存されたデータを取得
        res = intent.getStringExtra("res");
        projectName=intent.getStringExtra("projectName");
        projectID=intent.getStringExtra("projectID");
        userID=intent.getStringExtra("userID");

        //20150615_kawai 遷移元からの値の受け渡し-ここまで-


        //カメラ
        mView = (SurfaceView) findViewById(R.id.surfaceView);

        //発話ボタン
        //ImageButton button = (ImageButton) findViewById(R.id.talk);
        button = (ImageButton) findViewById(R.id.talk);

        //テストの押した動作
        Button send = (Button) findViewById(R.id.send);


        //TTSの初期化
        //tts = new TextToSpeech(context, this);

        //2015/09/16 yokoi ダイアログ非表示音声認識
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(this);

        //CompleteEvent 2015/09/15 yokoi
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                // 発話終了のListnerを登録
                tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
                    public void onUtteranceCompleted(String utteranceId) {
                       //listening();
                        try {
                            Thread.sleep(500);

                            // サブスレッドで実行するタスクを作成
                            task = new MyAsyncTask() {
                                @Override
                                protected String doInBackground(String... params) {
                                    return res;
                                }
                                @Override
                                protected void onPostExecute(String json_org) {
                                    button.performClick();//ボタン押下のエミュレート
                                }
                            };

                            //task.setActivity(this);
                            task.execute( "" );

                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //SET UTTERANCE_ID
        String UTTERANCE_ID = "1";
        ttsparam = new HashMap<String, String>();
        ttsparam.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);


        //resの常時リロードのスケジュール
        reloadSchedule(5000, 5000);


        //トグルボタン（顔認識のON/OFF）
        //ToggleButton tglbtn = (ToggleButton) findViewById(R.id.toggleButton);

        // 会話表示エリア
        mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        sScrollView = (ScrollView) findViewById(R.id.scrollView);
        //sCommandTv = (TextView) findViewById(R.id.textView1);
        // 会話表示View管理クラス
        //sListItemManager = new ListItemManager(this, mBaseLayout);

        //20150617_kawai プロジェクトごとに画像を変えるためprojectID月のコンストラクタを使用
        sListItemManager = new ListItemManager(this, mBaseLayout,projectID, projectName);


        //20150615_kawai initRobotから移動--ここから--
        //アクションハンドラの生成
        //mainActivity


        act = new ActionHandler(this,userID);


        //20150615_kawai コメントアウト
       // task.setActivity(this);
        //20150615_kawai コメントアウト
        //task.setTts(this.tts);
        act.setContext(context);
        act.setsListItemManager(sListItemManager);
        act.setsScrollView(sScrollView);
        //20150615_kawai initRobotから移動--ここまで--



        //20150617_kawai マイクボタン用の動作をコピペ--ここから--
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listening();

            }
        });


//20150617_kawai マイクボタン用の動作をコピペ--ここまで--


        //20150615_kawai sendボタン用の動作をコピペ--ここから--
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //テストの押した動作
                EditText txt = (EditText) findViewById(R.id.editText);
                SpannableStringBuilder sb = (SpannableStringBuilder)txt.getText();
                String str = sb.toString();


                if(str.length() == 0){
                    return;
                }
                else {
                    txt.setText("");
                    executeRobot(str, NORMAL);
                }
            }
        });
        //20150615_kawai sendボタン用の動作をコピペ--ここまで--

    }

    public void reloadSchedule(int initDelay, int delay) {

        if(mTimer != null)
            mTimer.cancel();

        mTimer   = new Timer(true);            //onClickメソッドでインスタンス生成
        mTimer.schedule( new TimerTask(){
            @Override
            public void run() {

                // サブスレッドで実行するタスクを作成
                reload = new MyAsyncTask() {
                    @Override
                    protected String doInBackground(String... params) {
                        SendHttpRequest http = new SendHttpRequest();
                        String json_org = http.sendRequestToGarako(projectID);

                        return json_org;
                    }
                    @Override
                    protected void onPostExecute(String json_org) {
                        res = json_org;
                        //Toast.makeText(TalkActivity.this, "リロード", Toast.LENGTH_LONG).show();

                        //タイマーイベント監視
                        //会話から実行
                        executeRobot("time", TIME);
                    }
                };
                reload.execute("");
            }
        }, initDelay, delay);
    }

    public void cancelSchedule(){
        mTimer.cancel();
    }

    public void listening2(){
        try {
            // インテント作成
            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    "Let's say!"); // お好きな文字に変更できます


            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);// 取得する結果の数


            // インテント発行
            startActivityForResult(intent, REQUEST_CODE);


        } catch (ActivityNotFoundException e) {
            // このインテントに応答できるアクティビティがインストールされていない場合
            Toast.makeText(TalkActivity.this,
                    "ActivityNotFoundException", Toast.LENGTH_LONG).show();
        }
    }


    //============================= start SpeechRecognizer ============================
    public void listening(){

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getPackageName());

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);// 取得する結果の数

        mSpeechRecognizer.startListening(intent);
    }

    // 音声認識準備完了
    @Override
    public void onReadyForSpeech(Bundle params) {
        button.setImageResource(R.drawable.btn_mic_on_red);
        Toast.makeText(this, "お話しどうぞ", Toast.LENGTH_SHORT).show();
    }

    // 音声入力開始
    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(this, "認識中・・・", Toast.LENGTH_SHORT).show();
        //iv.setImageResource(R.drawable.listen);
    }

    // 録音データのフィードバック用
    @Override
    public void onBufferReceived(byte[] buffer) {
        //Log.v(LOGTAG,"onBufferReceived");
    }

    // 入力音声のdBが変化した
    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.v(LOGTAG,"recieve : " + rmsdB + "dB");
    }

    // 音声入力終了
    @Override
    public void onEndOfSpeech() {
        //Toast.makeText(this, "認識完了", Toast.LENGTH_SHORT).show();
    }

    // ネットワークエラー又は、音声認識エラー
    @Override
    public void onError(int error) {

        //ボタンを元に戻す
        button.setImageResource(R.drawable.btn_mic_on);

        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                // 音声データ保存失敗
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                // Android端末内のエラー(その他)
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                // 権限無し
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // ネットワークエラー(その他)
               //Log.e(LOGTAG, "network error");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // ネットワークタイムアウトエラー
                //Log.e(LOGTAG, "network timeout");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // 音声認識結果無し
                Toast.makeText(this, "もう一回マイクボタンを押してね！", Toast.LENGTH_LONG).show();
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // RecognitionServiceへ要求出せず
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // Server側からエラー通知
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // 音声入力無し
                Toast.makeText(this, "no input?", Toast.LENGTH_LONG).show();
                break;
            default:
        }
    }

    // イベント発生時に呼び出される
    @Override
    public void onEvent(int eventType, Bundle params) {
        //Log.v(LOGTAG,"onEvent");
    }

    // 部分的な認識結果が得られる場合に呼び出される
    @Override
    public void onPartialResults(Bundle partialResults) {
        //Log.v(LOGTAG,"onPartialResults");
    }

    // 認識結果
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> recData = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String getData = new String();
        for (String s : recData) {
            //getData += s + ",";
            getData += s;
            break;//1ループのみ
        }

        button.setImageResource(R.drawable.btn_mic_on);
        Toast.makeText(this, getData, Toast.LENGTH_SHORT).show();

        //会話から実行
        executeRobot( getData, NORMAL );

    }

    //============================= end SpeechRecognizer ============================



    // アクティビティ終了時に呼び出される
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "";


            // 結果文字列リスト
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);


        /*for (int i = 0; i< results.size(); i++) {
            // ここでは、文字列が複数あった場合に結合しています
            resultsString += results.get(i);
        }*/
            resultsString = results.get(0);


            // トーストを使って結果を表示
            Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();


            //会話から実行
            executeRobot( resultsString, NORMAL );


            super.onActivityResult(requestCode, resultCode, data);

        }
    }




    /**
     * executeRobot
     */
    private void executeRobot( String resultsString, int MODE ){
        String msg=resultsString;

        if(MODE == NORMAL)
            act.startDialogue(resultsString);

        //表示
        //20160616_kawai ぐるぐる不要なためコメントアウト
        //progressBar.show();

        // サブスレッドで実行するタスクを作成
        task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {

                    if(res == null) {

                        // Twitter フォロー実行
                        SendHttpRequest http = new SendHttpRequest();
                        String json_org = http.sendRequestToGarako(resultsString);
                        res = json_org;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                //20160616_kawai ぐるぐる不要なためコメントアウト
                //progressBar.dismiss();//消去
                this.setParam(resultsString);
                return res;
            }

            @Override
            protected void onPostExecute(String json_org) {

                // トーストを使って結果を表示
                //Toast.makeText(this.getActivity(), json_org, Toast.LENGTH_SHORT).show();

                //WebView webView = (WebView) findViewById(R.id.webView);
                //webView.loadUrl(url);
                //webView.loadData(data, "text/html", null);
                //webView.loadDataWithBaseURL(null, json_org, "text/html", "UTF-8", null);

                String resultsString = this.getParam();


                act.setTts(this.getTts());
                act.setTtsparam(ttsparam);
                //act.setContext(context);

                //----------------------------------
                //-- JSONの振り分け処理
                //----------------------------------

                switch(this.getMODE()) {
                    case NORMAL:
                        act.analyzeJson(resultsString, json_org) ;
                    break;

                    case TIME:
                        act.analyzeJsonForTimer(resultsString, json_org, TalkActivity.this);
                        break;
                }
            }
        };

        task.setActivity(this);
        task.setTts( this.tts);
        task.setMODE(MODE);
        //アクションハンドラの生成
        //act = new ActionHandler( this );
        act.setContext(context);
        act.setmCam(mCamera);

        task.execute( resultsString );
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
        // 音声認識サービスの終了
        sListItemManager = null;
        //mCamera.release();
    }

    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        //Toast.makeText(getApplicationContext(), "Pause!" , Toast.LENGTH_SHORT).show();
       // this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                //戻るボタンが押された時の処理。
                Toast.makeText(this, "Good bye!" , Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }
        return false;
    }


//--------------------------------------------------------------------------------
//--- Camera -----------------------------------------------------------------------------
//--------------------------------------------------------------------------------
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //コールバック関数をセット
        SurfaceHolder holder = mView.getHolder();
        holder.addCallback(surfaceHolderCallback);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /** カメラのコールバックです。 */
    private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                // 生成されたとき
                mCamera = Camera.open(1);

                // リスナをセット  // 顔検出の開始
                //mCamera.setFaceDetectionListener(faceDetectionListener);

                //mCamera.stopFaceDetection();

                // プレビューをセットする
                mCamera.setPreviewDisplay(holder);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // 変更されたとき
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = previewSizes.get(0);
            //parameters.setPreviewSize(previewSize.width, previewSize.height);
            parameters.setPreviewSize(640, 480);
            // width, heightを変更する
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 破棄されたとき
            mCamera.release();
            mCamera = null;
        }

    };


//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------


}
