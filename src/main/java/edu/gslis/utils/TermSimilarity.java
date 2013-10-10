package edu.gslis.utils;

import lemurproject.indri.QueryEnvironment;

public class TermSimilarity {

	public static double miKarm(String x, String y, QueryEnvironment env) {
		double h = 0.0;

		try {
			double toks = (double)env.termCount();
			double xCount = (double)env.termCount(x) + 1.0;
			double yCount = (double)env.termCount(y) + 1.0;
			double joint  = (double)env.expressionCount("#band(" + x + " " + y + ")") + 1.0;
			
			double px1 = xCount / toks;
			double py1 = yCount / toks;
			double px0 = 1.0 - px1;
			double py0 = 1.0 - py1;
			double px1y1 = joint / toks;
			double px1y0 = ((xCount - joint)) / toks;
			double px0y1 = ((yCount - joint)) / toks;
			double px0y0 = 1 - px1y1 - px1y0 - px0y1;
			
			h = px1y1 * Math.log(px1y1 / (px1 * py1)) +
					px0y1 * Math.log(px0y1 / (px0 * py1)) +
					px1y0 * Math.log(px1y0 / (px1 * py0)) +
					px0y0 * Math.log(px0y0 / (px0 * py0));
			
 		} catch (Exception e) {
			e.printStackTrace();
		}

		return h;
	}
	public static double mi(String x, String y, QueryEnvironment env) {
		double h = 0.0;

		try {
			double toks = (double)env.termCount();
			double xCount = (double)env.termCount(x) + 1.0;
			double yCount = (double)env.termCount(y) + 1.0;
			double joint  = (double)env.expressionCount("#band(" + x + " " + y + ")") + 1.0;
			
			h = Math.log(toks * joint / (xCount * yCount));
			
 		} catch (Exception e) {
			e.printStackTrace();
		}

		return h;
	}
	
	public static double dice(String x, String y, QueryEnvironment env) {
		double h = 0.0;

		try {
			double xCount = (double)env.termCount(x) + 1.0;
			double yCount = (double)env.termCount(y) + 1.0;
			double joint  = (double)env.expressionCount("#band(" + x + " " + y + ")") + 1.0;
			
			h = 2 * joint / (xCount + yCount);
			
 		} catch (Exception e) {
			e.printStackTrace();
		}

		return h;
	}
	
}
