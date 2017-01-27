/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var appId = 'developerTestApp';

// application token, returned from /authentication, passed to /validateTokens
var appToken;

// ID of pod - comes back from 'hello'
var podId;

// Kicks off app authentication flow at server.  Passes pod ID which came from 'hello' call previously.
// Returns Symphony token.
function authenticate(response) {

    console.log('Response: ', response);
    // console.log('pod ID: ' + response);
    podId = 'nexus2';

    // /authenticate returns app token in body (only)
    return ajax.call('/authenticate', podId, 'POST', 'text/text')
        .then(function(data)
        {
            appToken = data;
            return Q({appId: appId, tokenA: data});
        }.bind(this));
}

// Sends the application token (and the symphony token) back to the server for validation.  In this implemetation,
// we are passing back both tokens so that back end can remain stateless. If a sticky session had been establishd,
// only the app token would be required since the Symphony token could have been stored in the session on the server.
function validate(response)
{
    var request = {
        podId : podId,
        symphonyToken : response.tokenS,
        appToken : appToken
    };

    return ajax.call('/validate-tokens', request, 'POST', 'application/json')
        .then(function(data)
        {
            console.log("Response: ", data);
        }.bind(this));
}

// Once tokens have been validated and trust between javascript apps is extablished, client extentsion app can call
// Extended User Info service to get a signed JWT that includes PII for the logged in user.  Pass this JWT to the
// backend server to establish user's identity at the server.
function login()
{
    userService = SYMPHONY.services.subscribe('extended-user-info');

    if (userService) {
        return userService.getJwt()
            .then(function(jwt)
            {
                var request =
                {
                    jwt : jwt,
                    podId : podId   // Must pass pod ID so that signature can be verified using certificate from pod
                };

                return ajax.call('/login-with-jwt', request, 'POST', 'application/json')
                    .then(function(data)
                    {
                        console.log("Response: ", data);
                    }.bind(this));

            });
    }

    return Q.reject(new Error("Could not login"));

}

var userId;
var uiService;
var navService;
var modulesService;
var userService;

function register(appData) {
    return SYMPHONY.application.register(appData, ['ui', 'modules', 'applications-nav', 'extended-user-info'], ['authexample:controller'])
        .then(validate)
        .then(login)
        .then(function(response) {
            // We only get here if login was successful.  In this case successful login means the JWT was valid.
            // It is possible that the Symphony user is not recognized by the application.  This is handled by
            // displaying login form for the application which allows a mapping from Symphony user ID to application
            // user ID to be recorded.  This only need to be done one time.

            uiService = SYMPHONY.services.subscribe('ui');
            navService = SYMPHONY.services.subscribe('applications-nav');
            modulesService = SYMPHONY.services.subscribe('modules');

            // LEFT NAV: Add an entry to the left navigation for our application
            navService.add("app-auth-nav", "App Auth Example", "authexample:controller");

            // Implement some methods on our local service. These will be invoked by user actions.
            controllerService.implement({

                // LEFT NAV & MODULE: When the left navigation item is clicked on, invoke Symphony's module service to show our application in the grid
                select: function (id) {
                    if (id == "app-auth-nav") {
                        // Focus the left navigation item when clicked
                        navService.focus("hello-nav");
                    }

                    modulesService.show("app-auth", {title: "App Auth Example"}, "authexample:controller", "https://localhost:8443/app.html", {
                        // You must specify canFloat in the module options so that the module can be pinned
                        "canFloat": true
                    });
                    // Focus the module after it is shown
                    modulesService.focus("app-auth");
                }
            });

            // Response contains user's display name
            console.log(response);



        })
}

var controllerService = SYMPHONY.services.register('authexample:controller');

// All Symphony services are namespaced with SYMPHONY
SYMPHONY.remote.hello()
    .then(authenticate)
    .then(register)
    .fail(function(e) {
        console.log(e.stack);
    });
