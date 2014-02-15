package jp.crudefox.library.sound;


import jp.crudefox.library.help.LibUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;

public class CFSoundPlayer {

//	public static CFSoundPlayer create(File file){
//		//WaveUtil.lo
//		return null;
//	}


	public interface OnPlayListener{
		public int onWriteInThread(CFSoundPlayer p,short[] buf,int offset,int size,long pointer);
		public void onWroteInThread(CFSoundPlayer p,short[] buf,int offset,int size,long pointer);
		public void onPlayedInThread(CFSoundPlayer p,short[] buf,int offset,int size,long pointer);
		//public void onErrorInThread(CFSoundPlayer p);
		public void onReachedEndInThread(CFSoundPlayer p);
		public boolean onStartPlayInThread(CFSoundPlayer p);
		public void onStopPlayInThread(CFSoundPlayer p,boolean error);
		public void onStateChanged(CFSoundPlayer p,int state);
	}

    //AudioTrack
    final private AudioTrack mAudioTrack;
    //サンプルレート
    final private int mSampleRate;

    //バッファーサイズ
    private int mBufferSize;
    private int mAudioTrackBufferSize;
    private int mMinBufferSize;

    //private int mBufferSwitchingCount;

    private OnPlayListener mClientPlayListner;

    private Object mUserObject;


//    final private Handler mHandler;

//    private int mSeparateInSec = 5;

//    final private OnPlaybackPositionUpdateListener mOnPlaybackPositionUpdateListener = new OnPlaybackPositionUpdateListener() {
//		@Override
//		public void onPeriodicNotification(AudioTrack track) {
//			AppUtil.Log("onPeriodicNotification-------------------------------");
//		}
//		@Override
//		public void onMarkerReached(AudioTrack track) {
//			AppUtil.Log("onMarkerReached--------------------------------------");
//			stopThread();
//		}
//	};

    //コンストラクタ
    public CFSoundPlayer() {
    	this(AudioManager.STREAM_MUSIC);
    }

    //コンストラクタ
    public CFSoundPlayer(int streamType) {
    	//int out_sample_rate = AudioTrack.getNativeOutputSampleRate(streamType);
    	//super(handler, streamType, out_sample_rate);
    	this(streamType, AudioTrack.getNativeOutputSampleRate(streamType));
    }

    //コンストラクタ
    public CFSoundPlayer(int streamType,int sample_rate) {

//    	mHandler = handler;

    	int channel = AudioFormat.CHANNEL_OUT_MONO;
    	int encoding = AudioFormat.ENCODING_PCM_16BIT;
//    	int streamType = AudioManager.STREAM_MUSIC;

//    	int out_sample_rate = AudioTrack.getNativeOutputSampleRate(streamType);

    	//mBufferSwitchingCount = 2;
    	//int audioTrackSwitch = 2;
    	int bufSwitch = 1;

        mSampleRate=sample_rate;
        mMinBufferSize=AudioTrack.getMinBufferSize(
        		sample_rate,
        		channel,
        		encoding) / 2;
        LibUtil.Log("minBufferSize="+mMinBufferSize);

        if(mMinBufferSize<=0) throw new IllegalArgumentException();

        mBufferSize = mMinBufferSize*bufSwitch;
        //最小の２倍か0.2秒分
        mAudioTrackBufferSize = Math.max( mMinBufferSize * 2, mSampleRate/5 );

        mPlaybackRate = mSampleRate;

        //AudioTrackオブジェクトの作成
        mAudioTrack = new AudioTrack(
                streamType,                			//streamType
                mSampleRate,          				//サンプリング周波数
                channel,    						//モノラルとか
                encoding,				            //audioFormat
                mAudioTrackBufferSize*2,						//バッファサイズ（バイト単位）
                AudioTrack.MODE_STREAM);            //static or stream

        //mAudioTrack.setPlaybackPositionUpdateListener(mOnPlaybackPositionUpdateListener);

    }

//    public static CFSoundPlayer create(File file){
//    	FileInputStream
//    }

//    public void setSeparateInSec(int s){
//    	mSeparateInSec = s;
//    }

    public int getSampleRate(){
    	return mSampleRate;
    }

    public void setOnPlayListner(OnPlayListener lis){
    	mClientPlayListner = lis;
    }


    public void release(){
    	stop();
    	mAudioTrack.release();
    }


    public void setUserObject(Object obj){
    	mUserObject = obj;
    }
    public Object getUsetObject(){
    	return mUserObject;
    }







    public final int PLAYSTATE_STOPED = 0;
    public final int PLAYSTATE_PLAYING = 1;
    public final int PLAYSTATE_PAUSE = 2;

    //private int mTime;
    private long mPlayPointer;
    private long mChangePlayPointer = -1;
    //private boolean mIsChangePlayPointer;

    private int mPlaybackRate;

    PlayThread mThread;
    private int mPlayState = PLAYSTATE_STOPED;

    //final private short[][] mBuf = new short[2][];
    private short[] mBuf;

    public boolean isPlaying(){
    	return mPlayState==PLAYSTATE_PLAYING;
    }
    public boolean isPause(){
    	return mPlayState==PLAYSTATE_PAUSE;
    }
    public boolean isStopped(){
    	return mPlayState==PLAYSTATE_STOPED;
    }

    private void setState(int state){
    	if(mPlayState==state) return ;
    	mPlayState = state;
    	OnPlayListener lis = mClientPlayListner;
    	if(lis!=null) lis.onStateChanged(this, mPlayState);
    }

    public synchronized void ready(){
    	stop();
//    	mTime = time;
//    	for(int i=0;i<mBuf.length;i++){
//    		if(mBuf[i]==null) mBuf[i] = new short[mSampleRate];
//    	}
    	mPlayPointer = 0;
    	mChangePlayPointer = -1;
    	mPlaybackRate = mSampleRate;
    	if(mBuf==null || mBuf.length!=mBufferSize) mBuf = new short[mBufferSize];
    }

    public synchronized void play(){
    	if(mPlayState!=PLAYSTATE_STOPED && mPlayState!=PLAYSTATE_PAUSE) return ;
    	if(mPlayState==PLAYSTATE_STOPED){
    		setState(PLAYSTATE_PLAYING);
	    	PlayThread th = mThread = new PlayThread();
	    	th.start();
    	}else if(mPlayState==PLAYSTATE_PAUSE){
    		setState(PLAYSTATE_PLAYING);
    		PlayThread th = mThread;
    		th.interrupt();
    	}
    }

    public synchronized void stop(){
    	if(mPlayState==PLAYSTATE_STOPED) return ;
    	stopThread();
    	//endPlay();
    	mBuf = null;
    	setState(PLAYSTATE_STOPED);
    }

    public synchronized void pause(){
    	if(mPlayState!=PLAYSTATE_PLAYING) return ;
    	setState( PLAYSTATE_PAUSE );
    }

    public void seekTo(long pointer){
    	if(pointer<0) return ;
    	mChangePlayPointer = pointer;
    	//mPlayPointer = pointer;
    	//mIsChangePlayPointer = true;
    }


    public void setPlaybackRate(int rate){
    	if(rate>mSampleRate*2 || rate<1) return ;
    	mPlaybackRate = rate;
    }
    public int getPlaybackRate(){
    	return mPlaybackRate;
    }
//    private synchronized void _setPlayPointer(long pointer){
//    	mPlayPointer = pointer;
//    }
    public long getPlayPointer(){
    	return mPlayPointer;
    }

    private void stopThread(){
    	final PlayThread th = mThread;
    	if(th!=null){
    		mThread = null;
    		th.cancel();
    		//if(th.isAlive()){
    			try { th.join(); }
    			catch (InterruptedException e) { e.printStackTrace(); }
    		//}
    	}
    }




    private class PlayThread extends Thread{

    	boolean mmIsCanceled;

	    final private OnPlaybackPositionUpdateListener mmOnPlaybackPositionUpdateListener = new OnPlaybackPositionUpdateListener() {
			@Override
			public void onPeriodicNotification(AudioTrack track) {
				LibUtil.Log("onPeriodicNotification-------------------------------");
			}
			@Override
			public void onMarkerReached(AudioTrack track) {
				LibUtil.Log("onMarkerReached--------------------------------------");
				PlayThread.this.interrupt();
			}
		};

		private boolean consumeChangePointer(){
			if(mChangePlayPointer>=0){
				mPlayPointer = mChangePlayPointer;
				mChangePlayPointer = -1;
				return true;
			}
			return false;
		}

		@Override
		public void run() {
			super.run();

			//int pointer = 0;

			OnPlayListener lis;
			boolean error = false;

			lis = mClientPlayListner;
			if(lis!=null){
				boolean suc = lis.onStartPlayInThread(CFSoundPlayer.this);
				if(!suc){ mmIsCanceled = true; error = true; }
			}


			if(mAudioTrack.getPlayState()!=AudioTrack.PLAYSTATE_PLAYING){
				///AppUtility.Log(""+total+" "+(total*mSampleRate/MNote.MEA_TIME));
				//mAudioTrack.setPlaybackPositionUpdateListener(mOnPlaybackPositionUpdateListener);
				//mAudioTrack.setNotificationMarkerPosition(mSampleRate);
				//int res1 = mAudioTrack.setNotificationMarkerPosition((total*mSampleRate/MNote.MEA_TIME)*(pb_rate));
				//int res2 = mAudioTrack.setPositionNotificationPeriod(total*mSampleRate/MNote.MEA_TIME);
				//AppUtility.Log("ret1="+res1+"; ret2="+res2);


				mAudioTrack.setPlaybackPositionUpdateListener(null);
				//mAudioTrack.setPositionNotificationPeriod(periodInFrames)
				mAudioTrack.play();
			}


			int playbackRate = mAudioTrack.getPlaybackRate();

//			long startmills = SystemClock.uptimeMillis();
//			int premills = 2000;
//			long wrote_size = 0;
			//long wrotemills = startmills;
			//int pre_size = mSampleRate/2;
			long wrote_pointer = 0;
			int buf_pointer = 0;

			loop1:
			while(!mmIsCanceled){
				//long nownow = SystemClock.uptimeMillis();

				if(mPlayState==PLAYSTATE_PAUSE){
					try { sleep(Integer.MAX_VALUE); } catch (InterruptedException e) {}
					continue loop1;
				}
				if(consumeChangePointer()) continue loop1; //Seekあったのでやり直し

				if(playbackRate!=mPlaybackRate){
					playbackRate = mPlaybackRate;
					mAudioTrack.setPlaybackRate(playbackRate);
				}
//				int ideal_write = Math.min( mMinBufferSize , mBufferSize - buf_pointer );
//				if(ideal_write<=0){ buf_pointer = 0; ideal_write=mMinBufferSize; }
				int ideal_write = mMinBufferSize;
				buf_pointer=0;

				long play_pointer = mPlayPointer;
				lis = mClientPlayListner;
				if(lis!=null){
					ideal_write = lis.onWriteInThread(CFSoundPlayer.this, mBuf, buf_pointer, ideal_write, play_pointer);
				}

				if(consumeChangePointer()) continue loop1; //Seekあったのでやり直し

				int wrote = ideal_write;
				if(ideal_write>0){
					wrote = mAudioTrack.write(mBuf, 0, ideal_write);
				}
				LibUtil.Log("wrote="+wrote);


				if(wrote==0){
					mAudioTrack.setPlaybackPositionUpdateListener(mmOnPlaybackPositionUpdateListener);
					mAudioTrack.setNotificationMarkerPosition((int)wrote_pointer);
					try {sleep(Long.MAX_VALUE);} catch (InterruptedException e) {}
					if(consumeChangePointer()) continue loop1;
					lis.onReachedEndInThread(CFSoundPlayer.this);
					if(consumeChangePointer()) continue loop1;
					break loop1; ///Seekしなかったので抜ける
				}
				else if(wrote>=0){//書き込み成功
					lis = mClientPlayListner;
					if(lis!=null){
						lis.onWroteInThread(CFSoundPlayer.this,mBuf, buf_pointer, wrote, play_pointer);
					}

					if(consumeChangePointer()) continue loop1;

					mPlayPointer = play_pointer + wrote; //Seekなかったので普通に進める
					wrote_pointer += wrote;
					//buf_pointer += wrote;

					lis = mClientPlayListner;
					if(lis!=null){
						lis.onPlayedInThread(CFSoundPlayer.this, mBuf, buf_pointer, wrote, play_pointer);
					}
//					long wrotemills = (wrote_size*1000/mSampleRate) + startmills;
//					long nowmills = SystemClock.uptimeMillis();
//					long smills = wrotemills - (nowmills+premills);
//					if(smills>0){
//						try {
//							AppUtil.Log("sleep "+smills);
//							sleep(smills);
//							AppUtil.Log("sleep end");
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//							AppUtil.Log("InterruptedException");
//						}
//					}
				}
				else{//エラー
					error = true;
//					lis = mClientPlayListner;
//					if(lis!=null) lis.onErrorInThread(CFSoundPlayer.this);
					break loop1;
				}

			}

			mAudioTrack.stop();

			lis = mClientPlayListner;
			if(lis!=null) lis.onStopPlayInThread(CFSoundPlayer.this,error);


			LibUtil.Log("Thread is finish.");
		}

		public void cancel(){
			if(mmIsCanceled) return ;
			mmIsCanceled = true;
			PlayThread.this.interrupt();
		}
    }














    //
//  public byte[] getSquareWave(double frequency) {
//      byte[] b = new byte[this.bufferSize];
////      double dt = 1.0 / 44100;
//
//      for (int i = 0; i < b.length; i++) {
////          double r = i / (this.sampleRate / frequency);
////          b[i] = (byte)((Math.round(r) % 2 == 0) ? 100 : -100);
//      	double t = (double)i / (double)sampleRate;
//      	double sum = Math.sin(2.0 * Math.PI * t * frequency);
////      		        + Math.sin(2.0 * Math.PI * t * freq_e3)
////      		        + Math.sin(2.0 * Math.PI * t * freq_g3);
////      	b[i] = (byte) (100 * (sum>0?+1.0:-1.0));
//      	b[i] = (byte) (100 * (sum));
//      }
//      return b;
//  }

//  public short[] makeWave(short[] buf,double a,double frequency) {
//      short[] b = buf;
//      if(b==null) b = new short[this.mBufferSize];
//
//      for (int i = 0; i < b.length; i++) {
//      	double t = (double)i / (double)mSampleRate;
//      	double sum = a * Math.sin(2.0 * Math.PI * t * frequency);
//      	if(sum>+1.0) sum = +1.0; else if(sum<-1.0) sum = -1.0;
//      	b[i] = (short) (sum * Short.MAX_VALUE);
//      }
//      return b;
//  }

//  public void write(short[] data,int offset,int size){
//  	mAudioTrack.write(data, offset, size);
//  }

//  public void play(){
//  	AppUtility.Log("play MSounder");
//  	mAudioTrack.play();
//  }
//  public void stop(){
//  	AppUtility.Log("stop MSounder");
//  	mAudioTrack.stop();
//  }
//  public void reloadStaticData(){
//  	mAudioTrack.reloadStaticData();
//  }
//  public void pause(){
//  	mAudioTrack.pause();
//  }
//  public void release(){
//  	mAudioTrack.release();
//  }
//  public int getPlayState(){
//  	return mAudioTrack.getPlayState();
//  }

//  public boolean isPlaying(){
//  	return mAudioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING;
//  }
//  public boolean isPaused(){
//  	return mAudioTrack.getPlayState()==AudioTrack.PLAYSTATE_PAUSED;
//  }
//  public boolean isStopped(){
//  	return mAudioTrack.getPlayState()==AudioTrack.PLAYSTATE_STOPPED;
//  }

//  //getter
//  public AudioTrack getAudioTrack() {
//      return mAudioTrack;
//  }


}
