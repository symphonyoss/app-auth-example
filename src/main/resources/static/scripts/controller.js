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

function authenticate() {
    var data = {
    };

    return ajax.call('', data)
        .then(function(data)
        {
            return Q({appId: appId, tokenA: ''});
        }.bind(this));
},

function validate(tokenS)
{
    var data = {
    };

    return ajax.call('', data)
        .then(function(data)
        {
        }.bind(this));
}

var userId;
var uiService;
var navService;
var modulesService;
var userService;

function register(appData) {
    return SYMPHONY.application.register(appData, ['ui', 'modules', 'applications-nav', 'extended-user-info'], ['authexample:controller'])
        .then(validate)
        .then(function(response) {
            userId = response.userReferenceId;
            uiService = SYMPHONY.services.subscribe('ui');
            navService = SYMPHONY.services.subscribe('applications-nav');
            modulesService = SYMPHONY.services.subscribe('modules');
            userService = SYMPHONY.services.subscribe('extended-user-info');

            if (userService) {
                userService.getJwt()
                    .then(function(jwt)
                    {
                        var base64Url = jwt.split('.')[1];
                        var base64 = base64Url.replace('-', '+').replace('_', '/');
                        console.log('jwt', JSON.parse(atob(base64)));
                    });
            }
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
