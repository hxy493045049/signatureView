package com.demo.shawn.signature;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by shawn on 2017/9/14 0014.
 * <p>
 * description :
 */

public class SignatureView extends View {
    private static final String TAG = SignatureView.class.getSimpleName();
    private Paint mPathPaint;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private Path mPath;
    private Bitmap mBitmap;
    /**
     * 画图区域的最大位置
     */
    private float mSmallX = 0, mSmallY = 0, mBigX = 0, mBigY = 0;

    private float mPreX, mPreY;

    public SignatureView(Context context) {
        this(context, null);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resetSign();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPathPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreX = event.getX();
                mPreY = event.getY();
                computeDrawMaxRang(mPreX, mPreY);
                mPath.reset();
                mPath.moveTo(mPreX, mPreY);
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                computeDrawMaxRang(x, y);
                //绘制圆滑曲线（贝塞尔曲线）
                mPath.quadTo(mPreX, mPreY, x, y);
                mPreX = x;
                mPreY = y;
                break;
            case MotionEvent.ACTION_UP:
                computeDrawMaxRang(event.getX(), event.getY());
                mPath.lineTo(event.getX(), event.getY());
                mCanvas.drawPath(mPath, mPathPaint);
                break;
        }
        invalidate();
        return true;
        //                return super.onTouchEvent(event);
    }

    /**
     * 保存bitmap到文件
     *
     * @param fileapth
     * @return 成功返回true，反之false
     */
    public boolean SaveBitmapToFile(String fileapth) {
        float signAreaWidth = mBigX - mSmallX;
        float signAreaHeight = mBigY - mSmallY;
        if (signAreaWidth <= 0 || signAreaHeight <= 0) {
            return false;
        }
        File file = new File(fileapth + File.separator + "aaa.jpg");
        file.deleteOnExit();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            Bitmap bitmap = Bitmap.createBitmap(mBitmap, (int) mSmallX, (int) mSmallY,
                    (int) signAreaWidth, (int) signAreaHeight, new Matrix(), true);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else
            return false;
    }

    public void reset() {
        resetSign();
        invalidate();
    }


    // ----------------------------------   private   ----------------------------------

    /**
     * 计算画图区域的位置, 统计出最大和最小的值,用于后期限定矩形范围
     *
     * @param x 最新的X坐标
     * @param y 最新的Y坐标
     */
    private void computeDrawMaxRang(float x, float y) {
        if (mSmallX == 0 && mSmallY == 0 && mBigX == 0 && mBigY == 0) {
            mSmallX = mBigX = x;
            mBigY = mSmallY = y;
        } else {
            mSmallX = Math.min(mSmallX, x);
            if (mSmallX < 0) {
                mSmallX = 0;
            }
            mSmallY = Math.min(mSmallY, y);
            if (mSmallY < 0) {
                mSmallY = 0;
            }
            mBigX = Math.max(mBigX, x);
            if (mBigX > getMeasuredWidth()) {

            }
            mBigY = Math.max(mBigY, y);
            if (mBigY > getMeasuredHeight()) {
                mBigY = getMeasuredHeight();
            }
        }
    }

    /**
     * 重置path,canvas,path
     */
    private void resetSign() {
        mSmallX = 0;
        mSmallY = 0;
        mBigX = 0;
        mBigY = 0;

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        } else {
            mBitmap.eraseColor(Color.WHITE);
        }

        if (mPath == null) {
            mPath = new Path();
        } else {
            mPath.reset();
        }
    }


    private void init() {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);//设置防抖动,能让不同颜色的区块界限模糊

        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
        mPathPaint.setDither(true);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);//线段结束处的形状
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);//线段开始结束处的形状


        // TODO: 2017/9/14 0014   can be advanced
        mPathPaint.setStrokeWidth(10);           //这里可以优化,让客户端来设置宽度
        mPathPaint.setColor(Color.parseColor("#ff3f4a57"));
    }
}
