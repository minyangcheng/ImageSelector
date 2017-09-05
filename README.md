# Android图片选择器

当需要用户选择图片的时候，可以调用系统相机或相册。如果需要用户进行多图选择就需要自定义图片选择器。这里介绍我自己封装的图片选择器。

## 实现思路

1. 对外开放api的功能：多图选择，单张图片选择，可以设置,用户可以在外部配置图片加载器
2. Android M以上进行权限处理
3. 通过ContentResolver取到系统相册所有图片，并对其分类
4. 用recycleview图片展示，可以进行相册文件夹选择，图片预览

## 实现代码

### 如何使用？

#### 在Application中配置AlbumConfig
```java
public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        ImageLoaderWrap.init(this);
        initAlbumConfig();
    }

    private void initAlbumConfig() {
        AlbumConfig albumConfig=new AlbumConfig.Builder().setImageLoader(new AlbumImageLoader() {
            @Override
            public void loadImage(String imagePath, ImageView imageView,int expectWidht,int expectHeight) {
                //配置图片加载器
				ImageLoaderWrap.displayFileImage(new File(imagePath),imageView,expectWidht,expectHeight);
            }
        }).setColumnNum(3)  //配置一行显示多少张图片
                .build();
        Album.setConfig(albumConfig);
    }

    public static Context getContext(){
        return context;
    }

}
```

#### 在Activity或Fragment中使用

```java
@OnClick(R.id.btn_single)
void clickBtnForSingle(){
    Album.startAlbumForSingleChoice(this);
}

@OnClick(R.id.btn_mutil_four)
void clickBtnForMutilFour(){
    Album.startAlbumForMultiChoice(this,4,null);
}

@OnClick(R.id.btn_mutil_pre)
void clickBtnForMutilPre(){
    ArrayList<String> pathList=new ArrayList<>();
    pathList.add("/storage/emulated/0/DCIM/Camera/plateID_20161126_170827.jpg");
    pathList.add("/storage/emulated/0/DCIM/Camera/plateID_20161110_163801.jpg");
    Album.startAlbumForMultiChoice(this,4,pathList);
}

@Override
protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Album.handleActivityResult(requestCode, resultCode, data, new Album.OnResultListener() {
        @Override
        public void onResult(ArrayList<String> resultList) {
            mResultTv.setText(GsonUtil.toPrettyJson(resultList));
            L.d(TAG, GsonUtil.toPrettyJson(resultList));
        }
    });
}
```

## 核心实现类

1. AlbumActivity：图片展示类
2. ImageFolderDialog:图片文件夹展示类,用BottomSheetDialog实现
3. ImagePreviewDialog:图片预览类
4. AlbumConfig：Album配置类，可以配置图片显示列数，图片加载器

具体实现请自行查看代码。

## 碰到的问题

### 频繁滑动图片列表，一段时间后，界面会出现卡顿？

最开始我怀疑是由于某些对象的内存泄漏引起的，用Android studio的Memory Monitor分析，当我退出AlbumActivity界面时候，点击Initiate GC，内存又会回到正常状态，而且用Dump Java Heap生成的hprof执行内存泄漏分析，也没有发现有内存泄漏的对象。于是我开始觉得问题是由UIL加载图片引起的，查看了一下UIL打印出的日志，发现UIL几个问题：
1. RecycleView中的第一次加载ImageView，UIL不能读取其宽高的，不过后面重用了后就能正常读取宽高了
2. ImageView的layout_height如果设置为wrap_content，UIL也是不能读取其宽高
3. 即使UIL设置了内存缓存，发现当频繁滑动列表的时候也会出现内存爆张的情况

解决：
1. 加载图片的时候指定需要加载图片的宽高，指定的宽高一定别超过屏幕的宽高`new ImageSize(width,height)`;
2. 设置延迟加载`delayBeforeLoading(100)`
3. 设置图片像素格式`bitmapConfig(Bitmap.Config.RGB_565)`
4. 使用内部默认的线程池大小即可

### ViewPager中展示很多图片的时候，左右滑动，在Memory Monitor上会发现内存一直在涨？

一开始初始化PagerAdapter的时候我会把ViewPager中的item view构造好，放入一个List中，然后直接使用，这样会造成即使ViewPager实际上只加载三张图片，但是其他的图片是在View中，这些view放在List中并没有被释放，导致滑动的时候内存话一直涨。

解决：
当需要显示该图片的时候，再构造view，不需要的时候从viewpager中移除view

```java
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
```

## 总结

当我们写一个可能耗内存的功能时，最好能结合android studio的Memory Monitor进行测试，这样才能观测到你写的功能又没吃掉很多内存，如果在操作的过程出现内存一直上涨的情况，那就可能出现了内存泄漏或者出现了重复创建许多的大内存对象。分析工具可以用Android studio的Memory Monitor或者MAT工具。
