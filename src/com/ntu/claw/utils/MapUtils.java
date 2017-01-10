package com.ntu.claw.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class MapUtils {

	public static LatLng getTraceCenter(List<LatLng> mList) {
		List<Double> list1 = new ArrayList<Double>();
		List<Double> list2 = new ArrayList<Double>();
		for (LatLng latLng : mList) {
			list1.add(latLng.latitude);
			list2.add(latLng.longitude);
		}
		double maxLat = Collections.max(list1);
		double maxLon = Collections.max(list2);
		double minLat = Collections.min(list1);
		double minLon = Collections.min(list2);
		LatLng ll = new LatLng((maxLat + minLat) / 2, (maxLon + minLon) / 2);
		return ll;
	}

	public static double getDistance(List<LatLng> mList) {
		double dis = 0;
		if (mList.size() > 1) {
			for (int i = 0; i < mList.size() - 1; i++) {
				dis += DistanceUtil.getDistance(mList.get(i), mList.get(i + 1));
			}
		}
		return dis;

	}
}
