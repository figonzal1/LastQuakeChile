# Changelog

## [1.8.0](https://github.com/figonzal1/LastQuakeChile/compare/v1.7.10...v1.8.0) (2026-06-03)


### Features

* add Android developer agent foundation skills ([912d48f](https://github.com/figonzal1/LastQuakeChile/commit/912d48f012e3b30387f25e8aac9156939cce7ecb))
* major refactor — DomainResult, coroutines, map clustering, and CI improvements ([ca74dbb](https://github.com/figonzal1/LastQuakeChile/commit/ca74dbb6965cf58409d1889b5e549f0a5b6c5a25))
* **map:** replace loadPins with ClusterManager and cap markers at 50 ([e52fc95](https://github.com/figonzal1/LastQuakeChile/commit/e52fc959a44ea5c24cfa42265698926fc3c1c76c)), closes [#75](https://github.com/figonzal1/LastQuakeChile/issues/75)


### Bug Fixes

* **coroutines:** replace Channel with MutableSharedFlow for one-shot error events ([89a6643](https://github.com/figonzal1/LastQuakeChile/commit/89a6643fa6e5db8b6de298601cf6098609d97001))
* decouple FCM subscription from user preference switch state ([6564d9a](https://github.com/figonzal1/LastQuakeChile/commit/6564d9ac386f9fde1a93b5acbbad3985d739418a))
* exclude SharedPreferences from Auto Backup to prevent stale permission restore ([c6eecfc](https://github.com/figonzal1/LastQuakeChile/commit/c6eecfc9150856c10700645e7150d782be3be766))
* filter DeadSystemRuntimeException from Crashlytics reports ([2283dca](https://github.com/figonzal1/LastQuakeChile/commit/2283dca71d8159e2043b6cde7283473e6da34517)), closes [#76](https://github.com/figonzal1/LastQuakeChile/issues/76)
* gate FCM subscription on permission and fix POST_NOTIFICATIONS cardview flow ([c4a66f0](https://github.com/figonzal1/LastQuakeChile/commit/c4a66f055202774f4cec9715da750519ab7eb118))
* **notifications:** use quake code as unique PendingIntent request ID ([11142a8](https://github.com/figonzal1/LastQuakeChile/commit/11142a8f56bc464f063add867d712df80aefe655))
* prevent Maps background snapshot crash in MapsFragment ([9cf6a54](https://github.com/figonzal1/LastQuakeChile/commit/9cf6a54ace64649f2783ea19ee09fcf5ac5e7668)), closes [#74](https://github.com/figonzal1/LastQuakeChile/issues/74)
* re-evaluate notification permission cardview in onResume ([5e5bf47](https://github.com/figonzal1/LastQuakeChile/commit/5e5bf4768f89210a5a3e8cf14210c8e907919f48))
* replace random channel scheme with two fixed notification channels ([75940d9](https://github.com/figonzal1/LastQuakeChile/commit/75940d9a1e4741140fb45ecd547e8aba1af4d000))
* restrict destructive DB migration and HTTP logging to debug builds ([857304b](https://github.com/figonzal1/LastQuakeChile/commit/857304b3c4067c5e0057a1fbe13187db7743936d))
