package com.min.imagewrapper.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.min.imagewrapper.util.ImageUtils;
import com.min.imagewrapper.util.L;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback{

	private String TAG = CameraView.class.getSimpleName();

	public static final int FLASH_OFF=0;
	public static final int FLASH_ON=1;
	public static final int FLASH_AUTO=2;

	private int mNowFlashMode=FLASH_OFF;  //初始状态下闪关灯为关闭

	private int mScreenWidth;
	private int mScreenHeight;
	
	private Camera.Size mPictureSize = null;
	private Camera.Size mPreviewSize = null;

	private List<Camera.Size> mSupportedPreviewSizes;
	private List<Camera.Size> mSupportedPictureSizes;

	private SurfaceHolder mHolder;
	private Camera mCamera;

	private Camera.Parameters mParameters;

	private Context mContext;

	private int mCameraPosition = 0;//0代表前置摄像头，1代表后置摄像头

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private int mX, mY, mZ;
	
	private int mBitmapDegree;

	private ShutterCallback sh = new ShutterCallback() {

		@Override
		public void onShutter() {
			try {
				String uriStr="file:///system/media/audio/ui/camera_click.ogg";
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				mediaPlayer.setDataSource(mContext, Uri.parse(uriStr));
				mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });
				mediaPlayer.prepareAsync();
			} catch (Exception e) {
				L.e(TAG, e);
			}
		}
	};

	private SensorEventListener sensorListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor == null) {
				return;
			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				int x = (int) event.values[0];
				int y = (int) event.values[1];
				int z = (int) event.values[2];

				int px = Math.abs(mX - x);
				int py = Math.abs(mY - y);
				int pz = Math.abs(mZ - z);
				int maxvalue = getMaxValue(px, py, pz);
				if (maxvalue > 2) {
					focus();
				}

				mX = x;
				mY = y;
				mZ = z;

			}
		}

		public int getMaxValue(int px, int py, int pz) {
			int max = 0;
			if (px > py && px > pz) {
				max = px;
			} else if (py > px && py > pz) {
				max = py;
			} else if (pz > px && pz > py) {
				max = pz;
			}

			return max;
		}
	};

	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if(success){
				Log.d(TAG, "autoFocusCallback");
			}
		}
	};

	public CameraView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		getScreenParams();
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFixedSize(getScreenWidth(context), getScreenHeight(context)); // 设置Surface分辨率
		mHolder.setKeepScreenOn(true);// 屏幕常亮
		
		initSensor();
	}

	private void getScreenParams() {
		DisplayMetrics metrics=getResources().getDisplayMetrics();
		mScreenWidth=metrics.widthPixels;
		mScreenHeight=metrics.heightPixels;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		L.d(TAG, "onAttachedToWindow");
		if(mSensorManager!=null){
			mSensorManager.registerListener(sensorListener, mSensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		L.d(TAG, "onDetachedFromWindow");
		if(mSensorManager!=null){
			mSensorManager.unregisterListener(sensorListener, mSensor);
		}
	}

	private void initSensor() {
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (null == mSensorManager) {
			L.d(TAG, "deveice not support SensorManager");
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		L.d(TAG, "surfaceCreated");
		try {
			mCamera = Camera.open(mCameraPosition);
			if(mCamera != null){
				mCamera.setPreviewDisplay(holder);
				initCameraParams();
				mCamera.startPreview(); // 开始预览
				mCamera.autoFocus(autoFocusCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// 拍照状态变化时调用该方法
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		L.d(TAG, "surfaceChanged");
		initCameraParams();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		L.d(TAG, "surfaceDestroyed");
		if (mCamera != null) {
			try {
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release(); // 释放照相机
				mCamera = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}


	private void initCameraParams(){
		if(mCamera != null){
			mBitmapDegree = getDisplayOrientation(0, mCameraPosition);
			mCamera.setDisplayOrientation(mBitmapDegree);

			mParameters = mCamera.getParameters();
			mParameters.setPictureFormat(PixelFormat.JPEG);
			mParameters.setJpegQuality(100);

			try {
				mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
				mPreviewSize=getOptimalPreviewSize(mSupportedPreviewSizes, mScreenWidth, mScreenHeight);

				mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
				mPictureSize=getOptimalPreviewSize(mSupportedPictureSizes, mScreenWidth, mScreenHeight);
			} catch (Exception e) {
				L.e(TAG, e);
			}

			if (mPreviewSize != null) {
				mParameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			}
			if (mPictureSize != null) {
				mParameters.setPictureSize(mPictureSize.width, mPictureSize.height);
			}
			L.d(TAG, "mPictureSize.width=%s , mPictureSize.heigh=%s , mPreviewSize.width=%s , mPreviewSize.height=%s"
					, mPictureSize.width, mPictureSize.height, mPreviewSize.width, mPreviewSize.height);
			try {
				mCamera.setParameters(mParameters);
			} catch (Exception e) {
				L.e(TAG, e);
			}
		}

	}

	/**
	 * 获取状态栏高度
	 * @param context context
	 * @return 状态栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		// 获得状态栏高度
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		return context.getResources().getDimensionPixelSize(resourceId);
	}

	private int getScreenWidth(Context context) {
		WindowManager manager = ((Activity) context).getWindowManager();
		int width = 0;
		width = manager.getDefaultDisplay().getWidth();

		return width;
	}

	public static int getScreenHeight(Context context) {
		WindowManager manager = ((Activity) context).getWindowManager();
		int height = 0;
		height = manager.getDefaultDisplay().getHeight();
		int statusHeight=getStatusBarHeight(context);
		if(statusHeight>0){
			height=height-statusHeight;
		}
		return height;
	}

	public int getMaxZoom(){
		mParameters=mCamera.getParameters();
		if(!mParameters.isZoomSupported()) return 0;
		return mParameters.getMaxZoom();
	}

	public void setZoom(int zoom){
		mParameters=mCamera.getParameters();
		if(!mParameters.isZoomSupported()){
			return;
		}
		if(mParameters.getZoom()==zoom){
			return;
		}
		mParameters.setZoom(zoom);
		try {
			mCamera.setParameters(mParameters);
		} catch (Exception e) {
			L.e(TAG, e);
		}
	}

	public int getZoom(){
		mParameters=mCamera.getParameters();
		if(!mParameters.isZoomSupported()) return 0;
		return mParameters.getZoom();
	}

	public void takePicture(final TakePictureListener listener) {
		if(mCamera != null&&listener!=null){
			listener.onCameraStart(mCamera);
			try {
				mCamera.takePicture(sh, null, null, new Camera.PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						try { // 拍摄完成后保存照片
							if(mCameraPosition == 1){//前置摄像头
								mBitmapDegree += 180;
								if(mBitmapDegree > 360){
									mBitmapDegree -= 360;
								}
							}
							Bitmap bp = BitmapFactory.decodeByteArray(data, 0, data.length);
							Bitmap resultBp = ImageUtils.rotaingImageView(mBitmapDegree, bp);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							resultBp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
							if(bp!=null&&!bp.isRecycled()){
								bp.recycle();
							}
							if(resultBp!=null&&!resultBp.isRecycled()){
								resultBp.recycle();
							}

							listener.onCameraSuccess(baos.toByteArray(), camera);
						}catch (Exception e) {
							listener.onCameraError(e);
						}
					}
				});
			} catch (Exception e) {
				listener.onCameraError(e);
			}
		}
	}

	public void startPreview(){
		if(mCamera!=null){
			mCamera.startPreview();
		}
	}

	private void focus(){
		if(mCamera != null){
			try {
				mCamera.autoFocus(autoFocusCallback);
			} catch (Exception e) {
				L.e(TAG,e);
			}
		}
	}

	public void onFocus(Point point,AutoFocusCallback callback){
		Camera.Parameters parameters= mCamera.getParameters();
		//不支持设置自定义聚焦，则使用自动聚焦，返回
		if (parameters.getMaxNumFocusAreas()<=0) {
			try {
				mCamera.autoFocus(callback);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		List<Area> areas=new ArrayList<Area>();
		int left=point.x-300;
		int top=point.y-300;
		int right=point.x+300;
		int bottom=point.y+300;
		left=left<-1000?-1000:left;
		top=top<-1000?-1000:top;
		right=right>1000?1000:right;
		bottom=bottom>1000?1000:bottom;
		areas.add(new Area(new Rect(left,top,right,bottom), 100));
		try {
			parameters.setFocusAreas(areas);
			//本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
			//目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
			mCamera.setParameters(parameters);
			mCamera.autoFocus(callback);
		} catch (Exception e) {
			L.e(TAG,e);
		}
	}

	public int changeFlashMode(){
		mParameters = mCamera.getParameters();
		if(mNowFlashMode==FLASH_OFF){
			mParameters.setFlashMode(Parameters.FLASH_MODE_ON);
			mNowFlashMode=FLASH_ON;
		}else if(mNowFlashMode==FLASH_ON){
			mParameters.setFlashMode(Parameters.FLASH_MODE_AUTO);
			mNowFlashMode=FLASH_AUTO;
		}else{
			mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mNowFlashMode=FLASH_OFF;
		}
		try {
			mCamera.setParameters(mParameters);
		} catch (Exception e) {
			L.e(TAG,e);
		}
		return mNowFlashMode;
	}

	public void changeCamera(){
		//切换前后摄像头
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

		for(int i = 0; i < cameraCount; i++   ) {
			Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
			if(mCameraPosition == 0) {
				//现在是后置，变更为前置
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
					mCameraPosition = 1;
					mCamera.stopPreview();//停掉原来摄像头的预览
					mCamera.release();//释放资源
					mCamera = null;//取消原来摄像头
					mCamera = Camera.open(i);//打开当前选中的摄像头
					try {
						mCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					initCameraParams();
					mCamera.startPreview();//开始预览
					mCamera.autoFocus(autoFocusCallback);
					break;
				}
			} else if(mCameraPosition == 1){
				//现在是前置， 变更为后置
				if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
					mCameraPosition = 0;
					mCamera.stopPreview();//停掉原来摄像头的预览
					mCamera.release();//释放资源
					mCamera = null;//取消原来摄像头
					mCamera = Camera.open(i);//打开当前选中的摄像头
					try {
						mCamera.setPreviewDisplay(mHolder);//通过surfaceview显示取景画面
					} catch (IOException e) {
						e.printStackTrace();
					}
					initCameraParams();
					mCamera.startPreview();//开始预览
					mCamera.autoFocus(autoFocusCallback);
					break;
				}
			}

		}
	}

	public int getDisplayOrientation(int degrees, int cameraId) {
		// See android.hardware.Camera.setDisplayOrientation for
		// documentation.
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		return result;
	}

	public interface TakePictureListener{
		void onCameraStart(Camera camera);
		void onCameraSuccess(byte[] data, Camera camera);
		void onCameraError(Exception e);
	}

}