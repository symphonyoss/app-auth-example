This is an example back end application that provides an implementation of the [App Authentication Flow](TBD) used by
Symphony to authenticate javascript applications (and users) that use the Symphony Extension API.

The application is build using Spring Boot.  A Feign client is used to make calls the Symphony REST API.  The client
is configured to use client certificate authentication.

There are examples for:
* Handling webhook from Symphony for pod info updates 
* Initiating authentication flow exchanging authentication tokens with the Symphony pod
* Validating authentication tokens received from the Javascrip app
* Logging in using signed JWT authenticated against public signing cert from Symphony pod.

This is not meant to be production quality code.  Only minimal error handling code has been provided.

The actual endpoint URLs exposed by this application are only examples. They are either called by a matching Javascript
app or are called by Symphony via registering the URL (+ API key) when the app is installed on the pod.

The AuthenticationClient is a [Feign](https://github.com/OpenFeign/feign) client which is configured to use an
OkHttp client that is set up for client certificate authentication.  The values for keystore and truststore are 
parameterized and can be configured in the application.yaml file.  The pod must have a public cert uploaded that allows
it to trust the client cert. The subject of the client cert must be the app ID of the installed app.

Not included is any example of user reconciliation.  Typically, if the user in the JWT is not recognized, the back
end server would send a response that causes the front end app to display a form which allows the user to input
their app specific user ID (and password).  These values are returned to the back end and if validated, a mapping
between Symphony user ID and app user ID is persisted.  This is a one-time operation.