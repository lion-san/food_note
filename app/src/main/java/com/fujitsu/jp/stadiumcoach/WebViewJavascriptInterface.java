package com.fujitsu.jp.stadiumcoach;

import android.webkit.JavascriptInterface;

/**
 * Created by yokoi on 2015/05/02.
 */
public class WebViewJavascriptInterface {

    MainActivity activity;

    /** Instantiate the interface and set the context */
    WebViewJavascriptInterface(MainActivity c) {
        activity = c;

    }

    /** Active Mode ====================*/

    /**
     * 赤外線データ送信処理
     */
    @JavascriptInterface
    public void pushButton(String projectName) {

       //ここに呼び出しロジックを記述してください
        activity.initRobot(projectName);
    }

}//Class
