package com.fujitsu.jp.foodnote;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.dialogue.Dialogue;
import jp.ne.docomo.smt.dev.dialogue.data.DialogueResultData;
import jp.ne.docomo.smt.dev.dialogue.param.DialogueRequestParam;

/**
 * Created by clotcr_22 on 2015/02/16.
 */
public class ActionHandler {

    private Activity activity;
    private Context context;
    private TextToSpeech tts;

    private ScrollView sScrollView;             // 会話表示のスクロールビューオブジェクト
    private LinearLayout mBaseLayout;           // 会話表示のベースレイアウトオブジェクト
    private ListItemManager sListItemManager;

    private Camera mCam;

    private Boolean face_ditect = false;

    private static DialogueResultData resultData = null;

    //20150622_kawai 名前を読み上げるためにuserIDを追加
    private String userID;

    //20150623_kawai クイズゲームのために追加

    String qCode=null;

    //20150831_kawai gFlagがTrueのとき、ゲーム中
    Boolean gFlag = false;

    //20150831_kawai 正解数
    int score=0;

    //20150915 会話終了検知
    private HashMap<String, String> ttsparam;


    /**
     * コンストラクタ
     */
    public ActionHandler(Activity activity){ this.activity = activity; }

    //20150622_kawai userIDのためのコンストラクタ
    public ActionHandler(Activity activity,String userID)
    {
        this.activity = activity;
        this.userID=userID;
    }


    /**
     *
     * @param resultsString
     * @param json_org
     */
    synchronized protected void analyzeJsonForTimer( String resultsString, String json_org, TalkActivity act) {

        Calendar now = Calendar.getInstance(); //インスタンス化
        int hh = now.get(now.HOUR_OF_DAY);//時を取得
        int mm = now.get(now.MINUTE);     //分を取得

        int count = 0;

        try {

            JSONArray jsons = new JSONArray(json_org);

            for (int i = 0; i < jsons.length(); i++) {
                // 予報情報を取得
                JSONObject event = jsons.getJSONObject(i);
                // Event
                String e = event.getString("event");
                // Operator
                String operator = event.getString("operator");
                // 条件
                String param = event.getString("param");

                if(e.equals("time")) {

                    //1回でも該当
                    count++;

                    //条件が一致(HH24:MI)
                    String h;
                    if(hh < 10) {
                        h = "0" + String.valueOf(hh);
                    }
                    else{
                        h =  String.valueOf(hh);
                    }

                    String m;
                    if(mm < 10) {
                        m = "0" + String.valueOf(mm);
                    }
                    else{
                        m =  String.valueOf(mm);
                    }

                    if (param.equals(h + m)) {

                        this.executeAction(this.getActivity(), event.getJSONArray("actions"));

                        //繰り返し頻度を変える
                        act.reloadSchedule(60000, 60000);

                    }
                }
            }

            //カウントされない（タイマーイベント該当ない場合
            if(count == 0){
                //スケジュールを元に戻す
                act.reloadSchedule(5000, 5000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


        //20150622_kawai userIDを追加

    /**
     *
     * @param resultsString
     * @param json_org
     */
    synchronized protected void analyzeJson( String resultsString, String json_org ){


        Boolean flg = false;
        String str = resultsString;//しゃべった内容
        //paramは条件

        String log = "";

        try{

            JSONArray jsons = new JSONArray(json_org);

            for (int i = 0; i < jsons.length(); i++) {
                // 予報情報を取得
                JSONObject event = jsons.getJSONObject(i);
                // Event
                String e = event.getString("event");
                // Operator
                String operator = event.getString("operator");
                // 条件
                String param = event.getString("param");


                //条件の検査
                switch (e){

                    //言語一致
                    case "say":
                        if (operator.equals("==")) {//完全一致の場合

                            if (resultsString.equals(param)) {
                                //処理の実行
                                log = this.executeAction(this.getActivity(), event.getJSONArray("actions"));
                                flg = true;
                            }
                        } else if ((param.equals(StaticParams.FACE_DETECT) && (face_ditect))) {//顔検知の場合
                            //処理の実行
                            log = this.executeAction(this.getActivity(), event.getJSONArray("actions"));
                            flg = true;
                        } else {//部分一致の場合

                            //20150623_kawai クイズゲームのため追加 --ここから--

                            if (str.contains("クイズ")) {//クイズをキーワードにゲーム開始
                                gFlag = true;
                                score = 0;
                            }

                            //20150831_kawai しゃべった内容にコードを追加
                            if (qCode != null) {
                                str = qCode + str;
                            }

                            //20150623_kawai クイズゲームのため追加 --ここまで--

                            //20150622_kawai 正規表現を導入する--ここから--
                            String regex = ".*" + param + ".*";

                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(str);

                            if (m.find()) {

                                log = this.executeAction(this.getActivity(), event.getJSONArray("actions"));
                                flg = true;
                            }

                            //20160622_kawai 正規表現を導入する--ここまで--

                            //20160622_kawai 正規表現を使用するためコメントアウト
                        /*
                        if(resultsString.indexOf(param) != -1){
                            //処理の実行
                            this.executeAction(this.getActivity(), event.getJSONArray("actions"));
                            flg = true;
                        }
                        */
                        }
                   break;

                }//end switch

                //20150623 クイズゲームのため追加
                if (flg){
                    break;
                }
            }// end of for

            if(( !flg ) && (!resultsString.equals((StaticParams.FACE_DETECT)))) {
                //Toast.makeText(activity, "何も該当しませんでした。", Toast.LENGTH_SHORT).show();
                //doTalk(resultsString +"が理解できませんでした。意味を教えてください。");
                //Toast.makeText(activity, "Connecting to DoCoMo", Toast.LENGTH_SHORT).show();
                doDocomo(resultsString);
            }
            else{//該当があった場合
                //ログの出力
                Log.sendLog(userID, "say", resultsString, log);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity, "Network Busy!", Toast.LENGTH_SHORT).show();
            return;
        }

        //ログの出力

    }



    /**
     * 処理の実行
     * @param actions
     */
    protected String executeAction( Activity act, JSONArray actions ) throws JSONException {

        //インスタンス変数にセット
        activity = act;

        String ret = "";

        for(int i = 0; i < actions.length(); i++){
            JSONObject action = actions.getJSONObject(i);


            //20150623_kawai クイズゲームのため追加 --ここから--

            String param = action.getString("param");

            //20150623_kawai クイズゲームのため追加 --ここまで



            //Toast.makeText(activity, action.getString("action"), Toast.LENGTH_SHORT).show();
            //Toast.makeText(activity, action.getString("param"), Toast.LENGTH_SHORT).show();

            //actionに基づき動作
            //20150623_kawai クイズゲーム対応によりコメントアウト
           // exec(action.getString("action"),  action.getString("param"));
            ret += exec(action.getString("action"),  param);

        }

        return ret;
    }

    private String exec( String action, String param){

        String str = "";

            switch (action) {

                case "talk":
                    str = param;
                    //20150622_kawai お名前機能を追加する--ここから--

                    if (param.contains("userID")) {
                        //処理の実行
                        param = param.replace("userID", userID);
                    }
                    //20150622_kawai お名前機能を追加する--ここまで--

                    //20150623_kawai クイズゲーム機能
                    //20150827_kawai elseはクイズに無関係のとき

                    if(gFlag){
                        qCode=param.substring(0,5);
                        param=param.replace(qCode,"");
                        if(param.contains("正解") && !param.contains("はずれ")) {
                            score = score + 1;
                        }else{//不正解のときの処理

                        }
                        if(qCode.equals("Q999:")){
                            param=param+userID+"さんの今回の正解数は"+score+"でした。";
                            gFlag=false;
                            qCode=null;
                        }
                        doTalk(param);
                    }else {
                        doTalk(param);
                    }

                    break;

                case "camera":
                    doCamera();
                    break;

                case "light":
                    doFlash();
                    break;

                case "wait":
                    doWait(param);
                    break;

                case "application":
                    doApplication(param);
                    break;

                case "media":
                    doMedia(param);
                    break;

                default:
                    break;
            }


        return str;

    }


    synchronized private void doDocomo( String param ){
        AuthApiKey.initializeAuth("4d65436a6f3770753131784444772f3462526e594f575076772f6d716441336d6e3341376c4d534a434e34");

        // サブスレッドで実行するタスクを作成
        MyAsyncTask task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {

                //雑談対話要求処理クラスを作成
                Dialogue dialogue = new Dialogue();
                //雑談対話要求リクエストデータクラスを作成してパラメータをset する
                DialogueRequestParam param1 = new DialogueRequestParam();
                param1.setUtt(resultsString);

                //対話を継続するために context には任意の文字列を設定する。
                if( resultData != null ){
                    param1.setContext(resultData.getContext());
                }

                //雑談対話要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = dialogue.request(param1);

                //ログ
                Log.sendLog(userID, "doDocomo", resultsString, resultData.getYomi());

                return resultData.getYomi();
                }
                catch (Exception e){
                    e.printStackTrace();

                }

                return null;
            }

            @Override
            protected void onPostExecute(String yomi) {
                doTalk( yomi );
            }
        };

        task.execute(param);
    }

    /**
     *
     * @param param
     */
    synchronized private void doWait( String param ){

        try {
            Thread.sleep( Integer.parseInt(param) * 1000 );
            Toast.makeText(activity, param+"秒待ちます", Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param param
     */
    synchronized private void doTalk( String param){

        tts.speak(param, TextToSpeech.QUEUE_ADD, ttsparam);
        //Toast.makeText(activity, param, Toast.LENGTH_SHORT).show();

        sListItemManager.setSpeechResult(param);

        scrollToBottom();

    }

    /**
     * 雑談対話の開始
     */
    public void startDialogue(String msg) {
        sListItemManager.setInterpretationProgress(msg);
        //画面下までスクロール
        scrollToBottom();

        //new DialogueAPI(sHandler).start(msg);
    }


    /**
     * スクロールビューを一番下まで自動スクロールさせる。
     */
    public void scrollToBottom() {
        AsyncTask<Void, Void, Boolean> waitScroll = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(100); // 0.1秒ディレイ
                } catch (InterruptedException e) {
                    // エラー表示
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                sScrollView.fullScroll(View.FOCUS_DOWN);

                //テキストにフォーカス
                EditText txt = (EditText) activity.findViewById(R.id.editText);
                txt.requestFocus();
            };
        };
        waitScroll.execute();
    }

    synchronized private void doCamera( ){

        try {

            // 画像取得
            mCam.takePicture(null, null, mPicJpgListener);

        }
            catch (Exception e){
                e.printStackTrace();
            }
    }

    /**
     * JPEG データ生成完了時のコールバック
     */
    private Camera.PictureCallback mPicJpgListener = new Camera.PictureCallback() {
        synchronized  public void onPictureTaken(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }

            String saveDir = Environment.getExternalStorageDirectory().getPath() + "/foodnote";

            // SD カードフォルダを取得
            File file = new File(saveDir);

            // フォルダ作成
            if (!file.exists()) {
                if (!file.mkdir()) {
                    android.util.Log.e("Debug", "Make Dir Error");
                }
            }

            // 画像保存パス
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String imgPath = saveDir + "/" + sf.format(cal.getTime()) + ".jpg";

            // ファイル保存
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(imgPath, true);
                fos.write(data);
                fos.close();

                // アンドロイドのデータベースへ登録
                // (登録しないとギャラリーなどにすぐに反映されないため)
                registAndroidDB(imgPath);

            } catch (Exception e) {
                android.util.Log.e("Debug", e.getMessage());
            }

            fos = null;

            // takePicture するとプレビューが停止するので、再度プレビュースタート
            mCam.startPreview();

            // mIsTake = false;
        }
    };

    /**
     * アンドロイドのデータベースへ画像のパスを登録
     * @param path 登録するパス
     */
    private void registAndroidDB(String path) {
        // アンドロイドのデータベースへ登録
        // (登録しないとギャラリーなどにすぐに反映されないため)
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put("_data", path);
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }



    /**
     *
     */
    synchronized private void doFlash(){

        generateNotification("");
    }


    synchronized private void doApplication( String param ){

        PackageManager pm = activity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(param);

        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "対象のアプリがありません", Toast.LENGTH_SHORT).show();
        }
    }


    synchronized private void doMedia( String param ){

        try {
            sListItemManager.setMedia(param);
            scrollToBottom();
        } catch (Exception e) {
            Toast.makeText(activity, "動画のロードに失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @param param
     */
    private void generateNotification(String param) {

        //システムトレイに通知するアイコン
       // int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(0, "", when);
        //String title = context.getString(R.string.app_name);

              //ステータスバーをクリックした時に立ち上がるアクティビティ
        //Intent notificationIntent = new Intent(context, OfferDisplayActivity.class);

        /*notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
            Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent intent =
            PendingIntent.getActivity(context, 0, notificationIntent, 0);*/

        //notification.setLatestEventInfo(context, title, message, intent);
                //通知の種類　音 バイブにしている時は鳴らない　
        //notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE ;
        notification.flags =  Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL ;
        notification.ledOnMS = 3000;
        notification.ledOffMS = 1000;
        notification.ledARGB = Color.BLUE;

        NotificationManager notificationManager =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

        doWait("3");
        notificationManager.cancel( 0 );
    }


    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public TextToSpeech getTts() {
        return tts;
    }

    public void setTts(TextToSpeech tts) {
        this.tts = tts;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public Boolean getFace_ditect() {
        return face_ditect;
    }

    public void setFace_ditect(Boolean face_ditect) {
        this.face_ditect = face_ditect;
    }


    public  void setsScrollView(ScrollView sScrollView) {
        this.sScrollView = sScrollView;
    }

    public void setsListItemManager(ListItemManager sListItemManager) {
        this.sListItemManager = sListItemManager;
    }

    public HashMap<String, String> getTtsparam() {
        return ttsparam;
    }

    public void setTtsparam(HashMap<String, String> ttsparam) {
        this.ttsparam = ttsparam;
    }

    public Camera getmCam() {
        return mCam;
    }

    public void setmCam(Camera mCam) {
        this.mCam = mCam;
    }

}
