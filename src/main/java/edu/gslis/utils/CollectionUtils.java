package edu.gslis.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class CollectionUtils {

	public static List<Double> normalizeToOne(List<Double> x) {
		double sum = CollectionUtils.getSum(x);
		List<Double> y = new ArrayList<Double>(x.size());
		Iterator<Double> it = x.iterator();
		while(it.hasNext()) {
			double val = it.next() / sum;
			y.add(val);
		}
		return y;
	}
	
	public static double getSum(List<Double> x) {
		double sum = 0.0;
		Iterator<Double> it = x.iterator();
		while(it.hasNext()) {
			sum += it.next();
		}
		return sum;
	}
}
