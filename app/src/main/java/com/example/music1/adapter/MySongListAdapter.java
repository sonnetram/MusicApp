package com.example.music1.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music1.R;
import com.example.music1.data.Song;

import java.util.ArrayList;

public class MySongListAdapter extends RecyclerView.Adapter<MySongListAdapter.MySongItemViewHolder> {


    private ArrayList<Song> mSongArrayList;    //数据源
    private Context mContext;   //上下文
    private OnItemClickListener mItemClickListener;



    //构造方法
    public MySongListAdapter(ArrayList<Song> mSongArrayList, Context mContext) {
        this.mSongArrayList = mSongArrayList;
        this.mContext = mContext;
    }


    //将数据与View 绑定
    @Override
    public void onBindViewHolder(@NonNull MySongItemViewHolder holder, int position) {
        Song song = mSongArrayList.get(position);
        holder.bind(song);
    }

    //计数 的方法
    @Override
    public int getItemCount() {
        return mSongArrayList == null  ? 0 : mSongArrayList.size();
    }


    //返回值为MySongItemViewHolder
    //将布局转化为View
    @NonNull
    @Override
    public MySongItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View itemView  = LayoutInflater.from(mContext).inflate(R.layout.song_item_layout,parent,false);
        return new MySongItemViewHolder(itemView);
    }



    //MySongItemViewHolder 继承RecyclerView内部类ViewHolder
    class MySongItemViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvSongName;
        private LinearLayout llContainer;


        public MySongItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvSongName = itemView.findViewById(R.id.tv_song_name);

            //横条点击区域
            llContainer = itemView.findViewById(R.id.ll_song_item);
            //设置点击事件 使用接口回调让外部activity知道
            llContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mItemClickListener != null){
                        mItemClickListener.onItemClick(getAdapterPosition());
                    }

                }
            });
        }
        //填充数据到view
        public void bind(Song song){
            mTvSongName.setText(song.getSongName());
        }
    }

    //接口回调监听者
    public void setItemClickListener(OnItemClickListener ItemClickListener) {
        this.mItemClickListener = ItemClickListener;
    }

    //接口回调
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}
