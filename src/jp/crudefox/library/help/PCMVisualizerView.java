package jp.crudefox.library.help;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.View;

public class PCMVisualizerView extends View {
	private int mLen;
    private byte[] mBytes;
	private int mPointer = 0;
    private float[] mPoints;
    //private final Rect mRect = new Rect();
    private int mNoneColor = Color.rgb(255, 128, 0);
    private int mActiveColor = Color.rgb(0, 128, 255);
    
    private final Paint mForePaint;
    
//    private boolean mIsSurfaceCreated = false;
    private SurfaceHolder mSurfaceHolder;
    
    //private boolean mNeedDraw = false;
    
    private DrawThread mThread;

    public PCMVisualizerView(Context context) {
        super(context);
        
        float density = getResources().getDisplayMetrics().density;
        
        float linesize = (1.0f * density);
        
        mForePaint = new Paint();
        mForePaint.setStrokeWidth(linesize);
        mForePaint.setAntiAlias(true);
        //mForePaint.setColor(Color.rgb(0, 128, 255));
     
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

    public void postDraw(){
    	postInvalidate();
//    	DrawThread th = mThread;
//    	if(th!=null){
//    		th.requestDraw();
//    	}
    }
    public synchronized void initialize(int len){
    	if(len<=0) return ;
    	//mBytes = null;
    	clear(true);
    	mLen = len;
    	mPointer = 0;
    	postDraw();
    }
//    public synchronized void clear(){
//    	clear(true);
//    }
    public synchronized void clear(boolean invalidate){
    	mBytes = null;
    	mLen = 0;
    	mPointer = 0;
    	mPoints = null;
    	if(invalidate) postDraw();
    }
    
    public synchronized void updateVisualizer(short[] shortes,int offset,int size,boolean invalidate) {
    	if(shortes==null) return ;
    	if(mBytes==null){
    		mBytes = new byte[mLen];
    	}
    	
    	for(int i=0;i<size;i++){
    		mBytes[(mPointer+i)%mLen] = (byte)( shortes[offset+i]*Byte.MAX_VALUE/Short.MAX_VALUE );
    	}
    	mPointer = (mPointer+size)%mLen;
        
        //postInvalidate();
    	//drawVisulize();
    	if(invalidate) postDraw();
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
        
        long time = System.currentTimeMillis();
        
        final int len = mLen;
        final byte[] bytes = mBytes;
        
        final float width = getWidth();
        final float height = getHeight();

        if (bytes == null || len<=0) {
        	mForePaint.setColor(mNoneColor);
	        canvas.drawLine(0,height/2.0f, width, height/2.0f, mForePaint);
            return;
        }
        
        //if(len<0) len = mBytes.length;

        int points_buf_size = Math.min( len , 8000 ) * 4;
        if (mPoints == null || mPoints.length < points_buf_size ) {
            mPoints = new float[points_buf_size];
        }

        //mRect.set(0, 0, getWidth(), getHeight());

        mForePaint.setColor(mActiveColor);

        final int pointer = mPointer;//(mPointer%mLen);
        int points_buf_pointer = 0;
        for (int i = 0; i < len - 1; i++) {
        	final int index_d = points_buf_pointer;
        	final int index_s = (i+pointer) % (len);
            mPoints[index_d + 0] = width * i / (len - 1);
            mPoints[index_d + 1] = height / 2
                    - ((byte) (bytes[index_s])) * (height / 2) / Byte.MAX_VALUE;
            mPoints[index_d + 2] = width * (i + 1) / (len - 1);
            mPoints[index_d + 3] = height / 2
                    - ((byte) (bytes[(index_s+1)%len])) * (height / 2) / Byte.MAX_VALUE;
            points_buf_pointer += 4;
            if(points_buf_pointer == points_buf_size){
            	long time10 = System.currentTimeMillis();
    	        canvas.drawLines(mPoints, 0, points_buf_pointer, mForePaint);
    	        long time11 = System.currentTimeMillis();
    	        LibUtil.Log("drawLines "+(time11-time10)+"ms");
    	        points_buf_pointer = 0;
            }
        }
        if( points_buf_pointer > 0 ){
	        canvas.drawLines(mPoints, 0, points_buf_pointer, mForePaint);
	        points_buf_pointer = 0;
        }

        long end_time = System.currentTimeMillis();
        LibUtil.Log("visualizer draw pass time = "+(end_time-time)+" ms");
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
					PCMVisualizerView.this.drawVisulize();
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

