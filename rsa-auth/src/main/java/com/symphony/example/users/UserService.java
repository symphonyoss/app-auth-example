/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.symphony.example.users;

import lombok.Data;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * User service backed by in-memory collection of Users  These are the registered users of this application.
 * They need to be correllated with Symphony users.  This can be done by asking the user to identify themsleves
 * (initiateAppAuth) to this application (e.g. username/password, SSO, etc) and then storing the Symphony ID in user
 * record of the now-authenticated user. This only needs to be done one time.
 *
 * @author Dan Nathanson
 */
@Component
@Data
public class UserService {

    private final Map<String, User> users = new HashMap<>();

    @Autowired
    public UserService(UserStore userStore) {
        userStore.getUsers().forEach(user -> users.put(user.getUsername(), user));
    }
    /**
     * Finds a user by Symphony ID.  If no user is found with specified ID, null is returned.
     *
     * @param symphonyId Users ID in Symphony
     * @return User record, or null if no record found with matching Symphony ID
     */
    public User findBySymphonyId(@NonNull String symphonyId) {
        return users.values()
                    .stream()
                    .filter(user -> symphonyId.equals(user.getSymphonyId()))
                    .findFirst()
                    .orElse(null);
    }

    /**
     * Gets a user by username.  If no user is found with username, it is an error (since user is expected to be
     * found).
     *
     * @param username Username in this application
     * @return User record
     * @throws UserNotFoundException if no user exists with specified username
     */
    public User get(@NonNull String username) throws UserNotFoundException {
        User user = users.get(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }
}
