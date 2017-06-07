package com.chat.service.aidl;

import com.chat.service.aidl.VCardInfo;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.GroupItem;

interface IMXmppManager {
	//��������
	boolean connect();
	
	//��¼
	boolean login();
	
	//�Ͽ�����
	boolean disconnect();
	
	//��ȡ����״̬
	int getPresenceMode(String jid);
	
	//���ø���״̬
	void setPresenceMode(int mode);
	
	//������Ϣ
	void sendMessage(String sessionJID,String sessionName,String message,String type);
	
	//�޸�����
	boolean changePassword(String pwd);
	
	//��ȡ��ϵ����Ϣ
	VCardInfo getVCard(String jid);
	
	//��ȡͷ��
	byte[] getVCardIcon(String jid);
	
	//���ø�����Ϣ
	boolean setVCard(in VCardInfo info);
	
	//�����˻�
	java.util.List<Contact> searchUser(String jid);
	
	//��Ӧ���˵Ķ���
	void setPresence(String type,String to);
	
	//��Ӻ���
	void addFri(String jid,String name,in String[] groups);
	
	//ɾ������
	void deleteFri(String jid);
	
	//���ú��ѱ�ע
	void setRosterName(String fJid,String fName);
	
	//�����ļ�
	void sendFile(String userID,String path,String description);
	
	//�ж�ָ��JID�Ƿ�����
	boolean isOnlineByJID(String usernameJID);
	
	//�ж�ָ��JID�Ƿ��Ǻ���
	boolean isFriendByJID(String usernameJID);
	
	//��ȡ��
	java.util.List<GroupItem> getGroup();
	
}