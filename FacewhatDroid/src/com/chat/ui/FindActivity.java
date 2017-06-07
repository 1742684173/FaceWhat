package com.chat.ui;

import org.jivesoftware.smack.util.StringUtils;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.adapter.FindAdapter;
import com.chat.ui.adapter.FindAdapter.OnItemFindClick;
import com.chat.ui.base.TTBaseActivity;

public class FindActivity  extends TTBaseActivity implements 
OnClickListener,
OnItemFindClick{
	private Context context;
	private ViewGroup contentView;
	private ListView list;
	private FindAdapter findAdapter;
	private ContentObserver coPresence;
	private Cursor cursor;

	private final static int ACCEPT = 1000;
	private final static int REFUSE = 1001;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String jid = msg.obj.toString();
			switch(msg.what){
			case ACCEPT:
				try {
					//�ȴ�Ӧ�Է��Ķ�������
					binder.createConnection().setPresence(IM.PRESENCE_TYPE[1],jid);

					//Ȼ���ٶ��ĶԷ�
					binder.createConnection().addFri(
							jid,
							StringUtils.parseName(jid), 
							new String[]{"Friends"});

					ContentValues values = new  ContentValues();
					values.put(PresenceProvider.PresenceColumns.TYPE, IM.PRESENCE_TYPE[1]);
					getContentResolver()
					.update(PresenceProvider.PRESENCE_URI,
							values,
							PresenceProvider.PresenceColumns.FROM + " = ?",
							new String[]{jid});

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			case REFUSE:
				try {
					//�ܾ��Է��Ķ�������,�ָ�ԭ״unsubscribe
					binder.createConnection().setPresence(IM.PRESENCE_TYPE[2],jid);
					
					ContentValues values = new  ContentValues();
					values.put(PresenceProvider.PresenceColumns.TYPE, IM.PRESENCE_TYPE[3]);
					getContentResolver()
					.update(PresenceProvider.PRESENCE_URI,
							values,
							PresenceProvider.PresenceColumns.FROM + " = ?",
							new String[]{jid});
				} catch (Exception e) {
					Log.e("findActivity-->","refuse "+e.toString());
					e.printStackTrace();
				}
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initPatent();
		init();
		initData();
	}

	private void initData(){
		//���ݹ۲���
		coPresence = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				//��δ����Ϣ�ĳ��Ѷ�
				ContentValues values = new  ContentValues();
				values.put(PresenceProvider.PresenceColumns.READ, "1");
				getContentResolver()
				.update(PresenceProvider.PRESENCE_URI,values,
						PresenceProvider.PresenceColumns.READ + " = ?",
						new String[]{"0"});
				
				//��ѯ���еĶ�����Ϣ
				cursor = getContentResolver()
						.query(PresenceProvider.PRESENCE_URI,null,null,null,null);
				if(cursor==null || cursor.getCount()<=0){
					finish();
				}
				findAdapter.changeCursor(cursor);
			}
		};
		//ע�����ݹ۲���
		getContentResolver()
		.registerContentObserver(PresenceProvider.PRESENCE_URI,true, coPresence);
		coPresence.onChange(false);
	}

	private void initPatent(){
		//��ʼ������
		contentView = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.tt_activity_find, topContentView);
		setLeftButton(R.drawable.tt_top_back);
		setLeftText(getResources().getString(R.string.top_left_back));
		setTitle("������֤");
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	private void init(){
		context = this;
		list = (ListView)contentView.findViewById(R.id.tt_activity_find_list);
		findAdapter = new FindAdapter(cursor);
		findAdapter.setOnItemFindClick(this);
		list.setAdapter(findAdapter);
	}

	public void onStart(){
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect, BIND_AUTO_CREATE);
	}

	public void onDestroy(){
		super.onDestroy();
		getContentResolver().unregisterContentObserver(coPresence);
		unbindService(serviceConnect);
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.left_btn://���� ��һ��
		case R.id.left_txt:this.finish();break;
		}
	}

	//���ܺ�������
	public void onAcceptClick(View v) {
		Log.e("findActivity-->","accept");
		final Cursor cursor = (Cursor)findAdapter.getItem((Integer) v.getTag());

		String jid = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.FROM));
		Message msg = mHandler.obtainMessage();
		msg.what = ACCEPT;
		msg.obj = jid;
		mHandler.sendMessage(msg);
	}

	//�ܾ���������
	public void onRefuseClick(View v) {
		Log.e("findActivity-->","refuse");
		final Cursor cursor = (Cursor)findAdapter.getItem((Integer) v.getTag());
		
		String jid = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.FROM));
		Message msg = mHandler.obtainMessage();
		msg.what = REFUSE;
		msg.obj = jid;
		mHandler.sendMessage(msg);
	}
}
