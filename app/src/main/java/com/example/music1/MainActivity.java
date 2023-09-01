package com.example.music1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.music1.adapter.MySongListAdapter;
import com.example.music1.data.GlobalConstants;
import com.example.music1.data.Song;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRCVSongList;
    private MySongListAdapter mySongListAdapter;
    private ArrayList<Song> mSongArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initSongList();



    }

    private void initData() {
        mSongArrayList = new ArrayList<>();
        mSongArrayList.add(new Song("Saturn.mp3"));
        mSongArrayList.add(new Song("大海.mp3"));
        mSongArrayList.add(new Song("素颜-许嵩何曼婷.mp3"));

    }

    private void initSongList() {
        mySongListAdapter = new MySongListAdapter(mSongArrayList,this);
        mySongListAdapter.setItemClickListener(new MySongListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                Toast.makeText(MainActivity.this,"点击了"+position,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,MusicPlayActivity.class);
                intent.putExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
                intent.putExtra(GlobalConstants.KEY_SONG_INDEX,position);
                intent.putParcelableArrayListExtra(GlobalConstants.KEY_SONG_LIST,mSongArrayList);
                startActivity(intent);
            }
        });

        mRCVSongList.setAdapter(mySongListAdapter);  //在此布局上设置适配器
        mRCVSongList.setLayoutManager(new LinearLayoutManager(this));
    }

    private  void initView(){
        mRCVSongList = findViewById(R.id.rcv_song_list);

    }
}