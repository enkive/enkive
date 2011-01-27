var userName = context.properties["userName"];
var admin = context.properties["admin"];
var can_read_all = context.properties["can_read_all"];
var addresses = context.properties["addresses"];

// get a connector to the Alfresco repository endpoint
var connector = remote.connect("alfresco");

// retrieve the web script index page
var response = connector.post("/enkive/permissions/add", "{\"userName\":\"" + userName + "\", \"admin\":\"" + admin + "\", \"can_read_all\":\"" + can_read_all + "\", \"addresses\":\"" + addresses + "\"}", 'application/json');

model.userName = userName;
