package jp.crudefox.library.sound;


//import java.util.LinkedList;
import jp.crudefox.library.help.LibUtil;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

public class CFSoundRecorder {



	public enum CFAudioSource{
		Mic,
		Camcorder,
		VoiceCall,
		VoiceCommunication,
		VoiceDownLink,
		VoiceRecognition,
		VoiceUpLink,
	}

//	public static class Message{
//		int what;
//		int arg1;
//		int arg2;
//		Object obj1;
//		Object obj2;
//	}

	public interface OnRecordListener{
		public boolean onStartInThread(CFSoundRecorder rec);
		public void onStopInThread(CFSoundRecorder rec,boolean error);
		public boolean onReadInThread(CFSoundRecorder rec,short[] data,int offset,int size,long total_pointer);
		//public void onErrorInThread(CFSoundRecorder rec);
		//public void onMessageInThread(CFSoundRecorder rec,Message mes);
		public void onStateChanged(CFSoundRecorder rec,int state);
	}



    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAY = 1;

    private int mState = STATE_STOPPED;

    private RecordThread mThread;

    private short[] mBuf;

    private AudioRecord mAudioRecord;
    private int mSampleRate;


    private int mBufferSize;
    private int mAudioRecordBufferSize;
    private int mMinBufferSize;
    //private int mSwitchBufferCount;

    private boolean mIsCallRead = true;

    //private int mSeparateInSec = 5;


    private Object mUserObject;

    private long mRecordePointer = 0;


    private OnRecordListener mOnRecordListener;

    //private final LinkedList<Message> mMessageInThread =  new LinkedList<Message>();

    public static final boolean isSupportedSampleRateMono16bit(int sample_rate){
    	int channel = AudioFormat.CHANNEL_IN_MONO;
    	int encoding = AudioFormat.ENCODING_PCM_16BIT;

        int minsize = AudioRecord.getMinBufferSize(
        		sample_rate,
        		channel,
        		encoding) / 2;
        return minsize>0;
    }

    public CFSoundRecorder(CFAudioSource source,int sample_rate){

    	int audio_source;

    	switch(source){
    	case Mic: 					audio_source = AudioSource.MIC; break;
    	case Camcorder: 			audio_source = AudioSource.CAMCORDER; break;
    	case VoiceCall: 			audio_source = AudioSource.VOICE_CALL; break;
    	case VoiceCommunication:	audio_source = AudioSource.VOICE_COMMUNICATION; break;
    	case VoiceDownLink:			audio_source = AudioSource.VOICE_DOWNLINK; break;
    	case VoiceRecognition: 		audio_source = AudioSource.VOICE_RECOGNITION; break;
    	case VoiceUpLink: 			audio_source = AudioSource.VOICE_UPLINK; break;
    	default: throw new IllegalArgumentException("can not use that audio source.");
    	}


//        mAudioRecordSampleRate = 8000*2;
//        mAudioRecordBufSize = mAudioRecordSampleRate/2;

    	int channel = AudioFormat.CHANNEL_IN_MONO;
    	int encoding = AudioFormat.ENCODING_PCM_16BIT;
    	//int streamType = AudioManager.STREAM_MUSIC;

    	//int sample_rate = 8000;//AudioTrack.getNativeOutputSampleRate(streamType);

    	//mSwitchBufferCount = 3;
    	//int audioRecBufSwitch = 3;
    	//int bufSwitch = 2;

        mSampleRate=sample_rate;
        mMinBufferSize=AudioRecord.getMinBufferSize(
        		sample_rate,
        		channel,
        		encoding) / 2;

        if(mMinBufferSize<=0) throw new IllegalArgumentException();

        mBufferSize = mMinBufferSize;//*bufSwitch;
        //0.25秒分か最小の３つ分
        mAudioRecordBufferSize = Math.max( mMinBufferSize*3, mSampleRate/4 );

		mAudioRecord = new AudioRecord(
				audio_source,
				mSampleRate,
				channel,
				encoding,
				mAudioRecordBufferSize*2 );

    }

    public void setUserObject(Object obj){
    	mUserObject = obj;
    }
    public Object getUserObject(){
    	return mUserObject;
    }

    public int getSampleRate(){
    	return mSampleRate;
    }

    public long getRecordPointer(){
    	return mRecordePointer;
    }

//    public void setSeparateInSec(int s){
//    	mSeparateInSec = s;
//    }

    public void setCallReadEnabled(boolean enable){
    	mIsCallRead = enable;
    }
//    public boolean offerMessageInThread(Message mes){
//    	return mMessageInThread.offer(mes);
//    }
//    public Message pollMessageInThread(){
//    	return mMessageInThread.poll();
//    }
//    private void procMessage(){
//    	for(Message mes : mMessageInThread){
//    		OnRecordListener lis = mOnRecordListener;
//    		if(lis!=null){
//    			lis.onMessageInThread(CFSoundRecorder.this, mes);
//    		}
//    	}
//    }
//    public void clearMessageInThread(){
//    	mMessageInThread.clear();
//    }



//    public synchronized void setRawOutputStream(OutputStream os){
//    	mRawOutStream = os;
//    }

    public void setOnRecordListener(OnRecordListener lis){
    	mOnRecordListener = lis;
    }

    private void setState(int state){
    	if(mState==state) return ;
    	OnRecordListener lis = mOnRecordListener;
    	mState = state;
    	if(lis!=null) lis.onStateChanged(this, state);
    }

    public boolean isStarted(){
    	return mState==STATE_PLAY;
    }

    public synchronized void start(){
    	if(mState!=STATE_STOPPED) return ;

		if(mBuf==null || mBuf.length!=mBufferSize) mBuf = new short[mBufferSize];

		mRecordePointer = 0;

    	RecordThread th = mThread = new RecordThread();
    	th.start();

    	setState(STATE_PLAY);

    }
    public synchronized void stop(){
    	if(mState!=STATE_PLAY) return ;
    	stopThread();
    	mBuf = null;
    	setState(STATE_STOPPED);
    }


    public void release(){
    	stop();
    	if(mAudioRecord!=null){
    		mAudioRecord.release();
    		mAudioRecord = null;
    	}
    }

    private void stopThread(){
    	final RecordThread th = mThread;
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

    private class RecordThread extends Thread{

    	boolean mmIsCanceled;

//    	boolean mIsFinished;
//    	public boolean isFinished(){
//    		return mIsFinished;
//    	}

		@Override
		public void run() {
			super.run();

			boolean error=false;

			{
				OnRecordListener lis = mOnRecordListener;
				if(lis!=null){
					boolean suc = lis.onStartInThread(CFSoundRecorder.this);
					if(!suc){ mmIsCanceled = true; error = true; }
				}
			}

			try{
				mAudioRecord.startRecording();
			}catch (Exception e) {
				e.printStackTrace();
				mmIsCanceled = true; error = true;
			}

			final int pointer=0;
			//long total_pointer=0;

			loop1:
			while(!mmIsCanceled){
				int read = Math.min( mMinBufferSize, mBuf.length-pointer);
				read = mAudioRecord.read(mBuf, pointer, read);

				if(read>=0){
					OnRecordListener lis = mOnRecordListener;
					if(lis!=null){
						if(mIsCallRead){
							boolean suc = lis.onReadInThread(CFSoundRecorder.this, mBuf, pointer, read, mRecordePointer);
							if(!suc){
								error = true;
								break loop1;
							}
						}
					}

					//pointer = (pointer + read) % mBuf.length;
					mRecordePointer += read;
				}else{
//					OnRecordListener lis = mOnRecordListener;
//					if(lis!=null){
//						lis.onErrorInThread(CFSoundRecorder.this);
//					}
					error = true;
					break loop1;
				}

//				try {
//					sleep(1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					AppUtil.Log("InterruptedException");
//				}
			}

			try {
				mAudioRecord.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}


			{
				OnRecordListener lis = mOnRecordListener;
				if(lis!=null){
					lis.onStopInThread(CFSoundRecorder.this, error);
				}
			}
			//mIsFinished = true;

			LibUtil.Log("Thread is finish.");
		}

		public void cancel(){
			if(mmIsCanceled) return ;
			mmIsCanceled = true;
			RecordThread.this.interrupt();
		}
    }


}
