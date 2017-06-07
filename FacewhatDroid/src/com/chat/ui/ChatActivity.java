package com.chat.ui;

import java.io.File;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.DeptProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.db.provider.SMSProvider.SMSColumns;
import com.chat.service.LoginAsyncTask;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.adapter.ChatAdapter;
import com.chat.ui.base.TTBaseActivity;
import com.chat.ui.helper.Emoparser;
import com.chat.ui.widget.EmoGridView;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.EmoGridView.OnEmoGridViewItemClick;
import com.chat.ui.widget.YayaEmoGridView;
import com.chat.utils.FileUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity  extends TTBaseActivity implements
OnRefreshListener2<ListView>,
View.OnClickListener,
OnTouchListener,
TextWatcher,
SensorEventListener{

	private Contact contact;
	private static boolean isDeptChat = false;
	private Context context;

	private Cursor cursor;
	private ContentValues values;
	private ContentObserver co;

	private InputMethodManager inputManager = null;//���뷨����

	private ListView lvPTR = null;
	private TextView textView_new_msg_tip = null;//����Ϣ

	//�ײ��ؼ�
	private TextView sendBtn = null;//������Ϣ
	private ImageView showVoice = null;//�л���������
	private ImageView showEmo = null;//�л���������
	private ImageView showPhoto = null;//�л�ͼƬ����
	private ImageView showKeyboard = null;//�л��ı����룬�������л�
	private EditText textMsgEdt = null;//�ı�����
	private Button recordAudioBtn  = null;//��������,��ס˵��
	private LinearLayout emoLayout = null;//�������
	private EmoGridView emoGridView = null;//Ĭ�ϱ���
	private YayaEmoGridView yayaEmoGridView = null;//Ĭ�ϱ���
	private RadioGroup emoRadioGroup = null;//���������Ĭ�ϱ����л�
	private View addOthersPanelView = null;//��������
	private View pictureBtn = null; //��ȡ��Ƭ
	private View fileBtn = null; //��ȡ�ļ�
	private View recentlyBtn = null; //���ʹ��
	//getRotationMatrix����������ת���󣬹�getOrientation����豸�ķ��򣨺���ǡ������ǡ�����ǣ�


	boolean isShowEmo = false;//��������Ƿ�Ҫ��ʾ������棬false����ʾ��trueҪ��ʾ
	int rootBottom = Integer.MIN_VALUE, keyboardHeight = 0;//���̲�����ز���

	private ChatAdapter adapter = null;

	//������������Ƭѡ��
	private File tempFile;//�����ļ�

	private static int CROLL_FIRST_POS;//��¼list��һ�λ�����λ��
	private final static int SEND_MESSAGE = 1000;
	private final static int SEND_YAYA = 1001;
	private final static int SEND_FILE = 1002;
	private final static int UPDATE_UNREAD_SESSION = 1003;
	private final static int FIND_SESSION = 1003;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String chatType = isDeptChat?"groupchat":"chat";
			switch(msg.what){
			case SEND_MESSAGE:
				Log.e("�����ı���Ϣ",""+chatType);
				try {
					binder.createConnection().sendMessage(contact.getAccount(),contact.getName(), msg.obj.toString(),chatType);
					textMsgEdt.setText("");
					
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(context, "�������磬����ʧ��");
				}
				break;
			case SEND_YAYA:
				Log.e("������������",""+chatType);
				try {
					binder.createConnection().sendMessage(contact.getAccount(),contact.getName(), msg.obj.toString(),chatType);
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(ChatActivity.this, "�������磬����ʧ��");
				}
				break;
			case SEND_FILE:
				break;
			case UPDATE_UNREAD_SESSION:
				//				ListView lv = lvPTR.getRefreshableView();
				//				if(lv != null && (adapter.getCount() - lv.getSelectedItemPosition())<10){
				//��δ����Ϣ�ĳ��Ѷ�
				values=new ContentValues();
				values.put(SMSColumns.UNREAD, "read");
				getContentResolver().update(
						SMSProvider.SMS_URI, 
						values, 
						SMSColumns.SESSION_ID + "=? and "+SMSColumns.UNREAD + "=? ",
						new String[]{contact.getAccount(),"unread"});
				//				}
				break;
			}
		}
	};
	private IMXmppBinder binder;
	private LoginAsyncTask loginTask = new LoginTask();
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	// XMPP���ӷ��� 
	private class XmppServiceConnect implements ServiceConnection {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
			loginTask.execute(binder);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		Bundle bundle = getIntent().getExtras(); 
		contact = bundle.getParcelable("contact"); 
		Log.e("ChatActivity��ȡ���� ��jidΪ��", contact.getAccount()==null?"null":contact.getAccount());

		//��ѯ��ǰ�˻�����Ϣ
		cursor = getContentResolver().query(DeptProvider.DEPT_URI,
				null, DeptProvider.DeptColumns.GROUP_JID +"=?",
				new String[]{contact.getAccount()}, null);
		//�ж�Ⱥ�Ļ��Ǹ�������
		if(cursor != null && cursor.getCount() > 0 ){
			isDeptChat = true;
		}else{
			isDeptChat = false;
		}
		Log.e("�Ƿ��ǲ��ţ�",""+isDeptChat);



		initEmo();//��ʼ������
		initParent();//��ʼ�������
		initView();//��ʼ������
		initData();//��ʼ������

	}

	private void initParent(){
		// �󶨲�����Դ(ע���������Դ��ʼ��֮ǰ)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_message, topContentView);
		//�ж��Ƿ��ǲ���
		if(isDeptChat){
			//��ѯ��ǰ�˻�����Ϣ
			cursor = getContentResolver().query(DeptProvider.DEPT_URI,
					null, DeptProvider.DeptColumns.GROUP_JID +"=?",
					new String[]{contact.getAccount()}, null);
			
			if(cursor != null && cursor.moveToFirst()){
				cursor.moveToPosition(0);
				contact.setName(cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.DISPLAY_NAME)));
				setTitle(contact.getName());
			}
			setRightButton(R.drawable.tt_top_right_group_manager);
		}else{
			setTitle(contact.getName()==null?StringUtils.parseName(contact.getAccount()):contact.getName());
			setRightButton(R.drawable.tt_top_right_contact_manager);
		}

		//��ʼ������
		setLeftButton(R.drawable.tt_top_back);
		setLeftText("����");
		
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
		topRightBtn.setOnClickListener(this);
	}

	private void initData(){
		//���ݹ۲��߼���
		co = new ContentObserver(new Handler()){
			@Override
			public void onChange(boolean selfChange){
				mHandler.sendEmptyMessage(UPDATE_UNREAD_SESSION);
				cursor = getContentResolver()
						.query(
								SMSProvider.SMS_URI, 
								null,
								SMSColumns.SESSION_ID + "=?",
								new String[]{contact.getAccount()},
								null);
				adapter.changeCursor(cursor);
			}
		};
		//ע�����ݹ۲���
		getContentResolver().registerContentObserver(SMSProvider.SMS_URI,true, co);
		co.onChange(true);
	}

	//��ʼ������ؼ�
	private void initView() {
		//��ʼ�����̹���
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		textView_new_msg_tip = (TextView) findViewById(R.id.tt_new_msg_tip);
		
		//��Ϣ�б�
		lvPTR = (ListView) this.findViewById(R.id.message_list);
		adapter = new ChatAdapter(cursor,this);
		lvPTR.setAdapter(adapter);
		
		textView_new_msg_tip.setOnClickListener(this);

		// ����ײ�����򲼾�
		showVoice = (ImageView)findViewById(R.id.show_voice_btn);//�л���������
		showVoice.setOnClickListener(this);
		showEmo = (ImageView)findViewById(R.id.show_emo_btn);//��ʾ����
		showEmo.setOnClickListener(this);
		showPhoto = (ImageView)findViewById(R.id.show_photo_btn);//��ʾͼƬ
		showPhoto.setOnClickListener(this);
		showKeyboard = (ImageView)findViewById(R.id.show_keyboard_btn);//��ʾ��������
		showKeyboard.setOnClickListener(this);
		textMsgEdt = (EditText)findViewById(R.id.message_text);//�ı�������
		textMsgEdt.setOnFocusChangeListener(msgEditOnFocusChangeListener);
		textMsgEdt.setOnClickListener(this);
		textMsgEdt.addTextChangedListener(this);
		inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);//���ؼ���
		sendBtn = (TextView)findViewById(R.id.send_message_btn);//������Ϣ
		sendBtn.setOnClickListener(this);
		recordAudioBtn  = (Button)findViewById(R.id.record_voice_btn);//��������,��ס˵��

		//�л��ļ������
		emoLayout = (LinearLayout)findViewById(R.id.emo_layout);//�������
		emoGridView = (EmoGridView) findViewById(R.id.emo_gridview);
		emoGridView.setOnEmoGridViewItemClick(onEmoGridViewItemClick);
		emoGridView.setAdapter();
		yayaEmoGridView = (YayaEmoGridView) findViewById(R.id.yaya_emo_gridview);
		yayaEmoGridView.setOnEmoGridViewItemClick(yayaOnEmoGridViewItemClick);
		yayaEmoGridView.setAdapter();
		addOthersPanelView = (LinearLayout)findViewById(R.id.add_others_panel);//�������
		emoRadioGroup = (RadioGroup)findViewById(R.id.emo_tab_group);//����Ĭ�ϱ��������������л�
		emoRadioGroup.setOnCheckedChangeListener(emoOnCheckedChangeListener);

		pictureBtn = (View)findViewById(R.id.picture_btn); 
		pictureBtn.setOnClickListener(this);
		fileBtn = (View)findViewById(R.id.file_btn); 
		fileBtn.setOnClickListener(this);
		recentlyBtn = (View)findViewById(R.id.recently_btn); 
		recentlyBtn.setOnClickListener(this);
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect, BIND_AUTO_CREATE);
	}

	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(co);
		unbindService(serviceConnect);
	}

	public void onClick(View v) {
		final int id = v.getId();
		switch(id){
		case R.id.left_btn:
		case R.id.left_txt:
			ChatActivity.this.finish();//����
			break;
		case R.id.right_btn:
			Intent intent = new Intent();
			intent.setClass(ChatActivity.this, UserInfoActivity.class);
			intent.putExtra("contact", contact);
			if(isDeptChat){
				intent.putExtra("who_send", "dept");
			}else{
				intent.putExtra("who_send", "person");
			}
			ChatActivity.this.startActivity(intent);
			break;
		case R.id.show_photo_btn:
			if(isDeptChat){
				return;
			}
			//�����ļ�ѡ��
			inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			emoLayout.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.VISIBLE);
			break;
		case R.id.show_keyboard_btn:
			//��ʾ��������                                                  
			showVoice.setVisibility(View.VISIBLE);
			showKeyboard.setVisibility(View.GONE);
			showEmo.setVisibility(View.VISIBLE);
			recordAudioBtn.setVisibility(View.GONE);
			textMsgEdt.setVisibility(View.VISIBLE);
			textMsgEdt.requestFocus();
			break;
		case R.id.show_voice_btn:
			if(isDeptChat){
				return;
			}
			//��ʾ��������
			showVoice.setVisibility(View.GONE);
			showKeyboard.setVisibility(View.VISIBLE);
			showEmo.setVisibility(View.GONE);
			recordAudioBtn.setVisibility(View.VISIBLE);
			textMsgEdt.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.GONE);
			emoLayout.setVisibility(View.GONE);
			inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			break;
		case R.id.show_emo_btn:
			if(emoLayout.getVisibility() == View.GONE){
				//��ʾ�������
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.VISIBLE);
				emoGridView.setVisibility(View.VISIBLE);
				yayaEmoGridView.setVisibility(View.GONE);
			}else{
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.GONE);
				emoGridView.setVisibility(View.GONE);
				yayaEmoGridView.setVisibility(View.GONE);
			}
			break;
		case R.id.message_text:
			//��������ı�
			emoLayout.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.GONE);
			break;
		case R.id.tt_new_msg_tip:
			//������һ�μ�¼��λ�õײ�
//			ListView lv = lvPTR.getRefreshableView();
//			if (lv != null) {
//				lv.setSelection(adapter.getCount() + 1);
//			}
			textView_new_msg_tip.setVisibility(View.GONE);
			break;

			//ѡ����Ƭ
		case R.id.picture_btn: 
			Intent intentSelect = new Intent(Intent.ACTION_PICK, null);
			intentSelect.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  
					"image/*");
			startActivityForResult(intentSelect, IM.selectCode);
			break;

			//�����ļ�
		case R.id.file_btn:
			Intent file = new Intent(this, FileActivity.class);
			file.putExtra("chat_type",  IM.FILE_TYPE[4]);  
			startActivityForResult(file,IM.fileCode);
			break;
			
			//���ʹ�ù���
		case R.id.recently_btn:
			Intent recently = new Intent(this, FileActivity.class);
			recently.putExtra("chat_type", "rec");  
			startActivityForResult(recently,IM.fileCode);
			break;
			
		case R.id.send_message_btn:
			//������Ϣ
			String bodyStr = textMsgEdt.getText().toString();
			if(bodyStr.equals("")){
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = SEND_MESSAGE;
			msg.obj = bodyStr;
			mHandler.sendMessage(msg);
			break;
		}
	}    

	//ͨ��������飬�������Ӧ���������뵽�ı���
	private OnEmoGridViewItemClick onEmoGridViewItemClick = new OnEmoGridViewItemClick() {
		public void onItemClick(int facesPos, int viewIndex)  {
			int deleteId = (++viewIndex) * (IM.pageSize - 1);
			if (deleteId > Emoparser.getInstance(ChatActivity.this).getResIdList().length) {
				deleteId = Emoparser.getInstance(ChatActivity.this).getResIdList().length;
			}

			if (deleteId == facesPos) {
				String msgContent = textMsgEdt.getText().toString();
				if (msgContent.isEmpty())
					return;
				if (msgContent.contains("["))
					msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
				textMsgEdt.setText(msgContent);
			} else {
				int resId = Emoparser.getInstance(ChatActivity.this).getResIdList()[facesPos];
				String pharse = Emoparser.getInstance(ChatActivity.this).getIdPhraseMap()
						.get(resId);
				int startIndex = textMsgEdt.getSelectionStart();
				//Editable��������б༭
				Editable edit = textMsgEdt.getEditableText();
				if (startIndex < 0 || startIndex >= edit.length()) {
					if (null != pharse) {
						edit.append(pharse);
					}
				} else {
					if (null != pharse) {
						edit.insert(startIndex, pharse);
					}
				}
			}
			Editable edtable = textMsgEdt.getText();
			int position = edtable.length();
			Selection.setSelection(edtable, position);
		}
	};

	//ֱ�ӷ�����������
	private YayaEmoGridView.OnEmoGridViewItemClick yayaOnEmoGridViewItemClick = new YayaEmoGridView.OnEmoGridViewItemClick() {
		public void onItemClick(int facesPos, int viewIndex) {
			int resId = Emoparser.getInstance(ChatActivity.this).getYayaResIdList()[facesPos];

			String content = Emoparser.getInstance(ChatActivity.this).getYayaIdPhraseMap()
					.get(resId);
			if (content.equals("")) {
				Toast.makeText(ChatActivity.this,
						getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = SEND_YAYA;
			msg.obj = content;
			mHandler.sendMessage(msg);
		}
	};

	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	public void onSensorChanged(SensorEvent arg0) {}
	public void afterTextChanged(Editable arg0) {}
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			sendBtn.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams param = (LayoutParams) textMsgEdt
					.getLayoutParams();
			param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
			showPhoto.setVisibility(View.GONE);
		} else {
			showPhoto.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams param = (LayoutParams) textMsgEdt
					.getLayoutParams();
			param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
			sendBtn.setVisibility(View.GONE);
		}
	}

	public boolean onTouch(View arg0, MotionEvent arg1) {return false;}
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {}
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {}


	//��ʼ������
	private void initEmo() {
		Emoparser.getInstance(ChatActivity.this);
	}

	private OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				textMsgEdt.clearFocus();
				if (emoLayout.getVisibility() == View.VISIBLE) {
					emoLayout.setVisibility(View.GONE);
				}

				if (addOthersPanelView.getVisibility() == View.VISIBLE) {
					addOthersPanelView.setVisibility(View.GONE);
				}
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			}
			return false;
		}
	};

	private RadioGroup.OnCheckedChangeListener emoOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup radioGroup, int id) {
			switch (id) {
			case R.id.tab1:
				if (emoGridView.getVisibility() != View.VISIBLE) {
					yayaEmoGridView.setVisibility(View.GONE);
					emoGridView.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.tab2:
				if (yayaEmoGridView.getVisibility() != View.VISIBLE) {
					emoGridView.setVisibility(View.GONE);
					yayaEmoGridView.setVisibility(View.VISIBLE);
				}
				break;
			}
		}
	};

	//������ȡ�������
	private View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.GONE);
				inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	};

	private void sendFile(String filePath) {
		if(filePath.isEmpty()){
			return;
		}
		boolean bool = true;
		try {
			bool = binder.createConnection().isOnlineByJID(contact.getAccount());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		if(!bool){
			MyToast.showToastLong(context, "�Է������ߣ����ܷ��������ļ�");
			return;
		}

		Log.e("ChatActivity:sendFile", "filePath = " + filePath);
		String[] pathStrings = filePath.split("/"); // �ļ���
		String fileName = null ;
		if (pathStrings!=null && pathStrings.length>0) {
			fileName = pathStrings[pathStrings.length-1];
		}
		try {
			binder.createConnection().sendFile(contact.getAccount(), filePath, fileName);
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("ChatActivity:sendPicture",""+e.getMessage().toString());
		}
	}

	public void onBackPressed(){
		ChatActivity.this.finish();//����
	}

	//ͼƬѡ��֮��Ļص�
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
			return;
		}
		switch(requestCode){
		case IM.fileCode:
			String filePath = data.getExtras().getString("file_path");
			Log.e("ChatActivity:onActivityResult","sendfile");
			sendFile(filePath);
			break;
		case IM.selectCode:
			
			//���ͼƬ��uri
			Uri uri = data.getData();
			//��ȡͼƬ��·����
			String[] proj = {MediaStore.Images.Media.DATA};
			//filePath =
			//������android��ý�����ݿ�ķ�װ�ӿڣ�����Ŀ�Android�ĵ�
            Cursor cursor = managedQuery(uri, proj, null, null, null); 
            //���Ҹ������ ����ǻ���û�ѡ���ͼƬ������ֵ
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //�����������ͷ ���������Ҫ����С�ĺ���������Խ��
            cursor.moveToFirst();
            //����������ֵ��ȡͼƬ·��
            String path = cursor.getString(column_index);
            
			Log.e("ChatActivity:onActivityResult","ͼƬ·��= "+path);
			sendFile(path);
			break;
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
		}
	}
}
