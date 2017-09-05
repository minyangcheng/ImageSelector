package com.min.album.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.min.album.Album;
import com.min.album.R;
import com.min.album.bean.AlbumFolder;
import com.min.album.bean.AlbumImage;

import java.util.List;

/**
 * Created by minyangcheng on 2016/11/23.
 */

public class FolderAdapter extends BaseRecyclerViewAdapter<AlbumFolder,FolderAdapter.ItemHolder>{

    public FolderAdapter(Context context) {
        super(context);
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemHolder(mLayoutInflater.inflate(R.layout.item_folder,parent,false));
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(position,getData().get(position));
    }

    public class ItemHolder extends RecyclerView.ViewHolder{

        public ImageView iv;
        public TextView tv;
        public CheckBox cb;

        public ItemHolder(View itemView) {
            super(itemView);
            iv= (ImageView) itemView.findViewById(R.id.iv);
            tv= (TextView) itemView.findViewById(R.id.tv);
            cb= (CheckBox) itemView.findViewById(R.id.cb);
        }

        public void bind(final int pos, final AlbumFolder folder){
            List<AlbumImage> albumImageList=folder.getPhotos();
            AlbumImage firstImage=albumImageList.get(0);
            if(firstImage!=null){
                Album.getImageLoader().loadImage(firstImage.getPath(),iv,0,0);
            }
            cb.setChecked(folder.isChecked());
            tv.setText(mContext.getString(R.string.folder_name,albumImageList.size()+"",folder.getName()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemClickLitener!=null){
                        mOnItemClickLitener.onItemClick(itemView,pos);
                    }
                }
            });
        }

    }

}
