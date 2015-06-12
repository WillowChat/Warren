warren
=====

A Java IRC framework for personal use (but open-sourced). Attempts to achieve good separation of concerns and testability by splitting message parsing and handling. Notifies consumers of events using an event bus.

# Why?

* Personal interest,
 * Implementing a protocol,
 * Getting better at testing,
 * Producing a framework,
 * Testing a framework,
 * Scope for both simple and complex goals.
* Personal use,
 * Twitch bots,
 * GitHub helper bots.
* Fun.

# TODO

[RFC 2812](https://tools.ietf.org/html/rfc2812)

* **Make relevant TODOs Issues**
* ~~Server Connection cleanup / separation~~,
 * Still needs cleaning up a little,
* ~~Combine incoming and outgoing messages~~,
* ~~NickServ / password / file?~~,
* ~~High level events, not protocol dependent (channel message, PM, join, leave, etc)~~,
 * Expand events support
* Add concept of "acceptable next commands" for use in MOTD parsing, etc
* Consider splitting message validators out (shouldn't be instance specific),
* ~~Logging framework (chose SLF4J),~~
* More string sanitisation / safe output to log,
* Hiding NickServ passwords / etc from log,
* ~~Hostmask parsing~~,
* Annotations to register command handlers and holders,
* Unit testing (especially message parsing),
* CAP state (including SASL),
* Connection state,
* ~~Channel state (keeping track of who is currently in a channel)~~,
* ~~User state~~,
 * ~~This includes tracking Users somewhere, adding references to them to Channels, etc~~
* Tracking mode changes,
* Server state (finish ISupport),
* ~~Configurability~~,
* ~~Nullable / Not Null annotations~~,
* ~~Outgoing message queue~~,
* Incoming processing queue,
* Task queue with result / delegate,
* Rate limiting,
* Message splitting,
* ~~Allow user to specify accepted certificate fingerprint for servers with terrible CAs~~,
* Modules,
 * Think about IPC,
 * Also think about reloading modules on the fly,
* Consider using own high level events to drive bot, call it "core module",
* Think about reconnect / failure policy,
* Benchmarking,
* ~~License (chose BSD 2-clause)~~,
 * Consider whether to release,
* ~~Testing~~,
 * 100% coverage for message serialisation / deserialisation,
* Full session tests (check state of things afterwards are as expected),
* Non SSL connections (disabled by default).

##Code License
The source code of this project is licensed under the terms of the ISC license, listed in the [LICENSE](LICENSE.md) file. A concise summary of the ISC license is available at [choosealicense.org](http://choosealicense.com/licenses/isc/).

##Building
This project uses Gradle and IntelliJ IDEA for pretty easy setup and building. There are better guides around the internet for using them, and I don't do anything particularly special.

The general idea:
* **Setup**: `./gradlew clean idea`
* **Building**: `./gradlew build`
* **Producing an all-in-one Jar**: `./gradlew build shadowJar`

If you run in to odd Gradle issues, doing `./gradlew clean` usually fixes it.

##Getting SSL certificate fingerprints
A workaround for servers with terrible CAs is included. You DO NOT need to do this if the IRC server presents certificates signed by ubiquitous authorities.

The solution is **not** to blindly accept certificates, but to add exceptions based on certificate fingerprints. It is **not** intended to be simple. You must know the SHA1 fingerprints of the certificates you intend to trust for a server, and all presented certificates will be checked against them. Invalid certificate fingerprints are printed to the terminal on a failure, along with an exception, so you can add them to the exceptions manually.

##SSL Issues
Java cannot handle DH key exchange > 2048 bits. ECDH is a good alternative.
