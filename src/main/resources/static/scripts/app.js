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

// ID of pod/company - comes back from 'hello'
var podId;
var uiService;
var navService;
var modulesService;
var userService;
var jwt;



// Once tokens have been validated and trust between javascript apps is extablished, client extentsion app can call
// Extended User Info service to get a signed JWT that includes PII for the logged in user.  Pass this JWT to the
// backend server to establish user's identity at the server.
function login()
{
    userService = SYMPHONY.services.subscribe('extended-user-info');

    if (userService) {
        return userService.getJwt()
            .then(function(response)
            {
                jwt = response;
                var request =
                {
                    jwt : jwt,
                    podId : podId   // Must pass pod ID so that signature can be verified using certificate from pod
                };

                return ajax.call('/login-with-jwt', request, 'POST', 'application/json')
                    .then(function(data)
                    {
                        console.log("Response: ", data);
                        return data;
                    }.bind(this));

            });
    }

    return Q.reject(new Error("Could not login"));

}

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

// At this point, if userFound == false, we need to show the form to collect username then submit
// username, JWT and companyId (as a JSON structure) to /login-with-username
//
// If userFound == true, use userDisplayName to display a welcome message (or just show the message from
// the response which is a welcome message.

function setState(response) {
    if (!response.userFound) {
        $(document.body).addClass('login');
        $('#submit').click(saveUser);
    } else {
        $(document.body).removeClass('login');
        $('#welcome-message').text(response.message);
    }
}

function setError(message) {
    $('#error').html(message);
}

function clearError() {
   $('#error').html('');
}

function saveUser() {
    clearError();

    var username = $('#username').val();

    if (!username) {
        setError('You must enter a user name');
        return;
    }

    var request = {
        username: username,
        jwt: jwt,
        podId: podId
    };

    return ajax.call('/login-with-username', request, 'POST', 'application/json')
        .then(function(response) {
            $(document.body).removeClass('login');
            $('#welcome-message').text(response.message);
         })
        .fail(function(e) {
            setError('Cannot save username', username);
            return false;
        })
}

function theme(helloResponse) {
    setTheme(helloResponse.themeV2);
    return Q(helloResponse);
}

function focus()
{
    this.modulesService.focus('app-auth');
    this.navService.focus('app-auth-nav');
}


function setTheme(theme)
{
    $(document.body).removeClass(theme.classes.join(' '));
    $(document.body).addClass(theme.name);
    $(document.body).addClass(theme.size);
}

function connect(helloResponse) {
    podId = '' + helloResponse.pod;
    return SYMPHONY.application.connect(appId, ['ui', 'modules', 'applications-nav', 'extended-user-info'], [])
        .then(login)
        .then(function(loginResponse) {
            setState(loginResponse);

            uiService = SYMPHONY.services.subscribe('ui');
            navService = SYMPHONY.services.subscribe('applications-nav');
            modulesService = SYMPHONY.services.subscribe('modules');

            uiService.listen('themeChangeV2', function() {
                SYMPHONY.remote.hello().then(function(data) {
                    themeColor = data.themeV2.name;
                    themeSize = data.themeV2.size;
                    document.body.className = "symphony-external-app " + themeColor + " " + themeSize;
                });
            });

            focus();

            $(window).on('focus', function() {
                focus();
            }.bind(this));

        })
}

// All Symphony services are namespaced with SYMPHONY
SYMPHONY.remote.hello()
    .then(theme)
    .then(connect)
    .fail(function(e) {
        console.log(e.stack);
    });
