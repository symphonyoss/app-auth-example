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

package com.symphony.example.pods;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/**
 * Unit test for PodDirectory.
 *
 * Created by Dan Nathanson on 1/2/17.
 */
public class PodDirectoryTest {
    private PodDirectory podDirectory;

    @Before
    public void setup() {
        podDirectory = new PodDirectory();
    }

    @Test
    public void getPodInfo() throws Exception {
        assertThatThrownBy(() -> podDirectory.getPodInfo("pod-id"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pod-id");

        PodInfo added = new PodInfo();
        added.setPodId("pod-id");
        added.setPodHost("pod-host");
        added.setAgentHost("agent-host");

        podDirectory.addPodInfo(added);

        PodInfo returned = podDirectory.getPodInfo("pod-id");
        assertThat(returned).isEqualTo(added);
    }

}