package jp.crudefox.library.help;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

@SuppressLint("DefaultLocale")
public class MemoryUtil {
	
	private static Pattern PAT_MEMINFO = Pattern.compile("(\\S+):\\s*([0-9]+)\\s*(\\S*)");
	
	
	public static int toMBFromB(long b){
		return (int)( b/1024/1024 );
	}
	
	public static String toAutoString(long b){
		if(b<1000)	return String.format("%dB", b);
		if(b<1024*100)return String.format("%.1fKB", b/1024.0);
		long kb = b/1024;
		if(kb<1000) return String.format("%dKB", kb );
		if(kb<1024*100)return String.format("%.1fMB", kb/1024.0);
		long mb = kb/1024;
		if(mb<1000) return String.format("%dMB", mb );
		if(mb<1024*100)return String.format("%.1fGB", mb/1024.0);
		long gb = mb/1024;
		if(gb<1000) return String.format("%dGB", gb );
		if(gb<1024*100)return String.format("%.1fTB", gb/1024.0);
		long tb = gb/1024;
		return String.format("%dTB", tb );
	}
	public static String toAutoStringFromMB(long mb){
		if(mb<1000) return String.format("%dMB", mb );
		if(mb<1024*100)return String.format("%.1fGB", mb/1024.0);
		long gb = mb/1024;
		if(gb<1000) return String.format("%dGB", gb );
		if(gb<1024*100)return String.format("%.1fTB", gb/1024.0);
		long tb = gb/1024;
		return String.format("%dTB", tb );
	}	
	
    public static class MemoryInfo{
//    	public long memory_available = 0;
    	public int memory_MemTotal_mb;
//    	public int memory_MemFree_mb;
//    	public int memory_Inactive_mb;
    	
    	public int sd_available_mb = 0;
    	public int sd_total_mb = 0;
    	public int sd_free_mb = 0;
    	public int internal_available_mb = 0;
    	public int internal_total_mb = 0;
    	public int internal_free_mb = 0;
    	
    	ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    	
    	public boolean update(Context context){
    		setExternalMemory(this);
    		setInternalMemory(this);
    		setActivityManagerMemInfo(context, this);
    		if(memory_MemTotal_mb==0) setProcMemInfo(context,this);
    		return true;
    	}
    }

    public static void setActivityManagerMemInfo(Context context,MemoryInfo info){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		am.getMemoryInfo(info.mi);
    }
    
    public static boolean setProcMemInfo(Context context,MemoryInfo info){

//		info.memory_available = info.mi.availMem;

//		android.app.ActivityManager.MemoryInfo mi;m
		
		//am.getProcessMemoryInfo(null)[0].
		Runtime runtime = Runtime.getRuntime();		
		
		try {
			Process process = runtime.exec("cat /proc/meminfo");
	
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			
			String line;
	         while ((line = reader.readLine()) != null) {
	        	 //output += line + "\n";
	        	 //AppUtility.Log("line="+line);
	        	 Matcher mat = PAT_MEMINFO.matcher(line);
	        	 if(mat.find()){
	        		 String name = mat.group(1);
	        		 long val = Long.parseLong( mat.group(2) );
	        		 String unit = mat.group(3);
	        		LibUtil.Log(String.format("matches (%s) (%s) (%s)", name,val,unit)); 
	        		
	        		if("MemTotal".equals(name)){
	        			info.memory_MemTotal_mb = (int)( val / 1024 );
	        		}
//	        		else if("MemFree".equals(name)){
//	        			info.memory_MemFree_mb = (int)( val / 1024 );
//	        		}else if("Inactive".equals(name)){
//	        			info.memory_Inactive_mb = (int)( val / 1024 );
//	        		}
	        	 }
	         }
	         reader.close();
	         process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return true;
    }

	public static boolean setExternalMemory(MemoryInfo info){
		
		File externalMemPath = Environment.getExternalStorageDirectory();
//		boolean ch = false;
		long total,free,ava;
		if( externalMemPath != null && externalMemPath.exists() ){
			StatFs fs = new StatFs(externalMemPath.getPath());
			long blockSize = fs.getBlockSize();
			total = blockSize * fs.getBlockCount();
			free = blockSize * fs.getFreeBlocks();
			ava = blockSize * fs.getAvailableBlocks();
			
		}else{
			total = 0;
			free = 0;
			ava = 0;
		}
		info.sd_available_mb = toMBFromB( ava );
		info.sd_total_mb = toMBFromB( total );
		info.sd_free_mb = toMBFromB( free );
//		if(total!=mInfo.sd_total){
//			mInfo.sd_total = total;
//			ch = true;
//		}
//		if(free!=mInfo.sd_free){
//			mInfo.sd_free = free;
//			ch = true;
//		}
//		if(ava!=mInfo.sd_available){
//			mInfo.sd_available = ava;
//			ch = true;
//		}
		return true;
	}
	public static boolean setInternalMemory(MemoryInfo info){
		
		File internalMemPath = Environment.getDataDirectory();
//		boolean ch = false;
		long total,free,ava;
		if( internalMemPath != null && internalMemPath.exists() ){
			StatFs fs = new StatFs(internalMemPath.getPath());
			long blockSize = fs.getBlockSize();
			total = blockSize * fs.getBlockCount();
			free = blockSize * fs.getFreeBlocks();
			ava = blockSize * fs.getAvailableBlocks();
		}else{
			total = 0;
			free = 0;
			ava = 0;
		}

		info.internal_available_mb = toMBFromB( ava );
		info.internal_total_mb = toMBFromB( total );
		info.internal_free_mb = toMBFromB( free );
		
//		if(total!=mInfo.internal_total){
//			mInfo.internal_total = total;
//			ch = true;
//		}
//		if(free!=mInfo.internal_free){
//			mInfo.internal_free = free;
//			ch = true;
//		}
//		if(ava!=mInfo.internal_available){
//			mInfo.internal_available = ava;
//			ch = true;
//		}
		return true;
	}	
	
}
