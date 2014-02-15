package jp.crudefox.library.sound;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import jp.crudefox.library.help.Helper;
import jp.crudefox.library.help.WaveUtil;

public class WavPlayerClient implements CFSoundPlayer.OnPlayListener{

	public interface OnPlayListener{
		public void onUpdateVisualizer(short[] data,int offset,int size, long pointer);
		public void onStart();
		public void onStop(boolean error);
		public void onReachedEnd();
		//public void onError();
		public void onStateChanged(int state);
	}
	private OnPlayListener mOnClientListener;
	public void setOnClientListener(OnPlayListener lis){
		mOnClientListener = lis;
	}


	public enum PlayEffect{
		Normal,
		Reverse,
	}

	public static class PlayUserObject{
		public File file;
		public int dataStartPointerBytes;
		public int dataSizeBytes;
		public PlayEffect playEffect;
	}

	private int mVolumePercent=100;

    public void setVolumePercent(int val){
    	mVolumePercent = val;
    }
    public int getVolumePercent(){
    	return mVolumePercent;
    }

	byte[] mmBuf;
	private byte[] getBytesBuf(int length){
		if(mmBuf==null || mmBuf.length<length) mmBuf = new byte[length];
		return mmBuf;
	}

    private RandomAccessFile mmOpenRandomFile;
    private PlayUserObject mmPlayUserObject;

    public int readRaw(long filePointer,byte[] data,int offset,int size){
    	if(mmOpenRandomFile==null) return -1;
		int num = 0;
		try {
//		    	for(int i=0;i<size;i++){
//					data[i+offset] = mOpenRandomFile.readByte();
//					num++;
//		    	}
			if(filePointer>=mmOpenRandomFile.length() || filePointer<0) return 0;
			if(filePointer!=mmOpenRandomFile.getFilePointer()){
				mmOpenRandomFile.seek(filePointer);
			}
			num = mmOpenRandomFile.read(data, offset, size);
		}
		catch (EOFException e) { return num; }
		catch (IOException e) {	e.printStackTrace(); return -1; }
		return num;
    }
    public boolean openRandomFile(File file){
    	if(mmOpenRandomFile!=null) return false;
    	try { mmOpenRandomFile = new RandomAccessFile(file, "r"); }
    	catch (FileNotFoundException e) { return false; }
    	return true;
    }

    public boolean closeRandomFile(){
    	if(mmOpenRandomFile==null) return false;
    	try { mmOpenRandomFile.close(); }
    	catch (IOException e) { e.printStackTrace(); mmOpenRandomFile = null; return false; }
		mmOpenRandomFile = null;
    	return true;
    }

	@Override
	public void onWroteInThread(CFSoundPlayer p, short[] buf, int offset,int size,long pointer) {

	}
	@Override
	public int onWriteInThread(CFSoundPlayer p, short[] buf, int offset,int size,long pointer) {
		byte[] byteBuf = getBytesBuf(size*2);
		int readBytes;

		boolean reverse = false;
		if( mmPlayUserObject.playEffect != PlayEffect.Reverse ){
			readBytes = size*2;
			if(readBytes+pointer*2>mmPlayUserObject.dataSizeBytes) readBytes = (int)( mmPlayUserObject.dataSizeBytes - (pointer*2) );
			if(readBytes<0) readBytes = 0;
			readBytes = readRaw( pointer*2 + mmPlayUserObject.dataStartPointerBytes, byteBuf, 0, readBytes );
		}else{
			long fp;
			if(mmPlayUserObject.dataSizeBytes-pointer*2<0){
				fp = mmPlayUserObject.dataStartPointerBytes; size = (int)(-(mmPlayUserObject.dataSizeBytes-pointer*2)/2);
			}else{
				fp = (mmPlayUserObject.dataSizeBytes-pointer*2) + mmPlayUserObject.dataStartPointerBytes - size*2;
			}
			reverse = true;
			readBytes = readRaw(fp , byteBuf, 0, size*2 );
		}

		if(readBytes<=0) return readBytes; //EOFもしくはエラー
		int read=readBytes/2;
		if(!reverse){
			for(int i=0;i<read;i++) buf[i+offset] = WaveUtil.bytesToShort(byteBuf, i*2);
		}else{
			for(int i=0;i<read;i++) buf[i+offset] = WaveUtil.bytesToShort(byteBuf, (read-i-1)*2);
		}

		if(mVolumePercent!=100){
			for(int i=0;i<read;i++){
				int val = buf[i+offset] * mVolumePercent / 100;
				buf[i+offset] = (short)Helper.clamp(val, Short.MIN_VALUE, Short.MAX_VALUE);
			}
		}

		//if(read==0) p.stop();
		return read;
	}
	@Override
	public void onStopPlayInThread(CFSoundPlayer p, boolean error) {
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				stopPlay();
//			}
//		});
		closeRandomFile();
		mmPlayUserObject = null;
		mmBuf = null;
	}
	@Override
	public boolean onStartPlayInThread(CFSoundPlayer p) {
		mmPlayUserObject = (PlayUserObject) p.getUsetObject();
		File file = mmPlayUserObject.file;
		if(openRandomFile(file)){
			try {
				mmOpenRandomFile.seek( mmPlayUserObject.dataStartPointerBytes );
			} catch (IOException e) { return false; }
		}else{
			return false;
		}
		return true;
	}
//		@Override
//		public void onErrorInThread(CFSoundPlayer p) {
//			postErrorPlay();
//		}
	@Override
	public void onPlayedInThread(CFSoundPlayer p, short[] buf, int offset,int size, long pointer) {
//		long ep = pointer+size;
//		final int prog = ep>Integer.MAX_VALUE ? (Integer.MAX_VALUE) : ((int)ep);
//		mHandler.post(new Runnable() {
//			@Override
//			public void run() {
//				for(OnFragmentListener e : mFragmentListeners){
//					e.onUpdatePlaySeekProgress(prog);
//				}
//				updateSeek();
//			}
//		});
//		updateVisualizer(buf, offset, size);
		if(mOnClientListener!=null) mOnClientListener.onUpdateVisualizer(buf, offset, size, pointer);
	}
	@Override
	public void onStateChanged(CFSoundPlayer p, int state) {
		OnPlayListener lis = mOnClientListener;
		if( lis!=null ){
			lis.onStateChanged(state);
		}
	}
	@Override
	public void onReachedEndInThread(CFSoundPlayer p) {
		// TODO 自動生成されたメソッド・スタブ
		OnPlayListener lis = mOnClientListener;
		if( lis!=null ){
			lis.onReachedEnd();
		}
	}

}
