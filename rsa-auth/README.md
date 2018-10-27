[![Symphony Software Foundation - Active](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating) [![Dependencies](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0?child=summary) [![Build Status](https://travis-ci.org/symphonyoss/app-auth-example.svg?branch=master)](https://travis-ci.org/symphonyoss/app-auth-example)

## RSA-based App Authentication Sample Application
This is an example back end application that provides an implementation of the 
[App Authentication Flow](https://extension-api.symphony.com/docs/application-authentication) used by
Symphony to authenticate javascript applications (and users) that use the Symphony Extension API. This implementation
uses RSA authentication with signed JWTs.

The application is built upon Spring Boot.  A Feign client is used to make calls to the Symphony REST API. 

There are examples for:
* Handling the webhook from Symphony for pod info updates
* Initiating the authentication flow and exchanging authentication tokens with the Symphony pod
* Validating the authentication tokens received from the Javascript app
* Logging into the app's back end using the signed JWT authenticated against the public signing cert from the Symphony pod.
* Correlating the application user with the Symphony user.

This is not meant to be production quality code.  Only minimal error handling code has been provided.

The actual endpoint URLs exposed by this application are only examples. They are either called by a matching Javascript
app or are called by Symphony via registering the URL (+ API key).

The AuthenticationClient is a [Feign](https://github.com/OpenFeign/feign) client which is configured to use an
OkHttp client.  Authentication to Symphony back end is done by creating a JWT that contains this app's ID and 
an expiration time, then signing that JWT with a private RSA key.  The public part of the RSA key is included in the 
metadata for the app when it is installed on the pod.  For ease of installing the app, there is a bundle file
in the `conf` folder that is preconfigured with a public key that matches the private key in the `conf` folder.

Also included is an example of user reconciliation.  If the user in the JWT is not recognized, the back
end server sends a response that causes the front end app to display a form which allows the user to input
their app specific user ID (and password if this were production code).  This values is returned to the back end
and, if validated, a mapping between Symphony user ID and app user ID is persisted.  This is a one-time operation.

This Spring Boot application has SSL enabled.  By default, it is using the self-signed cert in `conf/tomcat.keystore`. 
If you use this cert, you will need to configure your browser (or system) to trust the cert.  Or you
can change to use a cert that is signed by a trusted root.  This is configured in `application.yaml`.


#### Requirements
* Java 8+
* Maven 3.0+
* An app installed on the pod configured with RSA public key for this app.
* Matching private key for the app configured in `application.yaml`

#### Configuration
Generate a RSA key pair using RSA512 algorith with 4096 bits.  Instructions can be found in the [Symphony REST 
documentation](https://rest-api.symphony.com/reference#rsa-bot-authentication-workflow).  You can also use the 
script in `support/generate-keys.sh`.

From the AC Portal, add a custom app (App Management --> Add Custom App). Set "App Url" to the URL of where the App Auth
Example app will be running on your local machine (Ex. https://localhost.symphony.com:9443/index.html). You can
automatically fill out the form from a JSON document by clicking the "Import Application Bundle File" button.  The JSON
document for this app is located in the `conf` folder. Copy the App ID - you will need it later. Remember to enable the 
app you just created and make it visible.  You can do this from the App Settings.  This app needs to be running
when you enable the app in Symphony in order to be available to receive the webhook from the pod.  If you miss it, 
you can trigger the pod to send the webhook again by disabling and enabling the app again.  Or you could follow the
instruction below to use Postman to call the webhook endpoint directly.


In `src/main/resources/application.yaml`:
* Configure SSL for embedded tomcat server.  Default configuration is to use a self-signed cert packaged with this
application. If you do that, you will need to explicitly trust the certificate. You can do that by loading the index.html
page directly in the browser.  It won't display anything, but the browser will warn you about an untrusted site.  Trust it.
* Configure the keystore and truststore for the HTTP client.  
  * Set `symphony.client.truststore-filename`: Trust store containing trusted roots.  Allows HTTP client to create secure
    connections to Symphony pod. Since Symphony pods use valid SSL certs, you can just use the standard cacerts file
    provided by the Java JDK.
* Configure the app's authentication data
  * Set `authentication.private-key-pem-filename`: location of this app's private key in PEM format
* Configure the ID of the application
  * Set `app.app-id`: must match the ID used when adding the app to the pod
  * Set `app.base-url`: Host:port of this server.  Must be reachable from a browser.
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

By default, the server will open port 9443 for SSL connections.  This can be changed in `application.yaml`.

Once running, push pod info into the server by POSTing to the /podInfo endpoint using Postman or curl or similar. 
You can find your companyId by logging into the Symphony client and viewing the source code of index.html.  Look for a
Javascript object called 'window.env' and the copy the value of 'POD_ID'.

```
POST /podInfo HTTP/1.1
Host: localhost:9443
Content-Type: application/json
API-Key: super-secret-api-key-1234
Cache-Control: no-cache
Postman-Token: 054900b2-1b05-be3d-057f-f35040249449

{
	"appId" : "cert-app-auth-example",
	"companyId" : "your pod ID / company ID",
	"eventType" : "appEnabled",
	"payload" : {
		"agentUrl" : "https://your.agent.domain:443",
		"podUrl" : "https://your.agent.domain:443",
		"sessionAuthUrl" : "https://your.pod.domain:8444"
	}
}
```

Then Login to Symphony client.  Once logged in, go to the Symphony Market and look for your app. If your app is not 
visible, confirm you enabled it and made it visible in the AC portal. Install the app and click on the newly-created 
left nav item.

If this is the first time have opened the module and
your pod username is not either "dnathanson" or "jsmith" (defined in application.yaml), you will be prompted
for your username in the sample app.  Enter either "tjones" or "jsmith" and Save.  The server will
respond with Hello Tom Jones (or John Smith) and the mapping between Symphony username
and app username will be remembered until the app server is rebooted.
