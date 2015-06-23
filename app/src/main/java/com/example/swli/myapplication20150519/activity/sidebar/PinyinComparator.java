package com.example.swli.myapplication20150519.activity.sidebar;

import java.util.Comparator;

/**
 * Created by swli on 6/18/2015.
 */
public class PinyinComparator implements Comparator<SortModel> {

    public int compare(SortModel o1, SortModel o2) {
        //������Ҫ��������ListView��������ݸ���ABCDEFG...������
        if (o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
