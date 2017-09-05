package com.min.album.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import com.min.album.Album;
import com.min.album.R;
import com.min.album.bean.AlbumImage;
import com.min.album.util.Utils;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by minyangcheng on 2016/11/23.
 */

public class ImagePreviewDialog extends AppCompatDialog {

    private static final String TAG="ImagePreviewDialog_TEST";

    private Context mContext;

    private ViewPager mVp;
    private CheckBox mCb;

    private List<AlbumImage> mAlbumImageList;
    private int mPos;

    private List<ImageItem> mImageItemList;

    private OnPreviewImageCheckListener mOnPreviewImageCheckListener;

    private int mImageWidth;
    private int mImageHeight;

    public ImagePreviewDialog(Context context , List<AlbumImage> albumImageList , int pos) {
        super(context);
        setDialogTheme();
        this.mContext=context;
        this.mAlbumImageList=albumImageList;
        this.mPos=pos;

        setImageSize();
    }

    private void setImageSize() {
        mImageWidth=Utils.getScreenWidth(mContext)/2;
        mImageHeight=Utils.getScreenHeight(mContext)/2;
    }

    private void setDialogTheme() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_image_preview);
        initViews();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width=WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
    }

    private void initViews() {
        mVp= (ViewPager) findViewById(R.id.vp);
        mCb= (CheckBox) findViewById(R.id.cb);

        if(Utils.isEmpty(mAlbumImageList)){
            return;
        }
        mImageItemList=createImageItemList();
        mVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                displayImage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ImagePageAdapter pageAdapter=new ImagePageAdapter(mImageItemList);
        mVp.setAdapter(pageAdapter);
        if(mPos==0){
            displayImage(mPos);
        }
        mVp.setCurrentItem(mPos);

        mCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnPreviewImageCheckListener!=null){
                    AlbumImage albumImage=mImageItemList.get(mPos).image;
                    mOnPreviewImageCheckListener.notifyCheckChange(mCb,albumImage,mCb.isChecked());
                }
            }
        });
    }

    private void displayImage(int pos){
        this.mPos=pos;
        ImageItem imageItem=mImageItemList.get(mPos);
        mCb.setChecked(imageItem.image.isChecked());
    }

    private List<ImageItem> createImageItemList() {
        List<ImageItem> imageItemList=new ArrayList<>();
        for(int i=0;i< mAlbumImageList.size();i++){
            imageItemList.add(getImageItem(mAlbumImageList.get(i)));
        }
        return imageItemList;
    }

    private ImageItem getImageItem(AlbumImage albumImage){
        ImageItem imageItem=new ImageItem();
        imageItem.image =albumImage;
        return imageItem;
    }

    public class ImagePageAdapter extends PagerAdapter {

        private List<ImageItem> imageItems;

        public ImagePageAdapter(List<ImageItem> imageItems){
            this.imageItems=imageItems;
        }

        @Override
        public int getCount() {
            return imageItems.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((View) object));
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView imageView = new PhotoView(container.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Album.getImageLoader().loadImage(imageItems.get(position).image.getPath(),imageView,mImageWidth,mImageHeight);
            container.addView(imageView);
            return imageView;
        }

    }

    public class ImageItem {

        public AlbumImage image;

    }

    public void setOnPreviewImageCheckListener(OnPreviewImageCheckListener listener){
        this.mOnPreviewImageCheckListener=listener;
    }

    public interface OnPreviewImageCheckListener{
        void notifyCheckChange(CheckBox checkBox,AlbumImage albumImage,boolean isCheck);
    }

}
