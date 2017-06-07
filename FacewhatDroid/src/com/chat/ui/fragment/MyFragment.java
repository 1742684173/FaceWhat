package com.chat.ui.fragment;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.DeptProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.base.TTBaseFragment;
import com.chat.ui.widget.ImageViewCircle;
import com.chat.ui.widget.MyToast;
import com.chat.utils.IMUIHelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class MyFragment extends TTBaseFragment implements OnClickListener{
	private View curView = null;
	private View exitView;//�˳�
	private View clearView;//������
	private View settingView;//֪ͨ����
	private ImageViewCircle icon;//ͷ��
	private TextView account;//�˻�
	private View userDetail;//�û�������Ϣ
	//	private TextView tvStatus;//����״̬

	private final static String[] STATUS_TEXT = new String[]{"����","����","æµ","����","�뿪","����"};
	private final static int[] STATUS_IMAGES = new int[]{R.drawable.tt_status_free_chat,
		R.drawable.tt_status_available,R.drawable.tt_status_dnd,R.drawable.tt_status_invisible,
		R.drawable.tt_status_away,R.drawable.tt_status_unavailable};

	//�޸�����
	private final static int CHANGE_PASSWORD = 1000;
	//����״̬
	private final static int SET_PRESENCE_STUTAS = 1001;
	//��ʾ״̬
	private final static int SET_SHOW_STATUS = 1002;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String str = msg.obj==null?"":msg.obj.toString();
			switch(msg.what){
			case CHANGE_PASSWORD:
				try {
					binder.createConnection().changePassword(str);
					MyToast.showToastLong(getActivity(), "�޸�����ɹ�");
				} catch (RemoteException e) {
					MyToast.showToastLong(getActivity(), "�޸�����ʧ��");
					e.printStackTrace();
				}
				break;
			case SET_PRESENCE_STUTAS:
				int status = Integer.parseInt(msg.obj.toString());
				try {
					binder.createConnection().setPresenceMode(status);
					//�����˻�
					account.setText("��������:"+StringUtils.parseName(IM.getString(IM.ACCOUNT_JID))+"("+STATUS_TEXT[status]+")");
				} catch (RemoteException e) {
					MyToast.showToastLong(getActivity(), "����ʧ��");
					e.printStackTrace();
				}
				break;
			case SET_SHOW_STATUS:
				String jidName = StringUtils.parseName(IM.getString(IM.ACCOUNT_JID));
				account.setText("��������:"+jidName+" ("+STATUS_TEXT[1]+")");
				break;
			}
		}
	};
	private IMXmppBinder binder;
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	// XMPP���ӷ��� 
	private class XmppServiceConnect implements ServiceConnection {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);
		initParent();
		init();

		return curView;
	}

	private void init() {
		account = (TextView)curView.findViewById(R.id.tt_fragment_my_account);//�ʻ�
		userDetail = curView.findViewById(R.id.tt_fragment_my_container);//���������Ϣ����
		exitView = curView.findViewById(R.id.tt_fragment_my_exitPage);//�˳���¼
		clearView = curView.findViewById(R.id.tt_fragment_my_clearPage);//�����ڴ�
		settingView = curView.findViewById(R.id.tt_fragment_my_settingPage);//������Ϣ������

		userDetail.setOnClickListener(this);
		exitView.setOnClickListener(this);
		clearView.setOnClickListener(this);
		settingView.setOnClickListener(this);

		Message msg = mHandler.obtainMessage();
		msg.what = SET_SHOW_STATUS;
		mHandler.sendMessage(msg);
	}

	private void initParent(){
		onSearchDataReady();
		setTopTitle(getActivity().getString(R.string.chat_title));// ���ö���������
		//��������
		setTopSearchButton(R.drawable.search);
		topRightBtn.setOnClickListener(this);
	}

	public void onStart() {
		super.onStart();
		getActivity().bindService(new Intent(getActivity(), IMService.class), serviceConnect, getActivity().BIND_AUTO_CREATE);
	}

	public void onDestroy(){ 
		super.onDestroy();
		getActivity().unbindService(serviceConnect);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//ǰ��������Ϣ
		case R.id.tt_fragment_my_container:
			IMUIHelper.instance().openMyInfoSetActivity(getActivity());
			break;
			//�˳�
		case R.id.tt_fragment_my_exitPage:
			AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
			builder.setTitle("ȷ���˳�");
			builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					getActivity().finish();
					dialog.dismiss();
				}
			});

			builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
			builder.show();
			break;
			//�����ڴ�
		case R.id.tt_fragment_my_clearPage:
//			IM.clearAvatar(IM.ALL_FILE_PATH);
			getActivity().getContentResolver().delete(SMSProvider.SMS_URI, null, null);
			getActivity().getContentResolver().delete(DeptProvider.DEPT_URI, null, null);
			MyToast.showToastLong(getActivity(), "�������");
			break;
			// ��ת������ҳ��
		case R.id.tt_fragment_my_settingPage:
			IMUIHelper.instance().openSettingActivity(getActivity());
			break;
		case R.id.right_btn:
			PopupMenu popup = new PopupMenu(getActivity(),v);
			getActivity().getMenuInflater().inflate(R.menu.tt_popumenu_my,popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

				@SuppressLint("ResourceAsColor")
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					//�Ի��� 
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light_Dialog));

					LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
					View dialog_view = inflater.inflate(R.layout.tt_dialog_custom, null);
					LinearLayout layout = (LinearLayout)dialog_view.findViewById(R.id.tt_dialog_custom_layout);

					switch(item.getItemId()){
					//����״̬
					case R.id.menu_set_status:
						//��ѡ��ť��
						final RadioGroup rdGroup = new RadioGroup(getActivity());

						//����
						final RadioButton rdBtnChat = new RadioButton(getActivity());
						rdBtnChat.setText("����");
						rdBtnChat.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnChat);

						//����
						final RadioButton rdBtnAvailable = new RadioButton(getActivity());
						rdBtnAvailable.setText("����");
						rdBtnAvailable.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnAvailable);

						//æµ
						final RadioButton rdBtnDnd = new RadioButton(getActivity());
						rdBtnDnd.setText("æµ");
						rdBtnDnd.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnDnd);

						//����
						final RadioButton rdBtnHide = new RadioButton(getActivity());
						rdBtnHide.setText("����");
						rdBtnHide.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnHide);

						//�뿪
						final RadioButton rdBtnAway = new RadioButton(getActivity());
						rdBtnAway.setText("�뿪");
						rdBtnAway.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnAway);

						//����
						final RadioButton rdBtnOffline = new RadioButton(getActivity());
						rdBtnOffline.setText("����");
						rdBtnOffline.setTextColor(getResources().getColor(R.color.Black));
						rdGroup.addView(rdBtnOffline);

						layout.addView(rdGroup);

						builder.setView(dialog_view);
						final AlertDialog dialog = builder.show();

						rdGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
							@Override
							public void onCheckedChanged(RadioGroup radioGroup,int id) {
								Message msg = mHandler.obtainMessage();
								msg.what = SET_PRESENCE_STUTAS;
								//����
								if(rdBtnChat.isChecked()){
									msg.obj = 0;
								}
								//����
								if(rdBtnAvailable.isChecked()){
									msg.obj = 1;
								}
								//æµ
								if(rdBtnDnd.isChecked()){
									msg.obj = 2;
								}
								//����
								if(rdBtnHide.isChecked()){
									msg.obj = 3;
								}
								//�뿪
								if(rdBtnAway.isChecked()){
									msg.obj = 4;
								}
								//����
								if(rdBtnOffline.isChecked()){
									msg.obj = 5;
								}
								mHandler.sendMessage(msg);
								dialog.dismiss();
							}
						});

						break;
						//�޸�����
					case R.id.menu_modify_password:
						final EditText etPwd1 = new EditText(getActivity());
						etPwd1.setHint(getActivity().getResources().getString(R.string.tt_sign_input_pwd1));
						layout.addView(etPwd1);

						final EditText etPwd2 = new EditText(getActivity());
						etPwd2.setHint(getActivity().getResources().getString(R.string.tt_sign_input_pwd2));
						layout.addView(etPwd2);

						builder.setView(dialog_view);
						builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(!TextUtils.isEmpty(etPwd1.getText().toString().trim()) &&
										!TextUtils.isEmpty(etPwd2.getText().toString().trim())){
									if(etPwd1.getText().toString().trim().equals(etPwd2.getText().toString().trim())){
										Message msg = mHandler.obtainMessage();
										msg.what = CHANGE_PASSWORD;
										msg.obj = etPwd1.getText().toString();
										mHandler.sendMessage(msg);

									}else{
										MyToast.showToastLong(getActivity(), "�������벻һ��");
									}
								}else{
									MyToast.showToastLong(getActivity(), "���벻��Ϊ��");
								}
								dialog.dismiss();
							}
						});
						builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
							}
						});
						builder.show();
						break;
						//���Һ���
//					case R.id.menu_find:
//						IMUIHelper.instance().openSearchActivit(getActivity());
//						break;
					}
					return true;
				}
			});
			popup.show();
			break;
		}
	}
}
