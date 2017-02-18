# Warren

Kotlin (JVM targeted), unit tested, IRC v3.2 state management and observing. Made for personal use, and open sourced. Intended to provide the tools needed to make IRC related software, like bridges and bots.

[Kale](https://github.com/WillowChat/Kale) is the parsing and serialising counterpart.

[Thump](https://github.com/WillowChat/Thump) is the primary upstream project - a bridge that lets people chat between Minecraft and IRC whilst they play.

[![codecov](https://codecov.io/gh/WillowChat/Warren/branch/develop/graph/badge.svg)](https://codecov.io/gh/WillowChat/Warren)

## Features

Warren and Kale have a few interesting features:

* The responsibilities of parsing and state management are separated
* Both parsing and state management are verified by hundreds of unit tests
* Messages, and state handlers, are individually encapsulated

Planned releases (and their features) are tracked in [Projects](https://github.com/WillowChat/Warren/projects).

## Example usage

The project includes a simple [example runner](https://github.com/WillowChat/Warren/blob/develop/src/main/kotlin/chat/willow/warren/WarrenRunner.kt) that prints out events as they happen, logs in using SASL and replies to me saying `rabbit party` in a channel.

If you're interested in more complex usage, come talk to me on IRC: #carrot on [ImaginaryNet](http://imaginarynet.uk/)

## TODO

* [IRC v3](http://ircv3.net/irc/)
 * Goal is to support almost all IRCv3 extensions by default - progress is tracked on the libraries section of the IRCv3 site: http://ircv3.net/software/libraries.html

## Code License
The source code of this project is licensed under the terms of the ISC license, listed in the [LICENSE](LICENSE.md) file. A concise summary of the ISC license is available at [choosealicense.org](http://choosealicense.com/licenses/isc/).

## Building
This project uses Gradle for pretty easy setup and building.

* **Setup**: `./gradlew clean`
* **Building**: `./gradlew clean build` - this will also produce a fat Jar with shaded dependencies included

## Usage

Warren is published on my personal Maven repository. It uses a [Let's Encrypt](https://letsencrypt.org/) certificate, and you need to use Java 8 >= 8u101 or Java 7 >= 7u111 to connect to it.

### Gradle

* Add to `repositories`: ` maven { url = "https://maven.ci.carrot.codes" }`
* Add to `dependencies`: `compile 'chat.willow.warren:Warren:1.4.0'`
