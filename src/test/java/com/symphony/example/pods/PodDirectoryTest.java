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