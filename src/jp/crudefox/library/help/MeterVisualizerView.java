package jp.crudefox.library.help;



import jp.crudefox.library.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.View;

public class MeterVisualizerView extends View {
	

	public static class MeterMemory{
		public String str;
		public float value;
	}
	
	private MeterMemory[] mMemories;

	
	
	private float mDensiy;

	private float mMaxValue;
	private float mMinValue;
	private float mValue;
	private float mMovingValue;

    
    private int mNoneColor = Color.rgb(255, 128, 0);
    private int mActiveColor = Color.rgb(0, 128, 255);
    
    private final Paint mForePaint;
    private final Paint mKeisenPaint;
    private final Paint mKeisenHLPaint;
    
    private final TextPaint mTextPaint;
    
    
    private Drawable mMeterDrawable;
    private Drawable mMeterHandDrawable;
    
//    private boolean mIsSurfaceCreated = false;
    private SurfaceHolder mSurfaceHolder;
    
    //private boolean mNeedDraw = false;
    
    private Handler mHandler = new Handler();
    
    private DrawThread mThread;

    public MeterVisualizerView(Context context) {
        super(context);
        
        float density = mDensiy = getResources().getDisplayMetrics().density;
        
        float linesize = (1.0f * density);
        
        Paint p;
        
        p = mForePaint = new Paint();
        mForePaint.setStrokeWidth(linesize);
        mForePaint.setAntiAlias(true);
        //mForePaint.setColor(Color.rgb(0, 128, 255));
        
        p = mKeisenPaint = new Paint();
        p.setStrokeWidth(linesize);
        p.setAntiAlias(true);
        p.setColor(Color.argb(255, 128, 128, 128));
        
        p = mKeisenHLPaint = new Paint();
        p.setStrokeWidth(linesize);
        p.setAntiAlias(true);
        p.setColor(Color.argb(255, 255, 128, 128));
        
        TextPaint tp;
        
        tp = mTextPaint = new TextPaint();
        tp.setAntiAlias(true);
        tp.setTextSize(mDensiy*10);
     
//        mSurfaceHolder = getHolder();
//        mSurfaceHolder.addCallback(VisualizerView.this);
//        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
//        setZOrderOnTop(true);
        
        setBackgroundColor(Color.TRANSPARENT);
        
    }
    
    public void setActiveWaveColor(int color){
    	mActiveColor = color;
    }
    public void setNoneWaveColor(int color){
    	mNoneColor = color;
    }
    
    public void setMeterMemory(MeterMemory[] mem){
    	mMemories = mem;
    }

    public void postDraw(long ms){
    	postInvalidateDelayed(ms);
    }
    public void postDraw(){
    	postInvalidate();
//    	DrawThread th = mThread;
//    	if(th!=null){
//    		th.requestDraw();
//    	}
    }
    public synchronized void initialize(float value,float min_value,float max_value){
    	if(max_value<=min_value) return ;
    	
    	clear(false);
    	
    	mMinValue = min_value;
    	mMaxValue = max_value;
    	mValue = value;
    	mMovingValue = min_value;

    	postDraw();
    }
//    public synchronized void clear(){
//    	clear(true);
//    }
    public synchronized void clear(boolean invalidate){
    	mMinValue = 0;
    	mMaxValue = 0;
    	mMovingValue = 0;
    	mValue = 0;
    	mMemories = null;
    	//mAddNum = 0;
    	if(invalidate) postDraw();
    }
    
    public synchronized void updateVisualizer(float value,boolean invalidate) {

    	boolean upd = mValue!=value || mValue!=mMovingValue;
    	
    	mValue = value;
    	
    	if(invalidate && upd) postDraw();
    }
    
    
    public synchronized void drawVisulize(){
    	//if( !mIsSurfaceCreated ) return ;
    	Canvas cv = mSurfaceHolder.lockCanvas();
    	if(cv==null) return ;
    	cv.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR ); 
    	onDrawVisualize(cv);
    	mSurfaceHolder.unlockCanvasAndPost(cv);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        onDrawVisualize(canvas);
    }
    


    protected synchronized void onDrawVisualize(Canvas canvas) {
        
        final float width = getWidth();
        final float height = getHeight();
        
        float tx = mDensiy*15;
        float ty = mDensiy*15;
        float c_width = width-tx*2;
        float c_height = height-ty*2;

        
        //波
        canvas.save();
        canvas.translate(tx, ty);
        drawMeter(canvas, (int)c_width, (int)c_height);
        canvas.restore();

        if(mValue!=mMovingValue){
        	float sa = mValue - mMovingValue;
//        	mMovingValue = mMovingValue + sa * 0.05f;
        	float abs = Math.abs(sa);
        	if(abs>0.01f){
        		//AppUtil.Log("low pass.");
            	mMovingValue = mMovingValue + sa * 0.75f;
        	}else{
        		//AppUtil.Log("not low pass.");
        		mMovingValue = mValue;
        	}
        	postDraw(100);
        }

    }

    
    private void drawMeter(Canvas canvas,int width,int height) {
    	
    	if(mMeterDrawable==null) mMeterDrawable = getResources().getDrawable(R.drawable.vumeter);
    	if(mMeterHandDrawable==null) mMeterHandDrawable = getResources().getDrawable(R.drawable.meter_hand);
    	
    	float cx = width / 2.0f;
    	//float cy = height / 2.0f;
    	float haba_degree = 160.0f;
    	
    	mMeterDrawable.setBounds(0,0,width,height);
    	mMeterDrawable.draw(canvas);
    	
    	final MeterMemory[] memory = mMemories;
    	if(memory!=null){
	    	for(int i = 0;i<memory.length;i++){
		    	float percent = (memory[i].value-mMinValue) / (mMaxValue-mMinValue);
		    	float degree = ((percent) * (haba_degree/360.0f))*360.0f - haba_degree/2.0f;
		    	float radian = (float) Helper.toMathRadianFromWatchRadian((float)Math.toRadians(degree));
		    	
	    		float x = (float)  Math.cos(radian) * width * 0.5f + cx;
	    		float y = (float) -Math.sin(radian) * height  + height;
	    		
	    		canvas.drawText(memory[i].str, x, y, mTextPaint);
	    	}
    	}

    	{
        	Drawable hand = mMeterHandDrawable;
        	int hand_width = hand.getIntrinsicWidth();
        	int hand_height = hand.getIntrinsicHeight();
        	//float asp = hand_height / (float) hand_width;
        	float scale = (height) / (float) (hand_height/2);
        	mMeterHandDrawable.setBounds(0, 0, hand_width, hand_height);
        	
	    	float percent = (mMovingValue-mMinValue) / (mMaxValue-mMinValue);
	    	float degree = ((percent) * (haba_degree/360.0f))*360.0f - haba_degree/2.0f;
	    	
	    	canvas.save();
	    	//canvas.scale(scale, scale, hand_width/2.0f, hand_height/2.0f);
	    	canvas.rotate(degree, width/2.0f, height);
	    	canvas.translate(width/2.0f-(hand_width*scale/2.0f), 0);
	    	canvas.scale(scale, scale);
	    	mMeterHandDrawable.draw(canvas);
	    	canvas.restore();
    	}

    	
    }

//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		//drawVisulize();
//	}
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		mIsSurfaceCreated = true;
//		//startThread();
//		//drawVisulize();
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		mIsSurfaceCreated = false;
//		//stopThread();
//	}

	private void startThread(){
		stopThread();
		DrawThread th = mThread = new DrawThread();
		th.start();
		th.requestDraw();
	}
	private void stopThread(){
		DrawThread th = mThread;
		if(th!=null){
			mThread = null;
			th.cancel();
			if(th.isAlive()){
				try {
					th.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class DrawThread extends Thread {
		
		boolean mmIsCanceld;
		boolean mmIsInvalidate;

		@Override
		public void run() {
			super.run();
			
			while(!mmIsCanceld){
				
				if(mmIsInvalidate){
					mmIsInvalidate = false;
					MeterVisualizerView.this.drawVisulize();
				}
				
				try {
					Thread.sleep(1000*10);
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}				
			}			
			
		}
		
		public void requestDraw(){
			mmIsInvalidate = true;
			DrawThread.this.interrupt();
		}
		
		public void cancel(){
			if(mmIsCanceld) return ;
			mmIsCanceld = true;
			DrawThread.this.interrupt();
		}
		
	}

}	

