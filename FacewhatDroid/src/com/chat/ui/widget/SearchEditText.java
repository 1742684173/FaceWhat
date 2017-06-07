package com.chat.ui.widget;

import com.chat.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Դ��http://blog.csdn.net/xiaanming/article/details/11066685
 * @author Administrator
 *
 */
public class SearchEditText extends EditText implements OnFocusChangeListener,TextWatcher{
//	 ɾ����ť������
	private Drawable clearWordsImage;
//	�ؼ��Ƿ��н��� 
	private boolean hasFoucs;

	public SearchEditText(Context context) {
		this(context,null);
	}

	//	ʹ��AttributeSet����ɿؼ���Ĺ��캯��,���ڹ��캯���н��Զ���ؼ����б�����attrs.xml�е�������������.
	public SearchEditText(Context context,AttributeSet attrs){
		//���ﹹ�췽��Ҳ����Ҫ����������ܶ����Բ�����XML���涨��  
		this(context,attrs,android.R.attr.editTextStyle);
	}

	public SearchEditText(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
	}

	private void init(){
		//extView.setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom)
		//��������趨�������ĸ������ˣ��Ͱ�drawable�����ĸ���Ӧ��λ�á�
		//��������ı�������棬�����Ϳ����� getCompoundDrawables()[2]
		//�������������λ�ð�������д�Ϳ�����
		
		 //��ȡEditText��DrawableRight,����û���������Ǿ�ʹ��Ĭ�ϵ�ͼƬ 
		clearWordsImage = getCompoundDrawables()[2];
		if(clearWordsImage == null){
			clearWordsImage = getResources().getDrawable(R.drawable.tt_delete_bar);
		}
		clearWordsImage.setBounds(0, 0, clearWordsImage.getIntrinsicWidth(), clearWordsImage.getIntrinsicHeight());
		 //Ĭ����������ͼ�� 
		setClearIconVisible(false);
		//���ý���ı�ļ���  
		setOnFocusChangeListener(this);
		//����������������ݷ����ı�ļ���  
	
	}
	
	/** 
     * ��Ϊ���ǲ���ֱ�Ӹ�EditText���õ���¼������������ü�ס���ǰ��µ�λ����ģ�����¼� 
     * �����ǰ��µ�λ�� ��  EditText�Ŀ�� - ͼ�굽�ؼ��ұߵļ�� - ͼ��Ŀ��  �� 
     * EditText�Ŀ�� - ͼ�굽�ؼ��ұߵļ��֮�����Ǿ�������ͼ�꣬��ֱ�����û�п��� 
     */  
	@Override 
	public boolean onTouchEvent(MotionEvent event) { 
		if(event.getAction() == MotionEvent.ACTION_UP){
			if(getCompoundDrawables()[2] != null){
				boolean touchable = event.getX() > (getWidth()-getTotalPaddingRight())
						&& (event.getX() < ((getWidth()-getPaddingRight())));
				if(touchable){
					this.setText("");
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/** 
     * ��ClearEditText���㷢���仯��ʱ���ж������ַ��������������ͼ�����ʾ������ 
     */  
	@Override 
	public void onFocusChange(View v, boolean hasFocus) { 
		this.hasFoucs = hasFocus;
		if(hasFocus){
			setClearIconVisible(getText().length()>0);
		}else{
			setClearIconVisible(false);
		}
	}

	/** 
     * ��������������ݷ����仯��ʱ��ص��ķ��� 
     */  
	@Override 
	public void onTextChanged(CharSequence s, int start, int count, 
			int after) { 
		if (hasFoucs) {
			setClearIconVisible(s.length() > 0);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void afterTextChanged(Editable arg0) {

	}

	 /**
     * �������ͼ�����ʾ�����أ�����setCompoundDrawablesΪEditText������ȥ
     * @param visible
     */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible?clearWordsImage:null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1],
				right,
				getCompoundDrawables()[3]);
	}
	
	 /**
     * ���ûζ�����
     */
	public void setShakeAnimation(){
		this.setAnimation(shakeAnimation(5));
	}
	
	/**
     * �ζ�����
     * @param counts 1���ӻζ�������
     * @return
     */
	public static Animation shakeAnimation(int counts){
		Animation translateAnimation = new TranslateAnimation(0, 10,0,0);
		translateAnimation.setInterpolator(new CycleInterpolator(counts));
		translateAnimation.setDuration(1000);
		return translateAnimation;
	}
}
