package com.chat.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.service.aidl.Contact;
import com.chat.utils.pinyin.PinyinContactComparator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * ��ȡ�����б�
 * @author Administrator
 *
 */
public class ContactAdapter extends CursorAdapter{
	//��ȡ��ϵ��
	private Cursor cursor;
	//��¼�Ƿ���ʾ
	private boolean isShowChecked;
	//��¼�Ƿ�ѡ��
	private HashMap<Integer, Boolean> isSelected;
	
	public ContactAdapter(Cursor cursor) {
		super(IM.im,cursor,FLAG_REGISTER_CONTENT_OBSERVER);
		//Ĭ������
		this.cursor = cursor;
		isShowChecked = false;
		isSelected = new HashMap<Integer, Boolean>(); 
		// ��ʼ������
		initDate();
	}

	// ��ʼ��isSelected������
	private void initDate() {
		if(cursor == null){
			return;
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			getIsSelected().put(i, false);
		}
	}
	
	//���ݷ����е������Ż�ø����е��׸�λ��
	public int getPositionForSection(int section) {
		int index = 0;
		for(int i=0;i<cursor.getCount();i++){
			cursor.moveToPosition(i);
			String sort = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.SORT));
			int firstCharacter = sort.charAt(0);
			if(firstCharacter == section){
				return index;
			}
			index ++;
		}
		return -1;
	}
	
	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		ContactHolder contactHolder = null;

		if(view == null){
			contactHolder = new ContactHolder();
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tt_item_contact, parent, false);

			contactHolder.name = (TextView) view.findViewById(R.id.tt_fragment_contact_name);
			contactHolder.sort = (TextView) view.findViewById(R.id.tt_fragment_contact_sort);
			contactHolder.avatar = (ImageView)view.findViewById(R.id.tt_fragment_contact_portrait);
			contactHolder.divider = view.findViewById(R.id.tt_fragment_contact_divider);
			contactHolder.cbMeet = (CheckBox)view.findViewById(R.id.tt_fragment_contact_checkBox);
			contactHolder.cbMeet.setTag(pos);
			view.setTag(contactHolder);
		}else{
			contactHolder = (ContactHolder)view.getTag();
		}

		Cursor c = (Cursor)getItem(pos);
		//�˻�
		String account = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.ACCOUNT));
		//ƴ������ĸ
		String sort = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.SORT));
		//��ע
		String name = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.NAME));
		//��¼״̬
		String status = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.STATUS));

		//����position��ȡ���������ĸ��char asciiֵ  
		int section = sort.charAt(0);  
		
		//�����ǰλ�õ��ڸ÷�������ĸ��Char��λ�� ������Ϊ�ǵ�һ�γ���  
		if(pos == getPositionForSection(section)){ 
			contactHolder.sort.setVisibility(View.VISIBLE);  
			contactHolder.sort.setText(sort);  
		}else{  
			contactHolder.sort.setVisibility(View.GONE);  
		}  
		
		//��������
		contactHolder.name.setText(name);
		
		//����״̬����ͷ���Ƿ�Ҵ���
		if(status == null || status.equals("����")){
			Log.e("ContactAdapter:",status+" ���� " + IM.getAvatar(account));
			Bitmap bitmap = IM.drawableToBitmap(IM.getAvatar(StringUtils.parseBareAddress(account)));
			contactHolder.avatar.setImageBitmap(IM.grey(bitmap));
		}else{
			contactHolder.avatar.setImageDrawable(IM.getAvatar(account));  
		}
		
		//����CheckBox�Ƿ���ʾ
		if(isShowCheck()){
			contactHolder.cbMeet.setVisibility(View.VISIBLE);
			contactHolder.cbMeet.setChecked(getIsSelected().get(pos));
		}else{
			contactHolder.cbMeet.setVisibility(View.GONE);
			contactHolder.cbMeet.setChecked(false);
		}
		
		return view;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

	public static class ContactHolder{
		View divider;
		TextView sort;
		TextView account;
		TextView name;
		ImageView avatar;
		public CheckBox cbMeet;
	}

	//�����Ƿ���ʾcheckbox
	public void setShowCheck(boolean isShowChecked){
		Log.e("contactAdapter","������ʾcheckbox");
		this.isShowChecked = isShowChecked;
		notifyDataSetChanged();
	}

	//��ȡ�Ƿ���ʾcheckbox��ֵ 
	public boolean isShowCheck(){
		return this.isShowChecked;
	}

	//��ȡcheckbox��ֵ
	public HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}
	
	//����checkbox��ֵ
	public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		this.isSelected = isSelected;
	}

}
