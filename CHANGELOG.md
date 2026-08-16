# Changelog

## [1.9.0](https://github.com/figonzal1/LastQuakeChile/compare/v1.8.0...v1.9.0) (2026-08-16)


### Features

* share earthquakes to Instagram Stories and WhatsApp ([5752db5](https://github.com/figonzal1/LastQuakeChile/commit/5752db53b42782290bb0857ee0448a9eaf103472))


### Bug Fixes

* adapt QuakeClusterItem to android-maps-utils 5.1.1 ClusterItem API ([b1f0167](https://github.com/figonzal1/LastQuakeChile/commit/b1f0167a9dc21ae341314858b9e0f0e89070f281))
* expand map bottom sheet fully on marker tap and card tap ([86c84e8](https://github.com/figonzal1/LastQuakeChile/commit/86c84e83691c6bc8a2976189c089e9c770b02501))
* expand share bottom sheet fully on open ([70e2d12](https://github.com/figonzal1/LastQuakeChile/commit/70e2d121bd38aeb500e7ce5c908e73fbd42c00af))
* fix low-contrast alert dialog text in night mode ([8fd7170](https://github.com/figonzal1/LastQuakeChile/commit/8fd71707bc9c44eb60f97a0ec704f991d7106638))
* guard share flow against saved-state and missing-app crashes ([cc79f28](https://github.com/figonzal1/LastQuakeChile/commit/cc79f289c7ad2636b1cff8977598f2c3ff358ad1))
* improve dark-theme contrast for outlined and toggle buttons ([1a97c23](https://github.com/figonzal1/LastQuakeChile/commit/1a97c23ca2a5509d5f4ba3acbf7ab6e9b35d909f))
* keep native ad tab's app bar collapsed so CTA stays on screen ([17c888e](https://github.com/figonzal1/LastQuakeChile/commit/17c888e9c2d56ebfba80e9355298572bcad046e8))
* map bottom sheet/dialog styling and native ad rendering issues ([c051264](https://github.com/figonzal1/LastQuakeChile/commit/c0512647b88f1ffccf51f5a28caa8ea165c7f62d))
* paint status bar area with app bar color in settings and details ([940fe84](https://github.com/figonzal1/LastQuakeChile/commit/940fe84f935e7c6460aaf1902579f94bef7f684c))
* redesign native ad template to stop truncating text ([4572e86](https://github.com/figonzal1/LastQuakeChile/commit/4572e86567cbfed345c6f96617a9891714a48d69))
* remove duplicate banner ad initialization ([6f6f226](https://github.com/figonzal1/LastQuakeChile/commit/6f6f22691990bf2c571d6413e1bad672069fb381))
* remove nested NativeAdView and unblock swipe hint in ad fragment ([09d24cf](https://github.com/figonzal1/LastQuakeChile/commit/09d24cf7a219a7acc7b0f563833f4aaf33999281))
* shorten Instagram Stories destination label ([10a2637](https://github.com/figonzal1/LastQuakeChile/commit/10a2637e91ae7e6c25b3e1647428a7012f78bac0))
* show full date instead of relative time in share stickers ([03bcc4d](https://github.com/figonzal1/LastQuakeChile/commit/03bcc4d20db8b37a9800db734229ac11ed248f30))
* use MaterialAlertDialogBuilder for map type dialog styling ([a88173d](https://github.com/figonzal1/LastQuakeChile/commit/a88173d0ddaf198102b9553d1e6f0f0af45d580f))

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
