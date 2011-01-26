var userName = context.properties["userName"];
var admin = context.properties["admin"];
var addresses = context.properties["addresses"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");

// retrieve the web script index page
var response = connector.post("/api/people", "{ \"userName\":\"" + userName + "\", \"admin\":\"" + admin + "\", \"addresses\":\"" + addresses + "\"}", 'application/json');

model.userName = userName;
