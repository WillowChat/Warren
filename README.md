# Warren

Kotlin IRC state tracking counterpart to [Kale](https://github.com/CarrotCodes/Kale).

## Why is this better than other IRC libraries?

Warren and Kale have a few advantages over other IRC libraries:

* The responsibilities of parsing and state management are separated
* Both parsing and state management are verified by hundreds of unit tests
* Messages, and state handlers, are individually encapsulated - no enormous, unverifiable disaster zones

## TODO

* [RFC 1459](https://tools.ietf.org/html/rfc1459)
 * Essentials are done - last remaining thing is MODE tracking
* [IRC v3](http://ircv3.net/irc/)
 * 3.1 done
 * Goal is full 3.1 and 3.2 compliance by default

## Code License
The source code of this project is licensed under the terms of the ISC license, listed in the [LICENSE](LICENSE.md) file. A concise summary of the ISC license is available at [choosealicense.org](http://choosealicense.com/licenses/isc/).

## Building
This project uses Gradle and IntelliJ IDEA for pretty easy setup and building.

Basic usage:
* **Setup**: `./gradlew clean idea`
* **Building**: `./gradlew clean build` - this will also produce a fat Jar with shaded dependencies included
