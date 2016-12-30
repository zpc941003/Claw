package com.ntu.claw.utils;

import java.util.Comparator;

import com.ntu.claw.bean.CareIFriBean;

public class CareIPinyinComparator implements Comparator<CareIFriBean> {

	public int compare(CareIFriBean o1, CareIFriBean o2) {
		if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}

	}
}
