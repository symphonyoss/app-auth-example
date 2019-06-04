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

## Contributing

1. Fork it (<https://github.com/symphonyoss/app-auth-example/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Read our [contribution guidelines](.github/CONTRIBUTING.md) and [Community Code of Conduct](https://www.finos.org/code-of-conduct)
4. Commit your changes (`git commit -am 'Add some fooBar'`)
5. Push to the branch (`git push origin feature/fooBar`)
6. Create a new Pull Request

## License

The code in this repository is distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright 2019 Symphony LLC