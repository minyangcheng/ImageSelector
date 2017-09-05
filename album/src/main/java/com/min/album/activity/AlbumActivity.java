package com.min.album.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.min.album.Album;
import com.min.album.AlbumContant;
import com.min.album.R;
import com.min.album.adapter.ImageAdapter;
import com.min.album.bean.AlbumFolder;
import com.min.album.bean.AlbumImage;
import com.min.album.dialog.ImageFolderDialog;
import com.min.album.dialog.ImagePreviewDialog;
import com.min.album.util.AlbumScanner;
import com.min.album.util.CameraHelper;
import com.min.album.util.Utils;
import com.min.album.view.divider.DividerGridItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private static final String TAG="AlbumActivity_TEST";

    private Toolbar mToolbar;
    private RecyclerView mImageRv;
    private TextView mFolderTv;
    private TextView mPreviewTv;

    private boolean mIsMulti;
    private int mMinCount;
    private int mMaxCount;
    private ArrayList<Integer> mPreSelectedList;

    private ImageAdapter mImageAdapter;

    private List<AlbumFolder> mAlbumFolderList;
    private int mCurrentAlbumFolderIndex=0;

    private LoadImageTask mLoadImageTask;

    private List<AlbumImage> mAlbumSelectList=new ArrayList<>();

    private MenuItem mSureMenuItem;

    private CameraHelper mCameraHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        mCameraHelper=new CameraHelper(this);
        if(savedInstanceState!=null){
            mCurrentAlbumFolderIndex=savedInstanceState.getInt(AlbumContant.KEY_STATE_FOLDER_INDEX,0);
        }

        getDataFromIntent();
        findViews();
        initViews();
        scanImages();
        if(mIsMulti){
            setPreviewCount();
            setMenuSureText();
        }
    }

    private void getDataFromIntent() {
        Intent intent=getIntent();
        mIsMulti=intent.getBooleanExtra(AlbumContant.KEY_IS_MULTI,false);
        mMinCount=intent.getIntExtra(AlbumContant.KEY_MIN_COUNT,-1);
        mMaxCount =intent.getIntExtra(AlbumContant.KEY_MAX_COUNT,AlbumContant.DEFAULT_MAX_COUNT);
        mPreSelectedList= (ArrayList<Integer>) intent.getSerializableExtra(AlbumContant.KEY_PRE_SELECTED);
    }

    private void initViews() {
        initToolbar();
        initImageRv();
        if(!mIsMulti){
            mPreviewTv.setVisibility(View.GONE);
        }
    }

    private void initImageRv() {
        RecyclerView.LayoutManager layoutManager=new GridLayoutManager(this, Album.getColumnNum());
        mImageRv.setLayoutManager(layoutManager);
        DividerGridItemDecoration decoration=new DividerGridItemDecoration(this,
                Utils.getShapeDrawable(android.R.color.transparent, Utils.dpToPxInt(this,5)));
        mImageRv.addItemDecoration(decoration);
        mImageRv.setHasFixedSize(true);
        mImageAdapter=new ImageAdapter(this,mIsMulti);
        mImageRv.setAdapter(mImageAdapter);
        mImageAdapter.setOnCameraClickListener(new ImageAdapter.OnCameraClickListener() {
            @Override
            public void click() {
                handleCameraClick();
            }
        });
        mImageAdapter.setOnImageClickListener(new ImageAdapter.OnImageClickListener() {
            @Override
            public void click(ImageView imageView, int pos, AlbumImage data) {
                if(mIsMulti){
                    handlePreviewClick(mAlbumFolderList.get(mCurrentAlbumFolderIndex).getPhotos(),pos);
                }else{
                    handleSingleChoice(data);
                }
            }

            @Override
            public void check(CheckBox checkBox,int pos, AlbumImage data, boolean isChecked) {
                handleCheck(checkBox,data,isChecked);
            }
        });
    }

    private void handleCameraClick(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                camera();
            } else if (permissionResult == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}
                        , AlbumContant.CAMERA_PERMISSION_REQUEST_CODE);
            }
        } else {
            camera();
        }
    }

    private void camera(){
        mCameraHelper.openCamera(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCameraHelper.handleActivityResult(requestCode, resultCode, new CameraHelper.Callbacks() {
            @Override
            public void onImagePickerError(Exception e) {
                Utils.toast(AlbumActivity.this,getString(R.string.camera_fail));
            }

            @Override
            public void onImagePicked(File imageFile) {
                AlbumImage image=new AlbumImage(0,imageFile.getAbsolutePath(),"",0L,false);
                mAlbumSelectList.clear();
                mAlbumSelectList.add(image);
                result();
            }
        });
    }

    private void handleSingleChoice(AlbumImage image){
        mAlbumSelectList.add(image);
        result();
    }

    private void handleCheck(CheckBox checkBox, AlbumImage data, boolean isChecked){
        if(isChecked&&mAlbumSelectList.size()>= mMaxCount){
            Utils.toast(this,getString(R.string.has_arrive_max_need_count,mMaxCount+""));
            checkBox.setChecked(false);
            return;
        }
        data.setChecked(isChecked);
        if(!mAlbumSelectList.contains(data)){
            mAlbumSelectList.add(data);
        }else{
            mAlbumSelectList.remove(data);
        }
        setPreviewCount();
        setMenuSureText();
    }

    private void result(){
        ArrayList<String>  resultList=new ArrayList<>();
        for(AlbumImage image : mAlbumSelectList){
            resultList.add(image.getPath());
        }
        Intent intent=new Intent();
        intent.putExtra(AlbumContant.KEY_RESULT,resultList);
        setResult(RESULT_OK,intent);
        finish();
    }

    private void notifyAdapterAtPos(AlbumImage albumImage){
        int pos=mAlbumFolderList.get(mCurrentAlbumFolderIndex).getPhotos().indexOf(albumImage);
        if(pos>-1){
            mImageAdapter.notifyMyItemChanged(pos);
        }
    }

    private void scanImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                executeTask();
            } else if (permissionResult == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                    , AlbumContant.STORAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            executeTask();
        }
    }

    private void executeTask(){
        mLoadImageTask=new LoadImageTask();
        mLoadImageTask.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AlbumContant.STORAGE_PERMISSION_REQUEST_CODE: {
                int permissionResult = grantResults[0];
                if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                    executeTask();
                } else {
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle(R.string.album_dialog_permission_failed)
                            .setMessage(R.string.album_permission_storage_failed_hint)
                            .setPositiveButton(R.string.album_dialog_sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            }
            case AlbumContant.CAMERA_PERMISSION_REQUEST_CODE: {
                int permissionResult = grantResults[0];
                if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                    camera();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.album_dialog_permission_failed)
                            .setMessage(R.string.album_permission_camera_failed_hint)
                            .setPositiveButton(R.string.album_dialog_sure, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private void findViews() {
        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        mImageRv= (RecyclerView) findViewById(R.id.rv_image);
        mFolderTv= (TextView) findViewById(R.id.tv_folder);
        mFolderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFolderTvClick();
            }
        });
        mPreviewTv= (TextView) findViewById(R.id.tv_preview);
        mPreviewTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePreviewClick(mAlbumSelectList,0);
            }
        });
    }

    public void handleFolderTvClick(){
        ImageFolderDialog imageFolderDialog=new ImageFolderDialog(this,mAlbumFolderList);
        imageFolderDialog.setOnImageFolderSelect(new ImageFolderDialog.OnImageFolderSelectListener() {
            @Override
            public void onFolderSelect(int pos, AlbumFolder albumFolder) {
                if(pos==mCurrentAlbumFolderIndex){
                    return;
                }
                mAlbumFolderList.get(mCurrentAlbumFolderIndex).setChecked(false);
                mAlbumFolderList.get(pos).setChecked(true);
                showAlbumFolder(pos);
            }
        });
        imageFolderDialog.show();
    }

    public void handlePreviewClick(List<AlbumImage> albumImageList , int pos){
        if(Utils.isEmpty(albumImageList)){
            return;
        }
        ImagePreviewDialog imagePreviewDialog=new ImagePreviewDialog(this,albumImageList,pos);
        imagePreviewDialog.setOnPreviewImageCheckListener(new ImagePreviewDialog.OnPreviewImageCheckListener() {
            @Override
            public void notifyCheckChange(CheckBox checkBox,AlbumImage albumImage,boolean isCheck) {
                handleCheck(checkBox,albumImage,isCheck);
                notifyAdapterAtPos(albumImage);
            }
        });
        imagePreviewDialog.show();
    }

    private void initToolbar() {
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(mIsMulti){
            mToolbar.inflateMenu(R.menu.main_menu);
            mSureMenuItem=mToolbar.getMenu().findItem(R.id.menu_sure);
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId()==R.id.menu_sure){
                        handleMenuSureClick();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void handleMenuSureClick(){
        if(mIsMulti){
            if(mMinCount>0){
                if(mAlbumSelectList.size()< mMinCount){
                    Utils.toast(this,getString(R.string.has_image_no_select,mMinCount+""));
                    return;
                }
            }
        }
        result();
    }

    private void setMenuSureText(){
        mSureMenuItem.setTitle(getString(R.string.sure_num
                    ,String.valueOf(mAlbumSelectList.size()),String.valueOf(mMaxCount)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(AlbumContant.KEY_STATE_FOLDER_INDEX,mCurrentAlbumFolderIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadImageTask!=null&&!mLoadImageTask.isCancelled()){
            mLoadImageTask.cancel(true);
        }
    }

    private void setPreviewCount(){
        String countStr=String.valueOf(mAlbumSelectList.size());
        mPreviewTv.setText(countStr);
    }

    private void showAlbumFolder(int index){
        if(Utils.isEmpty(mAlbumFolderList)){
            Utils.toast(this,R.string.this_album_is_empty);
            return;
        }
        mCurrentAlbumFolderIndex=index;
        mImageAdapter.setData(mAlbumFolderList.get(mCurrentAlbumFolderIndex).getPhotos());
        mFolderTv.setText(mAlbumFolderList.get(mCurrentAlbumFolderIndex).getName());
        mImageRv.scrollToPosition(0);
    }

    private class LoadImageTask extends AsyncTask<Void,Void,List<AlbumFolder>>{

        @Override
        protected List<AlbumFolder> doInBackground(Void... params) {
            List<AlbumFolder> albumFolderList=null;
            try {
                albumFolderList=AlbumScanner.getInstance().getPhotoAlbum(AlbumActivity.this);
                setPreSelectCheckStatus(albumFolderList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return albumFolderList;
        }

        private void setPreSelectCheckStatus(List<AlbumFolder> albumFolderList){
            if(Utils.isEmpty(albumFolderList)||Utils.isEmpty(mPreSelectedList)){
                return;
            }
            List<AlbumImage> imageList=null;
            for(AlbumFolder folder : albumFolderList){
                imageList=folder.getPhotos();
                if(!Utils.isEmpty(imageList)){
                    for(AlbumImage image : imageList){
                        if(mPreSelectedList.contains(image.getPath())){
                            if(!mAlbumSelectList.contains(image)){
                                mAlbumSelectList.add(image);
                                image.setChecked(true);
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(List<AlbumFolder> albumFolders) {
            if(!AlbumActivity.this.isFinishing()){
                mAlbumFolderList=albumFolders;
                if(mIsMulti){
                    setMenuSureText();
                    setPreviewCount();
                }
                showAlbumFolder(mCurrentAlbumFolderIndex);
            }
        }
    }

}
