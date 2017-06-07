package com.chat.service;

import android.os.RemoteException;
import android.util.Log;

import com.chat.IMService;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.IMXmppManager;

// ʵ����aidl�ķ���
public class XmppBinder extends IMXmppBinder.Stub{
	private IMService imService;
	
	/**����service*/
	public XmppBinder(IMService imService){
		this.imService = imService;
	}
	
	@Override
	public IMXmppManager createConnection() throws RemoteException {
		return imService.createConnection();
	}

}
