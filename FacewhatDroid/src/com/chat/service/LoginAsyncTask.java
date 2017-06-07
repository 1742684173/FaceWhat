package com.chat.service;

import com.chat.IM;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.IMXmppManager;
import com.chat.utils.NetUtil;

import android.os.AsyncTask;
import android.os.RemoteException;

public class LoginAsyncTask extends AsyncTask<IMXmppBinder,Void,Integer>{

	@Override
	protected Integer doInBackground(IMXmppBinder... binder) {
		try {
			IMXmppManager connection = binder[0].createConnection();
			if(IM.mNetWorkState == NetUtil.NETWORN_NONE){
				return IM.LOGIN_NET_ERROR;
			}
			//���ӳɹ�
			if(connection.connect()){
				//��¼�ɹ�
				if(connection.login()){
					return IM.LOGIN_OK;
				//��¼ʧ��
				}else{
					return IM.LOGIN_PASSWORD_ERROR;
				}
			//����ʧ��
			}else{
				return IM.LOGIN_SERVER_ERROR;
			}
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
