[![Symphony Software Foundation - Active](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating) [![Dependencies](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0?child=summary) [![Build Status](https://travis-ci.org/symphonyoss/app-auth-example.svg?branch=master)](https://travis-ci.org/symphonyoss/app-auth-example)

##App Authentication Sample Application
This is an example back end application that provides an implementation of the [App Authentication Flow](TBD) used by
Symphony to authenticate javascript applications (and users) that use the Symphony Extension API.

The application is built upon Spring Boot.  A Feign client is used to make calls the Symphony REST API.  The client
is configured to use client certificate authentication.

There are examples for:
* Handling webhook from Symphony for pod info updates
* Initiating authentication flow exchanging authentication tokens with the Symphony pod
* Validating authentication tokens received from the Javascrip app
* Logging in using signed JWT authenticated against public signing cert from Symphony pod.
* Correlating application user with Symphony user.

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
pushes pod info into this application is not implemented so Postman (or simililar) must be used to push pod info into
the server after it is started.  An example Postman collection is provided (App Auth.postman_collection).  You will
need to edit it with your own app and pod info.

This Spring Boot application has SSL enabled.  By default, it is using the self-signed cert in conf/keystore.p12. 
If you use this cert, you will need to configure your browser (or system) to trust the cert.  Or you
can change to use a cert that is signed by a trusted root.  This is configured in application.yaml.


####Requirements
* Java 8+
* Maven 3.0+
* Pod at least at 1.45
* An app installed on the pod
* A certificate for your app (with Subject matching app name) with public cert uploaded to the pod
* Keystore with private cert for your app (configure location in application.yaml)

####Build

```
mvn clean install
```

####Run

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

```
POST /podInfo HTTP/1.1
Host: localhost:8443
Content-Type: application/json
API-Key: super-secret-api-key-1234
Cache-Control: no-cache
Postman-Token: 054900b2-1b05-be3d-057f-f35040249449

{
	"appId" : "your-app-name",
	"companyId" : "your pod ID / company ID",
	"eventType" : "APP_ENABLED",
	"payload" : {
		"agentUrl" : "https://your.agent.domain:443",
		"podUrl" : "https://your.agent.domain:443",
		"sessionAuthUrl" : "https://your.pod.domain:8444"
	}
}
```

Then Login to Symphony client.  Once logged in, go into developer mode by adding the 'bundle' query string argument
(assumes you are running the server on localhost)

```
https://your.pod.domain/client/index.html?bundle=https://localhost:8443/json/bundle.json#
```

You will get a warning dialog about "Unauthorized App(s)". Verify and continue.  If you don't get the warning
dialog, it is probably because the SSL connection to localhost cannot be established, especially if you are 
using the default self-signed cert.  Try accessing the bundle file directly by pasting the bundle
URL directly into your browsers address bar.  That should give you an "untrusted cert" warning.  Follow the 
browser's instructions to trust the cert.  Once you can get the bundle file to load by itself, try the
full pod URL above again.

You will see "App Auth Example" application show up under "Applications" on the left nav.

Click on "App Auth Example" to open a module.  If this is the first time have openend the module and
your pod username is not one of "dnathanson" or "jsmith" (defined in application.yaml), you will be prompted
for your username in the sample app.  Enter either "dnathanson" or "jsmith" and Save.  The server will
respond with Hello Dan Nathanson (or John Smith) and the mapping between Symphony username
and app username will be remembered until the app server is rebooted.
