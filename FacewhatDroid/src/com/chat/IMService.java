package com.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;

import com.chat.service.XmppBinder;
import com.chat.service.XmppManager;
import com.chat.service.aidl.IMXmppBinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * ����XMPP�ĺ�̨
 * @author Administrator
 *
 *startService:һ���ڳ����ڲ�ʹ�ã����ܷ���Service��ҵ�񷽷�
 *bindService:�����ʱʹ��,ͨ��AIDL�������ݴ��ݣ���ȡbinder����󣬷���service����
 *aidl�ڶ�����������service���н���
 */
public class IMService extends Service{
	private XmppManager connection;
	private ConnectionConfiguration connectionConfig;
	private IMXmppBinder.Stub binder;

	public void onCreate() {
		super.onCreate();
		Log.e("IMService:onCreate()"," is here");
		binder = new XmppBinder(this);
	}

	public void onDestroy() {
		super.onDestroy();
		connection = null;
		Log.e("IMService:"," onDestroy()");
		System.exit(0);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("IMService:onStartCommand()"," is here");
		try {
			createConnection().connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public IBinder onBind(Intent arg0) {
		Log.e("IMService:onBind()"," is here");
		return binder;
	}

	//��ʼ��ConnectionConfiguration
	private ConnectionConfiguration initConnectionConfig() {
		if (connectionConfig == null) {
//			try {  
//                Class.forName("org.jivesoftware.smack.ReconnectionManager");  
//            } catch (Exception e1) {  
//            }    
			connectionConfig = new ConnectionConfiguration(IM.getString(IM.HOST), IM.PORT);
			connectionConfig.setReconnectionAllowed(true);//�����Զ�����
			connectionConfig.setSendPresence(false);//��Ҫ���߷������Լ���״̬��Ϊ�˻�ȡ������Ϣ
			connectionConfig.setDebuggerEnabled(true);
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		return connectionConfig;
	}


	//����XmppManager
	public XmppManager createConnection() {
		if (connection == null) {
			Log.e("imservice:createConnection",IM.getString(IM.ACCOUNT_JID));
			//ע�������account��ʽ������¼����һ��
			String account = StringUtils.parseName(IM.getString(IM.ACCOUNT_JID));
			String pwd = IM.getString(IM.ACCOUNT_PASSWORD);
			connection = new XmppManager(initConnectionConfig(), account,
					pwd, this);
		}
		return connection;
	}

}
