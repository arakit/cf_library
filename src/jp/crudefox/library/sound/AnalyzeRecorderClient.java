package jp.crudefox.library.sound;


import jp.crudefox.library.help.LibUtil;
import jp.crudefox.library.help.Helper;



public class AnalyzeRecorderClient implements CFSoundRecorder.OnRecordListener{

	public interface OnRecordListener{
		public void onUpdateVisualizer(short[] data,int offset,int size);
		public void onStart();
		public void onStop(boolean error);
		//public void onError();
		public void onStateChanged(int state);
	}

	private OnRecordListener mOnClientListener;
	public void setOnClientListener(OnRecordListener lis){
		mOnClientListener = lis;
	}
	
	private int mVolumePercent=100;
	
   // private File mRawOutputStreamFile;
    //private DataOutputStream mRawOutputStream = null;
    //private int mHeaderBytes;
    //private int mDataBytes;
    
    public void setVolumePercent(int val){
    	mVolumePercent = val;
    }
    public int getVolumePercent(){
    	return mVolumePercent;
    }
    
    private boolean writeRaw(short[] data,int offset,int size){

		return true;
    }
    
	
	@Override
	public boolean onReadInThread(CFSoundRecorder rec,short[] data, int offset, int size,long total_pointer) {
		LibUtil.Log("read "+size);
		
		if(mVolumePercent!=100){
			for(int i=0;i<size;i++){
				int val = data[i+offset] * mVolumePercent / 100;
				data[i+offset] = (short)Helper.clamp(val, Short.MIN_VALUE, Short.MAX_VALUE);
			}
		}
		
		boolean ret = writeRaw(data, offset, size);//writeRaw(data, offset, size);
//		if(!ret){
//			//if(mOnClientListener!=null) mOnClientListener.onError();
//		}
		
		if(mOnClientListener!=null) mOnClientListener.onUpdateVisualizer(data, offset, size);
		return ret;
	}
//	@Override
//	public void onErrorInThread(CFSoundRecorder rec) {
//		AppUtil.Log("error");
//		if(mOnClientListener!=null) mOnClientListener.onError();
//	}
	@Override
	public boolean onStartInThread(CFSoundRecorder rec) {
		
		
		return true;
	}
	@Override
	public void onStopInThread(CFSoundRecorder rec, boolean error) {
		
		
	}
//	@Override
//	public void onMessageInThread(CFSoundRecorder rec, Message mes) {
//		
//	}
	@Override
	public void onStateChanged(CFSoundRecorder rec, int state) {
		OnRecordListener lis = mOnClientListener;
		if( lis!=null ){
			lis.onStateChanged(state);
		}
	}	

}
