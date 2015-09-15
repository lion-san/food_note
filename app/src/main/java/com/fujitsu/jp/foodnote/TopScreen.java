package com.fujitsu.jp.foodnote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.VideoView;
import android.widget.MediaController;


public class TopScreen extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top);

        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        //videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kaiten));
        videoView.setMediaController(new MediaController(this));


        try {
            videoView.setVideoPath("file:///android_asset/kaiten.mp4");
            sleep(1000);
            videoView.start();

        }catch (Exception e){
            sleep(5000);

        }

        sleep(5000);

        // インテントのインスタンス生成
        Intent intent = new Intent(TopScreen.this, MainActivity.class);
        // 次画面のアクティビティ起動
        startActivity(intent);

    }



    //指定ミリ秒実行を止めるメソッド
    public synchronized void sleep(long msec)
    {
        try
        {
            wait(msec);
        }catch(InterruptedException e){}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_screen, menu);
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
}
