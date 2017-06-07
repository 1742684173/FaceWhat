package com.chat.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ContactProvider extends ContentProvider{
	private final static String AUTHORITY = ContactProvider.class.getCanonicalName();

	/**��ϵ�����ݿ�*/
	private final static String DB_NAME =  "contact.db";

	/**��ϵ��*/
	private final static String CONTACT_TABLE = "contact";

	/**��ϵ����*/
	private final static String CONTACT_GROUP_TABLE = "group";

	/**���ݿ�汾*/
	private final static int DB_VERSION = 1;

	/**��ϵ�� uri*/
	public final static Uri CONTACT_URI = Uri.parse("content://"+AUTHORITY+"/"+CONTACT_TABLE);

	/**��ϵ�� uri*/
	public final static Uri CONTACT_GROUP_URI = Uri.parse("content://"+AUTHORITY+"/"+CONTACT_GROUP_TABLE);

	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private static final UriMatcher URI_MATCHER;

	/**UriMatcherƥ��ֵ*/
	public static final int CONTACTS = 1;
	public static final int GROUPS = 2;

	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, CONTACT_TABLE, CONTACTS);
		URI_MATCHER.addURI(AUTHORITY, CONTACT_GROUP_TABLE, GROUPS);
	}

	@Override
	public boolean onCreate() {
		dbHelper  = new ContactDatabaseHelper(getContext());
		return (dbHelper == null) ?false:true;
	}

	/**����uri��ѯ��selection������ƥ���ȫ����¼����projection����һ�������б�����ֻѡ��ָ����������*/
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
		Log.e("SQLite��","�����ѯ ");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		db = dbHelper.getReadableDatabase();
		Cursor ret = null;

		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			qb.setTables(CONTACT_TABLE);
			ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case GROUPS:
			
			break;
		}

		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}


	/**����uri������values*/
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.e("SQLite��","������� ");
		db = dbHelper.getWritableDatabase();
		Uri result = null;
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			long rowId = db.insert(CONTACT_TABLE, ContactColumns.ACCOUNT, values);
			result = ContentUris.withAppendedId(uri, rowId);
			break;
		default:break;
		}
		if(result!=null){
			getContext().getContentResolver().notifyChange(result,null);
		}
		return result;
	}


	/** ����Uriɾ��selection������ƥ���ȫ����¼ */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite��","����ɾ�� ");
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			count = db.delete(CONTACT_TABLE, selection, selectionArgs);
			break;
		default:break;
		}
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	/**����uri�޸�selection������ƥ���ȫ����¼*/
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite��","������� ");
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			count = db.update(CONTACT_TABLE, values, selection, selectionArgs);
			break;
		default:break;
		}
		Log.e("SQLite��","���½�� " + count);
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			
		}
		return count;
	}
	
	/**
	 *�÷������ڷ��ص�ǰUri����������ݵ�MIME���� 
	 */
	@Override
	public String getType(Uri uri) {
		return null;
	}

	/**��ϵ����Ϣ���ݿ�*/
	private class ContactDatabaseHelper extends SQLiteOpenHelper{

		public ContactDatabaseHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + CONTACT_TABLE  + "("
					+ ContactColumns._ID+ " INTEGER PRIMARY KEY,"
					+ ContactColumns.AVATAR + " BLOB,"
					+ContactColumns.SORT + " TEXT,"
					+ContactColumns.NAME + " TEXT,"
					+ContactColumns.JID + " TEXT,"
					+ContactColumns.TYPE + " TEXT,"
					+ContactColumns.STATUS + " TEXT,"
					+ContactColumns.ACCOUNT + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+ CONTACT_TABLE);
			onCreate(db);
		}
	}

	/**
	 * ��ϵ������
	 *BaseColumns���Զ��������� �����������ֶ� _id,_count,��������չ
	 */
	public static class ContactColumns implements BaseColumns{
		//�û�ͷ��
		public static final String AVATAR = "avatar";
		//�û���ע
		public static final String NAME = "name";
		//���˵ĺ���
		public static final String ACCOUNT = "account";
		//���ѵ�����ĸ
		public static final String SORT = "sort";
		//����
		public static final String JID = "jid";
		//��������(��Ӻ���ʱ��both��to��from)
		public static final String TYPE = "type";
		//����״̬�����߻������� ��
		public static final String STATUS = "status";
		

	}
}
