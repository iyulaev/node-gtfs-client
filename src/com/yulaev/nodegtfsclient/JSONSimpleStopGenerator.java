package com.yulaev.nodegtfsclient;

import org.json.JSONException;
import org.json.JSONObject;

/** This class implements a Generator that takes JSONObject Objects representing
 * a transit stop and converts them into SimpleStop Objects. Ideally this would
 * be built into the SimpleStop class as an additional constructor but due to 
 * compatibility requirements with legacy systems (AnyStop in particular) the 
 * SimpleStop object could not be modified and so this Generator was built to 
 * "construct" SimpleStop objects from JSONObjects.
 *  
 * @author ivany
 *
 */

public class JSONSimpleStopGenerator {
	
	//Serial version ID for this JSONSimpleStop Object (version)
	//private static final long serialVersionUID = 1L;

	/** This method takes a JSONObject representing a transit stop and returns a 
	 * SimpleStop object that can be used elsewhere in a Java-based program.
	 * @param stopObject JSONObject representing transit stop
	 * @return SimpleStop representing the same data as the JSONObject.
	 */
	public static SimpleStop jsonToSimpleStop(JSONObject stopObject) {
		SimpleStop returned = new SimpleStop();
		
		try {
			if(stopObject.has("stop_name")) returned.intersection = stopObject.getString("stop_name");
			if(stopObject.has("stop_lat")) returned.lat = (int) (1000000.0 * stopObject.getDouble("stop_lat"));
			if(stopObject.has("stop_lon")) returned.lon = (int) (1000000.0 * stopObject.getDouble("stop_lon"));
			if(stopObject.has("agency_key")) returned.agency = stopObject.getString("agency_key");
		} catch (JSONException e) {
			System.err.println(e);
		}
		
		//Something about direction?
		
		return(returned);
	}
	
	/** This method returns a convenient-for-debug String representation of SimpleStop objects
	 * 
	 * @param s SimpleStop object to create String representation for
	 * @return String representation of s (written in set and tuple-esque notation)
	 */
	public static String toString(SimpleStop s) {
		String returned = new String();
		returned += "SimpleStop={";
		
		if(s.routeName != null) {
			returned += s.routeName;
			returned += ", ";
		}
		
		//For intersection names we should replace ':' with ' & '
		String intersection = s.intersection;
		intersection = intersection.replaceAll(":", " & ");
		returned += intersection;
		returned += ", (";
		
		//Convert lat, lon to (floating-point) co-ordinates from micro-degree formatted ints
		returned += String.format("%.2f", new Double(( (double) s.lat)/1000000.0));
		returned += ", ";
		returned += String.format("%.2f", new Double(( (double) s.lon)/1000000.0));
		returned += ")}";
		
		return(returned);
	}
}
