package com.example.music1;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.music1.data.GlobalConstants;
import com.example.music1.data.Song;
import com.example.music1.listener.MyPlayerListener;
import com.example.music1.service.MyMusicService;
import com.example.music1.util.PlayModelHelper;
import com.example.music1.util.TimeUtil;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


public class MusicPlayActivity extends AppCompatActivity {
    private ImageView ivPlayPrPause, ivPre, ivNext;
    private TextView tvTitle,tvCurTIme,tvDuration,tvPlayMode;
    private SeekBar mSeekBar;
    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    //当前播放的歌曲
    private Song mCurSong;
    private MyMusicService.MyMusicBind mMusicBind;
    private boolean isSeekbarDragging;

    private Timer timer ;
    private int currentPlayMode = PlayModelHelper.PLAY_MODE_ORDER;


    private  ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //服务已建立，传递信息    iBinder中介
            //拿到回调数据
            mMusicBind= (MyMusicService.MyMusicBind) iBinder;
            mMusicBind.updateMusicList(mSongArrayList);
            mMusicBind.updateCurrentMusicIndex(curSongIndex);
//            mMusicBind.setPlayMode(currentPlayMode);
            mMusicBind.setPlayerListener(new MyPlayerListener() {
                @Override
                public void onComplete(int songIndex, Song song) {

                }

                @Override
                public void onPre(int songIndex, Song song) {
                    curSongIndex = songIndex;
                    mCurSong = song;
                    updateTitle();
                    updateUI();

                }

                @Override
                public void onPause(int songIndex, Song song) {

                }

                @Override
                public void onNext(int songIndex, Song song) {
                    curSongIndex = songIndex;
                    mCurSong = song;
                    updateTitle();
                    updateUI();

                }

                @Override
                public void onPlay(int songIndex, Song song) {

                }
            });

            updateUI();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        initView();

        Intent intent = getIntent();
        curSongIndex = intent.getIntExtra(GlobalConstants.KEY_SONG_INDEX,0);
        mSongArrayList =(ArrayList<Song>) intent.getSerializableExtra(GlobalConstants.KEY_SONG_LIST);
//          intent.getParcelableArrayExtra(GlobalConstants.KEY_SONG_LIST);
        mCurSong = mSongArrayList.get(curSongIndex);
        Log.d("tag","当前歌曲：" + curSongIndex);
        if(mSongArrayList != null){
            Log.d("tag","当前歌曲列表：" + mSongArrayList);
        }
        updateTitle();

        startMusicService(); //启动服务播放音乐
    }

    private void updateUI() {
        //当前时间更新
        int curProgress =   mMusicBind.getCurProgress();
        updateCurTime(curProgress);

        //总时间更新
        int duration =   mMusicBind.getDuration();
//        tvCurTIme.setText(TimeUtil.millToTimeFormat(curProgress));
        tvDuration.setText(TimeUtil.millToTimeFormat(duration));
        // 更新进度条
        mSeekBar.setMax(duration);
        mSeekBar.setProgress(curProgress);
        updateSeekbar();

    }

    private void updateSeekbar() {
        if(timer != null){
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int curProgress = mMusicBind.getCurProgress();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!isSeekbarDragging && mMusicBind.isPlaying()) {
                            mSeekBar.setProgress(curProgress);
//                            tvCurTIme.setText(TimeUtil.millToTimeFormat(curProgress));
                        }
                    }
                });

            }


        },0,200);
    }






    //通过bind 的形式启动 service
    private void startMusicService() {

        Intent intent = new Intent(this, MyMusicService.class);



        bindService(intent,conn,BIND_AUTO_CREATE);
    }
    public void initView(){
        ivPlayPrPause  = findViewById(R.id.iv_play_pause);
        ivNext = findViewById(R.id.iv_next);
        ivPre = findViewById(R.id.iv_previous);
        tvTitle = findViewById(R.id.tv_music_title);
        tvCurTIme = findViewById(R.id.tv_cur_time);
        tvDuration = findViewById(R.id.tv_duration);
        mSeekBar = findViewById(R.id.seek_bar_music);
        tvPlayMode = findViewById(R.id.tv_play_mode);

        //对进度条进行监听
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateCurTime(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarDragging = true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarDragging = false;
               int progress = seekBar.getProgress();
                mMusicBind.seekTo(progress);

            }
        });
    }
    private void updateCurTime(int progress){
        //当前时间更新
        tvCurTIme.setText(TimeUtil.millToTimeFormat(progress));
    }
    private void updateTitle() {
        tvTitle.setText(mCurSong.getSongName());

    }
    public void playOrPause(View view){
       if( mMusicBind.isPlaying()){
           //暂停音乐
           mMusicBind.pause();
           //修改图标为即将播放
           ivPlayPrPause.setImageResource(android.R.drawable.ic_media_play);
       }else {
           //播放音乐
           mMusicBind.play();
           ivPlayPrPause.setImageResource(android.R.drawable.ic_media_pause);

       }


    }

    public void nextMusic(View view) {
        mMusicBind.previous();
        if(mMusicBind.isPlaying()){
            ivPlayPrPause.setImageResource(android.R.drawable.ic_media_pause);
        }
        mCurSong = mMusicBind.getCurSong();
        updateTitle();
        updateUI();

    }

    public void preMusic(View view) {
        mMusicBind.next();
        if(mMusicBind.isPlaying()){
            ivPlayPrPause.setImageResource(android.R.drawable.ic_media_pause);
        }
        mCurSong = mMusicBind.getCurSong();
        updateTitle();
        updateUI();

    }

    public void stop(View view) {
        mMusicBind.stop();
        ivPlayPrPause.setImageResource(android.R.drawable.ic_media_play);
        updateCurTime(0);
        mSeekBar.setProgress(0);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    public void switchPlayMode(View view) {
        int playMode = PlayModelHelper.changePlayMode(currentPlayMode);
        currentPlayMode = playMode;
        String strPlayMode = PlayModelHelper.strPlayMode(currentPlayMode);
        tvPlayMode.setText(strPlayMode);
        mMusicBind.setPlayMode(currentPlayMode);
      }
    }
