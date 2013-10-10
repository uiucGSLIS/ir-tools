package edu.gslis.textrepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.gslis.utils.MatrixUtils;



public class DocumentTermMatrix {
	private double[][] matrix;
	int rowCount = 0;
	int colCount = 0;
	private Map<Integer,String> indexToTermMap;
	private Map<String,Integer> termToIndexMap;
	
	public DocumentTermMatrix(List<FeatureVector> docVectors) {
		Set<String> vocab = new HashSet<String>();
		Iterator<FeatureVector> docIterator = docVectors.iterator();
		while(docIterator.hasNext()) {
			vocab.addAll(docIterator.next().getFeatures());
		}
		
		indexToTermMap = new HashMap<Integer,String>();
		termToIndexMap = new HashMap<String,Integer>();
		
		
		int i=0;
		Iterator<String> features = vocab.iterator();
		while(features.hasNext()) {
			String feature = features.next();
			int termID = i++;
			indexToTermMap.put(termID, feature);
			termToIndexMap.put(feature, termID);
		}
		
		rowCount = docVectors.size();
		colCount = vocab.size();
		matrix = new double[rowCount][colCount];
		
		int row = 0;
		docIterator = docVectors.iterator();
		while(docIterator.hasNext()) {
			FeatureVector docVector = docIterator.next();
			for(int column=0; column<vocab.size(); column++) {
				String term = indexToTermMap.get(column);
				double queryTermWeight = docVector.getFeaturetWeight(term);
				matrix[row][column] = queryTermWeight;
			}
			row++;
		}
	}
	
	public Map<Integer,String> getIndexToTermMap() {
		return indexToTermMap;
	}
	public Map<String,Integer> getTermToIndexMap() {
		return termToIndexMap;
	}
	public double[][] getMatrix() {
		return matrix;
	}
	
	@Override 
	public String toString() {
		StringBuilder bb = new StringBuilder();
		StringBuilder b = new StringBuilder(); 
		
		for(int i=0; i<colCount; i++) {
			b.append(indexToTermMap.get(i) + " ");
		}
		String line = b.toString().trim();
		bb.append(line);
		bb.append("\n");
		for(int i=0; i<rowCount; i++) {
			b = new StringBuilder();
			for(int j=0; j<colCount; j++) {
				b.append(matrix[i][j] + " ");
			}
			line = b.toString().trim();
			bb.append(line);
			if(i < rowCount-1)
				bb.append("\n");
		}
		return bb.toString();
	}
	
	
	public static void main(String[] args) {
		FeatureVector x = new FeatureVector(null);
		x.addTerm("this");
		x.addTerm("hubble", 2.0);
		x.addTerm("telescope");

		FeatureVector y = new FeatureVector(null);
		y.addTerm("another");
		y.addTerm("telescope");
		y.addTerm("hubble", 5.0);
		
		List<FeatureVector> vectors = new ArrayList<FeatureVector>(2);
		vectors.add(x);
		vectors.add(y);
		
		DocumentTermMatrix matrixObj = new DocumentTermMatrix(vectors);
		System.out.println(matrixObj);
		
		double[][] matrix = matrixObj.getMatrix();
		double[] col = MatrixUtils.extractColumn(matrix, 3);
		for(double c : col) {
			System.out.println(c);
		}
		double[] row = MatrixUtils.extractRow(matrix, 1);
		for(double c : row) {
			System.out.print(c + " ");
		}
		System.out.println();
	}
}
