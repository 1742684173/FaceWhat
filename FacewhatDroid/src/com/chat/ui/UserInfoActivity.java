package com.chat.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.DeptProvider;
import com.chat.service.LoginAsyncTask;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.VCardInfo;
import com.chat.ui.base.TTBaseActivity;
import com.chat.ui.widget.ImageViewCircle;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.NoScrollListview;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class UserInfoActivity extends TTBaseActivity implements OnClickListener{
	private Context context;
	private Contact contact;
	private static String jid;//��������jid
	private static String WHO_SEND;//˭��������
	private LinearLayout personLayout,deptLayout;//�ֱ���������ʾ������Ϣ�Ͳ�����Ϣ
	private TextView tvName,tvJid,tvEmail,tvTel,tvAddress;//�û�
	private ImageViewCircle ivcIcon;//ͷ��
	private Button btnAddFri;//������Ϣ����Ӻ���
	private ProgressBar pbLoad;
	
	private NoScrollListview listDeptNumber;//��ʾ���ų�Ա
	private SimpleAdapter myDeptAdapter;
	
	private final static String PERSON = "person";
	private final static String DEPT = "dept";

	private final static int BEGIN_PROGRESS = 1000;
	private final static int END_PROGRESS = 1001;
	private final static int GET_VCARD = 1002;
	private final static int GET_DEPT = 1003;
	private final static int ADD_FRI = 1004;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			switch(msg.what){
			case BEGIN_PROGRESS:
				pbLoad.setVisibility(View.VISIBLE);
				break;
				
			case END_PROGRESS:
				pbLoad.setVisibility(View.GONE);
				break;
			
			//��Ϊ���� 
			case ADD_FRI:
				try {
					binder.createConnection().addFri(jid,msg.obj.toString(),new String[]{"Friends"});
					MyToast.showToastLong(context,"�ѷ����������");
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(context,"�����������ʧ��");
				}
				break;
				
			//��ȡ���ų�Ա
			case GET_DEPT:
				Cursor myDeptNumber = getContentResolver().query(
						DeptProvider.DEPT_URI, null, 
						DeptProvider.DeptColumns.GROUP_JID + "=?",
						new String[]{contact.getAccount()},
						null);
				Log.e("��ѯ��Ա���",""+myDeptNumber==null?"null":"�ɹ�");
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				for(int i = 0;i < myDeptNumber.getCount();i++){
					myDeptNumber.moveToPosition(i);
					String userJid = myDeptNumber.getString(myDeptNumber.getColumnIndex(DeptProvider.DeptColumns.USER_JID));
					String userName = myDeptNumber.getString(myDeptNumber.getColumnIndex(DeptProvider.DeptColumns.USER_NICK_NAME));
					
					Log.e("��ѯ��Ա"+i,""+userName);
					
					Map<String, Object> item = new HashMap<String, Object>(); 
					item.put("name", userName);
					item.put("jid", userJid);
					
					list.add(item);
				}
				
				myDeptAdapter = new SimpleAdapter(UserInfoActivity.this,
						list, R.layout.tt_item_group_parent, new String[]{"jid","name"},
						new int[]{R.id.tt_item_group_parent_id,R.id.tt_item_group_parent_name});
				listDeptNumber.setAdapter(myDeptAdapter);
				
				mHandler.sendEmptyMessage(END_PROGRESS);
				break;
				
			//�õ�VCARD
			case GET_VCARD:
				//�ж�jid�Ƿ����Լ�
				if (jid.equals(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)))) {
					btnAddFri.setVisibility(View.GONE);
				}else{
					System.out.println("�ж��Ƿ��Ǻ���:" + jid);
					try {
						//�ж�jid�Ƿ����Լ�����
						if(binder.createConnection().isFriendByJID(jid)){
							btnAddFri.setVisibility(View.GONE);
						}else{
							btnAddFri.setVisibility(View.VISIBLE);
						}
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
				}
				
				//��ʾ������Ϣ
				try {
					VCardInfo vCardInfo = binder.createConnection().getVCard(jid);
					if(vCardInfo == null) return;
					tvJid.setText(vCardInfo.jid);
					if(vCardInfo.name!=null)
						tvName.setText(vCardInfo.name);
					else
						tvName.setText(StringUtils.parseName(jid));

					if(vCardInfo.emailHome!= null)
						tvEmail.setText(vCardInfo.emailHome);
					if(vCardInfo.phoneNum != null)
						tvTel.setText(vCardInfo.phoneNum);
					if(vCardInfo.homeAddress != null)
						tvAddress.setText(vCardInfo.homeAddress);
					if(IM.getAvatar(vCardInfo.jid)!=null){
						ivcIcon.setImageDrawable(IM.getAvatar(jid));
					}
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(context, "����ʧ��");
				}
				mHandler.sendEmptyMessage(END_PROGRESS);
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
			loginTask.execute(binder);
		}

		public void onServiceDisconnected(ComponentName arg0) {
			binder = null;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context= this;
		//��ȡ��������jid
		Bundle bundle = getIntent().getExtras(); 
		contact = bundle.getParcelable("contact"); 
		jid = contact.getAccount();
		WHO_SEND = getIntent().getSerializableExtra("who_send").toString(); 
		Log.e(WHO_SEND+"��������", "ֵΪ��"+jid==null?"null":jid);

		initParent();
		
		personLayout = (LinearLayout)findViewById(R.id.tt_activity_user_info_person_layout);
		deptLayout = (LinearLayout)findViewById(R.id.tt_activity_user_info_dept_layout);
		personLayout.setVisibility(View.GONE);
		deptLayout.setVisibility(View.GONE);
		pbLoad = (ProgressBar)findViewById(R.id.tt_activity_user_info_bar);
		
		//�ж��ǻ�ȡ������Ϣ���ǲ�����Ϣ
		if(WHO_SEND.equals(PERSON)){
			setTitle(getResources().getString(R.string.fri_detail));
			initPersonLayout();
		}else if(WHO_SEND.equals(DEPT)){
			setTitle(contact.getName()+"��Ա");
			initDeptLayout();
		}
		
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnection, BIND_AUTO_CREATE);
	}

	private void initParent(){
		// �󶨲�����Դ(ע���������Դ��ʼ��֮ǰ)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_user_info, topContentView);

		//��ʼ������
		setLeftButton(R.drawable.tt_top_back);
		setLeftText(getResources().getString(R.string.top_left_back));
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	private void initDeptLayout(){
		deptLayout.setVisibility(View.VISIBLE);
		listDeptNumber = (NoScrollListview)findViewById(R.id.tt_activity_user_info_dept_list);
	}
	
	private void initPersonLayout(){
		personLayout.setVisibility(View.VISIBLE);
		tvName = (TextView)findViewById(R.id.tt_activity_user_info_name);
		tvJid = (TextView)findViewById(R.id.tt_activity_user_info_jid);
		tvEmail = (TextView)findViewById(R.id.tt_activity_user_info_email);
		tvTel = (TextView)findViewById(R.id.tt_activity_user_info_tel);
		tvAddress = (TextView)findViewById(R.id.tt_activity_user_info_address);
		ivcIcon = (ImageViewCircle)findViewById(R.id.tt_activity_user_info_icon);
		btnAddFri = (Button) findViewById(R.id.tt_activity_user_info_add_fri);
		
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
		btnAddFri.setOnClickListener(this);
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	public void onClick(View v) {
		switch(v.getId()){
		//���� ��һ��
		case R.id.left_btn:
		case R.id.left_txt:this.finish();break;
			//����Ϣ
//		case R.id.tt_activity_user_info_send_msg:
//			this.finish();
//			Intent intent = new Intent();
//			intent.setClass(this, ChatActivity.class);
//			intent.putExtra("contact", contact);
//			startActivity(intent);
//			break;
			//�Ӻ���
		case R.id.tt_activity_user_info_add_fri:
			showPopupWindow(v);
			break;
		}
	}

	private void showPopupWindow(View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this,android.R.style.Theme_Holo_Light_Dialog));
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View dialog_view = inflater.inflate(R.layout.tt_dialog_custom, null);
		LinearLayout layout = (LinearLayout)dialog_view.findViewById(R.id.tt_dialog_custom_layout);
		final EditText editText = new EditText(this);
		editText.setHint(R.string.tt_dialog_custom_input_note);
		layout.addView(editText);
		
		builder.setView(dialog_view);
		builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(!TextUtils.isEmpty(editText.getText().toString().trim()))
				{
					Message msg = mHandler.obtainMessage();
					msg.what = ADD_FRI;
					msg.obj = editText.getText().toString();
					mHandler.sendMessage(msg);
					dialog.dismiss();
					finish();
				}else{
					MyToast.showToastLong(context, "��ע����Ϊ��");
				}
			}
		});
		builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		builder.show();
	}
	
	class LoginTask extends LoginAsyncTask{

		protected void onPostExecute(Integer result) {
			switch(result){
			case IM.LOGIN_OK:
				//�ж��ǻ�ȡ������Ϣ���ǲ�����Ϣ
				if(WHO_SEND.equals(PERSON)){
					mHandler.sendEmptyMessage(GET_VCARD);
				}else if(WHO_SEND.equals(DEPT)){
					mHandler.sendEmptyMessage(GET_DEPT);
				}
				break;

			case IM.LOGIN_NET_ERROR:
				mHandler.sendEmptyMessage(END_PROGRESS);
				MyToast.showToastLong(context, "������δ����");
				break;

			case IM.LOGIN_PASSWORD_ERROR:
				mHandler.sendEmptyMessage(END_PROGRESS);
				break;
			}
		}
	}
}
