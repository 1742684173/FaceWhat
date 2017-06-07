package com.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.chat.utils.NetUtil;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class IM extends Application{
	public static IM im;//����

	public static int PORT = 5222;//�˿ں�

	public static final int LOGIN_OK = 200;//��¼�ɹ�
	public static final int LOGIN_PASSWORD_ERROR = 205;//�������
	public static final int LOGIN_ACCOUNT_NOT_EXIST = 404;//�˻�������
	public static final int LOGIN_REPEAT = 409;//�ظ���¼
	public static final int LOGIN_SERVER_ERROR = 502;//������δ����
	public static final int LOGIN_UN_KNOWN = 588;//δ֪����
	public static final int LOGIN_OUT_TIME = 5000;//���ӳ�ʱ
	public static final int LOGIN_NET_ERROR = 5001;//������δ����

	public static int mNetWorkState;//������������״̬

	//�Ƿ����ηǺ�����Ϣ
	public static final String IS_ACCEPT_UN_FRI_MSG = "is_accept_un_fri_msg";
	
	public static final String HOST = "host";
	public static final String ACCOUNT_JID = "account_jid";
	public static final String ACCOUNT_PASSWORD = "account_password";
	public static final String ACCOUNT_NICKNAME = "account_nickname";
	public static final String AUTO_LOGIN = "auto_login";
	public static final String SAVE_PWD = "save_pwd";

	public static final String KEY_CONTACT_JID = "key_jid";//����������ϵ����Ϣ��key
	public static final String KEY_SESSION_KEY = "session_jid";//�������ݻỰ��Ϣ��key
	public static final String KEY_SET_MY_INFO_AVATOR = "key_set_my_info_avator";//����ͷ��ʱ���б���

	public static final int LOGIN_SIGN_REQUEST_CODE = 1000;//�˳���¼ʱ�ķ�����
	public static final int LOGIN_MAINACTIVITY_REQUEST_CODE = 1002;//��¼�����������

	public static final int pageSize = 21;//ÿҳĬ�ϱ��������
	public static final int yayaPageSize = 8;//ÿҳ�������������
	public static final int defaultEmoSize = 45;//Ĭ�ϱ�������

	// ��ȡ�������ļ��� ��֧�ж�������
	public static final int FILE_SAVE_TYPE_IMAGE = 0X00013;
	public static final int FILE_SAVE_TYPE_AUDIO = 0X00014;

	//�����ļ���ŵ�ַ
	public static final String FILE_PATH = "/data/data/com.chat/file";
	public static final String PICTURE_PATH = "/storage/emulated/0/DCIM/Camera/";
	public static final String ALL_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
	
	public static final String FILE_PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String[] FILE_TYPE = new String[]{"text","picture","music","audio","file","zip","application","other"};
	public static final String[] FILE_TYPE_TEXT = new String[]{"[�ı�]","[ͼƬ]","[����]","[��Ƶ]","[�ļ�]","[ѹ����]","[Ӧ��]"};
	public static final String[] PICTURE_SUFFIX = new String[]{".png",".jpg",".jpeg",".jif"};
	public static final String[] MUSIC_SUFFIX = new String[]{".aif",".mp3",".wav"};
	public static final String[] VIDEO_SUFFIX = new String[]{".avi",".rmvb",".rm",".asf",".divx",".mpg"};
	public static final String[] FILE_SUFFIX = new String[]{".txt",".doc",".docx",".pdf",".ppt","."};
	public static final String[] ZIP_SUFFIX = new String[]{".zip",".jar"};
	public static final String[] APPLICATION_SUFFIX = new String[]{".exe",".apk"};

	//�㲥
	public static final String FILE_RECEIVER_BROADCAST = "com.chat.broadcast.BroadcastReceiverMsg";

	public static final int fileCode = 122;// ѡ����Ƭ������
	public static final int selectCode = 123;// ѡ����Ƭ������
	public static final int cameraCode = 124;// ���շ�����
	public static final int picCode = 125;// ϵͳ �ü�������
	
	
//	 subscribe �����ı��ˣ�������ӶԷ�Ϊ����
//	  subscribed  ��Ӧ�Է�������
//	  unsubscribe  ����ܾ�����
//	  unsubscribed  ��Ӧ���˵ľܾ�
	public static final String[] PRESENCE_TYPE = new String[]{"subscribe","subscribed","unsubscribe","unsubscribed"};

	public void onCreate(){
		super.onCreate();
		im = this;
		mNetWorkState = NetUtil.getNetworkState(this);
	}

	public static boolean putString(String key, String value) {
		SharedPreferences settings = im.getSharedPreferences(key, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	public static String getString(String key) {
		SharedPreferences settings = im.getSharedPreferences(key, MODE_PRIVATE);
		return settings.getString(key, "");
	}

	public static void putBoolean(String key,boolean value)
	{
		SharedPreferences sharedPreferences = im.getSharedPreferences(key, MODE_PRIVATE);
		sharedPreferences.edit().putBoolean(key, value).commit();
	}

	public static boolean getBoolean(String key,Boolean... defaultValue)
	{
		SharedPreferences sharedPreferences = im.getSharedPreferences(key, MODE_PRIVATE);
		Boolean dv = false;
		for(boolean v:defaultValue)
		{
			dv = v;
			break;
		}
		return sharedPreferences.getBoolean(key,dv);
	}

	//ͼƬ��Ҵ���
	public static Bitmap grey(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap
				.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
				colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	}

	//ͼƬתBitmap
	public static Bitmap getBitmap(int id){
		Resources res = im.getResources();
		return BitmapFactory.decodeResource(res, id);
	}
	/**��ȡ�����ļ�*/
	public static File getCameraFile(){
		//ʹ��ϵͳ��ǰ���ڼ��Ե�����Ϊ��Ƭ������
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");

		/**
		 * Environment.DIRECTORY_DCIM ���������Ƭ����Ƶ�ı�׼Ŀ¼
		 * getExternalStoragePublicDirectory(String type)�����������һ������������Ŀ¼���ŵ��ļ�������
		 */
		return new File(Environment.getExternalStorageDirectory()+
				"/"+dateFormat.format(date)+".jpg");
	}

	/**
	 * Bitmapת��byte[]
	 * bitmapҪת����bitmap�ļ�
	 */
	public static byte[] Bitmap2Bytes(Bitmap bitmap){
		if(bitmap == null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Drawable Bitmap2Drawable(Bitmap bitmap){
		return new BitmapDrawable(im.getResources(),bitmap);
	}

	//ͼƬѹ������
	public static Bitmap zoomImg(Bitmap bm,int w,int h){   
		// ���ͼƬ�Ŀ��   
		int width = bm.getWidth();   
		int height = bm.getHeight();   
		// �������ű���   
		float scaleWidth = ((float) w) / width;   
		float scaleHeight = ((float) h) / height;   
		// ȡ����Ҫ���ŵ�matrix����   
		Matrix matrix = new Matrix();   
		matrix.postScale(scaleWidth, scaleHeight);
		// �õ��µ�ͼƬ   www.2cto.com
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);   
		return newbm;   
	}  

	//��ȡ��ͼƬ��ʽ��ͷ��
	public static Drawable getAvatar(String fileName) {
		byte[] bytes = getFile(fileName, FILE_PATH);
		if (bytes != null) {
			if (bytes.length > 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				return IM.Bitmap2Drawable(bitmap);
			}
		}
		return IM.im.getResources().getDrawable(R.drawable.facewhat);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ?
						Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		//canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	//��ȡbyte[]�ε�ͷ��
	public static byte[] getByteAvatar(String fileName){
		return getFile(fileName, FILE_PATH);
	}

	public static boolean setAvatar(byte[] bytes, String fileName) {
		if (bytes == null || TextUtils.isEmpty(fileName)) {
			return false;
		}
		return setFile(bytes, fileName, FILE_PATH);
	}

	public static void clearAvatar(String path){
		//		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		//			String SDCardPath = Environment.getExternalStorageDirectory().getPath() + FILE_PATH;
		File file = new File(path);
		delete(file);
		//		}
	}

	//����ͼƬ
	public static void delete(File file){
		if(file.isFile()){
			file.delete();
			return;
		}
		if(file.isDirectory()){
			File[] childFiles = file.listFiles();
			if(childFiles == null || childFiles.length == 0){
				file.delete();
				return;
			}
			for(int i =0;i<childFiles.length;i++){
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	//	�ļ�ת����byte[]
	//	public static 
	//��ȡͷ��
	public static byte[] getFile(String fileName, String directory) {
		FileInputStream fis = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory().getPath() + directory;
				File file = new File(SDCardPath, fileName);
				fis = new FileInputStream(file);
			} else {  
				fis = im.openFileInput(fileName);
			}
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			return buffer;
		} catch (IOException e) {                              
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}



	//�����ļ�
	public static boolean setFile(byte[] bytes, String fileName, String directory) {
		FileOutputStream fos = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory().getPath() + directory;
				File fileDirectory = new File(SDCardPath);
				if (!fileDirectory.exists()) {
					fileDirectory.mkdirs();
				}
				File file = new File(fileDirectory, fileName);
				fos = new FileOutputStream(file);
			} else {
				fos = im.openFileOutput(fileName, MODE_PRIVATE);
			}
			fos.write(bytes);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static byte[] getFile(String filePath) {
		FileInputStream fis = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			return buffer;
		} catch (IOException e) {                              
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	//�����ļ�
	public static boolean copyFile(byte[] bytes, String fileName, String directory) {
		FileOutputStream fos = null;
		try {
			File fileDirectory = new File(directory);
			if (!fileDirectory.exists()) {
				fileDirectory.mkdirs();
			}
			File file = new File(fileDirectory, fileName);
			fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
