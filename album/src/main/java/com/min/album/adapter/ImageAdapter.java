package com.min.album.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.min.album.Album;
import com.min.album.R;
import com.min.album.bean.AlbumImage;
import com.min.album.util.Utils;

/**
 * Created by minyangcheng on 2016/11/22.
 */

public class ImageAdapter extends BaseRecyclerViewAdapter<AlbumImage,RecyclerView.ViewHolder> {

    private static final int CAMEAR_TYPE=1;
    private static final int IMAGE_TYPE=2;

    private OnCameraClickListener mOnCameraClickListener;
    private OnImageClickListener mOnImageClickListener;

    private int mItemHeight;

    private boolean mIsMulti;

    public ImageAdapter(Context context, boolean isMulti) {
        super(context);
        int screenWidth= Utils.getScreenWidth(context);
        mItemHeight= screenWidth/Album.getColumnNum();
        this.mIsMulti=isMulti;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder=null;
        View view=null;
        if(viewType==CAMEAR_TYPE){
            view=mLayoutInflater.inflate(R.layout.item_camera,parent,false);
            view.getLayoutParams().height=mItemHeight;
            viewHolder=new CamearViewHolder(view);
        }else{
            view=mLayoutInflater.inflate(R.layout.item_image,parent,false);
            view.getLayoutParams().height=mItemHeight;
            viewHolder=new ImageViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CamearViewHolder){
            CamearViewHolder itemHolder= (CamearViewHolder) holder;
            itemHolder.bind(position);
        }else if(holder instanceof ImageViewHolder){
            position=position-1;
            ImageViewHolder itemHolder= (ImageViewHolder) holder;
            itemHolder.bind(position,getData().get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return CAMEAR_TYPE;
        }else{
            return IMAGE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount()+1;
    }

    @Override
    public void notifyMyItemChanged(int position) {
        super.notifyItemChanged(position+1);
    }

    public void setOnCameraClickListener(OnCameraClickListener listener){
        this.mOnCameraClickListener=listener;
    }

    public void setOnImageClickListener(OnImageClickListener listener){
        this.mOnImageClickListener=listener;
    }

    public class CamearViewHolder extends RecyclerView.ViewHolder{


        public CamearViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(int pos){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnCameraClickListener!=null){
                        mOnCameraClickListener.click();
                    }
                }
            });
        }

    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView iv;
        public AppCompatCheckBox cb;

        public ImageViewHolder(View itemView) {
            super(itemView);
            iv= (ImageView) itemView.findViewById(R.id.iv);
            cb= (AppCompatCheckBox) itemView.findViewById(R.id.cb);
        }

        public void bind(final int pos, final AlbumImage data){
            Album.getImageLoader().loadImage(data.getPath(),iv,mItemHeight,mItemHeight);
            if(!mIsMulti){
                cb.setVisibility(View.GONE);
            }
            cb.setChecked(data.isChecked());
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnImageClickListener!=null){
                        mOnImageClickListener.click(iv,pos,data);
                    }
                }
            });
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnImageClickListener!=null){
                        mOnImageClickListener.check(cb,pos,data,cb.isChecked());
                    }
                }
            });
        }

    }

    public interface OnCameraClickListener{
        void click();
    }

    public interface OnImageClickListener{
        void click(ImageView imageView,int pos,AlbumImage data);
        void check(CheckBox checkBox,int pos,AlbumImage data,boolean isChecked);
    }

}
