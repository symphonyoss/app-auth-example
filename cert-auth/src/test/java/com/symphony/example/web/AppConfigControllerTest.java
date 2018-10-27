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

package com.symphony.example.web;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for AppConfigController.
 *
 * @author Dan Nathanson
 */
public class AppConfigControllerTest {
    private AppConfigController controller;

    @Before
    public void setup() {
        AppConfig appConfig = new AppConfig();
        appConfig.setAppId("app-id");
        appConfig.setBaseUrl("base-url");
        controller = new AppConfigController(appConfig);
    }

    @Test
    public void testAppConfig() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        PrintWriter writer = new PrintWriter(byteArrayOutputStream);
        when(response.getWriter()).thenReturn(writer);

        controller.appConfig(null, response);

        assertThat(byteArrayOutputStream.toString()).isEqualTo("var appId = 'app-id';\n" +
                "var baseUrl = 'base-url';\n");
    }
 }