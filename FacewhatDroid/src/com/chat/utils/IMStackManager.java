package com.chat.utils;

import android.app.Activity;

/**
 * Created by zhujian on 15/1/23.
 */
public class IMStackManager {
    /**
     * Stack �ж�Ӧ��Activity�б�  ��Ҳ����д�� Stack<Activity>��
     */
    private static java.util.Stack<Activity> mActivityStack;
    private static IMStackManager mInstance;

    /**
     * @���� ��ȡջ������
     * @return ActivityManager
     */
    public static IMStackManager getStackManager() {
        if (mInstance == null) {
            mInstance = new IMStackManager();
        }
        return mInstance;
    }

    /**
     * �Ƴ�ջ��Activity
     */
    public void popActivity(Activity activity) {
        if (activity != null) {
            activity.finish();
            mActivityStack.remove(activity);
            activity = null;
        }
    }

    /**
     * ��õ�ǰջ��Activity
     */
    public Activity currentActivity() {
        //lastElement()��ȡ������Ԫ�أ�������ջ����Activity
        if(mActivityStack == null || mActivityStack.size() ==0){
            return null;
        }
        Activity activity = (Activity) mActivityStack.lastElement();
        return activity;
    }

    /**
     * ����ǰActivity����ջ��
     */
    public void pushActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new java.util.Stack();
        }
        mActivityStack.add(activity);
    }

    /**
     * ����ָ����clsss����ջ������������Activity
     * @clsss : ָ������
     */
    public void popTopActivitys(Class clsss) {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(clsss)) {
                break;
            }
            popActivity(activity);
        }
    }

    /**
     * ����ջ������Activity
     */
    public void popAllActivitys() {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            popActivity(activity);
        }
    }
}
