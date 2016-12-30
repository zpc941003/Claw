package com.ntu.claw.utils;

import java.util.Comparator;

import com.ntu.claw.bean.ICareFriBean;

public class ICarePinyinComparator implements Comparator<ICareFriBean> {

	public int compare(ICareFriBean o1, ICareFriBean o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
