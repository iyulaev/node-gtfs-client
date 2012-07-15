package com.yulaev.nodegtfsclient;

import java.util.HashMap;
import java.util.Iterator;

/** 
 * This class implements a node-gtfs URL creator. Given a query type, and the query parameters, it constructs a valid URL
 * to the relevant endpoint on a node-gtfs server.
 * @author ivany
 * @date 2012-07-05
 */
public class NodeGtfsUrlMaker {
	//domain URL that points to the server we'll be using
	private String domainURL;
	
	//Map from query types to endpoint URLs
	private String [] endPointMap;
	
	//Enum that defines the query tipes supported by this URL maker
	private int QUERY_TYPES;
	public static int QUERY_ROUTES_BY_AGENCY = 0;
	public static int QUERY_STOPS_BY_ROUTE = 1;
	public static int QUERY_ROUTES_BY_LOC = 2;
	public static int QUERY_STOPS_BY_LOC = 3;
	public static int QUERY_STOP_DETAILS = 4;
	public static int QUERY_STOP_DETAILS_BY_ROUTE = 5;
	//bonusQueries is for supporting additional queries that can be added at runtime
	private HashMap <String, String> bonusQueries; 
	
	//Default radius for location-based searches
	public double DEFAULT_SEARCH_RADIUS_ROUTES = 1.0;
	public double DEFAULT_SEARCH_RADIUS_STOPS = 1.0;
	public double DEFAULT_SEARCH_RADIUS_AGENCIES = 1.0;
	
	/**Utility function to convert a double-formatted co-ordinate (like latitude or longitude) to a 
	 * int-formatted one given in microdegrees.
	 * @param coordinate the co-ordinate given as a double
	 * @return Int representation of that co-ordinate, given in microdegrees.
	 */
	public static int doubleToMicroDegrees(double coordinate) {
		return((int) (coordinate*1000000.0));
	}
	
	/**Utility function to convert a flota-formatted co-ordinate (like latitude or longitude) to a 
	 * int-formatted one given in microdegrees.
	 * @param coordinate the co-ordinate given as a float
	 * @return Int representation of that co-ordinate, given in microdegrees.
	 */
	public static int floatToMicroDegrees(float coordinate) {
		return doubleToMicroDegrees(((double) coordinate)); 
	}
	
	/** Utility function to
	 * 
	 */
	public static double microDegreesToDouble(int coordinate) {
		return(((double)coordinate) / 1000000.0);
	}
	
	/** This method populates the endPointMap, a map from query types to URL strings. It inserts
	 * the default map entries in addition to any "bonus" query types that may be defined at run-time.
	 * @param newBonusQueries A map from query names to query types for any runtime-defined queries.
	 */
	private void populateEndPointMap(HashMap <String, String> newBonusQueries) {
		if(newBonusQueries != null) QUERY_TYPES += newBonusQueries.size();
			
		//Populate the default endpoints in endPointMap
		endPointMap = new String[QUERY_TYPES];
		endPointMap[QUERY_ROUTES_BY_AGENCY] = "/api/routes/";
		endPointMap[QUERY_STOPS_BY_ROUTE] = "/api/stops/";
		endPointMap[QUERY_ROUTES_BY_LOC] = "/api/routesNearby/";
		endPointMap[QUERY_STOPS_BY_LOC] = "/api/stopsNearby/";
		endPointMap[QUERY_STOP_DETAILS_BY_ROUTE] = "/api/times/";
		endPointMap[QUERY_STOP_DETAILS] = "not_implemented";
		
		//Now insert the custom queries, both for the endPointMap (int -> URL)
		//and bonusQuery (queryName -> URL)
		if(newBonusQueries != null) {
			int i = 6; 
			
			Iterator <String> keyIt = newBonusQueries.keySet().iterator();
			while (keyIt.hasNext()) {
				String currKey = keyIt.next();
				bonusQueries.put(currKey, newBonusQueries.get(currKey));
				endPointMap[i] = newBonusQueries.get(currKey);
			}
		}
	}
	
	/** This method populates the endPointMap without any runtime-defined queries
	 */
	private void populateEndPointMap() {
		populateEndPointMap(null);
	}
	
	/** This method returns the URL that can be used to query a list of routes using an Agency as a key
	 * 
	 * @param agency The agency that the URL will return routes for
	 * @return The URL that can be used to query a list of routes (from a node-gtfs server, of course!)
	 */
	public String getRoutesByAgencyString(String agency) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_ROUTES_BY_AGENCY];
		retval += agency;
				
		return(retval);
	}
	
	/** This method returns a URL that points to a node-gtfs server API endpoint, which will return
	 * a list of stops for a given route. 
	 * @param agency The agency (id) to query stops from
	 * @param route_id The route_id (Route identifier) to query stops for.
	 * @return A String representing a URL which can be used to retrieve a JSON-formatted list of stops keyed
	 * by agency,route pair (from a node-gtfs server).
	 */
	public String getStopsByRoute(String agency, String route_id) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_STOPS_BY_ROUTE];
		retval += agency;
		retval += "/" + route_id;
		
		return(retval);
	}

	/** Same as the previous method, except that we allow you to specify direction also.
	 * 
 	 * @param agency The agency (id) to query stops from
	 * @param route_id The route_id (Route identifier) to query stops for.
	 * @param direction The direction to filter the stops by
	 * @return A String representing a URL which can be used to retrieve a JSON-formatted list of stops keyed
	 */
	public String getStopsByRoute(String agency, String route_id, String direction) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_STOPS_BY_ROUTE];
		retval += agency;
		retval += "/" + route_id;
		retval += "/" + direction;
		
		return(retval);
	}
	
	/** getRoutesByLoc returns the URL pointing to a node-gtfs API endpoint, which will return a JSON-formatted
	 * list of routes that are within radius miles of the point (lat, lon).
	 * @param lat Latitude of the center of the Route search radius.
	 * @param lon Longitude of the center of the Route search radius.
	 * @param radius The radius ot the search "circle".
	 * @return A URL pointing to the node-gtfs server endpoint that returns JSON-formatted list of routes within
	 * radius miles of (lat, lon).
	 */
	public String getRoutesByLoc(int lat, int lon, double radius) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_ROUTES_BY_LOC];
		//retval += (lat % 10000) / 100.0;
		retval += microDegreesToDouble(lat);
		retval += "/";
		//retval += (lon % 10000) / 100.0;
		retval += microDegreesToDouble(lon);
		retval += "/";
		retval += String.format("%.2f", radius);
		
		return(retval);
	}
	
	/**Same as above method but uses DEFAULT_SEARCH_RADIUS_ROUTES as search radius for routes.
	 * 
	 * @param lat Latitude to center the search circle.
	 * @param lon Longitude to center the search circle.
	 * @return A URL pointing to the node-gtfs server endpoint that returns JSON-formatted list of routes within
	 * DEFAULT_SEARCH_RADIUS_ROUTES miles of (lat, lon).
	 */
	public String getRoutesByLoc(int lat, int lon) {
		return getRoutesByLoc(lat, lon, DEFAULT_SEARCH_RADIUS_ROUTES);
	}
	
	public String getRoutesByLoc(double lat, double lon) {
		return(getRoutesByLoc(lat, lon, DEFAULT_SEARCH_RADIUS_ROUTES));
	}
	
	public String getRoutesByLoc(double lat, double lon, double radius) {
		int lat_i = doubleToMicroDegrees(lat);
		int lon_i = doubleToMicroDegrees(lon);
		return(getRoutesByLoc(lat_i, lon_i, radius));
	}
	
	/** getRoutesByLoc returns the URL pointing to a node-gtfs API endpoint, which will return a JSON-formatted
	 * list of routes that are within radius miles of the point (lat, lon). Note that if you're looking for
	 * stops for a particular agency you *may* have to do some filtering of the returned list,since it will
	 * return ALL stops within some distance of the provided co-ordinates.
	 * @param lat Latitude of the center of the Route search radius, given in microdegrees.
	 * @param lon Longitude of the center of the Route search radius, given in microdegrees.
	 * @param radius The radius ot the search "circle".
	 * @return A URL pointing to the node-gtfs server endpoint that returns JSON-formatted list of stops within
	 * radius miles of (lat, lon).
	 */
	public String getStopsByLoc(int lat, int lon, double radius) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_STOPS_BY_LOC];
		//retval += (lat % 10000) / 100.0;
		retval += microDegreesToDouble(lat);
		retval += "/";
		//retval += (lon % 10000) / 100.0;
		retval += microDegreesToDouble(lon);
		retval += "/";
		retval += String.format("%.2f", radius);
		
		return(retval);
	}
	
	/**Same as above method but uses DEFAULT_SEARCH_RADIUS_STOPS as search radius for stops.
	 * 
	 * @param lat Latitude to center the search circle.
	 * @param lon Longitude to center the search circle.
	 * @return A URL pointing to the node-gtfs server endpoint that returns JSON-formatted list of stops within
	 * DEFAULT_SEARCH_RADIUS_STOPS miles of (lat, lon).
	 */
	public String getStopsByLoc(int lat, int lon) {
		return getStopsByLoc(lat, lon, DEFAULT_SEARCH_RADIUS_STOPS);
	}
	
	/**This method returns a URL that points to a node-gtfs API endpoint which would return a JSON-formatted Object, 
	 * containing data on predicted stop service times.
	 * @param agency The agency to query predictions for.
	 * @param route_id The route_id to query predictions for
	 * @param stop_id The stop_id to query predictions for
	 * @param direction_id Which direction to get predictions for (if null, will return both directions.
	 * @return URL String for node-gtfs API endpoint where Stop predictions can be found.
	 */
	public String getStopDetails(String agency, String route_id, String stop_id, String direction_id) {
		String retval = new String();
		retval += domainURL;
		retval += endPointMap[QUERY_STOP_DETAILS_BY_ROUTE];
		retval += agency;
		retval += "/";
		retval += route_id;
		retval += "/";
		retval += stop_id;
		
		if(direction_id != null) {
			retval += "/";
			retval += direction_id;
		}
		
		return(retval);
	}
	
	public String getStopDetails(String agency, String route_id, String stop_id) {
		return getStopDetails(agency, route_id, stop_id, null);
	}
	
	/**This method returns a URL that points to a node-gtfs API endpoint which would return a JSON-formatted Object, 
	 * containing data on predicted stop service times, given an agency and a stop_id. But right now it is not implemented
	 * since node-gtfs server does not support such queries.
	 * @param agency The agecy to query stop details for
	 * @param stop_id The stop to query details for
	 * @return URL String pointing to a node-gtfs API endpoint that will return predictions for a particular stop.
	 */
	public String getStopDetailsNoRoute(String agency, String stop_id) {
		return("Not implemented!");
	}
	
	/** Default constructor for this class. Takes only one argument, a domain name that points to the node-gtfs
	 * server that we're to fetch data from.
	 * 
	 * @param domain The name of the node-gtfs server that we will query data from. Example: 
	 * http://node-gtfs.myfuckingserver.com
	 */
	public NodeGtfsUrlMaker(String domain) {
		QUERY_TYPES = 6;
		this.domainURL = domain;
		
		//Make sure to initialize the endPointMap with default values
		populateEndPointMap();
	}
	
}
