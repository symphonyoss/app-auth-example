package com.symphony.example.pods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to handle webhook callback from Symphony pod when app is enabled via Symphony Admin Portal.  The
 * callback includes information about the pod on which the app was installed.  It is stored in the PodDirectory.
 *
 * URL for this endpoint should be part of the app manifest.
 *
 * This endpoint is protected by an "API key" to prevent unauthorized access which could inject bogus pod information
 * into the PodDirectory. For this implementation, the API key is just a hardcoded constant.  Real implementations
 * probably want something more elaborate, maybe a different API key for each pod.
 *
 * Created by Dan Nathanson on 12/19/16.
 */
@RestController
public class DirectoryWebhookController {
    private final PodDirectory podDirectory;

    private WebhookConfiguration webhookConfiguration;


    @Autowired
    public DirectoryWebhookController(PodDirectory podDirectory, WebhookConfiguration webhookConfiguration) {
        this.podDirectory = podDirectory;
        this.webhookConfiguration = webhookConfiguration;
    }


    /**
     * Create / update pod info.  This endpoint is protected by an "API key" to prevent unauthorized access which
     * could inject bogus pod information into the PodDirectory. For this implementation, the API key is just a
     * hardcoded constant.  Real implementations probably want something more secure - at least a different API key
     * for each pod. The API key is passed as an HTTP header value with name "API-Key".
     *
     * @param podInfo PodInfo object containing metadata about a pod
     * @param apiKey API Key.  Comes from HTTP Header value "API-Key"
     *
     * @return HTTP 401 - if API key missing or invalid<br/>HTTP 400 - if PodInfo bad or missing<br/>HTTP 200 - otherwise.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/podInfo")
    public ResponseEntity updatePodInfo(@RequestBody PodInfo podInfo, @RequestHeader("API-Key") String apiKey) {

        // Check API key
        if (StringUtils.isEmpty(apiKey) || !webhookConfiguration.getApiKey().equals(apiKey)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        // Ensure pod info present and at least has pod ID.
        if (StringUtils.isEmpty(podInfo.getPodId())) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // Store pod info
        podDirectory.addPodInfo(podInfo);

        return new ResponseEntity(HttpStatus.OK);

    }
}
