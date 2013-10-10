package edu.gslis.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapUtils {

	public static Map<Object,Double> sumToOne(Map<Object,Double> in) {
		double sum = MapUtils.sumValues(in);
		Map<Object, Double> out = new HashMap<Object,Double>(in.size());
		Iterator<Object> inIt = in.keySet().iterator();
		while(inIt.hasNext()) {
			Object key = inIt.next();
			Double val = (Double)in.get(key) / sum;
			out.put(key, val);
		}
		return out;
	}
	
	public static double sumValues(Map<Object,Double> in) {
		double sum = 0.0;
		Iterator<Object> inIt = in.keySet().iterator();
		while(inIt.hasNext()) {
			sum += (Double)in.get(inIt.next());
		}
		return sum;
	}
	
	public static void main(String[] args) {
		Map<Object,Double> x = new HashMap<Object,Double>();
		x.put("one", 1.0);
		x.put("two", 2.0);
		x.put("three", 3.0);
		
		System.out.println("sum: " + MapUtils.sumValues(x));
		System.out.println("norm: " + MapUtils.sumToOne(x));
	}
}
