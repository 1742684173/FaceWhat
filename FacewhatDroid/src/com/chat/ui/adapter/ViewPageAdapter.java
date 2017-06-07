package com.chat.ui.adapter;



import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import java.util.List;

public class ViewPageAdapter extends PagerAdapter {
    private List<GridView> mListViews;

    public ViewPageAdapter(List<GridView> mListViews) {
        this.mListViews = mListViews;// ���췽�������������ǵ�ҳ���������ȽϷ��㡣
    }

    @Override
    public int getCount() {
        return mListViews.size();// ����ҳ��������
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            container.removeView(mListViews.get(position));// ɾ��ҳ��
        } catch (Exception e) {
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            container.addView(mListViews.get(position), 0);// ���ҳ��
            return mListViews.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;// �ٷ���������д
    }

}
