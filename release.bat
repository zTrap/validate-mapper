@echo off

set tag=%1

call gradlew.bat assemble check publishReleasePublicationToSonatypeRepository closeAndReleaseRepository --no-build-cache --no-daemon --no-parallel

git fetch -p
git tag %tag%
git push origin --tags