[![Symphony Software Foundation - Active](https://cdn.rawgit.com/symphonyoss/contrib-toolbox/master/images/ssf-badge-incubating.svg)](https://symphonyoss.atlassian.net/wiki/display/FM/Incubating) [![Dependencies](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58accf374ca76f00331ce1c0?child=summary) [![Build Status](https://travis-ci.org/symphonyoss/app-auth-example.svg?branch=master)](https://travis-ci.org/symphonyoss/app-auth-example)

## App Authentication Sample Applications

This repository contains two implementation of the 
[App Authentication Flow](https://extension-api.symphony.com/docs/application-authentication) used by
Symphony to authenticate javascript applications (and users) that use the Symphony Extension API. The two examples
are nearly identical, except of the method in which they initiate the flow the the Symphony pod.  One example
uses X.509 client certificates and the other example uses a JWT signed with an RSA key.  The examples are completely
independent, stand-alone modules.

* [RSA Authentication](rsa-auth/README.md)
* [Certificate Authentication](cert-auth/README.md)