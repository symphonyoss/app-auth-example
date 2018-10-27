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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit test for UserStore
 *
 * @author Dan Nathanson
 */
public class UserServiceTest {
    private UserService userService;

    @Before
    public void setup() {
        UserStore userStore = new UserStore();
        userStore.getUsers().add(new User("John Doe", "johndoe"));
        userStore.getUsers().add( new User("Tom Smith", "tomsmith"));
        userService = new UserService(userStore);

    }

    @Test
    public void testGet() throws Exception {
        assertThatThrownBy(() -> userService.get("foo"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("foo");

        User johndoe = userService.get("johndoe");
        assertThat(johndoe).isNotNull();
        assertThat(johndoe.getDisplayName()).isEqualTo("John Doe");
    }

    @Test
    public void testFindBySymphonyId() throws UserNotFoundException {
        User johnBySymphonyId = userService.findBySymphonyId("jdsymphony");
        assertThat(johnBySymphonyId).isNull();

        User johnByUsername = userService.get("johndoe");
        assertThat(johnByUsername).isNotNull();

        johnByUsername.setSymphonyId("jdsymphony");

        johnBySymphonyId = userService.findBySymphonyId("jdsymphony");
        assertThat(johnBySymphonyId).isNotNull();
        assertThat(johnBySymphonyId).isSameAs(johnBySymphonyId);
    }
}