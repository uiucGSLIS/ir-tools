package edu.gslis.utils;

public class MatrixUtils {

	public static double[][] rowNormalize(double[][] x) {
		int cols = x[0].length;
		int rows = x.length;
		
		for(int i=0; i<rows; i++) {
			double sum = 0.0;
			for(int j=0; j<cols; j++) {
				sum += x[i][j];
			}
			for(int j=0; j<cols; j++) {
				x[i][j] /= sum;
			}
		}
		return x;
	}
	
	public static double[][] columnNormalize(double[][] x) {
		int cols = x[0].length;
		int rows = x.length;
		
		for(int i=0; i<cols; i++) {
			double sum = 0.0;
			for(int j=0; j<rows; j++) {
				sum += x[j][i];
			}
			for(int j=0; j<rows; j++) {
				x[j][i] /= sum;
			}
		}
		return x;
	}
	
	
	
	public static void main(String[] args) {
		double[][] x = new double[2][3];
		for(int i=0; i<2; i++) {
			for(int j=0; j<3; j++) {
				x[i][j] = i+j;
			}
		}
		double[][] y = MatrixUtils.columnNormalize(x);
		for(int i=0; i<2; i++) {
			for(int j=0; j<3; j++) {
				System.out.print(y[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static double[] extractColumn(double[][] x, int colNumber) {
		double[] column = new double[x.length];
		for(int i=0; i<column.length; i++) {
			column[i] = x[i][colNumber];
		}
		return column;
	}
	
	public static double[] extractRow(double[][] x, int rowNumber) {
		double[] row = new double[x[0].length];
		for(int i=0; i<row.length; i++) {
			row[i] = x[rowNumber][i];
		}
		return row;
	}
}
