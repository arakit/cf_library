package jp.crudefox.library.help;


import java.util.ArrayList;

import jp.crudefox.library.sound.Complex;
import jp.crudefox.library.sound.FFT2;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.text.TextPaint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class FFTVisualizerView extends View {
	
	private static class FFTItem{
		double amp;
		byte val;
		float hensa;
		
//		public void setByAmp(double amp){
//    		int v = (int)( FFT2.log(abs,mTei)*Byte.MAX_VALUE/max_amp_log);// + Byte.MIN_VALUE;
//    		v = Helper.clamp(v, Byte.MIN_VALUE, Byte.MAX_VALUE);			
//		}
	}
	
	
	private float mDensiy;
	
	private FFT2 mFFT;
	
	private int mSampleRate;
	private int mMaxZigen;
	//private int mAddNum;
	//private ArrayList<Float> mAnalyzeHzResults = new ArrayList<Float>();
	private float[] mAnalyzeHzResults;
	
	//private int mMinDisplayAmp=0;
	private int mMaxDisplayAmp=10000*10*10*5;
	
	//private byte[] mFFTData;
	private FFTItem[] mFFTDatas;
	
	private int mMinDisplayHz;
	private int mMaxDisplayHz;
	
	private float mPCMBufRate=1;
	private int mPCMLen;
    private short[] mPCMData;
	private int mPointer = 0;
    private float[] mPoints;
    //private final Rect mRect = new Rect();
    private int mNoneColor = Color.rgb(255, 128, 0);
    private int mActiveColor = Color.rgb(0, 128, 255);
    
    private float mTei = 1.5f;
    
    private final Paint mForePaint;
    private final Paint mKeisenPaint;
    private final Paint mKeisenHLPaint;
    
    private final TextPaint mTextPaint;
    
//    private boolean mIsSurfaceCreated = false;
    private SurfaceHolder mSurfaceHolder;
    
    //private boolean mNeedDraw = false;
    
    private DrawThread mThread;

    public FFTVisualizerView(Context context) {
        super(context);
        
        float density = mDensiy = getResources().getDisplayMetrics().density;
        
        float linesize = (1.0f * density);
        
        mFFT = new FFT2();
        
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

    public void postDraw(){
    	postInvalidate();
//    	DrawThread th = mThread;
//    	if(th!=null){
//    		th.requestDraw();
//    	}
    }
    public synchronized void initialize(int pcm_len,int sample_rate,int min_hz,int max_hz){
    	if(pcm_len<=0) return ;
    	//mBytes = null;
    	clear(true);
    	mPCMLen = (int) Helper.roundPowerOf(pcm_len, 2);
    	if(mPCMLen>mSampleRate) mPCMLen = (int) Helper.floorPowerOf(pcm_len, 2);
    	mSampleRate = sample_rate;
    	mMaxDisplayHz = max_hz;
    	mMinDisplayHz = min_hz;
    	mPointer = 0;
    	//mAddNum=0;
    	mPCMBufRate = (mPCMLen/(float)mSampleRate);
    	mMaxZigen = (int)( (mSampleRate/2) * (mPCMBufRate));
    	postDraw();
    }
//    public synchronized void clear(){
//    	clear(true);
//    }
    public synchronized void clear(boolean invalidate){
    	mPCMData = null;
    	mPCMLen = 0;
    	mPointer = 0;
    	mPoints = null;
    	mFFTDatas = null;
    	mMaxZigen = 0;
    	//mAddNum = 0;
    	if(invalidate) postDraw();
    }
    
    public synchronized void updateVisualizer(short[] shortes,int offset,int size,boolean invalidate) {
    	if(shortes==null) return ;
    	if(mPCMData==null){
    		mPCMData = new short[mPCMLen];
    	}
    	boolean upd_fft = false;
    	for(int i=0;i<size;i++){
    		//mPCMData[(mPointer+i)%mPCMLen] = (byte)( shortes[offset+i]*Byte.MAX_VALUE/Short.MAX_VALUE );
			mPCMData[mPointer] = shortes[offset+i];
			mPointer++;
			if(mPointer>=mPCMLen){
				updateFFT();
				mPointer=0;
				upd_fft = true;
			}
    	}        
        //postInvalidate();
    	//drawVisulize();
    	if(invalidate && upd_fft) postDraw();
    }
    
    public float[] getFreq(){
    	return mAnalyzeHzResults;
    }
    
    private void updateFFT(){
    	if(mFFTDatas==null || mFFTDatas.length!=mMaxZigen){
    		mFFTDatas = new FFTItem[mMaxZigen];
    		for(int i=0;i<mFFTDatas.length;i++){
    			mFFTDatas[i] = new FFTItem();
    		}
    	}
    	float max_amp_log = (float) FFT2.log(mMaxDisplayAmp, mTei);
    	//int pow_len = (int) Helper.findFloorPowerOf(mPCMLen, 2);
    	int hz = mFFT.calcSpectol(mPCMData, 0, mPCMLen);
    	Complex[] c = mFFT.getComplexs();
    	ArrayList<Integer> al = new ArrayList<Integer>();
    	al.clear();
    	
		//int thres_amp = 10000 * 10 * 5;
    	float thres_hensa = 250;
		double amp_sum = 0;
		double amp_ave = 0;
		double hyouzyun_hensa=0;
    	//ＦＦＴデータを変換
    	for(int i=0;i<mMaxZigen;i++){
    		FFTItem item = mFFTDatas[i];
    		if(i<c.length){
    			double amp = c[i].abs(); //振幅スペクトル
	    		int v = (int)( FFT2.log(amp,mTei)*Byte.MAX_VALUE/max_amp_log);// + Byte.MIN_VALUE;
	    		v = Helper.clamp(v, Byte.MIN_VALUE, Byte.MAX_VALUE);
	    		item.amp = amp;
	    		item.val = (byte)(v);
    		}else{
	    		item.amp = 0;
	    		item.val = 0; 
    		}
	        amp_sum += item.amp;
    	}
    	//標準偏差の計算
	    amp_ave = amp_sum / (double)(mMaxZigen);
    	for(int i=0;i<mMaxZigen;i++){
    		FFTItem item = mFFTDatas[i];
	        hyouzyun_hensa += (item.amp-amp_ave)*(item.amp-amp_ave);
    	}
    	hyouzyun_hensa = Math.sqrt( hyouzyun_hensa/mMaxZigen );
    	
		LibUtil.Log("標準偏差  = "+hyouzyun_hensa);
    	//偏差値の計算
    	for(int i=0;i<mMaxZigen;i++){
    		FFTItem item = mFFTDatas[i];
    		item.hensa = (float)( (item.amp-amp_ave) / hyouzyun_hensa * 10 + 50 );
	    	if( hyouzyun_hensa>500 && item.hensa > thres_hensa ){
	    		LibUtil.Log("偏差値"+i+" = "+item.hensa);
	    		Integer f_item = i;//(int)( i*(double)mSampleRate/(double)mPCMLen );
	    		al.add(f_item);
	    	}
    	}
    	
    	int ren_st_i = -1;
    	int ren_high_i = -1;
    	double ren_high_amp = 0;
    	ArrayList<Integer> rm_zi = new ArrayList<Integer>();
    	//山なりの隣接を削除
    	for(int i=0;i<al.size();i++){
    		int zi = al.get(i);
    		if(ren_st_i==-1){
    			ren_st_i = i; ren_high_i = i; ren_high_amp=mFFTDatas[zi].amp; 
    		}else if(i-ren_st_i>1 || i==al.size()-1){
    	    	for(int j=ren_st_i;j<=i;j++){
    	    		if(ren_high_i!=j) rm_zi.add(al.get(j));
    	    	}
    	    	ren_st_i = -1;
    	    	if(i==al.size()-1) { break; }
    	    	else{ i--; continue; }
    		}else{ 
    			double amp = mFFTDatas[zi].amp;
    			if(amp>ren_high_amp){
    				ren_high_amp = amp;
    				ren_high_i = i;
    			}
    		}
    	}
    	for(int i=0;i<rm_zi.size();i++){
    		al.remove(rm_zi.get(i));
    	}
    	
    	//配列へ元の周波数で
    	float[] res_hz = new float[al.size()];
    	for(int i=0;i<res_hz.length;i++){
    		int zi = al.get(i);
    		res_hz[i] = (float)( zi*(double)mSampleRate/(double)mPCMLen );
    	}
    	
    	mAnalyzeHzResults = res_hz;
    	//mAnaHz = (float)( hz*(double)mSampleRate/(double)mPCMLen );
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
        
        int[] freq_mem = new int[]{50,100,200,500,1000,2000,5000,10000,20000};
        int[] amp_mem = new int[]{0,10,100,1000,10000,100000,100000*10,100000*10*10};
    	float max_amp_log = (float) FFT2.log(mMaxDisplayAmp, mTei);
        
        //
        

        //ampの表示
        //int ym = (int)mMaxDisplayAmp;//(int) Math.pow(mTei, 20);
        for(int i=0;i<amp_mem.length;i++){
        	//float y = height - ty - (float) (FFT2.log(i, mTei)*c_height/max_amp_log) + 127*c_height/Byte.MAX_VALUE  - mDensiy*15;
        	int v = (int)( FFT2.log(amp_mem[i],mTei)*Byte.MAX_VALUE/max_amp_log);
        	float y = height - ty - v*c_height/Byte.MAX_VALUE;
        	String str = Helper.toAutoBigValueString(amp_mem[i], 1000);//String.format("%d", amp_mem[i]);
        	canvas.drawText(str, 0, y, mTextPaint);
        }
        

        float max_hz_log = (float)FFT2.log(mMaxDisplayHz, 2);
        float min_hz_log = (float) FFT2.log(mMinDisplayHz, 2);
        float sa_log = max_hz_log - min_hz_log;
        
        //周波数
        for(int i=0;i<freq_mem.length;i++){
        	int freq = freq_mem[i];
        	float x = tx + (float) (FFT2.log(freq, 2)-min_hz_log) * c_width / sa_log;
        	canvas.drawLine(x, ty, x, height-ty, mKeisenPaint);
        	String str = Helper.toAutoBigValueString(freq, 1000);//String.format("%d", freq);
        	canvas.drawText(str, x, height-ty/2, mTextPaint);
        }
        
        float[] anahz = mAnalyzeHzResults;
        if(anahz!=null && mPCMLen>0){
            float samp_pcmlen_rate = mSampleRate/(float)mPCMLen;
	        for(int i=0;i<anahz.length;i++){
	        	float hz = anahz[i];
	            if(hz>samp_pcmlen_rate){
	            	float x = tx + (float) (FFT2.log(hz, 2)-min_hz_log) * c_width / sa_log;
	            	canvas.drawLine(x, ty, x, height-ty, mKeisenHLPaint);
	            }
	        }
        }


        
        //波
        canvas.save();
        canvas.translate(tx, ty);
        drawGrapgh(canvas, c_width, c_height);
        canvas.restore();


    }
    
    private void drawGrapgh(Canvas canvas,float width,float height) {
    	
        long time = System.currentTimeMillis();
        
        final int len = mMaxZigen;
        final FFTItem[] bytes = mFFTDatas;

        if (bytes == null || len<=0) {
        	mForePaint.setColor(mNoneColor);
	        canvas.drawLine(0, height/2.0f, width, height/2.0f, mForePaint);
            return;
        }
        
        //if(len<0) len = mBytes.length;

        int points_buf_size = Math.min( len , 8000 ) * 4;
        if (mPoints == null || mPoints.length < points_buf_size ) {
            mPoints = new float[points_buf_size];
        }

        float max_hz_log = (float)FFT2.log(mMaxDisplayHz, 2);
        float min_hz_log = (float) FFT2.log(mMinDisplayHz, 2);
        float sa_log = max_hz_log - min_hz_log;
        //mRect.set(0, 0, getWidth(), getHeight());

        mForePaint.setColor(mActiveColor);

        //final int pointer = mPointer;//(mPointer%mLen);
        double pcm_i_rate = mSampleRate/(double)mPCMLen;
        int points_buf_pointer = 0;
        for (int i = 0; i < len - 1; i++) {
        	final int index_d = points_buf_pointer;
        	final int index_s = i;//(i+pointer) % (len);
            mPoints[index_d + 0] = (float)( (FFT2.log(i*pcm_i_rate, 2.0) - min_hz_log) * width / sa_log );
            //mPoints[index_d + 0] = width * i / (len - 1);
            mPoints[index_d + 1] = height 
                    - ((byte) (bytes[index_s].val)) * (height) / Byte.MAX_VALUE;
            //mPoints[index_d + 2] = width * (i + 1) / (len - 1);
            mPoints[index_d + 2] = (float)( (FFT2.log((i+1)*pcm_i_rate, 2.0) - min_hz_log ) * width / sa_log );
            mPoints[index_d + 3] = height 
                    - ((byte) (bytes[(index_s+1)%len]).val) * (height) / Byte.MAX_VALUE;
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
					FFTVisualizerView.this.drawVisulize();
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

