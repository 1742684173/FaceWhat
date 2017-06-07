package com.chat.utils.pinyin;

import java.util.Comparator;  

import com.chat.service.aidl.Contact;

/**  
 *   
 * @author xiaanming  
 *  
 */ 
public class PinyinContactComparator implements Comparator<Contact> {  
 
    public int compare(Contact o1, Contact o2) {  
        //������Ҫ��������ListView��������ݸ���ABCDEFG...������  
        if (o2.getSort().equals("#")) {  
            return -1;  
        } else if (o1.getSort().equals("#")) {  
            return 1;  
        } else {  
            return o1.getSort().compareTo(o2.getSort());  
        }  
    }  
} 
