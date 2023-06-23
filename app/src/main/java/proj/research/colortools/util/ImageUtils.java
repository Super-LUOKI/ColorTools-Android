package proj.research.colortools.util;

import android.graphics.Bitmap;

public class ImageUtils {
    private ImageUtils(){}

    /**
     * 向右旋转位图
     * @param bm 位图
     * @param degree 旋转度数
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bm, int degree){
        if(degree == 0){
            return bm;
        }
        int w = bm.getWidth();
        int h = bm.getHeight();
        // Setting post rotate to 90
        android.graphics.Matrix mtx = new android.graphics.Matrix();
        mtx.postRotate(degree);
        return Bitmap.createBitmap(bm, 0, 0, w, h, mtx, true);

    }
}
