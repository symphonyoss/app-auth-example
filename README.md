[![Symphony Software Foundation - Active](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating) [![Dependencies](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0?child=summary) [![Build Status](https://travis-ci.org/symphonyoss/app-auth-example.svg?branch=master)](https://travis-ci.org/symphonyoss/app-auth-example)

## App Authentication Sample Application
This is an example back end application that provides an implementation of the [App Authentication Flow](TBD) used by
Symphony to authenticate javascript applications (and users) that use the Symphony Extension API.

The application is built upon Spring Boot.  A Feign client is used to make calls to the Symphony REST API.  The client
is configured to use client certificate authentication.

There are examples for:
* Handling the webhook from Symphony for pod info updates
* Initiating the authentication flow and exchanging authentication tokens with the Symphony pod
* Validating the authentication tokens received from the Javascript app
* Logging in using the signed JWT authenticated against the public signing cert from the Symphony pod.
* Correlating the application user with the Symphony user.

This is not meant to be production quality code.  Only minimal error handling code has been provided.

The actual endpoint URLs exposed by this application are only examples. They are either called by a matching Javascript
app or are called by Symphony via registering the URL (+ API key) when the app is installed on the pod (1.46+).

The AuthenticationClient is a [Feign](https://github.com/OpenFeign/feign) client which is configured to use an
OkHttp client that is set up for client certificate authentication.  The values for keystore and truststore are
parameterized and can be configured in the application.yaml file.  The pod must have a public cert uploaded that allows
it to trust the client cert. The subject of the client cert must be the app ID of the installed app.

Not included is any example of user reconciliation.  Typically, if the user in the JWT is not recognized, the back
end server would send a response that causes the front end app to display a form which allows the user to input
their app specific user ID (and password).  These values are returned to the back end and if validated, a mapping
between Symphony user ID and app user ID is persisted.  This is a one-time operation.

This example requires a Symphony pod with at least release 1.45 deployed.  Also, in 1.45, the callback/webhook that
pushes pod info into this application is not implemented so Postman (or similar) must be used to push pod info into
the server after it is started.  An example Postman collection is provided (App Auth.postman_collection).  You will
need to edit it with your own app and pod info.

This Spring Boot application has SSL enabled.  By default, it is using the self-signed cert in conf/keystore.p12. 
If you use this cert, you will need to configure your browser (or system) to trust the cert.  Or you
can change to use a cert that is signed by a trusted root.  This is configured in application.yaml.


#### Requirements
* Java 8+
* Maven 3.0+
* Pod at least at 1.45
* An app installed on the pod
* A certificate for your app (with the Subject common name matching app ID) with signing cert uploaded to the pod
* Keystore with private cert for your app (configure location in application.yaml)

#### Configuration
From the AC Portal, add a custom app (App Management --> Add Custom App). Set "App Url" to the URL of where the App Auth Example app will be running on your local machine (Ex. https://localhost.symphony.com:8443/index.html). After you save the app, click on the app
name in the App Management screen and copy the App ID (which will be a generated alphanumeric string). Remember to enable the app you just created and make it visible for the user with which you plan to test.

Generate a PKCS12 cert file for your app with the Subject common name matching the App ID.  The signing cert for this cert 
must be uploaded to the pod (AC Portal --> Manage Certificates --> Import)

In src/main/resources/application.yaml:
* Configure SSL for embedded tomcat server.  Default configuration is to use a self-signed cert packaged with this
application. If you do that, you will need to explicitly trust the certificate. You can do that by loading the index.html
page directly in the browser.  It won't display anything, but the browser will warn you about an untrusted site.  Trust it.
* Configure the keystore and truststore for the HTTP client.  
  * Set symphony.client.keystoreFilename: PKCS12 file generated above
  * Set symphony.client.keystorePassword: Password used to generate cert above
  * Set symphony.client.keystoreFilename: Trust store containing trusted roots.  Allows HTTP client to create secure
    connections to Symphony pod. Since Symphony pods use valid SSL certs, you can just use the standard cacerts file
    provided by the Java JDK.
* Configure the ID of the application
  * Set app.appId: alphanumeric string generated when custom app installed
* Configure users. These are the users of this application. There are two default users: 'dnathanson' and 'jsmith'.
  
#### Build

```
mvn clean install
```

#### Run

You can either build an executable jar and run that
```
mvn package
java -jar target/app-auth-example-0.0.1-SNAPSHOT.jar
```
or you can use Maven to run the application directly
```
mvn spring-boot:run
```

By default, the server will open port 8443 for SSL connections.  This can be changed in application.yaml.

Once running, push pod info into the server by POSTing to the /podInfo endpoint using Postman or curl or similar. 
You can find your companyId by logging into the Symphony client and viewing the source code of index.html.  Look for a
Javascript object called 'window.env' and the copy the value of 'POD_ID'.

```
POST /podInfo HTTP/1.1
Host: localhost:8443
Content-Type: application/json
API-Key: super-secret-api-key-1234
Cache-Control: no-cache
Postman-Token: 054900b2-1b05-be3d-057f-f35040249449

{
	"appId" : "your-app-Id",
	"companyId" : "your pod ID / company ID",
	"eventType" : "APP_ENABLED",
	"payload" : {
		"agentUrl" : "https://your.agent.domain:443",
		"podUrl" : "https://your.agent.domain:443",
		"sessionAuthUrl" : "https://your.pod.domain:8444"
	}
}
```

Then Login to Symphony client.  Once logged in, go to the Symphony Market and look for your app. If your app is not visible, confirm you enabled it and made it visible in the AC portal. Install the app and click on the newly-created left nav item.

If this is the first time have openend the module and
your pod username is not either "dnathanson" or "jsmith" (defined in application.yaml), you will be prompted
for your username in the sample app.  Enter either "dnathanson" or "jsmith" and Save.  The server will
respond with Hello Dan Nathanson (or John Smith) and the mapping between Symphony username
and app username will be remembered until the app server is rebooted.
