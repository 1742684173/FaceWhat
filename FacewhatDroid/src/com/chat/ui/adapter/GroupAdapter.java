package com.chat.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.db.provider.DeptProvider;
import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.UserItem;
import com.chat.ui.widget.ImageViewCircle;

import com.chat.IM;
import com.chat.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class GroupAdapter extends BaseExpandableListAdapter{
	private List<GroupItem> groupList;
	private List<UserItem> userList;
	private Map<String,GroupItem> map;
	private LayoutInflater mLayoutInflater = null;

	public GroupAdapter(Context ctx,List<GroupItem> groupList){
		this.groupList = groupList;

		mLayoutInflater = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	//��ȡָ�����б�ָ�����б������б�����
	@Override
	public UserItem getChild(int groupPos, int childPos) {
		userList = groupList.get(groupPos).getUserItem();
		return userList.get(childPos);
	}

	@Override
	public long getChildId(int grounpPos, int childPos) {
		return childPos;
	}

	//�����ض��鴦�����б�����
	@Override
	public int getChildrenCount(int groupPos) {
		userList = groupList.get(groupPos).getUserItem();
		return userList.size();
	}

	//�÷�������ÿ����ѡ������
	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView,
			ViewGroup parent) {
		ChildHolder holder = null;
		if(convertView == null ){
			convertView = mLayoutInflater.inflate(R.layout.tt_item_group_child, null);
			holder = new ChildHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (ChildHolder)convertView.getTag();
		}

		UserItem userItem = (UserItem)getChild(groupPos,childPos);
		String userNickName = userItem.getUserNickName();
		String userName = userItem.getUserName();
		String userJid = userItem.getUserJid();

		if(userNickName != null){
			holder.name.setText(userNickName);
		}else{
			holder.name.setText(userName);
		}

		holder.icon.setImageDrawable(IM.getAvatar(StringUtils.parseBareAddress(userJid)));  

		return convertView;
	}

	//��ȡָ����λ�ô�������
	@Override
	public GroupItem getGroup(int groupPos) {
		return groupList.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		return groupPos;
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupHolder holder = null;
		if(convertView == null){
			convertView = mLayoutInflater.inflate(R.layout.tt_item_group_parent, null);
			holder = new GroupHolder(convertView);
			convertView.setTag(holder);
		}else{
			holder = (GroupHolder)convertView.getTag();
		}

		GroupItem info = (GroupItem)getGroup(groupPos);

		holder.name.setText(info.getDisplayName()+"("+getChildrenCount(groupPos)+")");

		return convertView;
	}

	// �Ƿ�ָ��������ͼ��������ͼ��ID��Ӧ�ĺ�̨���ݸı�Ҳ�ᱣ�ָ�ID�� 
	@Override
	public boolean hasStableIds() {
		return true;
	}

	// ָ��λ�õ�����ͼ�Ƿ��ѡ��  
	@Override
	public boolean isChildSelectable(int groupPos, int childPos) {
		return true;
	}

	class ChildHolder{
		ImageViewCircle icon;
		TextView name;
		TextView accout;

		public ChildHolder(View v){
			icon = (ImageViewCircle)v.findViewById(R.id.tt_item_group_child_icon);
			name = (TextView)v.findViewById(R.id.tt_item_group_child_name);
			accout = (TextView)v.findViewById(R.id.tt_item_group_child_id);
		}
	}

	class GroupHolder{
		TextView name;
		TextView accout;

		public GroupHolder(View v){
			name = (TextView)v.findViewById(R.id.tt_item_group_parent_name);
			accout = (TextView)v.findViewById(R.id.tt_item_group_parent_id);
		}
	}
}
