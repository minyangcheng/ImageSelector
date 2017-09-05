package com.min.album.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.min.album.Album;
import com.min.album.R;
import com.min.album.adapter.BaseRecyclerViewAdapter;
import com.min.album.adapter.FolderAdapter;
import com.min.album.bean.AlbumFolder;

import java.util.List;

/**
 * Created by minyangcheng on 2016/11/23.
 */

public class ImageFolderDialog extends BottomSheetDialog {

    private Context mContext;
    private RecyclerView mRv;
    private FolderAdapter mAdapter;
    private List<AlbumFolder> mAlbumFolderList;

    private OnImageFolderSelectListener mOnImageFolderSelectListener;

    public ImageFolderDialog(@NonNull Context context , List<AlbumFolder> albumFolderList) {
        super(context);
        this.mContext=context;
        this.mAlbumFolderList=albumFolderList;
        setContentView(R.layout.dialog_image_folder);
        findViews();
        initViews();
        initData();
    }

    private void initData() {
        mAdapter.setData(mAlbumFolderList);
    }

    private void initViews() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mContext);
        mRv.setLayoutManager(linearLayoutManager);
        mAdapter=new FolderAdapter(mContext);
        mAdapter.setOnItemClickLitener(new BaseRecyclerViewAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mOnImageFolderSelectListener!=null){
                    mOnImageFolderSelectListener.onFolderSelect(position,mAdapter.getData().get(position));
                }
                dismiss();
            }
        });
        mRv.setAdapter(mAdapter);
    }

    private void findViews() {
        mRv= (RecyclerView) findViewById(R.id.rv);
    }

    public void setOnImageFolderSelect(OnImageFolderSelectListener listener){
        this.mOnImageFolderSelectListener=listener;
    }

    public interface OnImageFolderSelectListener{
        void onFolderSelect(int pos , AlbumFolder albumFolder);
    }

}
