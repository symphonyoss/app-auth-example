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

// ID of pod - comes back from 'hello'
var podId;



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

function connect(helloResponse) {
    podId = '' + helloResponse.pod;
    return SYMPHONY.application.connect(appId, ['ui', 'modules', 'applications-nav', 'extended-user-info'], [])
        .then(login)
        .then(function(loginResponse) {

            // We only get here if login was successful.  In this case successful login means the JWT was valid.
            // It is possible that the Symphony user is not recognized by the application.  This is handled by
            // displaying login form for the application which allows a mapping from Symphony user ID to application
            // user ID to be recorded.  This only need to be done one time.


            // Response looks like:
            // {
            //     jwtValid : true | false,
            //     userFound : true | false,
            //     message : "Some message",
            //     userDisplayName : "application's user display name" | null
            // }
            console.log(loginResponse);

            // At this point, if userFound == false, we need to show the form to collect username then submit
            // username, JWT and podId (as a JSON structure) to /login-with-username
            //
            // If userFound == true, use userDisplayName to display a welcome message (or just show the message from
            // the response which is a welcome message.

            // Are these needed?
            uiService = SYMPHONY.services.subscribe('ui');
            navService = SYMPHONY.services.subscribe('applications-nav');
            modulesService = SYMPHONY.services.subscribe('modules');

        })
}

// All Symphony services are namespaced with SYMPHONY
SYMPHONY.remote.hello()
    .then(connect)
    .fail(function(e) {
        console.log(e.stack);
    });
