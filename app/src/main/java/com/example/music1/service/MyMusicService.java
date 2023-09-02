package com.example.music1.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.music1.R;
import com.example.music1.data.GlobalConstants;
import com.example.music1.data.Song;
import com.example.music1.listener.MyPlayerListener;
import com.example.music1.util.PlayModelHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


//service和activity一样要注册
public class MyMusicService extends Service {


    private static final String CHANNEL_ID = "song_play_channel";
    private static final int FOREGROUND_ID = 1;
    //播放器
    private MediaPlayer mMediaPlayer;
    //数据源信息
    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    private int curPlayMode;
    private MyPlayerListener myPlayerListener;
    private RemoteViews remoteView;
    private boolean haveNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer  = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();
            }
        });
        mSongArrayList = new ArrayList<>(); // 不new 可能会造成空指针异常
//        stopSelf();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        return super.onStartCommand(intent, flags, startId);


    }

    //返回值为IBinder中介  即自己实例化的Binder对象
    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return new MyMusicBind(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }




    //继承Binder类 使其实例化 成为一个真正的中介
   public class  MyMusicBind extends Binder{
        private MyMusicService myMusicService;

        public MyMusicBind(MyMusicService myMusicService) {
            this.myMusicService = myMusicService;
        }



        //播放
        public void startPlay(){

        }

        //更新列表
        //接收activity传进来的数据源
        public void updateMusicList(ArrayList<Song> mSongArrayList){
           myMusicService.updateMusicList(mSongArrayList);
        }
        public void updateCurrentMusicIndex(int index){
            myMusicService.updateCurrentMusicIndex(index);
        }

        public boolean isPlaying() {
          return   myMusicService.isPlaying();
        }

        public void pause() {
            myMusicService.pause();
        }

        public void play() {
            myMusicService.play();
        }
        public void previous(){
            myMusicService.previous();
        }
        public void next(){

            myMusicService.next();
        }

        public void stop(){
            myMusicService.stop();
        }

        public Song getCurSong(){
          return myMusicService.getCurSong();
        }
        public int getCurProgress(){
            return myMusicService.getCurProgress();
        }


        public int getDuration() {
            return myMusicService.getDuration();
        }

        public void seekTo(int progress) {
            myMusicService.seekTo(progress);
        }
        public void setPlayMode(int mode){
         myMusicService.setPlayMode(mode);
        }
        public void  setPlayerListener (MyPlayerListener playerListener){
            myMusicService.setPlayerListener(playerListener);
        }
    }



    public void  setPlayerListener (MyPlayerListener playerListener){
        this.myPlayerListener = playerListener;
    }

    private void setPlayMode(int mode) {
        this.curPlayMode = mode;
    }

    private void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    private int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurProgress(){
        return mMediaPlayer.getCurrentPosition();
    }
    public Song getCurSong(){

        if(curSongIndex < 0 || curSongIndex > mSongArrayList.size()){
            return null;
        }
        return mSongArrayList.get(curSongIndex);


    }

    private void stop() {
        mMediaPlayer.stop();
        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void next() {
        if(curPlayMode == PlayModelHelper.PLAY_MODE_CIRCLE){
            updateCurrentMusicIndex(curPlayMode);
        }else if(curPlayMode == PlayModelHelper.PLAY_MODE_RANDOM){
            int nextRandomIndex = getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);


        }else {
            int nextIndex =  curSongIndex + 1;
            if( nextIndex > mSongArrayList.size()-1){
                nextIndex = 0;
            }
            updateCurrentMusicIndex(nextIndex);
        }

        if(myPlayerListener != null){
            myPlayerListener.onNext(curSongIndex,getCurSong());
        }

    }

    private void previous() {
        if(curPlayMode == PlayModelHelper.PLAY_MODE_CIRCLE){
            updateCurrentMusicIndex(curPlayMode);
        }else if(curPlayMode == PlayModelHelper.PLAY_MODE_RANDOM){
            int nextRandomIndex = getNextRandomIndex();
            updateCurrentMusicIndex(nextRandomIndex);
        }else {

            int preIndex = curSongIndex - 1;
            if (preIndex < 0) {
                preIndex = mSongArrayList.size() - 1;
            }
            updateCurrentMusicIndex(preIndex);
        }
        if(myPlayerListener != null){
            myPlayerListener.onPre(curSongIndex,getCurSong());
        }
    }
    public int getNextRandomIndex(){
       int size =  mSongArrayList.size();
        Random random = new Random();
       int randomIndex =  random.nextInt(size);
        return  randomIndex;
    }
    public void play() {
        if(mMediaPlayer.isPlaying()){
            return;
        }
        mMediaPlayer.start();
    }
    public void pause() {
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
    }
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }



    private void updateMusicList(ArrayList<Song> mSongArrayList) {
        this.mSongArrayList = mSongArrayList;
    }
    public void updateCurrentMusicIndex(int index){
        if (index < 0 || index >= mSongArrayList.size()){
            return;
        }
        this.curSongIndex = index;
        //播放该条歌曲
       Song song = mSongArrayList.get(curSongIndex);
        String songName = song.getSongName();

        AssetManager assetManager = getAssets();

        try {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            AssetFileDescriptor fileDescriptor = assetManager.openFd(songName);
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor()
                 ,fileDescriptor.getStartOffset(),fileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void createNotification(){
        if(haveNotification){
            return;
        }



      NotificationManager  notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "音乐播放通知", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 自定义通知中的内容View
        remoteView = new RemoteViews(getPackageName(), R.layout.notification_music_layout);
        // 设置通知中的TextView文字
//        Song song = getCurSong();
//        if (song != null) {
//            remoteView.setTextViewText(R.id.tv_notification_title, song.getSongName());
//        }


        Notification  notification= new NotificationCompat.Builder(this, CHANNEL_ID)
                .setWhen(System.currentTimeMillis())
                .setContentText("音乐内容")
                .setContentTitle("这是音乐标题")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setCustomContentView(remoteView)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                .setContentIntent(startSongPlayPendIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_pause))
                .build();


        startForeground(FOREGROUND_ID, notification);
        haveNotification = true;




    }


}
