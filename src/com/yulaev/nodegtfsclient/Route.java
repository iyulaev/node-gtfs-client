package com.yulaev.nodegtfsclient;
import org.json.*;

public class Route implements Comparable {
	private String sName, lName, routeURL, routeId, agencyId;
	private int route_type;
	private boolean isValid; //isValid gets set to true is the route is "well formed", i.e. if all data members are defined
	
	//This "enum" of route type is based on the GTFS ??? spec
	private static final String [] ROUTE_TYPES = {"Tram", "Metro", "Rail", "Bus", "Ferry", "CableCar", "Gondola", "Furnicular"};
	
	public Route(String route_id, String sName, int route_type, String routeURL) {
		this.routeId = routeId;
		this.route_type = route_type;
		this.sName = sName;
		this.lName = sName;
		this.routeURL = routeURL;
		isValid = true;
	}
	
	/** This constructor gets used to put together a Route object from a general JSONObject
	 * @param jsonRoute JSONObject representing data that describes this Route
	 */
	public Route(JSONObject jsonRoute) {
		try {
			if(jsonRoute.has("route_id")) this.routeId = jsonRoute.getString("route_id");
			if(jsonRoute.has("route_type")) this.route_type = jsonRoute.getInt("route_type");
			
			if(jsonRoute.has("route_short_name") && jsonRoute.has("route_long_name")) {
				this.sName = jsonRoute.getString("route_short_name");
				this.lName = jsonRoute.getString("route_long_name");
			}
			else if (jsonRoute.has("route_short_name") && !jsonRoute.has("route_long_name")) {
				this.sName = jsonRoute.getString("route_short_name");
				this.lName = this.sName;
			}
			else if (!jsonRoute.has("route_short_name") && jsonRoute.has("route_long_name")) {
				this.lName = jsonRoute.getString("route_long_name");
				this.sName = this.lName;
			}
				
			if(jsonRoute.has("agency_id")) this.agencyId = jsonRoute.getString("agency_id");
			if(jsonRoute.has("route_url")) this.routeURL = jsonRoute.getString("route_url");
			this.isValid = true;
		} catch (JSONException e) {
			System.err.println(e);
		}
	}
	
	public boolean isValid() { return this.isValid; }
	
	/** Stupid string representation of this Route object, really just useful for simple testing */
	public String toString() {
		String returned = new String("");
		
		returned += "{Route: route_id=";
		returned += routeId;
		returned += ", route_type=\"";
		returned += ROUTE_TYPES[this.route_type];
		returned += "\", route_sName=\"";
		returned += this.sName;
		if(this.agencyId != null) {
			returned += "\", route_agency_id=\"";
			returned += this.agencyId;
		}
		returned += "\"}";
		
		return(returned);
	}

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
