# tyknid-android
Android wrapper for Tykn ID

## Requirements:
1. docker-compose
2. android studio

## Setup

- Build libindy wrapper for Android from [here](https://github.com/faisal00813/indy-sdk/blob/libindy-android-wrapper/wrappers/android/README.md)
- Copy aar in `libindy` folder
- Update the aar name in libindy module here `artifacts.add("default", file('com.hyperledger-libindy_1.0.0-23-08-2019T05-55_x86-armv7-debug.aar'))`
- Build using Android Studio
- The app expects the Tykn SSI System is running in the containers locally.
- Run the compose file available [here](https://github.com/global-121/121-platform/blob/master/services/docker-compose.yml)
- Run the compose file by executing `docker-compose run -build`

Few assumptions are made while executing the tests using the app:
    - TyknIms is running on `11.0.0.3:50001`
    - OrgIms is running on `11.0.0.4:50002`
    - IndyPool is running on `11.0.0.2`
    - All the above ips are reachable from the app

- Run test app in emulator (tests on actual device may not work with the assumptions made by above as the network on the actual device may not reach the docker network)
- Press the `Test Run Sequence` button to run the sequence of steps from issuance of credential to generating proof.
- Open and see steps being executed in Logcat.
- Clean the containers after the test by executing `docker-compose down` (PS: This needs to be done before each test run )

Runtime warnings in console:
 - Ignore
 ``` java.util.concurrent.ExecutionException: org.hyperledger.indy.sdk.pool.PoolLedgerConfigExistsException: A pool ledger configuration already exists with the specified name.```
 This error is gracefully handled in the code and is printed by the underlying indy wrapper for debugging purposes.
 In production this should be suppressed using the logging backend used by the android app.

 - If the TyknIms or OrgIms are not reachable OKHttp may print
 ```Rejecting re-init on previously-failed class java.lang.Class<okhttp3.internal.platform.ConscryptPlatform$configureTrustManager$1>```
 or
 ```No Network Security Config specified, using platform default```
 Make sure the SSI services are running and are reachable.

 ## Automated tests
 checkout [121-platform](https://github.com/global-121/121-platform)
 To run the automated tests make sure your working directory looks like this
 121-platform should be in the same directory where the tyknid-android is present.
 The directory structure would look something like below

```
.
├── 121-platform                   # 121-platform project
├── tyknid-android                 # tyknid-android project

```

- Start the emulator
- run test.sh in tyknid-android folder
- Keep an eye on logcat to check the output from.