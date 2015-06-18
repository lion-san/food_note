package com.fujitsu.jp.stadiumcoach;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;





//20150615_kawai implementsにTextToSpeech.OnInitListenerを追加
public class TalkActivity extends ActionBarActivity implements TextToSpeech.OnInitListener{

    // = 0 の部分は、適当な値に変更してください（とりあえず試すには問題ないですが）
    private static final int REQUEST_CODE = 0;


    private TextToSpeech tts;

    //20150615_kawai TextToSpeach用　onInit()によるインスタンスの初期化ステータス用
    //of. https://akira-watson.com/android/tts.html
    private static final String TAG = "TestTTS";

    private Context context;

    private ProgressDialog progressBar;

    private ActionHandler act;

    private MyAsyncTask task;
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
    //ActionHandlerクラスのコンストラクタで何とかする。
    private String projectID;



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
        projectID=intent.getStringExtra("projectID");

        //20150615_kawai 遷移元からの値の受け渡し-ここまで-



        //ボタンの押した動作
        ImageButton button = (ImageButton) findViewById(R.id.talk);

        //テストの押した動作
        Button send = (Button) findViewById(R.id.send);

        //TTSの初期化
        tts = new TextToSpeech(context, this);

        //トグルボタン（顔認識のON/OFF）
        //ToggleButton tglbtn = (ToggleButton) findViewById(R.id.toggleButton);

        // 会話表示エリア
        mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        sScrollView = (ScrollView) findViewById(R.id.scrollView);
        //sCommandTv = (TextView) findViewById(R.id.textView1);
        // 会話表示View管理クラス
        //sListItemManager = new ListItemManager(this, mBaseLayout);

        //20150617_kawai プロジェクトごとに画像を変えるためprojectID月のコンストラクタを使用
        sListItemManager = new ListItemManager(this, mBaseLayout,projectID);


        //20150615_kawai initRobotから移動--ここから--
        //アクションハンドラの生成
        //mainActivity


        act = new ActionHandler(this);


        //20150615_kawai コメントアウト
       // task.setActivity(this);
        //20150615_kawai コメントアウト
        //task.setTts(this.tts);
        act.setContext(context);
        act.setsListItemManager(sListItemManager);
        act.setsScrollView(sScrollView);
        //20150615_kawai initRobotから移動--ここまで--





//20150616_kawai　ぐるぐる不要なためコメントアウト--ここから--
        //ぐるぐる
        /*
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("処理を実行中しています");
        progressBar.setCancelable(true);
        progressBar.show();
*/
//20150616_kawai　ぐるぐる不要なためコメントアウト--ここまで--



        //20150617_kawai マイクボタン用の動作をコピペ--ここから--
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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
                    executeRobot(str);
                }
            }
        });
        //20150615_kawai sendボタン用の動作をコピペ--ここまで--







/*

        //WebView UI Load
        wb = (WebView) findViewById(R.id.webView);
        //Javascript有効化
        wb.getSettings().setJavaScriptEnabled(true);
        //Javascript Interface add
        wb.addJavascriptInterface(new WebViewJavascriptInterface(this), "Android");
        //WebVeiwの表示設定
        wb.loadUrl("file:///android_asset/index.html");
        //wb.loadUrl("file:///android_asset/index_test.html");
*/

    }


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
            executeRobot( resultsString );




            super.onActivityResult(requestCode, resultCode, data);
        }
    }




//20150617_kawai initRobotはTalkActivityでは不要なためコメントアウト
    /*

    protected void initRobot ( String projectName ){
        // サブスレッドで実行するタスクを作成
        task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {
                    // Twitter フォロー実行
                    SendHttpRequest http = new SendHttpRequest();
                    String json_org = http.sendRequestToGarako( resultsString );

                    res = json_org;//インスタンス変数にＪＳＯＮ(命令セット)をセット

                    this.setParam(resultsString);

                    //20150616_kawai ぐるぐる不要なためコメントアウト
                   // progressBar.dismiss();//消去

                    return json_org;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String json_org) {
                String ready = "コーチの準備ができました";
                tts.speak(ready, TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(this.getActivity(), ready, Toast.LENGTH_SHORT).show();

            }
        };

        try {

            //20150615_kawai talk_windowへの画面遷移 --ここから--
            //20150615_kawai インテントの生成

            //ntent intent = new Intent();
            //intent.setClassName("com.fujitsu.jp.stadiumcoach", "com.fujitsu.jp.stadiumcoach.SubActivity");





            ///20150615_kawai talk_windowへの画面遷移 ここまで

            //アクションハンドラの生成
            //mainActivity
            act = new ActionHandler(this);
            task.setActivity(this);
            task.setTts(this.tts);
            act.setContext(context);
            act.setsListItemManager(sListItemManager);
            act.setsScrollView(sScrollView);

            //非同期処理開始
            task.execute( projectName );
        }catch( Exception e){
            e.printStackTrace();
        }

    }
*/

    /**
     * executeRobot
     */
    private void executeRobot( String resultsString ){
        String msg=resultsString;
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
                //act.setContext(context);

                //----------------------------------
                //-- JSONの振り分け処理
                //----------------------------------

                act.analyzeJson(resultsString, json_org);

            }
        };

        task.setActivity(this);
        task.setTts( this.tts);
        //アクションハンドラの生成
        //act = new ActionHandler( this );
        act.setContext(context);

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
}
