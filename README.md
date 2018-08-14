# GraalSqueak [![Build Status][travis_badge]][travis] [![Codacy][codacy_badge]][codacy]

A [Squeak/Smalltalk][squeak] implementation for the [GraalVM][graalvm].


## Getting Started

1. Clone or download GraalSqueak.
2. Download the latest [Oracle Labs JDK][labsjdk] for your platform.
3. Ensure your `JAVA_HOME` environment variable is unset or set it to
   Oracle Labs JDK's home directory.
4. Run [`bin/graalsqueak`][graalsqueak] to build and start GraalSqueak with
   [mx] using the Oracle Labs JDK.
5. Open a pre-configured
   [Squeak/Smalltalk image for GraalSqueak][graalsqueak_image]
   (incl. [VMMaker] for BitBlt/Balloon simulation).

To list all available options, run `bin/graalsqueak -h`.


## Development

Active development is done on the [`dev` branch][dev] and new code is merged to
[`master`][master] when stable.
Therefore, please open pull requests against [`dev`][dev] if you like to
contribute a bugfix or a new feature.


### Setting Up A New Development Environment

It is recommended to use [Eclipse Oxygen][eclipse_oxygen] with the
[Eclipse Checkstyle Plugin][eclipse_cs] for development.

1. Run `mx eclipseinit` to create all project files for Eclipse.
2. Import all projects from the [graal] repository which `mx` should have
   already cloned into the parent directory of your GraalSqueak checkout during
   the build process.
3. Import all projects from the `graalsqueak` repository.
4. Launch [`GraalSqueakLauncher`][graalsqueaklauncher] to start GraalSqueak.

Run `mx --help` to list all commands that may help you develop GraalSqueak.


### Running Specific Squeak/Smalltalk Code

It is often useful to execute specific Squeak/Smalltalk code for debugging
purposes or when working on a new feature.
For this reason, GraalSqueak can be invoked with additional arguments
`-r|--receiver` and `-m|--method`.
Any `SmallInteger` can be a receiver while the method should be a selector
defined on `SmallInteger`.
Running `bin/graalsqueak -r 42 -m tinyBenchmarks my.image`, for example, loads
`my.image` and performs `42 tinyBenchmarks`.


## Contributing

Please [report any issues here on GitHub][issues] and open
[pull requests][pull_request] if you'd like to contribute code or documentation.



[codacy]: https://app.codacy.com/app/fniephaus/graalsqueak/dashboard
[codacy_badge]: https://api.codacy.com/project/badge/Coverage/9748bfe3726b48c8973e3808549f6d05
[dev]: ../../tree/dev
[eclipse_cs]: http://checkstyle.org/eclipse-cs/
[eclipse_oxygen]: https://www.eclipse.org/oxygen/
[graal]: https://github.com/oracle/graal
[graalsqueak]: bin/graalsqueak
[graalsqueaklauncher]: src/de.hpi.swa.graal.squeak.launcher/src/de/hpi/swa/graal/squeak/launcher/GraalSqueakLauncher.java
[graalsqueak_image]: https://github.com/hpi-swa-lab/graalsqueak/releases/latest
[graalvm]: http://www.graalvm.org/
[graalvm_download]: http://www.graalvm.org/downloads/
[issues]: ../../issues/new
[labsjdk]: http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads/index.html
[master]: ../../tree/master
[mx]: https://github.com/graalvm/mx
[pull_request]: ../../compare/dev...
[squeak]: https://squeak.org
[travis]: https://travis-ci.com/hpi-swa-lab/graalsqueak
[travis_badge]: https://travis-ci.com/hpi-swa-lab/graalsqueak.svg?token=7fqzGEv22MQpvpU7RhK5&branch=master
[vmmaker]: http://source.squeak.org/VMMaker/
