package com.fujitsu.jp.foodnote;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    // = 0 の部分は、適当な値に変更してください（とりあえず試すには問題ないですが）
    private static final int REQUEST_CODE = 0;

    private TextToSpeech tts;
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
    private String projectID;
    private String projectName;
    private String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        setContentView(R.layout.init_menu);

        //20150611_kawai webvieへのhtmlファイルのロード
        //C:\AndroidProjects\food_note\app\src\main\assets\index.html

         //Contextの取得
        context = getApplicationContext();


        //ボタンの押した動作
        //20150611_kawai  ImageButton button = (ImageButton) findViewById(R.id.talk);

        //テストの押した動作
        //20150611_kawai  Button send = (Button) findViewById(R.id.send);

        //TTSの初期化
        //20150611_kawai  tts = new TextToSpeech(context, this);

        //トグルボタン（顔認識のON/OFF）
        //ToggleButton tglbtn = (ToggleButton) findViewById(R.id.toggleButton);

        // 会話表示エリア
        //20150611_kawai   mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        //20150611_kawai  sScrollView = (ScrollView) findViewById(R.id.scrollView);
        //sCommandTv = (TextView) findViewById(R.id.textView1);
        // 会話表示View管理クラス
        //20150611_kawai  sListItemManager = new ListItemManager(this, mBaseLayout);



        //ぐるぐる
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        //20150616_kawai この段階でぐるぐるは不要なためコメントアウト--ここから--
        //progressBar.setMessage("処理を実行しています");
        //progressBar.setCancelable(true);
        //progressBar.show();
        //20150616_kawai この段階でぐるぐるは不要なためコメントアウト--ここまで--




        //WebView UI Load
        wb = (WebView) findViewById(R.id.webView);
        //Javascript有効化
        wb.getSettings().setJavaScriptEnabled(true);
        //Javascript Interface add
        wb.addJavascriptInterface(new WebViewJavascriptInterface(this), "Android");
        //WebVeiwの表示設定
        wb.loadUrl("file:///android_asset/index.html");
        //wb.loadUrl("file:///android_asset/index_test.html");


    }

    protected void initRobot ( String pjID, String pjName,String uID ){
        //20150617_kawai　コメント記載
        //ProjectNameと記載されているが実際にはJSONのidパラメタであることに注意
        //つまりProject IDいれとけということ

        projectID=pjID;
        projectName=pjName;
        userID=uID;


        //20150616_kawai この段階でぐるぐるは必要なため追加--ここから--
        progressBar.setMessage("処理を実行しています");
        progressBar.setCancelable(true);
        //20150616_kawai 自作の関数に変更
        //progressBar.show();
        //showprogressBar();
        //20150616_kawai この段階でぐるぐるは必要なため追加--ここから--

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



                    return json_org;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String json_org) {


               // progressBar.dismiss();//消去

                //20150615_kawai コメントアウト--ここから--
                /*
                String ready = "コーチの準備ができました";
                tts.speak(ready, TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(this.getActivity(), ready, Toast.LENGTH_SHORT).show();
                */
                 //20150615_kawai コメントアウト--ここまで--

                //20150615_kawai TalkActivityへの画面遷移-ここから-

                //of.http://seesaawiki.jp/w/moonlight_aska/d/%B2%E8%CC%CC%A4%F2%C1%AB%B0%DC%A4%B9%A4%EB

                // インテントのインスタンス生成
                Intent intent = new Intent(MainActivity.this, TalkActivity.class);

                //インテントに値をセット
                //of.http://seesaawiki.jp/w/moonlight_aska/d/%B2%E8%CC%CC%B4%D6%A4%C7%A5%C7%A1%BC%A5%BF%A4%F2%BC%F5%A4%B1%C5%CF%A4%B7%A4%B9%A4%EB
                //of.http://web-terminal.blogspot.jp/2013/06/android.html
                intent.putExtra("res", res);
                intent.putExtra("projectName",projectName);
                intent.putExtra("projectID", projectID);
                intent.putExtra("userID",userID);


                 // 次画面のアクティビティ起動
                 startActivity(intent);

                 //20150615_kawai TalkActivityへの画面遷移-ここまで-


            }
        };

        try {

            //20150615_kawai TalkActivityで実行するためコメントアウト--ここから--
/*

            //アクションハンドラの生成
            //mainActivity
            act = new ActionHandler(this);
            task.setActivity(this);
            task.setTts(this.tts);
            act.setContext(context);
            act.setsListItemManager(sListItemManager);
            act.setsScrollView(sScrollView);
*/
            //20150616_kawai なんかいる気がするので復活
            task.setActivity(this);
           // task.setTts(this.tts);

            //20150615_kawai TalkActivityで実行するためコメントアウト--ここまで--


            //非同期処理開始
            task.execute( projectID );
        }catch( Exception e){
            e.printStackTrace();
        }

    }








    /**
     * executeRobot
     */
    //20150616_kawai このクラスにexcuteRobotは不要なためコメントアウト--ここから--
    /*
    private void executeRobot( String resultsString ){

        act.startDialogue(resultsString);

        //表示
        progressBar.show();

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
                progressBar.dismiss();//消去
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
    }*/
//20150616_kawai このクラスにexcuteRobotは不要なためコメントアウト--ここまで--






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
