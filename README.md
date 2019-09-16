# tyknid-android
Android wrapper for Tykn ID

- Build libindy wrapper for Android from [here](https://github.com/faisal00813/indy-sdk/blob/libindy-android-wrapper/wrappers/android/README.md)
- Copy aar in `libindy` folder
- Update the aar name in libindy module here `artifacts.add("default", file('com.hyperledger-libindy_1.0.0-23-08-2019T05-55_x86-armv7-debug.aar'))`
- Build using Android Studio
- The app expects the Tykn SSI System is running in the containers locally.
- Run the compose file available [here](https://github.com/global-121/121-platform/blob/master/services/docker-compose.yml)
- Run the compose file by executing `docker-compose run -build`
- Run test app in emulator or actual device.
- Press the `Test Run Sequence` button to run the sequence of steps from issuance of credential to generating proof.
- Open and see steps being executed in Logcat.
- Clean the containers after the test by executing `docker-compose down`