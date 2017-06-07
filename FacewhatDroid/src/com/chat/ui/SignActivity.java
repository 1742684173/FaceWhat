package com.chat.ui;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import com.chat.IM;
import com.chat.R;
import com.chat.ui.base.TTBaseActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignActivity extends TTBaseActivity implements View.OnClickListener{
	//ע�������˻�
	private EditText etAccount;
	//����
	private EditText etPwd1;
	//ȷ������
	private EditText etPwd2;
	//������ʾ
	private TextView tvError;
	//ע���ύ
	private Button btnSign;
	
	private ConnectionConfiguration connectionConfig;
	private XMPPConnection connection;
	private AccountManager accountManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// �󶨲�����Դ(ע���������Դ��ʼ��֮ǰ)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_sign, topContentView);
		
		initParent();
		init();
	}

	private void initParent(){
		setLeftText(getString(R.string.tt_sign_back));
		setLeftButton(R.drawable.tt_back_btn);
		
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}
	
	private void init(){
		btnSign = (Button)findViewById(R.id.tt_sign_commit);
		etAccount = (EditText)findViewById(R.id.tt_sign_account);
		etPwd1 = (EditText)findViewById(R.id.tt_sign_pwd1);
		etPwd2 = (EditText)findViewById(R.id.tt_sign_pwd2);
		tvError = (TextView)findViewById(R.id.tt_sign_error);
		
		btnSign.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//������һ��
		case R.id.left_btn:
		case R.id.left_txt:
			this.finish();
			break;
		//�����˻�
		case R.id.tt_sign_commit:
			String account,pwd1,pwd2;
			account = etAccount.getText().toString();
			if(TextUtils.isEmpty(account)){
				tvError.setText("�˻�����Ϊ��");
				return;
			}
			
			pwd1 = etPwd1.getText().toString();
			if(TextUtils.isEmpty(pwd1)){
				tvError.setText("���벻��Ϊ��");
				return;
			}
			
			pwd2 = etPwd2.getText().toString();
			if(!pwd2.equals(pwd1)){
				tvError.setText("�������벻һ��");
				return;
			}
			
			new AsyncTask<String, Void, Boolean>(){
				private ProgressDialog dialog;
				private String account;
				protected void onPreExecute() {
					dialog = ProgressDialog.show(SignActivity.this, "", getString(R.string.tt_sign_wait));
				}

				protected Boolean doInBackground(String... strings) {
					connection = new XMPPConnection(initConnectionConfig());
					try {
						connection.connect();
						accountManager = new AccountManager(connection);
						accountManager.createAccount(strings[0], strings[1]);
						account = strings[0];
						return true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}

				protected void onPostExecute(Boolean result) {
					dialog.dismiss();
					if(result){
						Intent data = new Intent();
						data.putExtra("sign_account", account);
						setResult(Activity.RESULT_OK, data);
						connection.disconnect();
						finish();
					}else{
						tvError.setText("ע��ʧ��");
					}
				}

			}.execute(account, pwd1);
	
			break;
		}
		
	}
	
	private ConnectionConfiguration initConnectionConfig() {
		if (connectionConfig == null) {
			connectionConfig = new ConnectionConfiguration(IM.HOST, IM.PORT);
			connectionConfig.setDebuggerEnabled(true);
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		return connectionConfig;
	}
}
