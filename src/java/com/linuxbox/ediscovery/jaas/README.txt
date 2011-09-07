to run:
build, then go to ~/sessionServlet

Developing Note:
1 )The main class is in LoginServlet in the doGet()
Need to check/set 2 properties located around lines 139-140 "java.security.auth.login.config" and "LDAP_PROPERTIES" to wherever the files are located at
By default, those 2 files are in the same package here

2) ldap.properties - LDAP environment configurations

3) SamplePrinciple and sample_jaas is there for now for future implementation of action/principle for authorization.