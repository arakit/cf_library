package jp.crudefox.library.sound;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import jp.crudefox.library.help.Helper;
import jp.crudefox.library.help.LibUtil;
import jp.crudefox.library.help.WaveUtil;

public class WavRecorderClient implements CFSoundRecorder.OnRecordListener{
	
	

	public interface OnRecordListener{
		//public int onRead();
		
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
	
    private File mRawOutputStreamFile;
    private DataOutputStream mRawOutputStream = null;
    private int mHeaderBytes;
    //private int mDataBytes;
    
    public void setVolumePercent(int val){
    	mVolumePercent = val;
    }
    public int getVolumePercent(){
    	return mVolumePercent;
    }
    
    private boolean writeRaw(short[] data,int offset,int size){
    	if(mRawOutputStream==null) return false;
		try {
			byte[] bt = new byte[2];
	    	for(int i=0;i<size;i++){
				//mRawOutputStream.writeShort(data[i+offset]);
	    		WaveUtil.shortToBytes(data[offset+i], bt, 0);
				mRawOutputStream.write(bt,0,2);
	    	}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
    }
    private boolean openRawWriteFile(File file){
    	if(mRawOutputStream!=null) return false;
		try {
			mRawOutputStream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream(file) ) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		mRawOutputStreamFile = file;
		return true;
    }
    private boolean closeRawWrite(){
    	if(mRawOutputStream==null) return false;
    	try {
			mRawOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
	    	mRawOutputStreamFile = null;
	    	mRawOutputStream = null;
			return false;
		}
    	mRawOutputStream = null;
    	mRawOutputStreamFile = null;
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
		mHeaderBytes = 0;
		//mDataBytes = 0;
		File file = mRawOutputStreamFile = (File) rec.getUserObject();
		if(openRawWriteFile(file)){
			OutputStream out = mRawOutputStream;
			byte[] header = WaveUtil.createMono16BitHeader( rec.getSampleRate(), 0);
			try { out.write(header); } catch (IOException e) { return false; }
			mHeaderBytes = header.length;
		}else{
			return false;
		}
		return true;
	}
	@Override
	public void onStopInThread(CFSoundRecorder rec, boolean error) {
		File file = mRawOutputStreamFile;
		closeRawWrite();
		if(!error){
			RandomAccessFile raf=null;
			try {
				raf = new RandomAccessFile(file, "rw");
			} catch (FileNotFoundException e) { e.printStackTrace(); }
			long datasize = file.length()-mHeaderBytes;
			if(datasize>Helper.LONG_4GB) datasize = Helper.LONG_4GB;
			byte[] header = WaveUtil.createMono16BitHeader( rec.getSampleRate(), (int)(datasize) );
			try {
				raf.seek(0);
				raf.write(header);
			} catch (IOException e1) {}
			try { raf.close(); } catch (IOException e) { }
		}
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
