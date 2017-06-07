package com.chat.utils;

import com.chat.IM;
import com.chat.service.aidl.Contact;
import com.chat.ui.ChatActivity;
import com.chat.ui.MySetActivity;
import com.chat.ui.SearchActivity;
import com.chat.ui.SettingActivity;
import com.chat.ui.UserInfoActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextThemeWrapper;

public class IMUIHelper {
	private static IMUIHelper imUIHelper;
	
	private IMUIHelper(){}
	
	public static synchronized IMUIHelper instance(){
		if(imUIHelper == null)
			imUIHelper = new IMUIHelper();
		return imUIHelper;
	}

	//���������Ի���
	public void handleContactItemLongClick(Context ctx,final Contact contact){
		if(contact == null || ctx == null){
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(ctx,android.R.style.Theme_Holo_Light_Dialog));
		builder.setTitle("��Ϣ��ʾ");
		String[] items = new String[]{"���ñ�ע����ǩ","ɾ������"};
		builder.setItems(items,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int which) {
				switch(which){
					case 0:
						
						break;
					case 1:
						
						break;
				}
			}
		});
		AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
	}
	
	//��ת���û���Ϣҳ��
    public void openUserInfoActivity(Context ctx, String jid) {
        Intent intent = new Intent();
        intent.setClass(ctx, UserInfoActivity.class);
        intent.putExtra(IM.KEY_CONTACT_JID, jid);
        ctx.startActivity(intent);
    }
    
    // ��ת������ҳ��
    public void openChatActivity(Context ctx, String jid) {
    	Intent intent = new Intent();
        intent.setClass(ctx, ChatActivity.class);
        intent.putExtra(IM.KEY_CONTACT_JID, jid);
        ctx.startActivity(intent);
    }
    
    //ת����������
    public void openSearchActivit(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, SearchActivity.class);
    	ctx.startActivity(intent);
    }
    
    //�ܵ���Ϣ���ѽ���
    public void openSettingActivity(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, SettingActivity.class);
    	ctx.startActivity(intent);
    }
    
  //ǰ��������Ϣ
    public void openMyInfoSetActivity(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, MySetActivity.class);
    	ctx.startActivity(intent);
    }
    
}
