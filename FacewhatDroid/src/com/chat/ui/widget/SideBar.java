package com.chat.ui.widget;

import com.chat.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SideBar extends View {
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    public static String[] b = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"
    };
    private int sel = -1;
    private Paint paint = new Paint();

    private TextView textDialog;

    public void setTextView(TextView textview) {
        this.textDialog = textview;
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // ��ȡ����ı䱳����ɫ.
        int height = getHeight();// ��ȡ��Ӧ�߶�
        int width = getWidth(); // ��ȡ��Ӧ���
        int singleHeight = height / (b.length + 1);// ��ȡÿһ����ĸ�ĸ߶�

        if (b.length > 0) {
            BitmapDrawable bmpDraw = (BitmapDrawable) getResources().getDrawable(
                    R.drawable.tt_contact_side_search);
            Bitmap bmp = bmpDraw.getBitmap();
            float left = width / 2 - paint.measureText(b[0]) / 2 - 4;
            canvas.drawBitmap(bmp, left, 0, paint);
        }
        
        for (int i = 0; i < b.length; i++) {
            paint.setColor(Color.parseColor("#666666"));
            //paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setAntiAlias(true);
           
            paint.setTextSize(21);
            // ѡ�е�״̬
            if (i == sel) {
                paint.setColor(Color.WHITE);
                paint.setFakeBoldText(true);
            }
            // x��������м�-�ַ�����ȵ�һ��.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight+singleHeight/2;

            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// ���y����
        final int oldChoose = sel;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
     // ���y������ռ�ܸ߶ȵı���*b����ĳ��Ⱦ͵��ڵ��b�еĸ���.
        final int c = (int) (y / getHeight() * b.length);

        switch (action) {
            case MotionEvent.ACTION_UP:
                setBackgroundDrawable(new ColorDrawable(0x00000000));
                sel = -1;//
                invalidate();
                if (textDialog != null) {
                    textDialog.setVisibility(View.INVISIBLE);
                }
                break;

            default:
//                setBackgroundResource(R.drawable.sidebar_background);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (textDialog != null) {
                            textDialog.setText(b[c]);
                            textDialog.setVisibility(View.VISIBLE);
                        }

                        sel = c;
                        invalidate();
                    }
                }

                break;
        }
        return true;
    }

    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);
    }

}

