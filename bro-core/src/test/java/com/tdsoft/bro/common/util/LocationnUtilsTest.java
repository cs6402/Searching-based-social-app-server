package com.tdsoft.bro.common.util;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LocationnUtilsTest {
	// ylength 572500
	// xlength 163877838750
	
	// min point = 0 , -180.0 -90.0
	// 
	// max point = 163878411250 180.0 90.0
	
	@Test
	public void test() throws IOException {
//		double x = 22.3213d;
//		double y = 114.22093d;
		double x =12;
		double y = 123;
		double[] mer = LocationUtils.convertLatLonToMercator(x, y);
		long point = LocationUtils.convertMecratorToPoint(mer[0], mer[1]);
		Set<Long> range = LocationUtils.getRange(point, 2);
		System.out.println(range);
//		System.out.println(testMax() - testMin());
//		System.out.println(testMin());
//		cal();
		
		// 1 point +-1, point +- 572500, point +-1 + 572500, point +-1 - 572500
		// 2 point +
	}
	// 整個邊長
	int leng = 5;
	// 最大點
	long MAX = 29;
	// 目前可以用calHRowWRow，已基本測試過

	public void calHRowWRow() {
		// 圓周長
		int idx = 1;
		// 中心點
		long point = 4l;
		Set<Long> list = new TreeSet<Long>();
		list.add(point);
		addW(list, point, idx);
		// 以中心點開始計算x軸左右
		for (int i = 1; i <= idx; i++) {
			// 左邊
			long p = point + leng * i;
			if (!(p > MAX || p < 0)) {
				list.add(p);
				addW(list, p, idx);
			}
			// 右邊
			p = point - leng * i;
			if (!(p > MAX || p < 0)) {
				list.add(p);
				addW(list, p, idx);
			}
		}
		// 以中心點開始計算y軸上下
//		for (int i = 1; i <= idx; i++) {
//			// 上
//			long p = point + i;
//			// 避開突破上界
//			if (!(p > MAX || p < 0) && p % leng != 0) {
//				list.add(p);				
//			}
//			// 下
//			p = point - i;
//			// 避開突破下界
//			if (!(p > MAX || p < 0) && (p + 1) % leng != 0) {
//				list.add(p);				
//			}
//		}
		System.out.println(list);
	}
	
	/**
	 * 將x軸上下填入
	 * @param list
	 * @param point
	 * @param idx
	 */
	private void addW(Set<Long> list, long point, int idx) {
		for (int i = 1; i <= idx; i++) {
			// 上 
			long p = point + i;
			// 避開突破上界
			if (!(p > MAX || p < 0) && p % leng != 0) {
				list.add(p);				
			}
			// 下
			p = point - i;
			// 避開突破下界
			if (!(p > MAX || p < 0) && (p + 1) % leng != 0) {
				list.add(p);				
			}
		}
	}
	@Deprecated
	public void cal() {
//		int leng = 572500;
		
		int idx = 2;
		long point = 13l;
//		double x = 22.3213d;
//		double y = 114.22093d;
//		double[] mer = LocationUtils.convertLatLonToMercator(x, y);
//		long point = LocationUtils.convertMecratorToPoint(mer[0], mer[1]);
		
		Set<Long> list = new TreeSet<Long>();
		if (point / leng != 0) {
			addToSet(list, point, idx);
			for (int i = 1; i <= idx; i++) {
				long p = point + leng * i;
				addToSet(list, p, idx);
				p = point - leng * i;
				addToSet(list, p, idx);
			}
		} else {
			addToSet(list, point, idx);
			for (int i = 1; i <= idx; i++) {
				long p = point + leng * i;
				addToSet(list, p, idx);
				p = point - leng * i;
				addToSet(list, p, idx);
			}
		}
		System.out.println(list);
	}
	@Deprecated
	public void addToSet(Set<Long> list, long p, int idx) {
		if (!(p > 163878411250L || p < 0)) {
			list.add(p);				
		}
		for (int i = 1; i < idx; i++) {
			long m = p + i;
			if (!(m > 163878411250L || m < 0) && (p % leng ) != 0) {
				list.add(m);				
			}

			long n = p - i;
			if (!(n > 163878411250L || n < 0) && (p % leng ) != 0) {
				list.add(n);	
			}
		}
	}
	
	
//	@Test
	public long testMax() throws IOException {
		double x = 180.0;
		double y = 0.0;
		double[] mer = LocationUtils.convertLatLonToMercator(y, x);
		long point = LocationUtils.convertMecratorToPoint(mer[0], mer[1]);
		return point;
	}
	
//	@Test
	public long testMin() throws IOException {
		double x = -180.0;
		double y = 90.0;
		double[] mer = LocationUtils.convertLatLonToMercator(y, x);
		long point = LocationUtils.convertMecratorToPoint(mer[0], mer[1]);
		return point;
	}
}
