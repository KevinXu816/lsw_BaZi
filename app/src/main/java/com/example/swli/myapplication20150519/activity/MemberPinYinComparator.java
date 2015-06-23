package com.example.swli.myapplication20150519.activity;

import com.example.swli.myapplication20150519.model.Member;

import java.util.Comparator;

/**
 * Created by swli on 6/18/2015.
 */
public class MemberPinYinComparator implements Comparator<Member> {

    public int compare(Member o1, Member o2) {
        //������Ҫ��������ListView��������ݸ���ABCDEFG...������
        if (o2.getSortModel().getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortModel().getSortLetters().equals("#")) {
            return 1;
        } else {
            return o1.getSortModel().getSortLetters().compareTo(o2.getSortModel().getSortLetters());
        }
    }
}
