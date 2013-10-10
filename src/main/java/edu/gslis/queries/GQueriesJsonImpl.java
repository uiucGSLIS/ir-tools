package edu.gslis.queries;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;






import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.KeyValuePair;
import edu.gslis.utils.ScorableComparator;


/**
 * reads and holds GQueries stored as a serialized JSON file on disk.
 * 
 * @author Miles Efron
 *
 */
public class GQueriesJsonImpl implements GQueries {

	private static final JsonParser JSON_PARSER = new JsonParser();
	private List<GQuery> queryList;
	private Map<String,Integer> nameToIndex;

	public void read(String pathToQueries) {
		JsonObject obj = null;
		try {
			obj = (JsonObject) JSON_PARSER.parse(new BufferedReader(new FileReader(pathToQueries)));
		} catch (Exception e) {
			System.err.println("died reading queries from json file");
			System.exit(-1);
		}

		
		JsonArray queryObjectArray = obj.getAsJsonArray("queries");
		queryList = new ArrayList<GQuery>(queryObjectArray.size());
		nameToIndex = new HashMap<String,Integer>(queryList.size());
		Iterator<JsonElement> queryObjectIterator = queryObjectArray.iterator();
		int k=0;
		while(queryObjectIterator.hasNext()) {
			JsonObject queryObject = (JsonObject) queryObjectIterator.next();
			String title = queryObject.get("title").getAsString();
			String text  = queryObject.get("text").getAsString();
			
			nameToIndex.put(title, k++);
			FeatureVector featureVector = new FeatureVector(null);
			JsonArray modelObjectArray = queryObject.getAsJsonArray("model");
			Iterator<JsonElement> featureIterator = modelObjectArray.iterator();
			while(featureIterator.hasNext()) {
				JsonObject featureObject = (JsonObject)featureIterator.next();
				double weight  = featureObject.get("weight").getAsDouble();
				String feature = featureObject.get("feature").getAsString();
				featureVector.addTerm(feature, weight);
			}
			
			
			GQuery gQuery = new GQuery();
			gQuery.setTitle(title);
			gQuery.setText(text);
			gQuery.setFeatureVector(featureVector);
			
			if(queryObject.get("epoch") != null) {
				gQuery.setMetadata("epoch", queryObject.get("epoch").getAsString());
			}
			if(queryObject.get("querytweettime") != null) {
				gQuery.setMetadata("querytweettime", queryObject.get("querytweettime").getAsString());
			}
			if(queryObject.get("group") != null) {
				gQuery.setMetadata("group", queryObject.get("group").getAsString());
			}
			queryList.add(gQuery);
			
		}	
	}

	public void addQuery(GQuery query) {
		if(queryList == null)
			queryList = new LinkedList<GQuery>();
		
		queryList.add(query);
		
		if(nameToIndex==null)
			nameToIndex = new HashMap<String,Integer>();
		
		nameToIndex.put(query.getTitle(), queryList.size()-1);
	}
	
	public GQuery getIthQuery(int i) {
		if(queryList == null || i >= queryList.size()) {
			System.err.println("died trying to get query number " + i + "  when we have only " + queryList.size() + " queries.");
			System.exit(-1);		
		}
		return queryList.get(i);
	}
	
	public GQuery getNamedQuery(String queryName) {
		if(queryList == null || ! nameToIndex.containsKey(queryName)) {
			System.err.println("died trying to get query  " + queryName + ".");
			System.exit(-1);		}
		return queryList.get(nameToIndex.get(queryName));
	}
	

	public Iterator<GQuery> iterator() {
		return queryList.iterator();
	}

	public int numQueries() {
		return queryList.size();
	}
	

	@Override
	public String toString() {
		DecimalFormat format = new DecimalFormat("#.##########################");
		JsonObject outputObjects = new JsonObject();
		JsonArray outputJsonArray = new JsonArray();
		Iterator<GQuery> queryIterator = queryList.iterator();
		while(queryIterator.hasNext()) {
			GQuery query = queryIterator.next();

			JsonObject outputQueryObject = new JsonObject();
			outputQueryObject.addProperty("title", query.getTitle());
			outputQueryObject.addProperty("text", query.getText());
			
			if(query.getMetadata("epoch") != null)
				outputQueryObject.addProperty("epoch", query.getMetadata("epoch"));
			if(query.getMetadata("querytweettime") != null)
				outputQueryObject.addProperty("querytweettime", query.getMetadata("querytweettime"));			
			if(query.getMetadata("group") != null)
				outputQueryObject.addProperty("group", query.getMetadata("group"));
			
			JsonArray modelArray = new JsonArray();
			FeatureVector featureVector = query.getFeatureVector();
			List<KeyValuePair> pairs = new ArrayList<KeyValuePair>(featureVector.getDimensions());
			Iterator<String> featureIterator = featureVector.iterator();
			while(featureIterator.hasNext()) {
				String feature = featureIterator.next();
				double weight  = featureVector.getFeaturetWeight(feature);
				KeyValuePair tuple = new KeyValuePair(feature,weight);
				pairs.add(tuple);
			}
			ScorableComparator comparator = new ScorableComparator(true);
			Collections.sort(pairs, comparator);
			
			Iterator<KeyValuePair> tupleIterator = pairs.iterator();
			while(tupleIterator.hasNext()) {
				KeyValuePair tuple = tupleIterator.next();
				String feature = tuple.getKey();
				double weight  = tuple.getScore();
				
				JsonObject tupleObject = new JsonObject();
				tupleObject.addProperty("weight", format.format(weight));
				tupleObject.addProperty("feature", feature);
				modelArray.add(tupleObject);
			}
			outputQueryObject.add("model", modelArray);


			outputJsonArray.add(outputQueryObject);
		}	
		outputObjects.add("queries", outputJsonArray);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(outputObjects);
		return json;
	}

	

}
