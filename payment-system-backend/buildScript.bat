@echo off  
IF /i "%1" == "with-test" GOTO :runWithTest
GOTO :runWithoutTest
:runWithoutTest
./gradlew spotlessApply clean build -x test
:runWithTest
./gradlew spotlessApply clean build
exit /b