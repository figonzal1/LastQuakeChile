fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android unit_test

```sh
[bundle exec] fastlane android unit_test
```

Unit testing

### android ui_test

```sh
[bundle exec] fastlane android ui_test
```

Instrumentation testing

### android release

```sh
[bundle exec] fastlane android release
```

Build release version (el mismo AAB para beta y produccion)

### android beta_googleplay

```sh
[bundle exec] fastlane android beta_googleplay
```

Deploy Beta to the Google Play

### android prod_googleplay

```sh
[bundle exec] fastlane android prod_googleplay
```

Deploy Production to the Google Play (solo si no paso por beta)

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
