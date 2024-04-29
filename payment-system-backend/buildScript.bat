@echo off  
./gradlew spotlessApply clean build -x test
exit /b