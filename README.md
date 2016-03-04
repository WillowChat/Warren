Warren
=====

A Kotlin IRC framework for personal use (but open-sourced). Attempts to achieve good separation of concerns and testability by splitting message parsing and handling. Notifies consumers of events using an event bus.

# Why?

* Personal interest,
 * Implementing a protocol,
 * Producing and testing a library,
 * Scope for both simple and complex goals.
* Personal use,
 * Minecraft mods,
 * Twitch bots,
 * GitHub helper bots.
* Fun.

# Project Goals

* [RFC 1459](https://tools.ietf.org/html/rfc1459)
* [IRC v3](http://ircv3.net/irc/)
* Get more widely used
* GitHub notifier bot?

## Code License
The source code of this project is licensed under the terms of the ISC license, listed in the [LICENSE](LICENSE.md) file. A concise summary of the ISC license is available at [choosealicense.org](http://choosealicense.com/licenses/isc/).

## Building
This project uses Gradle and IntelliJ IDEA for pretty easy setup and building. There are better guides around the internet for using them, and I don't do anything particularly special.

Basic usage:
* **Setup**: `./gradlew clean idea`
* **Building**: `./gradlew clean build` - this will also produce a fat Jar with shaded dependencies included