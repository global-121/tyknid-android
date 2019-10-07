#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker-compose -f "${DIR}/../121-platform/services/docker-compose.yml" up -d --build

echo "waiting 10 secs for containers to start up..."
sleep 10s


echo "installing app on the emulator"
./gradlew installDebug
adb shell am start -n "tech.tykn.tyknidapp/tech.tykn.tyknidapp.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
echo "waiting 10 secs so that app gets installed on the emulator"
sleep 10s

echo "starting test"
adb shell input tap 830 830
echo "waiting 1 min for tests to be complete"
sleep 1m


adb uninstall "tech.tykn.tyknidapp"
echo "bringing down the docker containers"
docker-compose -f "${DIR}/../121-platform/services/docker-compose.yml" down
