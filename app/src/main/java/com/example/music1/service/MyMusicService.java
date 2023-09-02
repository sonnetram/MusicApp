package com.example.music1.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.music1.data.Song;
import com.example.music1.listener.MyPlayerListener;
import com.example.music1.util.PlayModelHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


//service和activity一样要注册
public class MyMusicService extends Service {


    //播放器
    private MediaPlayer mMediaPlayer;
    //数据源信息
    private ArrayList<Song> mSongArrayList;
    private int curSongIndex;
    private int curPlayMode;
    private MyPlayerListener myPlayerListener;

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


}
