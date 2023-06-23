package proj.research.colortools.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import proj.research.colortools.R;
import proj.research.colortools.bean.LinearModel;
import proj.research.colortools.db.LocalDatabase;
import proj.research.colortools.db.dao.ILinearModelDao;
import proj.research.colortools.db.model.LinearSeqModel;
import proj.research.colortools.util.ColorUtils;
import proj.research.colortools.util.Local;


public class ColorFragment extends Fragment {

    private static final int MODE_VIDEO = 10001;
    private static final int MODE_PIC = 10002;
    private int mMode = MODE_VIDEO;
    private LinearModel mModel;
    //选取的图片Uri列表
    private List<Uri> mPicUriList = new ArrayList<>();
    private int mCurrentPicIndex = 0;

    private static final String TAG = "ColorFragment";
    private Context mContext;

    private View mRootView;
    private SurfaceView mPreviewView;
    private ImageView mPickerIcon;
    private ImageView mPreviewSelectIv;
    private TextView mHexTv;
    private TextView mRgbTv;
    //浓度值
    private TextView mConceTv;
    private Button mChooseModeBtn;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Bitmap mPreViewBm;
    private LocalDatabase mDB;
    private ILinearModelDao mModelDao;
    private ImageView mPicPreviewImg;
    private Button mPreviewLastBtn;
    private Button mPreviewNextBtn;
    private RelativeLayout mPicPreviewLayout;
    //相机预览回调
    private CameraSurfaceHolderCallback mHolderCallback;
    //语言是否为中文
    private boolean mIsChinese;
    //临时测试的相机预览图
    //private ImageView mCameraPreViewTmpIV;


    public ColorFragment(Context context) {
        this.mContext = context;

        //this.mColorGetter = colorGetter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_color, container, false);
        mIsChinese = Locale.getDefault().getLanguage().equals("zh");
        initView();
        initListener();
        initOpenCv();
        initDB();
        return mRootView;
    }

    //初始化数据库选项
    private void initDB() {
        //获取数据库中的模型信息
        mDB = LocalDatabase.getInstance(mContext);
        mModelDao = mDB.getLinearModelDao();

    }


    private void initOpenCv() {

    }

    /**
     * 初始化颜色拾取器功能
     */
    private void initPickerFunc() {

    }


    private void initView() {
        //相机预览控件
        mPreviewView = mRootView.findViewById(R.id.sv_camera_preview);
        mSurfaceHolder = mPreviewView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolderCallback = new CameraSurfaceHolderCallback();
        mSurfaceHolder.addCallback(mHolderCallback);

        //颜色信息控件
        mPreviewSelectIv = mRootView.findViewById(R.id.iv_select_color_preview);
        mHexTv = mRootView.findViewById(R.id.tv_color_hex);
        mRgbTv = mRootView.findViewById(R.id.tv_color_rgb);

        mPickerIcon = mRootView.findViewById(R.id.iv_color_picker);

        mPickerIcon.setOnTouchListener(new ColorPickerTouchListener(mPickerIcon));

        //相机预览临时测试控件
        //mCameraPreViewTmpIV = mRootView.findViewById(R.id.camera_preview_temp);


        mConceTv = mRootView.findViewById(R.id.tv_conce_val);
        mChooseModeBtn = mRootView.findViewById(R.id.btn_choose_mode);

        //图片预览相关
        mPicPreviewLayout = mRootView.findViewById(R.id.rl_pic_preview);
        mPicPreviewImg = mRootView.findViewById(R.id.iv_pic_preview);
        mPreviewLastBtn = mRootView.findViewById(R.id.btn_last_pic);
        mPreviewNextBtn = mRootView.findViewById(R.id.btn_next_pic);

    }

    /**
     * 初始化事件
     */
    private void initListener() {
        //选择模式按钮
        this.mChooseModeBtn.setOnClickListener(view -> {
            /*
            显示 选项 对话框
                视频模式：
                图片模式:
                选择浓度模型：
             */
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            //判断系统的语言是中文还是其他，其他使用英语
            builder.setTitle(mIsChinese ? "选项选择" : "Choose Mode");
            String[] items = null;
            if (mIsChinese){
                items = new String[]{"视频模式", "图片模式", "选择浓度模型"};
            }else{
                items = new String[]{"Video Mode", "Picture Mode", "Select Model"};
            }

            builder.setItems(items, (dialogInterface, i) -> {
                switch (i) {
                    case 0:

                        startVideoMode();
                        break;
                    case 1:
                        chooseAndShowPics();
                        break;
                    case 2:
                        selectModel();
                        break;
                }
            });
            builder.show();
        });

        // 上一张图片按钮
        this.mPreviewLastBtn.setOnClickListener(view -> {
            if (mCurrentPicIndex - 1 >= 0) {
                mCurrentPicIndex--;
                showPreviewPic();
            }

        });
        // 下一张图片按钮
        this.mPreviewNextBtn.setOnClickListener(view -> {
            if (mCurrentPicIndex + 1 < mPicUriList.size()) {
                mCurrentPicIndex++;
                showPreviewPic();
            }
        });


    }

    /**
     * 开启相机预览模式
     */
    private void startVideoMode() {
        //显示相机预览，隐藏图片预览
        mPreviewView.setVisibility(View.VISIBLE);
        mPicPreviewLayout.setVisibility(View.GONE);

        //重新开启相机回调
        mSurfaceHolder.addCallback(mHolderCallback);
        startCameraPreview(mPreviewView.getWidth(), mPreviewView.getHeight());

        mMode = MODE_VIDEO;
    }

    /**
     * 批量选择图片并显示
     */
    private void chooseAndShowPics() {
        mPicUriList.clear();
        //调用系统相册选择图片
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                //获取选择的图片
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    //多选
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        //将选择的图片的uri添加到集合中
                        mPicUriList.add(uri);
                    }
                } else {
                    //单选
                    Uri uri = data.getData();
                    //将选择的图片的uri添加到集合中
                    mPicUriList.add(uri);
                }
                //显示第一张图片
                this.mCurrentPicIndex = 0;
                //关闭相机预览回调
                mSurfaceHolder.removeCallback(mHolderCallback);
                //隐藏视频，显示图片预览
                mPreviewView.setVisibility(View.GONE);
                mPicPreviewLayout.setVisibility(View.VISIBLE);
                mPicPreviewImg.setDrawingCacheEnabled(true);
                showPreviewPic();
                mMode = MODE_PIC;
            }
        }
    }

    /**
     * 显示选择的预览图片
     */
    private void showPreviewPic() {
        if (mPicUriList.size() == 0 || mCurrentPicIndex < 0 || mCurrentPicIndex >= mPicUriList.size()) {
            return;
        }
        mPicPreviewImg.setImageURI(mPicUriList.get(mCurrentPicIndex));
        //如果是第一张图片则隐藏 上一张 图片，如果是最后一张图片则隐藏 下一张 图片
        if (mCurrentPicIndex == 0) {
            mPreviewLastBtn.setVisibility(View.INVISIBLE);
        } else {
            mPreviewLastBtn.setVisibility(View.VISIBLE);
        }
        if (mCurrentPicIndex == mPicUriList.size() - 1) {
            mPreviewNextBtn.setVisibility(View.INVISIBLE);
        } else {
            mPreviewNextBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 选择浓度模型，以实时显示浓度
     */
    private void selectModel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mIsChinese ? "选择浓度模型" : "Select Model");
        List<LinearSeqModel> models = mModelDao.queryAll();
        String[] items = new String[models.size()];
        for (int i = 0; i < models.size(); i++) {
            items[i] = models.get(i).getName();
        }
        builder.setItems(items, (dialogInterface, i) -> {
            mModel = Local.seqModelToModel(models.get(i));
            Toast.makeText(mContext, mIsChinese ? "浓度模型选择成功" : "Select Model Success", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }


    /**
     * 布局重绘以后，会导致选择器图标的位置复位，所以视频模式不用移动事件，只有图片预览模式使用移动事件
     */
    private class ColorPickerTouchListener implements View.OnTouchListener {

        private View mColorPicker;
        private FrameLayout mColorLayout;
        private LinearLayout mColorInfoLayout;
        //选择器宽高
        private int mColorPickerWidth;
        private int mColorPickerHeight;
        //允许的移动边际
        private int mAllowWidthRange;
        private int mAllowTopBorder;
        private int mAllowBottomBorder;

        //移动控件
        private int startX = 0;
        private int startY = 0;
        private int endX = 0;
        private int endY = 0;
        private int mL;
        private int mB;
        private int mR;
        private int mT;


        public ColorPickerTouchListener(View colorPicker) {
            mColorPicker = colorPicker;
            mColorLayout = mRootView.findViewById(R.id.fl_color_content);
            mColorInfoLayout = mRootView.findViewById(R.id.ll_color_info_bar);
            initData();
        }


        private void initData() {
            mColorPicker.post(new Runnable() {
                @Override
                public void run() {
                    mColorPickerWidth = mColorPicker.getWidth();
                    mColorPickerHeight = mColorPicker.getHeight();
                    //允许移动的左右范围
                    mAllowWidthRange = mColorLayout.getWidth();
                    //获取允许移动的上下边界
                    mAllowTopBorder = mColorInfoLayout.getBottom();
                    mAllowBottomBorder = mColorLayout.getBottom();
                }
            });
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // 图片预览模式下才能移动
            //if(mMode == MODE_VIDEO) {
            //    return false;
            //}
            //图标跟随手指移动
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Log.e("TAG", "ACTION_DOWN");
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    //Log.e(TAG, "startX = " + startX +", startY = " + startY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Log.e("TAG", "ACTION_MOVE");
                    endX = (int) event.getRawX();
                    endY = (int) event.getRawY();
                    int dx = endX - startX;
                    int dy = endY - startY;
                    //Log.e(TAG, "dx=" + dx + ", dy=" + dy);
                    moveView(dx, dy);
                    updateColorValue();
                    //更新颜色预览值

                    //更新初始坐标（很重要）
                    startX = endX;
                    startY = endY;
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }


            return true;
        }

        private void moveView(int dx, int dy) {

            //int pickerL = mPickerIcon.getLeft();
            //int pickerT = mPickerIcon.getTop();
            //int pickerR = mPickerIcon.getRight();
            //int pickerB = mPickerIcon.getBottom();
            //Log.e(TAG, "before: " + "pickerL = " + pickerL + ", pickerT = " + pickerT + ", pickerR = " + pickerR + ", pickerB = " + pickerB);
            mL = this.mColorPicker.getLeft() + dx;
            mB = this.mColorPicker.getBottom() + dy;
            mR = this.mColorPicker.getRight() + dx;
            mT = this.mColorPicker.getTop() + dy;

            // 注意：对控件设置允许滑动范围，设置LayoutParams的右和下的margin值不起作用，只能通过设置left和top的margin
            ////判断是否超出屏幕左右两边
            if (mL < 0) {
                mL = 0;
            }
            if (mR > mAllowWidthRange) {
                mL = mAllowWidthRange - mColorPickerWidth;
            }
            //判断是否超出上下边界
            if (mT < mAllowTopBorder) {
                mT = mAllowTopBorder;
            }

            if (mB > mAllowBottomBorder) {
                mT = mAllowBottomBorder - mColorPickerHeight;
            }

            //移动控件，同时防止布局重绘的时候，控件复位
            //mColorPicker.layout(mL, mT, mR, mB);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mColorPickerWidth, mColorPickerHeight);
            //params.setMargins(mColorPicker.getLeft(), mColorPicker.getTop(), mColorPicker.getRight(), mColorPicker.getBottom());

            params.setMargins(mL, mT, 0, 0);
            mColorPicker.setLayoutParams(params);
            //Log.e(TAG, "mB = " + mB +  ", mColorPicker.getBottom() = " + mColorPicker.getBottom());
            //Log.e(TAG, "after: " + "pickerL = " + mL + ", pickerT = " + mT + ", pickerR = " + mR + ", pickerB = " + mB);
        }

    }

    //获取相机数据回调
    private class HanldeRGBPreviewCallback implements Camera.PreviewCallback {
        private int processFlag = 0;
        private final int SKIP_FREQ = 1;
        int a = 0;
        private ImageProcessThread mHandleThread;
        private Camera.Size mCameraSize;

        public HanldeRGBPreviewCallback() {
            mCameraSize = mCamera.getParameters().getPreviewSize();
            this.mHandleThread = new ImageProcessThread(mCameraSize.width, mCameraSize.height);
            this.mHandleThread.start();
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (++processFlag % SKIP_FREQ != 0) {
                return;
            }

            Log.e(TAG, "HanldeRGBPreviewCallback " + (++a) + " times");
            Log.e(TAG, "SurfaceView: " + "size = " + mPreviewView.getWidth() + "x" + mPreviewView.getHeight());
            //YuvImage image = new YuvImage(data, ImageFormat.NV21, mCameraSize.width, mCameraSize.height, null);
            if (data != null && data.length != 0) {
                this.mHandleThread.addTask(data);
            }

        }

        private class ImageProcessThread extends Thread {
            private List<byte[]> mUnHandleList = null;
            //传入图片的宽高
            private int mImageWidth;
            private int mImageHeight;

            public ImageProcessThread(int imageWidth, int imageHeight) {
                this.mUnHandleList = new ArrayList<>();
                this.mImageWidth = imageWidth;
                this.mImageHeight = imageHeight;
            }

            public void addTask(byte[] imageByte) {
                if (imageByte != null && imageByte.length != 0) {
                    mUnHandleList.add(imageByte);
                }
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        if (mUnHandleList.size() > 0) {
                            byte[] imageData = mUnHandleList.get(mUnHandleList.size() - 1);
                            mUnHandleList.clear();
                            Bitmap processedBm = processImage(imageData);
                            if (processedBm != null) {
                                mPreViewBm = processedBm;
                            }
                            handler.sendEmptyMessage(HANDLE_UPDATE_COLOR);
                            //handler.sendEmptyMessage(HANDLE_UPDATE_BITMAP);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error:" + e.getMessage());
                        e.printStackTrace();
                    }
                }

            }

            /**
             * 处理图片并返回处理后的位图
             *
             * @param data 待处理图片数据数组Yuv格式
             * @return 处理后的位图
             * @throws IOException
             */
            private Bitmap processImage(byte[] data) throws IOException {
                if (data == null) {
                    return null;
                }
                try {
                    YuvImage image = new YuvImage(data, ImageFormat.NV21, mCameraSize.width, mCameraSize.height, null);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, mImageWidth, mImageHeight), 60, stream);
                    Bitmap bm = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                    ////将图片旋转90度并压缩大小到和SurfaceView一样
                    //Matrix matrix = new Matrix();
                    //matrix.postRotate(90);
                    //Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    //mPreViewBm = Bitmap.createScaledBitmap(resizeBmp, mPreviewView.getWidth(), mPreviewView.getHeight(), true);

                    Mat srcMat = new Mat();
                    Utils.bitmapToMat(bm, srcMat);
                    //将Mat向右旋转90度
                    Mat mat90 = new Mat();
                    Core.rotate(srcMat, mat90, Core.ROTATE_90_CLOCKWISE);
                    //将Mat压缩到和SurfaceView一样的宽高
                    Mat fitSizeMat = new Mat();
                    Imgproc.resize(mat90, fitSizeMat, new Size(mPreviewView.getWidth(), mPreviewView.getHeight()));
                    //将Mat转换成Bitmap
                    Bitmap bitmap = Bitmap.createBitmap(fitSizeMat.cols(), fitSizeMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(fitSizeMat, bitmap);
                    stream.close();
                    return bitmap;
                } catch (UnsupportedOperationException e) {
                    Log.e(TAG, "processImage:UnsupportedOperationException");
                    e.printStackTrace();
                }
                return null;


                //将YuvImage转换成openCV的Mat对象
                //Mat mat = new Mat(mImageHeight,mImageWidth,CvType.CV_8UC1);//,byteBuffer 1440,1080
                //int re =  mat.put(0,0,data);
                //Mat bgr_i420 = new Mat();
                //Imgproc.cvtColor(mat , bgr_i420, Imgproc.COLOR_YUV2BGR_NV21);//COLOR_YUV2BGR_I420


                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //image.compressToJpeg(new Rect(0, 0, mImageWidth, mImageHeight), 60, stream);
                //Bitmap bm = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                ////将图片旋转90度并压缩大小到和SurfaceView一样
                //Matrix matrix = new Matrix();
                //matrix.postRotate(90);
                //Bitmap resizeBmp = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                //mPreViewBm = Bitmap.createScaledBitmap(resizeBmp, mPreviewView.getWidth(), mPreviewView.getHeight(), true);
                //stream.close();
                //return null;
            }
        }
    }

    /**
     * 更新颜色展示信息
     */
    private void updateColorValue() {

        //获取颜色选择器的中心点坐标
        int centerX = mPickerIcon.getLeft() + mPickerIcon.getWidth() / 2;
        int centerY = mPickerIcon.getTop() + mPickerIcon.getHeight() / 2;
        int pixel = -1;
        //如果是图片模式模式
        if (mMode == MODE_PIC) {
            //return;
            //获取当前预览的图片在颜色选择器中心点的颜色值
            Bitmap bitmap = mPicPreviewImg.getDrawingCache();
            if (bitmap == null) {
                return;
            }
            pixel = bitmap.getPixel(centerX, centerY);
            Log.e(TAG, "centerX = " + centerX + ", centerY = " + centerY);
        } else {
            if (mPreViewBm == null) {
                return;
            }
            //获取颜色选择器中心点的颜色值
            pixel = mPreViewBm.getPixel(centerX, centerY);
        }

        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        mPreviewSelectIv.setBackgroundColor(pixel);
        mRgbTv.setText("RGB: " + red + ", " + green + ", " + blue);
        mHexTv.setText("HEX: " + ColorUtils.rgbToHex(red, green, blue));
        if (mModel != null) {
            updateConcentration(red, green, blue);
        }

        //记录当前指示器位置
        //int pickerL = mPickerIcon.getLeft();
        //int pickerT = mPickerIcon.getTop();
        //int pickerR = mPickerIcon.getRight();
        //int pickerB = mPickerIcon.getBottom();
        //Log.e(TAG, "before: " + "pickerL = " + pickerL + ", pickerT = " + pickerT + ", pickerR = " + pickerR + ", pickerB = " + pickerB);


        //pickerL = mPickerIcon.getLeft();
        //pickerT = mPickerIcon.getTop();
        //pickerR = mPickerIcon.getRight();
        //pickerB = mPickerIcon.getBottom();
        //Log.e(TAG, "after: " + "pickerL = " + pickerL + ", pickerT = " + pickerT + ", pickerR = " + pickerR + ", pickerB = " + pickerB);
        //Log.e(TAG, "-----------------------------");

        //恢复指示器位置
        //mPickerIcon.layout(pickerL, pickerT, pickerR, pickerB);

    }

    private void updateConcentration(int red, int green, int blue) {
        //根据模型得到参数
        double[] weight = mModel.getWeights();
        double bias = mModel.getBias();
        //计算颜色值
        double concentration = weight[0] * red + weight[1] * green + weight[2] * blue + bias;
        //将浓度保留小数点后三位（四舍五入）
        concentration = (double) Math.round(concentration * 1000) / 1000;
        mConceTv.setText((mIsChinese ? "浓度: " : "Con" )+ concentration);
    }


    private final int HANDLE_UPDATE_COLOR = 1;
    private static final int HANDLE_UPDATE_BITMAP = 2;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case HANDLE_UPDATE_COLOR:
                    updateColorValue();
                    break;
                //case HANDLE_UPDATE_BITMAP:
                //    mCameraPreViewTmpIV.setImageBitmap(mPreViewBm);
                //    break;
            }
        }
    };


    /**
     * SurfaceHolder.Callback
     */
    private class CameraSurfaceHolderCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            Log.e(TAG, "surfaceCreated");


        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            Log.e(TAG, "surfaceChanged " + width + " " + height);
            startCameraPreview(width, height);


        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            Log.e(TAG, "surfaceDestroyed");
            closeCamera();
        }
    }

    private void startCameraPreview(int previewWidth, int previewHeight) {
        //获取相机
        mCamera = Camera.open();
        //设置相机参数
        Camera.Parameters parameters = mCamera.getParameters();
        //获取支持的预览尺寸
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        //因为接下来要对图片进行旋转90度，所以此处长宽调换
        Camera.Size bestSize = getProperPreviewSize(previewHeight, previewWidth, previewSizes);
        ////设置预览尺寸
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        //设置预览帧率
        List<int[]> fpsRange = parameters.getSupportedPreviewFpsRange();
        int[] bestFps = getProperFpsRange(fpsRange);
        parameters.setPreviewFpsRange(bestFps[0], bestFps[1]);
        //设置自动对焦
        if (parameters.getSupportedFocusModes().contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }

        parameters.set("orientation", "portrait");
        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示

        //设置相机参数
        mCamera.setParameters(parameters);
        //设置预览回调
        mCamera.setPreviewCallback(new HanldeRGBPreviewCallback());
        //设置预览显示
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //开启预览
        mCamera.startPreview();
    }

    private int[] getProperFpsRange(List<int[]> fpsRange) {
        //选择中间的帧率
        int[] bestFps = fpsRange.get(fpsRange.size() / 2);
        return bestFps;
    }

    /**
     * 获取相机支持的预览尺寸和需要的预览尺寸最接近的尺寸
     *
     * @param previewWidth  需要的预览尺寸宽度
     * @param previewHeight 需要的预览尺寸高度
     * @param supportSizes  相机支持的预览尺寸
     * @return
     */
    private Camera.Size getProperPreviewSize(int previewWidth, int previewHeight, List<Camera.Size> supportSizes) {
        Camera.Size bestSize = null;
        //获取需要的预览尺寸的比例
        float previewRatio = (float) previewWidth / previewHeight;
        //遍历相机支持的预览尺寸
        for (Camera.Size size : supportSizes) {
            //获取相机支持的预览尺寸的比例
            float supportRatio = (float) size.width / size.height;
            //如果相机支持的预览尺寸的比例和需要的预览尺寸的比例相等
            if (supportRatio == previewRatio) {
                //如果bestSize为空，或者相机支持的预览尺寸的宽度大于bestSize的宽度
                if ((bestSize == null || size.width > bestSize.width) && size.width <= 1500) {
                    bestSize = size;
                }
            }
        }
        //如果bestSize为空，说明相机支持的预览尺寸没有和需要的预览尺寸的比例相等的
        if (bestSize == null) {
            //遍历相机支持的预览尺寸
            for (Camera.Size size : supportSizes) {
                //如果bestSize为空，或者相机支持的预览尺寸的宽度大于bestSize的宽度
                if (bestSize == null || size.width > bestSize.width) {
                    bestSize = size;
                }
            }
        }
        return bestSize;
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    //public interface ColorGetter {
    //    /**
    //     * 获取指定坐标位置的颜色
    //     * @param x
    //     * @param y
    //     * @return
    //     */
    //    ColorValue getColor(int x, int y);
    //
    //    /**
    //     * 由使用者手动通知服务提供者刷新颜色
    //     * @param obj 可能需要的参数
    //     */
    //    void updateColor(Object obj);
    //}
}