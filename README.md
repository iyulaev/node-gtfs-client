node-gtfs-client: A Java-based node-gtfs Client

=-=-= Overview =-=-=

This project, node-gtfs-client, implements a Java-based client for fetching data
from a node-gtfs server, parsing the JSON data, and returning Java Objects
representing the data that was provided by the server.

The node-gtfs server can be found up on github here:
https://github.com/brendannee/node-gtfs

TransitDataFetcher.java is the top-level Java file for the node-gtfs-client 
codebase. The public methods in this file can be regarded as the API that this
project provides; see the Javadocs for an explicit listing of the methods 
provided and the argument/return types.

=-=-= Description of Data Types =-=-=
The base Java types that node-gtfs-client are described in this section.

Route - the Route type represents a transit route. A transit route has several
properties, including a name, an ID, a URL, and a route type.

SimpleStop - a SimpleStop represents a particular Stop that occurs on a transit
route. A SimpleStop has properties like parent Route, the intersection at which
it is located, the agency it belongs to, the location (and headsign) that it is
along, and the lat/lon where it is located.

Prediction - tbd

=-=-= API Examples =-=-=
An explicit listing of API functions (TransitDataFetcher public methods) is 
outside of the scope of this document. But, we will provide some examples of
API calls.

Fetch list of routes by agency:
Suppose we have an agency, Alameda Contra-Costa Transit, with agency ID 
ac-transit. Our node-gtfs server runs at www.mynodegtfsserver.com. We can fetch
a list of Routes on this agency by calling

TransitDataParser myTDP = new TransitDataParser \
	("http://www.mynodegtfsserver.com");
ArrayList<Route> listOfRoutes = myTDP.fetchRouteList("ac-transit");

Now, listOfRoutes contains a list of the routes available within this agency. 