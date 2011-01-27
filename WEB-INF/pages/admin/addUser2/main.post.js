var userName = context.properties["userName"];
var firstName = context.properties["firstName"];
var lastName = context.properties["lastName"];
var password = context.properties["password"];
var email = context.properties["email"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");

// retrieve the web script index page
var response = connector.post("/api/people", "{ \"userName\":\"" + userName + "\", \"firstName\":\"" + firstName + "\", \"lastName\":\"" + lastName + "\", \"password\":\"" + password + "\", \"email\":\"" + email + "\"}", 'application/json');

model.userName = userName;
