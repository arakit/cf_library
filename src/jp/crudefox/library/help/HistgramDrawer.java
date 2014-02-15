package jp.crudefox.library.help;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;


public class HistgramDrawer{
	
	
	private Paint mPaint;
	private Paint mPaint2;
	
	private int[] mDatas;
	private int mDataLen;
//    private float[] mPoints;
//    private Rect mRect = new Rect();
	private Path mPath;
    
    private int mWidth;
    private int mHeight;
    private int mMax;
    
    boolean mNeedUpdate = true;
    
    

	public HistgramDrawer() {
		init();
	}
	
	private void init(){
		mPaint = new Paint();		
		mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.argb(200, 0, 128, 255));
        
		mPaint2 = new Paint();		
		mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(1f);
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(Color.argb(255, 255,255,255));
        
        mNeedUpdate = true;
        
        mPath = new Path();
	}

    public void setData(int[] datas,int len) {
    	mDatas = datas;
    	mDataLen = len;
    	mNeedUpdate = true;
    }
    
    private boolean update(){
        int len = mDataLen;

        if (mDatas == null || mDatas.length==0 || len==0) {
            return false;
        }
        
        if(len<0) len = mDatas.length;

//        if (mPoints == null || mPoints.length < len * 4) {
//            mPoints = new float[len * 4];
//        }    	
        mPath.reset();

//        mRect.set(0, 0, getWidth(), getHeight());
        int width = mWidth;
        int height = mHeight;
        
        mPath.moveTo(0, height);
        for (int i = 0; i < len ; i++) {
        	float x = width * i / (len - 1);
        	float y = height - (mDatas[i]) * (height) / mMax;
        	mPath.lineTo(x, y);
//            mPoints[i * 4 + 0] = width * i / (len - 1);
//            mPoints[i * 4 + 1] = height
//                    - (mDatas[i]) * (height) / mMax;
//            mPoints[i * 4 + 2] = width * (i + 1) / (len - 1);
//            mPoints[i * 4 + 3] = height
//                    - (mDatas[i+1]) * (height) / mMax;
            
        }
//        mPath.lineTo(width, mDatas[len-1]);
        mPath.lineTo(width, height);
        mPath.close();
        
        mNeedUpdate = false;
        return true;
    }
    
    public void draw(Canvas canvas,int w,int h,int max){
    	if(w==0 || h==0 || max==0) return ;
    	boolean dif_size = w!=mWidth || h!=mHeight || mMax!=max;
    	mWidth = w;
    	mHeight = h;
    	mMax = max;
    	boolean need_update = dif_size || mNeedUpdate;
    	if(need_update){
    		update();
    	}
    	
    	if(mDatas==null || mPath==null) return ;
    		
        //canvas.drawLines(mPoints, mPaint);
    	canvas.drawPath(mPath, mPaint);
    	canvas.drawPath(mPath, mPaint2);
    }	
    
    public int getWidth(){
    	return mWidth;
    }
    public int getHeight(){
    	return mHeight;
    }


	
	
	

	

}
