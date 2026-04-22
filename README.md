# Smart Campus

**API Overview**
My Smart Campus API is a RESTful web service that lets managers create and manage rooms and sensors across a university campus. The API includes features like creating rooms, adding sensors to specific rooms, filtering sensors by type, keeping a history of sensor readings, and preventing users from deleting a room if it still has active sensors inside it. The API also returns helpful error messages with the correct HTTP status codes, such as 409 and 422. The base URL for the API is http://localhost:8080/SmartCampus/api/v1/.

**Build and Launch Instructions**
To build and run the Smart Campus API, first open the project in NetBeans IDE. Right-click on the project name and select Clean and Build to compile the code. After the build is successful, right-click the project again and select Run. This will deploy the API to the Tomcat server and start it. Wait for the console to say "Server started", which means the API is now running. To test if it works, open Postman or your browser and go to http://localhost:8080/SmartCampus/api/v1/. You should see a JSON response with the API version and links to rooms and sensors. 


**Part 1: Service Architecture & Setup**

**Project & Application Configuration:**
The default lifecycle of a JAX-RS resource class is determined by when a request is made. This means a new instance of the resource class is instantiated for every incoming request, rather than being treated as a singleton that serves all requests. Since each request creates a new instance, instance variables can't be used to store data across multiple requests because they would be lost when the request ends. Therefore, to maintain data across the entire application, I made my data storage maps as public static variables, which are shared among all instances of the resource class. 
I also used ConcurrentHashMap instead of a regular HashMap, to take into consideration the chance of multiple requests arriving simultaneously. ConcurrentHashMap prevents data loss when multiple users access the API at the same time, unlike a regular HashMap which could crash or corrupt data during simultaneous requests.

**The ”Discovery” Endpoint:**
HATEOAS makes the API self-discoverable, instead of clients needing to know all endpoint URLs in advance. The API provides relevant links within each response, telling the client what actions they can take next. This follows the original REST principle that a client would only need a starting URL and then discover all other resources through hyperlinks.

HATEOAS reduces how dependent different parts of a system are on each other, between the client and server as clients do not hardcode URLs. Also, if the server changes its URL structure, the client continues working as long as the link relationships remain the same. With HATEOAS, the API itself tells the client what to do next, making integration faster and more reliable.


**Part 2: Room Management**

**Room Resource Implementation:**
When you return a list of rooms, sending only the IDs uses less data and is faster, but the client has to do extra work to get the full details. Sending full room objects gives all the information right away, but it uses more bandwidth and can be slower.
Returning full room objects consumes more network bandwidth because each response includes all fields (id, name, capacity, sensorIds) for every room. However, this approach reduces client-side processing because the client receives all the data it needs in a single request and can display it immediately without making additional API calls.

Returning only IDs uses significantly less network bandwidth since only a small identifier is sent per room. This is more efficient for large datasets and slow network connections. However, the client would then need to make a separate GET /rooms/{id} request for each room to retrieve the full details. 

**Room Deletion & Safety Logic**
My DELETE operation is idempotent because multiple identical requests lead to the same final state. The first request deletes the room, and any requests sent after simply confirm the room no longer exists by returning 404. The end result is always the same as the room is gone.
If the room has sensors, both the first and second DELETE will return the same 409 Conflict error because the room cannot be deleted. The state would not change. So no matter how many times the client sends the same DELETE request, the outcome is consistent.

**Part 3: Sensor Operations & Linking**

**Sensor Resource & Integrity:**
When a client sends data in a format that does not match the @Consumes(MediaType.APPLICATION_JSON) annotation, JAX-RS automatically rejects the request and returns a 415 Unsupported Media Type error. The server does not attempt to parse or process the request body because it cannot convert the incoming format (e.g., XML or plain text) into a Java object. For example, if a client sends text/plain or application/xml instead of application/json, JAX-RS checks the request's Content-Type header against the declared @Consumes value. Since they do not match, the server immediately responds with HTTP status 415 before any of my resource method code is executed. This protects my API from receiving unprocessable data and ensures that only properly formatted JSON requests are accepted.

**Filtered Retrieval & Search:**
Using @QueryParam for filtering is generally better because query parameters are designed for optional filtering and searching. For example, GET /api/v1/sensors?type=CO2 clearly shows that the client is requesting a list of sensors filtered by type. Query parameters also make it easy to include multiple filters, such as ?type=CO2&status=ACTIVE, and their order does not matter. In contrast, using a path like /api/v1/sensors/type/CO2 can be confusing because path parameters are meant to represent specific resources, making it look like “type/CO2” is a resource rather than a filter. Additionally, query parameters are more flexible, as new filters can be added without changing the URL structure or breaking existing clients. For these reasons, query parameters are considered a more RESTful and practical approach for filtering and searching collections.

**Part 4: Deep Nesting with Sub - Resources**

**The Sub-Resource Locator Pattern:**
The Sub-Resource Locator pattern provides architectural benefits by improving how responsibilities are organised in an API. Instead of placing all logic in a single large controller, the pattern separates functionality into a parent class and dedicated child classes for nested resources.
This makes the code easier to read, navigate and test, since each class can be developed and tested independently. It also improves maintainability as changes to one area (such as sensor readings) can be made without affecting other parts of the API. This reduces the risk of bugs. Additionally, the pattern creates a natural alignment between URL structure and code structure, helping developers better understand the API design.
Overall, the Sub-Resource Locator pattern helps manage complexity by keeping classes small, focused, and easy to test. These benefits are difficult to achieve with a single, monolithic controller.


**Part 5: Advanced Error Handling, Exception Mapping & Logging**

**Resource Conflict (409)**
HTTP 422 (Unprocessable Entity) is more appropriate than HTTP 404 (Not Found) when the issue is a missing reference inside a valid JSON payload because the problem is with the data, not the endpoint. A 404 error is used when the requested resource or URL does not exist. However, in this case, the client is sending a request to a valid endpoint with correctly structured JSON. The server understands the request, but it cannot process it because the provided roomId does not match any existing resource. This makes it a semantic error rather than a missing resource. Therefore, HTTP 422 is more accurate because it clearly indicates that the request is valid in structure but contains incorrect or invalid data, helping the client understand what needs to be fixed.

**The Global Safety Net (500)**
From the stack traces, attackers can learn internal file paths. Stack traces reveal the exact file locations on the server, such as where your Java classes are stored. This tells attackers your operating system and directory structure. Additionally, they learn your code structure. Line numbers in stack traces tell attackers exactly where the code failed. They can use this to understand the application logic and identify potential weak points to exploit.
They may learn database information which is a security risk. If a database error occurs, the stack trace might reveal table names, column names, or even parts of SQL queries, helping attackers understand your data model.
My GlobalExceptionMapper  catches all unexpected exceptions and returns only a generic message like "An unexpected internal server error occurred". The real stack trace is printed to the server log, where only administrators can see it, but it is never sent to the client. This means attackers receive no useful information about my server. 

**API Request & Response Logging Filters**
Using JAX-RS filters for logging is better than manually adding Logger.info() in every resource method because it avoids code duplication and keeps the code clean. Instead of repeating logging statements in each method, you write the logging logic once in a filter, and it automatically applies to all incoming requests and outgoing responses. This makes the code easier to maintain since any change to the logging format only needs to be done in one place. It also ensures consistent logging across the entire application and prevents mistakes like forgetting to add logs in some methods. As a result, resource classes stay focused on business logic only, improving readability and following good separation of concerns.



**Curl Commands**

**Discovery Endpoint (GET)**
GET http://localhost:8080/SmartCampus

**API Discovery (GET)**
GET http://localhost:8080/SmartCampus/api/v1/


**Create a Room (POST)**
POST http://localhost:8080/SmartCampus/api/v1/rooms/
{
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50
}

**Create a sensor (POST)**
POST http://localhost:8080/SmartCampus/api/v1/sensors
{
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
}

**Get All Rooms (GET)**
GET http://localhost:8080/SmartCampus/api/v1/rooms
{
    "id":"LIB-301",
    "name":"Library Quiet Study",
    "capacity":50
  }


**Delete sensor (DELETE)**
DELETE http://localhost:8080/SmartCampus/api/v1/sensors/TEMP-001
204 No content
