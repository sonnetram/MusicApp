package com.example.music1.listener;

import com.example.music1.data.Song;

public interface MyPlayerListener {

    void onComplete(int songIndex, Song song);
    void onPre (int songIndex,Song song);
    void onPause (int songIndex,Song song);
    void onNext (int songIndex,Song song);
    void onPlay (int songIndex,Song song);


}