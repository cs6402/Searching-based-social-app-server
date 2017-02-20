package com.tdsoft.bro.common.util;

import java.util.Set;
import java.util.TreeSet;


public class LocationUtils {
	final private static double R_MAJOR = 6378137.0;
	final private static double R_MINOR = 6378137.0;
	final private static int POINT_LENGTH = 70;
	final private static int RADIUS = 20037508;
	final private static long Y_RANGE_LENGTH = 572500L;
	final private static long POINT_MAX = 163878411250L;
	// final private static double R_MINOR = 6356752.3142;

	/**
	 * @param lat latitude
	 * @param lon longitude
	 * @return [mecrator x, mecrator y]
	 */
	public static double[] convertLatLonToMercator(double lat, double lon) {
		return new double[] {toMercX(lon), toMercY(lat)};
	}

	/**
	 * @param x mecrator x
	 * @param y mecrator y
	 * @return [latitude, longitude]
	 */
	public static double[] convertMecratorToLatLon(double x, double y) {
		return new double[] {fromMercY(y), fromMercX(x)};
	}

	/**
	 * @param x mecrator x
	 * @param y mecrator y
	 * @return [point x, point y]
	 */
	public static long convertMecratorToPoint(double x, double y) {
		long nx = (long) (x + RADIUS) / POINT_LENGTH;
		long ny = (long) (y + RADIUS) / POINT_LENGTH;
		long point;
		if (nx == 0) 
			point = ny;
		else 
			point = (nx - 1) * (RADIUS / POINT_LENGTH) + ny;
		return point;
	}

	/**
	 * TODO
	 * @param pointx point
	 * @param pointy point
	 * @return [latitude, longitude]
	 */
	public static double[] convertPointToLatLon(long point) {
		int pointx = 0;
		int pointy = 0;
		if (point <= RADIUS / POINT_LENGTH) {
			pointx = 0;
			pointy = (int) point;
		} else {
			
		}
		
//		pointx = pointx * POINT_LENGTH - RADIUS;
//		pointy = pointy * POINT_LENGTH - RADIUS;
		return convertMecratorToLatLon(pointx, pointy);
	}

	private static double toMercY(double lat) {
		if (lat > 85.05112877980659) {
			lat = 85.05112877980659;
		}
		if (lat < -85.05112877980659) {
			lat = -85.05112877980659;
		}
		double temp = R_MINOR / R_MAJOR;
		double es = 1.0 - (temp * temp);
		double eccent = Math.sqrt(es);
		double phi = Math.toRadians(lat);
		double sinphi = Math.sin(phi);
		double con = eccent * sinphi;
		double com = 0.5 * eccent;
		con = Math.pow(((1.0 - con) / (1.0 + con)), com);
		double ts = Math.tan(0.5 * ((Math.PI * 0.5) - phi)) / con;
		double y = 0 - R_MAJOR * Math.log(ts);
		return y;
	}

	private static double toMercX(double lon) {
		return R_MAJOR * Math.toRadians(lon);
	}

	private static double fromMercY(double y) {
		double temp = R_MINOR / R_MAJOR;
		double e = Math.sqrt(1.0 - (temp * temp));
		return Math.toDegrees(phi2(Math.exp(-y / R_MAJOR), e));
	}

	private static double fromMercX(double x) {
		return Math.toDegrees(x / R_MAJOR);
	}

	private static double phi2(double ts, double e) {
		int N_ITER = 15;
		double HALFPI = Math.PI / 2;

		double TOL = 0.0000000001;
		double eccnth, phi, con, dphi;
		int i;
		eccnth = .5 * e;
		phi = HALFPI - 2. * Math.atan(ts);
		i = N_ITER;
		do {
			con = e * Math.sin(phi);
			dphi = HALFPI - 2. * Math.atan(ts * Math.pow((1. - con) / (1. + con), eccnth)) - phi;
			phi += dphi;

		} while (Math.abs(dphi) > TOL && (0 != --i));
		return phi;
	}
	
	public static Set<Long> getRange(long point, int idx) {
		Set<Long> pointSet = new TreeSet<Long>();
		pointSet.add(point);
		// 以中心點開始計算y軸上下
		addYAxisPoints(pointSet, point, idx);
		// 以中心點開始計算x軸左右
		for (int i = 1; i <= idx; i++) {
			// 左邊
			long p = point + Y_RANGE_LENGTH * i;
			if (!(p > POINT_MAX || p < 0)) {
				pointSet.add(p);
				addYAxisPoints(pointSet, p, idx);
			}
			// 右邊
			p = point - Y_RANGE_LENGTH * i;
			if (!(p > POINT_MAX || p < 0)) {
				pointSet.add(p);
				addYAxisPoints(pointSet, p, idx);
			}
		}
		return pointSet;
	}
	
	/**
	 * 將x軸上下填入
	 * @param list
	 * @param point
	 * @param idx
	 */
	private static void addYAxisPoints(Set<Long> list, long point, int idx) {
		for (int i = 1; i <= idx; i++) {
			// 上 
			long p = point + i;
			// 避開突破上界
			if (!(p > POINT_MAX || p < 0) && p % Y_RANGE_LENGTH != 0) {
				list.add(p);				
			}
			// 下
			p = point - i;
			// 避開突破下界
			if (!(p > POINT_MAX || p < 0) && (p + 1) % Y_RANGE_LENGTH != 0) {
				list.add(p);				
			}
		}
	}
}
