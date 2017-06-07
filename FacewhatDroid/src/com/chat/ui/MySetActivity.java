package com.chat.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.service.LoginAsyncTask;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.VCardInfo;
import com.chat.ui.adapter.PictureAdapter;
import com.chat.ui.base.TTBaseActivity;
import com.chat.ui.widget.ImageViewCircle;
import com.chat.ui.widget.MyToast;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

public class MySetActivity extends TTBaseActivity 
implements OnClickListener,
OnItemClickListener{
	private Context context;
	//����JID,�����ǳ�,���˵绰,����email,����סַ,
	private EditText etJid,etName,etTel,etEmail,etAddress;
	private ImageViewCircle ivcIcon;//����ͷ��
	private ImageView ivModify;//���������Ϣ�޸�
	private Button btnSave;//�����޸�����
	private ProgressBar pbShow;//������
	private GridView picList;//ͼƬ
	private List<Map<String,Object>> picId = new ArrayList<Map<String,Object>>();
	private SimpleAdapter adapter;

	private static int[] icon = new int[]{R.drawable.cartoon01,R.drawable.cartoon02,
		R.drawable.cartoon03,R.drawable.cartoon04,R.drawable.cartoon05,R.drawable.cartoon06,
		R.drawable.cartoon07,R.drawable.cartoon08,R.drawable.cartoon09,R.drawable.cartoon10,
		R.drawable.cartoon11,R.drawable.cartoon12,
		R.drawable.cartoon13,R.drawable.cartoon14,R.drawable.cartoon15,R.drawable.cartoon16,
		R.drawable.cartoon17,R.drawable.cartoon18,R.drawable.cartoon19,R.drawable.cartoon20,
		R.drawable.cartoon21,R.drawable.cartoon22,R.drawable.cartoon23,R.drawable.cartoon24,
		R.drawable.cartoon25,R.drawable.cartoon26,R.drawable.cartoon27,R.drawable.cartoon28};

	//�����ļ�
	private File tempFile;
	private AlertDialog alertDialog;
	private byte[] avatarBytes;

	private VCardInfo vCardInfo;//����VCard

	private final static int BEGIN_PROGROSS = 1000;
	private final static int END_PROGROSS = 1001;
	private final static int GET_VCARD = 1002;
	private final static int MODIFY_VCARD = 1003;
	
	private Handler mHandler = new Handler(){
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case BEGIN_PROGROSS:
				pbShow.setVisibility(View.VISIBLE);
				break;
			case GET_VCARD:
				mHandler.sendEmptyMessage(BEGIN_PROGROSS);
				try {
					//��ѯ������Ϣ
					vCardInfo = binder.createConnection().getVCard(IM.getString(IM.ACCOUNT_JID));
					if(vCardInfo == null) return;
					etJid.setText(IM.getString(IM.ACCOUNT_JID));
					if(vCardInfo.name!=null)
						etName.setText(vCardInfo.name);
					if(vCardInfo.emailHome!= null)
						etEmail.setText(vCardInfo.emailHome);
					if(vCardInfo.phoneNum != null)
						etTel.setText(vCardInfo.phoneNum);
					if(vCardInfo.homeAddress != null)
						etAddress.setText(vCardInfo.homeAddress);
					if(IM.getAvatar(vCardInfo.jid)!=null){
						ivcIcon.setImageDrawable(IM.getAvatar(vCardInfo.jid));
						Log.e("MySetActivity:",IM.getString(IM.ACCOUNT_JID)+" "+IM.getAvatar(IM.getString(IM.ACCOUNT_JID)));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(END_PROGROSS);
				break;
				
			case MODIFY_VCARD:
				mHandler.sendEmptyMessage(BEGIN_PROGROSS);
				try {
					//���ø�����Ϣ
					boolean bool = binder.createConnection().setVCard(vCardInfo);
					if(bool){
						etName.setEnabled(false);
						etTel.setEnabled(false);
						etEmail.setEnabled(false);
						etAddress.setEnabled(false);

						etName.setBackground(null);
						etTel.setBackground(null);
						etEmail.setBackground(null);
						etAddress.setBackground(null);
						MyToast.showToastLong(context, getString(R.string.tt_my_info_save_ok));
					}else{
						MyToast.showToastLong(context, getString(R.string.tt_my_info_save_no));
					}
					mHandler.sendEmptyMessage(END_PROGROSS);
				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(END_PROGROSS);
				break;
			case END_PROGROSS:
				pbShow.setVisibility(View.GONE);
				break;
			}
		}
	};
	private ServiceConnection serviceConnection = new LoginServiceConnection();//�������Ӷ���
	private IMXmppBinder binder;//����󶨶���
	private LoginAsyncTask loginTask = new LoginTask();

	//������serviceʱ���շ���˴�������Binder����
	class LoginServiceConnection implements ServiceConnection{
		public void onServiceConnected(ComponentName arg0, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
			loginTask .execute(binder);
			mHandler.sendEmptyMessage(GET_VCARD);
		}

		public void onServiceDisconnected(ComponentName arg0) {
			binder = null;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		initParent();
		init();
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	private void initParent(){
		// �󶨲�����Դ(ע���������Դ��ʼ��֮ǰ)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_my_set, topContentView);

		//���÷���ͼ��
		setLeftButton(R.drawable.tt_top_back);
		//���÷�������
		setLeftText(getResources().getString(R.string.top_left_back));
		//���ý������
		setTitle(getResources().getString(R.string.tt_my_info_detail));
	}

	private void init(){
		ivcIcon = (ImageViewCircle)findViewById(R.id.tt_activity_my_icon);
		etJid = (EditText)findViewById(R.id.tt_activity_my_jid);
		etName = (EditText)findViewById(R.id.tt_activity_my_name);
		etTel = (EditText)findViewById(R.id.tt_activity_my_phone);
		etEmail = (EditText)findViewById(R.id.tt_activity_my_email);
		etAddress = (EditText)findViewById(R.id.tt_activity_my_address);
		ivModify = (ImageView)findViewById(R.id.tt_activity_my_modify);
		btnSave = (Button)findViewById(R.id.tt_activity_my_save);
		pbShow = (ProgressBar)findViewById(R.id.tt_activity_my_bar);
		picList = (GridView)findViewById(R.id.tt_activity_my_picList);

		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
		ivModify.setOnClickListener(this);
		btnSave.setOnClickListener(this);
		ivcIcon.setOnClickListener(this);
		picList.setOnItemClickListener(this);

		for(int i = 0;i<icon.length;i++){
			Map<String,Object> map = new  HashMap<String,Object>();
			map.put("cartoon", icon[i]);
			picId.add(map);
		}

	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//������һ��
		case R.id.left_txt:
		case R.id.left_btn:
			if(picList.getVisibility() == View.VISIBLE){
				picList.setVisibility(View.GONE);
			}else{
				finish();
			}
			break;
		//�޸�ͷ��
		case R.id.tt_activity_my_icon:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(this,android.R.style.Theme_Holo_Light_Dialog));
			builder.setItems(new String[]{"����ͼƬ","�������ѡ��"}, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface arg0, int which) {
					switch(which){
					case 0:
						picList.setVisibility(View.VISIBLE);
						adapter = new SimpleAdapter(context, picId, R.layout.tt_item_my_set,
								new String[]{"cartoon"}, new int[]{R.id.tt_item_my_set_icon});
						picList.setAdapter(adapter);

						break;
					case 1:
						Intent intentSelect = new Intent(Intent.ACTION_PICK, null);
						intentSelect.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  
								"image/*");
						startActivityForResult(intentSelect, IM.selectCode);
						break;
					}
				}
			});
			builder.show();
			break;
			//�����޸�
		case R.id.tt_activity_my_modify:
			etName.setEnabled(true);
			etTel.setEnabled(true);
			etEmail.setEnabled(true);
			etAddress.setEnabled(true);

			etName.setBackgroundResource(R.drawable.textfield_focused_holo_dark);
			etTel.setBackgroundResource(R.drawable.textfield_focused_holo_dark);
			etEmail.setBackgroundResource(R.drawable.textfield_focused_holo_dark);
			etAddress.setBackgroundResource(R.drawable.textfield_focused_holo_dark);
			break;
			//�����޸�
		case R.id.tt_activity_my_save:
			//��ʾ������
			pbShow.setVisibility(View.VISIBLE);
			if(IM.getAvatar(IM.KEY_SET_MY_INFO_AVATOR)!= null || etName.isEnabled()){
				//��ȡ�޸ĵ���Ϣ
				vCardInfo = new VCardInfo();
				vCardInfo.jid = etJid.getText().toString();
				vCardInfo.name = etName.getText().toString();
				vCardInfo.phoneNum = etTel.getText().toString();
				vCardInfo.emailHome = etEmail.getText().toString();
				vCardInfo.homeAddress = etAddress.getText().toString();
				ivcIcon.getDrawable();

				ivcIcon.setDrawingCacheEnabled(true);
				avatarBytes = IM.Bitmap2Bytes(ivcIcon.getDrawingCache());
				ivcIcon.setDrawingCacheEnabled(false);
				IM.setAvatar(avatarBytes,IM.KEY_SET_MY_INFO_AVATOR);

				mHandler.sendEmptyMessage(MODIFY_VCARD);
			}else{
				MyToast.showToastLong(this, getString(R.string.tt_my_info_no_modify));
			}
			//���ؽ�����
			pbShow.setVisibility(View.GONE);
			break;
		}
	}

	//���շ�����
	protected void onActivityResult(int requstCode,int resultCode,Intent data) {
		if(resultCode != Activity.RESULT_OK){
			return;
		}
		switch (requstCode) {
		//��Ƭѡ��
		case IM.selectCode:
			if(data.getData() == null){
				return;
			}
			startPhotoZoom(data.getData());
			break;

			//���յõ�����Ƭ���вü�
		case IM.cameraCode:
			if(Uri.fromFile(tempFile) == null){
				return;
			}

			startPhotoZoom(Uri.fromFile(tempFile));
			break;

		case IM.picCode://�ü�
			Log.e("MySetActivity:�ü�","���");
			if(data != null){
				Bitmap photoPic = data.getParcelableExtra("data");
				if(photoPic != null){
					ivcIcon.setImageDrawable(IM.Bitmap2Drawable(photoPic));
				}
			}
			break;
		default:
			break;
		}
	}

	// �ü�ͼƬ����ʵ��  
	public void startPhotoZoom(Uri uri) {  
		Log.e("MySetActivity:�ü�","begin");
		/*  
		 * �����������Intent��ACTION����ô֪���ģ���ҿ��Կ����Լ�·���µ�������ҳ  
		 * yourself_sdk_path/docs/reference/android/content/Intent.html  
		 */ 
		Intent intent = new Intent("com.android.camera.action.CROP");  
		intent.setDataAndType(uri, "image/*"); 
		//�������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�  
		intent.putExtra("crop", "true");  
		// aspectX aspectY �ǿ�ߵı���  
		intent.putExtra("aspectX", 1);  
		intent.putExtra("aspectY", 1);  
		// outputX outputY �ǲü�ͼƬ���  
		intent.putExtra("outputX", 150);  
		intent.putExtra("outputY", 150);  
		intent.putExtra("return-data", true);  
		startActivityForResult(intent, IM.picCode);  
		Log.e("MySetActivity:�ü�","end");
	}  

	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		ivcIcon.setImageResource(icon[position]);
		picList.setVisibility(View.GONE);
	}

	public void onBackPressed(){
		if(picList.getVisibility() == View.VISIBLE){
			picList.setVisibility(View.GONE);
		}else{
			finish();
		}
	}
	
	class LoginTask extends LoginAsyncTask{

		protected void onPostExecute(Integer result) {
			switch(result){
			case IM.LOGIN_OK:
				break;

			case IM.LOGIN_NET_ERROR:
				MyToast.showToastLong(context, "����Ͽ�");
				break;

			case IM.LOGIN_SERVER_ERROR:
				MyToast.showToastLong(context, "������δ����");
				break;

			case IM.LOGIN_PASSWORD_ERROR:
				break;
			}
			pbShow.setVisibility(View.GONE);
		}

	}


}
