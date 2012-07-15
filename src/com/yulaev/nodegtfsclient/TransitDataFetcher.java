/** This class implements the top level of the node-gtfs client. It fetches data from a node-gtfs server
 * (see https://github.com/brendannee/node-gtfs for server side) and translates it into Java objects like
 * Route, SimpleStop. 
 * 
 * @author iyulaev
 */

package com.yulaev.nodegtfsclient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TransitDataFetcher {
	
	//These "defines" will determine which debugging tests/messages get run in the main() function
	private static final boolean TEST_ROUTE_BY_AGENCY = false;
	private static final boolean TEST_STOP_BY_ROUTE = false;
	private static final boolean TEST_ROUTE_BY_LOC = true;
	
	//Used to put together URLs pointing to API endpoints on the node-gtfs server
	private NodeGtfsUrlMaker urlMakr;
	
	public TransitDataFetcher(String domain) {
		this.urlMakr = new NodeGtfsUrlMaker(domain);
		
	}
	
	/** This method will return the JSON-formatted string residing at URL "urlString"
	 * @param urlString String representing URL to fetch from
	 * @return String located at the URL represeted by urlString
	 */
	private static String jsonStringFromURLString(String urlString) {
		URL targetURL;
		try { targetURL = new URL(urlString); } 
		catch (MalformedURLException e) { System.err.println(e); return(null);	}
		
		InputStream is;
		try { is = targetURL.openStream(); }
		catch (IOException e) { System.err.println(e); return(null); }
		
		try {
		  BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
		  String jsonText = readAll(rd);
		  is.close();
		  return(jsonText);
		} catch (Exception e) { System.err.println(e); return(null); } 		
	}
	
	//This method lifted from http://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
	/** This method returns the String representation that can be extracted from the Reader 'rd'
	 * 
	 * @param rd Reader to extract String from
	 * @return String extracted from rd.
	 * @throws IOException
	 */
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	}
	
	/**
	 * Converts a JSON-formatted string represeting GTFS routes into a set of Route Objects.
	 * @param jsonString String representing a list of GTFS routes (JSON formatted)
	 * @return ArrayList of Route objects
	 */
	private static ArrayList<Route> jsonStringToRouteList(String jsonString) {
		//Convert the JSON formatted string to JSONArray type
		JSONArray jsonRouteObjectArray = null;
		try { jsonRouteObjectArray = new JSONArray(jsonString); }
		catch (JSONException e) { System.err.println(e); }
		
		//Convert JSONArray to ArrayList of Route objects
		ArrayList <Route> routeList = new ArrayList<Route>();
		if(jsonRouteObjectArray != null) {
			System.out.println("Found " + jsonRouteObjectArray.length() + " JSON Objects in the Array");
			for(int i = 0; i < jsonRouteObjectArray.length(); i++) {
				Route newRoute;
				
				try { newRoute = new Route(jsonRouteObjectArray.getJSONObject(i)); }
				catch (JSONException e) { System.err.println(e); newRoute = null; }
				
				if(newRoute != null) routeList.add(newRoute);
			}
		} else {
			System.out.println("No objects in jsonRouteObjectArray! Terminating...");
			return routeList;
		}
		
		//Remove invalid entries
		for(int i = 0; i < routeList.size(); i++) if(routeList.get(i).isValid()) routeList.remove(i);
		
		return(routeList);	
	}

	/**
	 * Converts a JSON-formatted string representing GTFS stops into a set of SimpleStop Objects.
	 * @param jsonString String representing a list of GTFS stops (JSON formatted)
	 * @return ArrayList of SimpleStop objects
	 */
	private static ArrayList<SimpleStop> jsonStringToStopList(String jsonString) {
		//Convert the JSON formatted string to JSONArray type
		JSONArray jsonSimpleStopObjectArray = null;
		try { jsonSimpleStopObjectArray = new JSONArray(jsonString); }
		catch (JSONException e) { System.err.println(e); }
		
		//Convert JSONArray to ArrayList of Route objects
		ArrayList <SimpleStop> stopList = new ArrayList<SimpleStop>();
		if(jsonSimpleStopObjectArray != null) {
			System.out.println("Found " + jsonSimpleStopObjectArray.length() + " JSON Objects in the Array");
			for(int i = 0; i < jsonSimpleStopObjectArray.length(); i++) {
				SimpleStop newStop;
				
				try { newStop = JSONSimpleStopGenerator.jsonToSimpleStop(jsonSimpleStopObjectArray.getJSONObject(i)); }
				catch (JSONException e) { System.err.println(e); newStop = null; }
				
				if(newStop != null) stopList.add(newStop);
			}
		} else {
			System.out.println("No objects in jsonRouteObjectArray! Terminating...");
			return stopList;
		}
		
		//Remove invalid entries (not implemented for SimpleStops yet)
		//for(int i = 0; i < stopList.size(); i++) if(stopList.get(i).isValid()) stopList.remove(i);
		
		return(stopList);	
	}
	
	/** This method fetches a list of Routes based on latitude and longitude. Overloaded versions exist 
	 * where lat and lon can be passed in as doubles (in floating-point degrees). 
	 * @param lat The latitude (in microdegrees) 
	 * @param lon The longitude (in microdegrees)
	 * @param radius (the radius in miles - optional! Will default to 
	 * NodeGtfsUrlMaker.DEFAULT_SEARCH_RADIUS_ROUTES miles if not given)
	 * @return An ArrayList of Route objects 
	 */
	public ArrayList<Route> fetchRouteListByLatLon(int lat, int lon, double radius) {
		String urlStr = urlMakr.getRoutesByLoc(lat, lon, radius); //get URL for API endpoint
		String jsonRouteList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}
	
	public ArrayList<Route> fetchRouteListByLatLon(int lat, int lon) {
		String urlStr = urlMakr.getRoutesByLoc(lat, lon); //get URL for API endpoint
		String jsonRouteList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}
	public ArrayList<Route> fetchRouteListByLatLon(double lat, double lon, double radius) {
		String urlStr = urlMakr.getRoutesByLoc(lat, lon, radius); //get URL for API endpoint
		String jsonRouteList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}
	
	public ArrayList<Route> fetchRouteListByLatLon(double lat, double lon) {
		String urlStr = urlMakr.getRoutesByLoc(lat, lon); //get URL for API endpoint
		System.out.println("URL was " + urlStr);
		String jsonRouteList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}
	
	
	
	/** This method fetches a list of Stops based on latitude and longitude
	 * @param lat The latitude (in microdegrees) to fetch a list of stops around
	 * @param lon The longitude (in microdegrees) to fetch a list of stops around
	 * @param radius The radius (in miles) within which to search for stops 
	 * 		for (default: NodeGtfsUrlMaker.DEFAULT_SEARCH_RADIUS_STOPS)
	 * @return A List of SimpleStop objects within radius miles of (lat, lon)
	*/
	public ArrayList<SimpleStop> fetchStopListByLatLon(int lat, int lon, double radius) {
		String urlStr = urlMakr.getStopsByLoc(lat, lon, radius); //get URL for API endpoint
		String jsonStopList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToStopList(jsonStopList)); //Convert to list of routes
	}
	
	public ArrayList<SimpleStop> fetchStopListByLatLon(int lat, int lon) {
		String urlStr = urlMakr.getStopsByLoc(lat, lon); //get URL for API endpoint
		String jsonStopList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToStopList(jsonStopList)); //Convert to list of routes
	}
	
	/*public ArrayList<SimpleStop> fetchStopListByLatLon(double lat, double lon, double radius) {
		String urlStr = urlMakr.getStopsByLoc(lat, lon, radius); //get URL for API endpoint
		String jsonStopList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToStopList(jsonStopList)); //Convert to list of routes
	}
	
	public ArrayList<SimpleStop> fetchStopListByLatLon(double lat, double lon) {
		String urlStr = urlMakr.getStopsByLoc(lat, lon); //get URL for API endpoint
		String jsonStopList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToStopList(jsonStopList)); //Convert to list of routes
	}*/
	/**
	 * @param apiEndpointRouteListURL String representing the URL to fetch JSON route list from
	 * @return ArrayList of Route Objects that has all of the routes filled in per the JSON at the URL
	 */
	public ArrayList<Route> fetchRouteList(String agency) {
		String urlStr = urlMakr.getRoutesByAgencyString(agency); //get URL for API endpoint
		String jsonRouteList = jsonStringFromURLString(urlStr); //get JSON-formatted route list
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}
	
	/** This method returns a list of Route Objects when given a latitude, longitude (in microdegrees) 
	 * and a search radius in miles
	 * @param lat Latitude in microdegrees
	 * @param lon Longitude in microdegrees
	 * @param radius Search radius in miles (default = NodeGtfsUrlMaker.DEFAULT_SEARCH_RADIUS_ROUTES)
	 * @return List of routes within radius miles of (lat, lon)
	 */
	/*public ArrayList<Route> fetchRouteListByLoc(int lat, int lon, double radius) {
		String urlStr = urlMakr.getRoutesByLoc(lat, lon, radius); //get URL for API endpoint
		String jsonRouteList = jsonStringFromURLString(urlStr);
		return(jsonStringToRouteList(jsonRouteList)); //Convert to list of routes
	}*/
	
	/** This method will fetch a list of stops given a route name
	 * 
	 * @param agency the Agency within which to look for stops
	 * @param route_id The Route ID for which to fetch a list of stops
	 * @return A List of SimpleStops reprensting the stops on the route denoted by route_id
	 */
	public ArrayList<SimpleStop> fetchStopListByRoute(String agency, String route_id) {
		String urlStr = urlMakr.getStopsByRoute(agency, route_id);
		String jsonStopList = jsonStringFromURLString(urlStr);
		return(jsonStringToStopList(jsonStopList));
	}
	
	/** This method returns a List of Predictions given an agecy, route_id, stop_id, and an optional
	 * direction
	 * @param agency Agency key for prediction
	 * @param route_id Route ID key for prediction
	 * @param stop_id Stop ID key for prediction
	 * @param direction The Direction to get predictions for (optional!)
	 * @return A List of predicted times (Prediction Objects) for the stop/direction represented by
	 * the parameters. For detailed information on what the Predictions represent see the overview
	 * of the Prediction class, in Prediction.java.
	 */
	public ArrayList<Prediction> fetchPredictionByStop(String agency, String route_id, String stop_id, String direction) {
		return(null);
	}
	public ArrayList<Prediction> fetchPredictionByStop(String agency, String route_id, String stop_id) {
		return(fetchPredictionByStop(agency, route_id, stop_id, null));
	}
	
	/** The main() method simply implements some tests (which are enabled/disabled by the boolean "defines" 
	 * at the top of the TransitDataFetcher class). 
	 * @param args
	 */
	public static void main(String [] args) {
		int error_count = 0;
		
		final String serverUrl = "http://192.168.111.113:8081";
		
		if(TEST_ROUTE_BY_AGENCY) {
			TransitDataFetcher testEngine = new TransitDataFetcher(serverUrl);
			ArrayList<Route> routeList = testEngine.fetchRouteList("bay-area-rapid-transit");
			
			if(routeList.size() > 0) System.out.println("routeList has " + routeList.size() + " members!");
			else { System.out.println ("ERROR: Routelist has zero members!"); error_count++; }
			
			for(int i = 0; i < routeList.size(); i++) {
				System.out.println(routeList.get(i));
			}
			
			/*System.out.println("Fetched the string:");
			System.out.print(jsonString);*/
		}
		
		if(TEST_STOP_BY_ROUTE) {
			TransitDataFetcher testEngine = new TransitDataFetcher(serverUrl);
			ArrayList<SimpleStop> stopList = testEngine.fetchStopListByRoute("ac-transit", "1R-86");
			//ArrayList<SimpleStop> stopList = jsonStringToStopList("[{\"stop_id\":\"0306730\",\"stop_name\":\"Berkeley Way:Oxford St\",\"stop_lat\":37.873253,\"stop_lon\":-122.26651,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909c7f9\",\"loc\":[-122.26651,37.873253]},{\"stop_id\":\"0304790\",\"stop_name\":\"Shattuck Av:Allston Way\",\"stop_lat\":37.869476,\"stop_lon\":-122.26812,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909c75c\",\"loc\":[-122.26812,37.869476]},{\"stop_id\":\"0301990\",\"stop_name\":\"Durant Av:Dana St\",\"stop_lat\":37.867466,\"stop_lon\":-122.261375,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909c668\",\"loc\":[-122.261375,37.867466]},{\"stop_id\":\"0305510\",\"stop_name\":\"Telegraph Av:Dwight Way\",\"stop_lat\":37.864578,\"stop_lon\":-122.258621,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909c7a1\",\"loc\":[-122.258621,37.864578]},{\"stop_id\":\"0305620\",\"stop_name\":\"Telegraph Av:Webster St\",\"stop_lat\":37.855377,\"stop_lon\":-122.259911,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909c7ac\",\"loc\":[-122.259911,37.855377]},{\"stop_id\":\"1020380\",\"stop_name\":\"Telegraph Av:Alcatraz Av\",\"stop_lat\":37.850452,\"stop_lon\":-122.260582,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d392\",\"loc\":[-122.260582,37.850452]},{\"stop_id\":\"1030840\",\"stop_name\":\"Telegraph Av:59th St\",\"stop_lat\":37.844757,\"stop_lon\":-122.261368,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3f8\",\"loc\":[-122.261368,37.844757]},{\"stop_id\":\"1020250\",\"stop_name\":\"Telegraph Av:50th St\",\"stop_lat\":37.836571,\"stop_lon\":-122.262512,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d387\",\"loc\":[-122.262512,37.836571]},{\"stop_id\":\"1020150\",\"stop_name\":\"Telegraph Av:40th St\",\"stop_lat\":37.829494,\"stop_lon\":-122.264481,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d380\",\"loc\":[-122.264481,37.829494]},{\"stop_id\":\"1020070\",\"stop_name\":\"Telegraph Av:30th St\",\"stop_lat\":37.81921,\"stop_lon\":-122.267242,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d378\",\"loc\":[-122.267242,37.81921]},{\"stop_id\":\"1020020\",\"stop_name\":\"Telegraph Av:24th St\",\"stop_lat\":37.81329,\"stop_lon\":-122.268761,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d374\",\"loc\":[-122.268761,37.81329]},{\"stop_id\":\"1031070\",\"stop_name\":\"Thomas L Berkley Way:Telegragh Av\",\"stop_lat\":37.809193,\"stop_lon\":-122.268715,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d407\",\"loc\":[-122.268715,37.809193]},{\"stop_id\":\"1006360\",\"stop_name\":\"Broadway:14th St (12th St BART Station)\",\"stop_lat\":37.803978,\"stop_lon\":-122.271484,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cf16\",\"loc\":[-122.271484,37.803978]},{\"stop_id\":\"1000350\",\"stop_name\":\"11th St:Broadway (12th St BART Station)\",\"stop_lat\":37.802078,\"stop_lon\":-122.272156,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cd48\",\"loc\":[-122.272156,37.802078]},{\"stop_id\":\"1030760\",\"stop_name\":\"11th St:Harrison St\",\"stop_lat\":37.800671,\"stop_lon\":-122.268608,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3f3\",\"loc\":[-122.268608,37.800671]},{\"stop_id\":\"1030690\",\"stop_name\":\"11th St:Madison St\",\"stop_lat\":37.799503,\"stop_lon\":-122.26564,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3ec\",\"loc\":[-122.26564,37.799503]},{\"stop_id\":\"1008450\",\"stop_name\":\"International Blvd:2nd Av\",\"stop_lat\":37.798027,\"stop_lon\":-122.256531,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cfb8\",\"loc\":[-122.256531,37.798027]},{\"stop_id\":\"1030660\",\"stop_name\":\"International Blvd:5th Av\",\"stop_lat\":37.795509,\"stop_lon\":-122.253662,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3ea\",\"loc\":[-122.253662,37.795509]},{\"stop_id\":\"1008290\",\"stop_name\":\"International Blvd:10th Av\",\"stop_lat\":37.792152,\"stop_lon\":-122.249878,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cfa9\",\"loc\":[-122.249878,37.792152]},{\"stop_id\":\"1008310\",\"stop_name\":\"International Blvd:14th Av\",\"stop_lat\":37.789223,\"stop_lon\":-122.245911,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cfab\",\"loc\":[-122.245911,37.789223]},{\"stop_id\":\"1008420\",\"stop_name\":\"International Blvd:26th Av\",\"stop_lat\":37.781467,\"stop_lon\":-122.232796,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cfb5\",\"loc\":[-122.232796,37.781467]},{\"stop_id\":\"1030810\",\"stop_name\":\"International Blvd:34th Av\",\"stop_lat\":37.776417,\"stop_lon\":-122.223518,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3f6\",\"loc\":[-122.223518,37.776417]},{\"stop_id\":\"1030700\",\"stop_name\":\"International Blvd:High St\",\"stop_lat\":37.772213,\"stop_lon\":-122.214706,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3ed\",\"loc\":[-122.214706,37.772213]},{\"stop_id\":\"1030710\",\"stop_name\":\"International Blvd:Seminary Av\",\"stop_lat\":37.764923,\"stop_lon\":-122.199364,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3ee\",\"loc\":[-122.199364,37.764923]},{\"stop_id\":\"1008910\",\"stop_name\":\"International Blvd:Havenscourt Blvd\",\"stop_lat\":37.761734,\"stop_lon\":-122.19265,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909cfdb\",\"loc\":[-122.19265,37.761734]},{\"stop_id\":\"1008930\",\"stop_name\":\"International Blvd:Hegenberger Rd\",\"stop_lat\":37.758789,\"stop_lon\":-122.186462,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909cfdd\",\"loc\":[-122.186462,37.758789]},{\"stop_id\":\"1008780\",\"stop_name\":\"International Blvd:82nd Av\",\"stop_lat\":37.755608,\"stop_lon\":-122.179741,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909cfd0\",\"loc\":[-122.179741,37.755608]},{\"stop_id\":\"1031640\",\"stop_name\":\"International Blvd:90th Av\",\"stop_lat\":37.74926,\"stop_lon\":-122.174263,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d427\",\"loc\":[-122.174263,37.74926]},{\"stop_id\":\"1030770\",\"stop_name\":\"International Blvd:98th Av\",\"stop_lat\":37.744076,\"stop_lon\":-122.170448,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d3f4\",\"loc\":[-122.170448,37.744076]},{\"stop_id\":\"1008270\",\"stop_name\":\"International Blvd:104th Av\",\"stop_lat\":37.74004,\"stop_lon\":-122.16745,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0cfbae93780909cfa7\",\"loc\":[-122.16745,37.74004]},{\"stop_id\":\"1501340\",\"stop_name\":\"E 14th St:Best Av (Dutton Av)\",\"stop_lat\":37.730583,\"stop_lon\":-122.160385,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d664\",\"loc\":[-122.160385,37.730583]},{\"stop_id\":\"1501570\",\"stop_name\":\"E 14th St:W Estudillo Av\",\"stop_lat\":37.725227,\"stop_lon\":-122.156357,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d67a\",\"loc\":[-122.156357,37.725227]},{\"stop_id\":\"1501480\",\"stop_name\":\"E 14th St:Parrott St\",\"stop_lat\":37.722546,\"stop_lon\":-122.152626,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d671\",\"loc\":[-122.152626,37.722546]},{\"stop_id\":\"1501510\",\"stop_name\":\"E 14th St:San Leandro Hospital\",\"stop_lat\":37.714333,\"stop_lon\":-122.141624,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d674\",\"loc\":[-122.141624,37.714333]},{\"stop_id\":\"1504510\",\"stop_name\":\"E 14th St:150th Av\",\"stop_lat\":37.70546,\"stop_lon\":-122.129051,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d73b\",\"loc\":[-122.129051,37.70546]},{\"stop_id\":\"1504140\",\"stop_name\":\"Bayfair Dr:E 14th St\",\"stop_lat\":37.700836,\"stop_lon\":-122.123695,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d71a\",\"loc\":[-122.123695,37.700836]},{\"stop_id\":\"1500620\",\"stop_name\":\"Bayfair BART Station\",\"stop_lat\":37.696495,\"stop_lon\":-122.12545,\"agency_key\":\"ac-transit\",\"_id\":\"4ff3de0dfbae93780909d643\",\"loc\":[-122.12545,37.696495]}]");
			
			if(stopList.size() > 0) System.out.println("stopList has " + stopList.size() + " members!");
			else { System.out.println ("ERROR: stopList has zero members!"); error_count++; }
			
			for(int i = 0; i < stopList.size(); i++) {
				System.out.println(JSONSimpleStopGenerator.toString(stopList.get(i)));
			}
			
			/*System.out.println("Fetched the string:");
			System.out.print(jsonString);*/
		}
		
		if(TEST_ROUTE_BY_LOC) {
			TransitDataFetcher testEngine = new TransitDataFetcher(serverUrl);
			//	37.869476	-122.26812 (san diego random point)
			ArrayList<Route> routeList = testEngine.fetchRouteListByLatLon(37.8694, -122.26812);
			
			if(routeList.size() > 0) System.out.println("routeList has " + routeList.size() + " members!");
			else { System.out.println ("ERROR: Routelist has zero members!"); error_count++; }
			
			for(int i = 0; i < routeList.size(); i++) {
				System.out.println(routeList.get(i));
			}
			
			/*System.out.println("Fetched the string:");
			System.out.print(jsonString);*/
		}
		
		System.err.printf("Got %d errors, %s", error_count, error_count>0?"FAIL":"PASS");

	}
}
