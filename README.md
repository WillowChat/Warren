warren
=====

A Java IRC framework. Still very much in development.

# Why?

* Personal interest,
 * Implementing a protocol,
 * Getting better at testing,
 * Producing a framework,
 * Scope for both simple and complex goals.
* Personal use,
 * Twitch bots,
 * GitHub helper bots.
* Fun.

# TODO

[RFC 2812](https://tools.ietf.org/html/rfc2812)

* Server Connection cleanup / separation,
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
* Channel state (keeping track of who is currently in a channel),
* User state,
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
 * 100% coverage for message serialisation / deserialisation.
* Full session tests (check state of things afterwards are as expected)

##Code License
Copyright © 2015, Sky Welch
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

##Building
This project uses Gradle and IntelliJ IDEA for pretty easy setup and building. There are better guides around the internet for using them, and I don't do anything particularly special.

The general idea:
* **Setup**: `./gradlew clean idea`
* **Building**: `./gradlew build`

If you run in to odd Gradle issues, doing `./gradlew clean` usually fixes it.

##Getting SSL certificate fingerprints
A workaround for servers with terrible CAs is included. You DO NOT need to do this if the IRC server presents certificates signed by ubiquitous authorities.

The solution is **not** to blindly accept certificates, but to add exceptions based on certificate fingerprints. It is **not** intended to be simple. You must know the SHA1 fingerprints of the certificates you intend to trust for a server, and all presented certificates will be checked against them. Invalid certificate fingerprints are printed to the terminal on a failure, along with an exception, so you can add them to the exceptions manually.

##SSL Issues
Java cannot handle DH key exchange > 2048 bits. ECDH is a good alternative.
