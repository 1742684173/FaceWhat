package com.mogujie.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * �ļ��������, ����sd ���ļ���д����assets�е��ļ���д
 * 
 * @author 6a209 ����12:05:32 2011-12-21
 */
public class FileIOTools {
	/**
	 * ���˳�2��ǰ��ɾ
	 * 
	 * @author 6a209 ����10:16:22 2012-1-12
	 */
	private class TimeFlitter implements FileFilter{

		@Override
		public boolean accept(File pathname) {
			long modified = pathname.lastModified();
			long curTime = System.currentTimeMillis();
			if(curTime - modified > mTimeDuration){
				return true;
			}
			return false;
		}
	}
	private final Context mCtx;

	private long mTimeDuration;
	private static FileIOTools sFileIOTools;
	public static final String BASE_PATH =
			Environment.getExternalStorageDirectory() + "/mogujie/";
	/* ��־���ļ� */
	public static final String DIR_LOG = BASE_PATH + "log/";
	public static final String PICTURE_INDEX_DIR = BASE_PATH + "pic_index/";
	public static final String PICTURE_WALL_DIR = BASE_PATH + "pic_wall/";
	public static final String PICTURE_OTHRE_DIR = BASE_PATH + "pic_other/";

	public static final String DEFAULT_WELCOME = "default_welcome";

	public static FileIOTools instance(Context ctx){
		if(null == sFileIOTools){
			sFileIOTools = new FileIOTools(ctx);
		}
		return sFileIOTools;
	}
	private boolean isDeletingFiles =false;

	private FileIOTools(Context ctx){
		mCtx = ctx.getApplicationContext();
	}

	synchronized public void delFiles(){
		if(!hasSDCard()||isDeletingFiles){
			return;
		}
		isDeletingFiles = true;
		mTimeDuration = 1000 * 60 * 60 * 24 * 1;
		new Thread(){
			@Override
			public void run(){
				File file = new File(PICTURE_WALL_DIR);
				File [] list = file.listFiles(new TimeFlitter());
				if(null != list){
					for(int i = 0; i < list.length; i++){
						list[i].delete();
					}
				}
				file = new File(PICTURE_OTHRE_DIR);
				list = file.listFiles(new TimeFlitter());
				if(null != list){
					for(int i = 0; i < list.length; i++){
						list[i].delete();
					}
				}
				isDeletingFiles = false;

			}
		}.start();

	}

	//	private ArrayList<String> mListFiles = new ArrayList<String>();
	/**
	 * ����ɾ��
	 * 
	 * @param dir
	 * @param count
	 */
	public void delFilesByTime(final String dir, final int days){
		if(!hasSDCard()||isDeletingFiles){
			return;
		}
		isDeletingFiles = true;
		mTimeDuration = 1000 * 60 * 60 * 24 * days;
		new Thread(){
			@Override
			public void run(){
				File file = new File(dir);
				File [] list = file.listFiles(new TimeFlitter());
				if(null != list){
					int max = Math.min(100, list.length);
					for(int i = 0; i < max; i++){
						list[i].delete();
					}
				}
				isDeletingFiles = false;
			}
		}.start();

	}

	/**
	 * ���ļ���ȡͼ
	 * 
	 * @param context
	 * @param dirFile
	 * @param imgName
	 *            md5֮�������
	 * @return
	 */
	public  Bitmap getBitmapFromSD(Context context, String dirFile, String imgName) {
		if(!hasSDCard()){
			return null;
		}
//		MGUtils utils = MGUtils.instance(mCtx);
		if(null == imgName){
			return null;
		}
		imgName = EncryptTools.instance().toMD5(imgName);
		File file = new File(dirFile, imgName);
		if(!file.exists()){
			return null;
		}
		try {
			return BitmapFactory.decodeFile(dirFile + "/" + imgName);

		} catch (Throwable e) {
			ContentResolver resolver = context.getContentResolver();
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = 2;
			o2.inPreferredConfig = Bitmap.Config.RGB_565;
			InputStream is;
			try {
				is = resolver.openInputStream(Uri.fromFile(file));
				return BitmapFactory.decodeStream(is, null, o2);
			} catch (Throwable e1) {
				return null;
			}
		}
	}


	public Bitmap getImageFromAssetsFile(String fileName) {
	      Bitmap image = null;
	      AssetManager am = mCtx.getResources().getAssets();
	      try {
	          InputStream is = am.open(fileName);
	          image = BitmapFactory.decodeStream(is);
	          is.close();
	      }
	      catch (IOException e) {
	          e.printStackTrace();
	      }
	      return image;
	  }


	public  Bitmap getTempBitmapFromSD(Context context) {
		if(!hasSDCard()){
			return null;
		}

		File file = new File(PICTURE_OTHRE_DIR, "temp.jpg");
		if(!file.exists()){
			return null;
		}

		// ��ȡ���ű�������������800
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), o2);
		int s = o2.outWidth>o2.outHeight?o2.outWidth:o2.outHeight;
		int d= (int)Math.floor(s/800.0);
		d = d<1?1:d;

		o2.inSampleSize = d;
		o2.inPreferredConfig = Bitmap.Config.ARGB_8888;
		o2.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(PICTURE_OTHRE_DIR+"temp.jpg", o2);

	}
	
	/**
	 * �ж�sd���Ƿ����
	 * 
	 * @return
	 */
	public boolean hasSDCard(){
		return Environment.getExternalStorageState().equals(
			Environment.MEDIA_MOUNTED);
	}

	/**
	 * ��ʼ��sd���е��ļ�
	 */
	public void initDir(){
		File  wall = new File(PICTURE_WALL_DIR);
		if(!wall.exists()){
			wall.mkdirs();
		}

		File welcome = new File(PICTURE_INDEX_DIR);
		if(!welcome.exists()){
			welcome.mkdirs();
		}

		File other = new File(PICTURE_OTHRE_DIR);
		if(!other.exists()){
			other.mkdirs();

		}

		File log = new File(DIR_LOG);
		if(!log.exists()){
			log.mkdirs();
		}

	}

	/**
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public String readAssets(String fileName) throws Exception{
		InputStream in;
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		in = mCtx.getResources().getAssets().open(fileName);
		br = new BufferedReader(new InputStreamReader(in, "utf8"));
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		in.close();
		br.close();
		return sb.toString();
	}

	/**
	 * �����Դ��ڴ�
	 * 
	 * @param ctx
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public Bitmap readFile(Context ctx, String fileName) throws IOException{

        Bitmap image = null;
	      try {
	    	  FileInputStream fis = ctx.openFileInput(fileName);
	          image = BitmapFactory.decodeStream(fis);
	          fis.close();
	      }
	      catch (IOException e) {
	          e.printStackTrace();
	      }
	      return image;
	}


/**
	 * ��sd���ж�ȡ����
	 * 
	 * @param dir
	 *            �ļ���
	 * @param fileName
	 *            �ļ���
	 * @return ��������
	 * @throws Exception
	 */
	public String readSDFile(String dir, String fileName) throws Exception{
		if(!hasSDCard()){
			return null;
		}
		File targetFile = new File(dir, fileName);
		InputStream in;
		BufferedReader br;
		StringBuffer sb = new StringBuffer();
		if(targetFile.exists()){
			in = new BufferedInputStream(new FileInputStream(targetFile));
			br = new BufferedReader(new InputStreamReader(in, "utf8"));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			in.close();
			br.close();
		}
		return sb.toString();
	}

	public void writeBitmap2File(
			Context ctx, String file, Bitmap img, CompressFormat format)
			throws Exception{
		if (img == null) {
			return;
		}
		if(null == format){
			format = CompressFormat.JPEG;
		}
 		FileOutputStream fo = ctx.openFileOutput(file, Context.MODE_PRIVATE);
		img.compress(format, 100, fo);
		fo.flush();
		fo.close();
	}

	public void writeBtimapToSD(
			Context ctx, String dir, String file, Bitmap img, CompressFormat format)
			throws Exception{
		if(!hasSDCard()){
			return;
		}
		if (img == null) {
			return;
		}
		String path = dir + EncryptTools.instance().toMD5(file);
		File targetFile = new File(path);
		if(targetFile.exists()){
			return;
		}
		targetFile.createNewFile();
 		FileOutputStream fo = new FileOutputStream(path);
		img.compress(format, 100, fo);
		fo.flush();
		fo.close();
	}

	/**
	 * ���ļ�д��sd����
	 * 
	 * @param dir
	 *            д���Ŀ¼
	 * @param fileName
	 *            �ļ���
	 * @param data
	 *            Ҫд������
	 * @throws Exception
	 */
	public void writeSDFile(String dir, String fileName, String data) throws Exception{
		if(!hasSDCard()){
			return;
		}
		File targetFile = new File(dir, "welcome");
		OutputStreamWriter osw = null;
        if(!targetFile.exists()){
            targetFile.createNewFile();
        }
        osw = new OutputStreamWriter(new FileOutputStream(targetFile),"utf-8");
        osw.write(data);
        osw.flush();
        osw.close();
	}


}
