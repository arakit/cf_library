package jp.crudefox.library.help;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WaveUtil {


	
    public static final byte[] BYTE_RIFF = {'R', 'I', 'F', 'F'};
    public static final byte[] BYTE_WAVE = {'W', 'A', 'V', 'E'};
    public static final byte[] BYTE_FMT_ = {'f', 'm', 't', ' '};
    public static final byte[] BYTE_DATA = {'d', 'a', 't', 'a'};
    
    
	public static class WaveFileHeaderInfo{
	    public final byte[] byteRIFF = new byte[4]; //RIFF
	    public int riffChunkSize; //ファイルサイズ-8
	    public final byte[] byteWAVE = new byte[4]; //WAVE
	    public final byte[] byteFMT_ = new byte[4]; //FMT 
	    public int fmtChunkSize; // fmtチャンクのバイト数
	    public short fmtId ;//{0x01, 0x00};      // フォーマットID 1 =リニアPCM  
	    public short channel ;//{0x01, 0x00};      //  チャンネル 1 = モノラル
	    public int sampleRate;     // サンプルレート
	    public int bytesPerSec;    // バイト/秒 = サンプルレート x 1チャンネル x 2バイト
	    public short blockSize;     // ブロックサイズ2バイト 
	    public short bitPerSample;     // サンプルあたりのビット数16ビット WAV フォーマットでは 8bit か 16bit
	    public final byte[] byteDATA = new byte[4];//{'d', 'a', 't', 'a'};
	    public int dataSize;//intToBytes(datasize);         // データサイズ
	}

	public static byte[] createMono16BitHeader(int sampleRate, int datasize) {
		WaveFileHeaderInfo info = new WaveFileHeaderInfo();
		copyToAfromB( info.byteRIFF , BYTE_RIFF );
		info.riffChunkSize = datasize + 36;
		copyToAfromB( info.byteWAVE , BYTE_WAVE );
		copyToAfromB( info.byteFMT_ , BYTE_FMT_ );
		info.fmtChunkSize = 16;
		info.fmtId = 1;
		info.channel = 1;
		info.sampleRate = sampleRate;
		info.bytesPerSec = sampleRate*2;
		info.blockSize = 2;
		info.bitPerSample = 16;
		copyToAfromB( info.byteDATA , BYTE_DATA );
		info.dataSize = datasize;
		return createHeader(info);
	}
	
	// Wavファイルのヘッダを作成する（PCM16ビット モノラル）
	// sampleRate  サンプルレート
	// datasize データサイズ
	// これなんかもっとキレイに書けると思うが 。。 Ringroidのソースなんかキレイかも
	public static byte[] createHeader(WaveFileHeaderInfo info) {
		
//	    byte[] byteRIFF = {'R', 'I', 'F', 'F'};
//	    byte[] byteFilesizeSub8 = intToBytes((datasize + 36));  // ファイルサイズ-8バイト数
//	    byte[] byteWAVE = {'W', 'A', 'V', 'E'};
//	    byte[] byteFMT_ = {'f', 'm', 't', ' '};
//	    byte[] byteFmtChunkSize = intToBytes(16);    // fmtチャンクのバイト数
//	    //byte[] bytePcmMono = {0x01, 0x00, 0x01, 0x00};      // フォーマットID 1 =リニアPCM  ,  チャンネル 1 = モノラル
//	    byte[] byteFmtId = {0x01, 0x00};      // フォーマットID 1 =リニアPCM  
//	    byte[] byteChannel = {0x01, 0x00};      //  チャンネル 1 = モノラル
//	    byte[] byteSamplerate = intToBytes(sampleRate);     // サンプルレート
//	    byte[] byteBytesPerSec = intToBytes(sampleRate * 2);    // バイト/秒 = サンプルレート x 1チャンネル x 2バイト
//	    byte[] byteBlockSize = {0x02, 0x00};     // ブロックサイズ2バイト
//	    byte[] byteBitPerSample = {0x10, 0x00};     // サンプルあたりのビット数16ビット
//	    byte[] byteDATA = {'d', 'a', 't', 'a'};
//	    byte[] byteDatasize = intToBytes(datasize);         // データサイズ
	    
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	        out.write(info.byteRIFF);
	        out.write(intToBytes(info.riffChunkSize));
	        out.write(info.byteWAVE);
	        out.write(info.byteFMT_);
	        out.write(intToBytes(info.fmtChunkSize));
	        out.write(shortToBytes(info.fmtId));
	        out.write(shortToBytes(info.channel));
	        out.write(intToBytes(info.sampleRate));
	        out.write(intToBytes(info.bytesPerSec));
	        out.write(shortToBytes(info.blockSize));
	        out.write(shortToBytes(info.bitPerSample));
	        out.write(info.byteDATA);
	        out.write(intToBytes(info.dataSize));
	 
	    } catch (IOException e) {
	        return null;
	    }
	 
	    return out.toByteArray();
	}
	

	
	public static void copyToAfromB(byte[] destA,byte[] srcB){
		for(int i=0;i<srcB.length;i++){
			destA[i] = srcB[i];
		}
	}
	

	public static WaveFileHeaderInfo readHeader(byte[] data){
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		return readHeader(in);
	}
	public static WaveFileHeaderInfo readHeader(InputStream in){
		return readHeader(in, null);
	}
	public static WaveFileHeaderInfo readHeader(InputStream in,int[] readBytes){
		
		int read = 0;
		WaveFileHeaderInfo info = new WaveFileHeaderInfo();
		//ByteArrayInputStream in = new ByteArrayInputStream(data);		
	    try {
	    	readByteArray(in, info.byteRIFF); read+=4;
	    	info.riffChunkSize = readInt(in); read+=4;
	    	readByteArray(in, info.byteWAVE); read+=4;
	    	readByteArray(in, info.byteFMT_); read+=4;
	    	info.fmtChunkSize = readInt(in); read+=4;
	    	info.fmtId = readShort(in); read+=2;
	    	info.channel = readShort(in);  read+=2;
	    	info.sampleRate = readInt(in);  read+=4;
	    	info.bytesPerSec = readInt(in); read+=4;
	    	info.blockSize = readShort(in); read+=2;
	    	info.bitPerSample = readShort(in); read+=2;
	    	readByteArray(in, info.byteDATA);  read+=4;
	    	info.dataSize = readInt(in);  read+=4;
	 
	    } catch (IOException e) {
	        return null;
	    }
	    
	    if(readBytes!=null) readBytes[0]=read;
	    
	    return info;
	}
	

	public static void readByteArray(InputStream in,byte[] ba) throws IOException{
		readByteArray(in, ba, 0, ba.length);
	}
	public static void readByteArray(InputStream in,byte[] ba,int offset,int length) throws IOException{
		int read = in.read(ba,offset,length);
		if(read!=length) throw new EOFException();
	}
	public static int readInt(InputStream in) throws IOException{
		byte[] bt = new byte[4];
		int read = in.read(bt,0,4);
		if(read!=4) throw new EOFException();
		return bytesToInt(bt, 0);
	}
	public static short readShort(InputStream in) throws IOException{
		byte[] bt = new byte[2];
		int read = in.read(bt,0,2);
		if(read!=2) throw new EOFException();
		return bytesToShort(bt, 0);
	}
	
	
	// int型32ビットデータをリトルエンディアンのバイト配列にする
	public static byte[] intToBytes(int value) {
	    byte[] bt = new byte[4];
	    bt[0] = (byte)(value & 0x000000ff);
	    bt[1] = (byte)((value & 0x0000ff00) >> 8);
	    bt[2] = (byte)((value & 0x00ff0000) >> 16);
	    bt[3] = (byte)((value & 0xff000000) >> 24);
	    return bt;
	}
	// int型32ビットデータをリトルエンディアンのバイト配列にするの逆
	public static int bytesToInt(byte[] buf,int offset) {
		return (
	    ((buf[offset+0] & 0xff) ) |
	    ((buf[offset+1] & 0xff) << 8) | 
	    ((buf[offset+2] & 0xff) << 16) |
	    ((buf[offset+3] & 0xff) << 24)
	    );
	}
	
	
	// short型16ビットデータをリトルエンディアンのバイト配列にする
	public static byte[] shortToBytes(short value) {
		byte[] bt = new byte[2];
		shortToBytes(value, bt, 0);
		return bt;
	}
	// short型16ビットデータをリトルエンディアンのバイト配列にする
	public static void shortToBytes(short value,byte[] buf,int offset) {
	    buf[offset+0] = (byte)(value & 0x000000ff);
	    buf[offset+1] = (byte)((value & 0x0000ff00) >> 8);
	}
	// short型16ビットデータをリトルエンディアンのバイト配列にするの逆
	public static short bytesToShort(byte[] buf,int offset) {
		return (short)(
	    ((buf[offset+0] & 0xff) ) |
	    ((buf[offset+1] & 0xff) << 8)
	    );
	}
	
	
	
	
	
	
	
	// WAVヘッダをつけて保存
	// 各種例外処理略 チェック処理略
	public static boolean addWavHeader(File outFile,File inRawFile,int sampleRate) {
		
		if(!inRawFile.exists() || !inRawFile.isFile()) return false;
		
//	    // 録音したファイル
//	    File recFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.raw");
//	    // WAVファイル
//	    File wavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rec.wav");
	    // ストリーム
	    InputStream in = null;
	    OutputStream out = null;
		try {
			in = new BufferedInputStream( new FileInputStream(inRawFile) );
			out = new BufferedOutputStream( new FileOutputStream(outFile) );
		} catch (FileNotFoundException e) {
			if(in!=null) try { in.close(); } catch (IOException e1) {}
			//if(out!=null) try { out.close(); } catch (IOException e1) {}
			return false;
		}
	     
	    // ヘッダ作成  サンプルレート8kHz
	    byte[] header = createMono16BitHeader(sampleRate, (int)inRawFile.length());
	    // ヘッダの書き出し
	    try {
			out.write(header);
		} catch (IOException e) {
			e.printStackTrace();
			try { in.close(); } catch (IOException e1) {}
			try { out.close(); } catch (IOException e1) {}
			return false;
		}
	    header = null;
	 
	    // 録音したファイルのバイトデータ読み込み
	    int len = 0;
	    //int offset = 0;
	    byte[] buffer = new byte[8192];
	    try {
			while((len=in.read(buffer,0,buffer.length))>=0){
				out.write(buffer, 0, len);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			try { in.close(); } catch (IOException e2) {}
			try { out.close(); } catch (IOException e2) {}
			return false;
		}
	    
//	    while (offset < buffer.length
//	            && (n = in.read(buffer, offset, buffer.length - offset)) >= 0) {
//	        offset += n;
//	    }
//	    // バイトデータ書き込み
//	    out.write(buffer);
	     
	    // 終了
		try { in.close(); } catch (IOException e3) {}
		try { out.close(); } catch (IOException e2) { return false; }
		
		return true;
	}
	
	
	
	public static WaveFileHeaderInfo loadWaveInfo(File file,int[] readBytes){
		InputStream in;
		
		try {
			in = new BufferedInputStream( new FileInputStream(file) );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		WaveFileHeaderInfo info = readHeader(in,readBytes);
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return info;		
	}
	
	
	
	
	
	
//	public static class ToWaveTranslateThread extends Thread{		
//		private boolean mIsCanceled;
//		private File 
//		
//		@Override
//		public void run() {
//			super.run();
////			while(!mIsCanceled){
////				
////			}
//			
//		}
//		public void cancel(){
//			if(mIsCanceled) return ;
//			mIsCanceled = true;
//			ToWaveTranslateThread.this.interrupt();
//		}
//	}
	
}
