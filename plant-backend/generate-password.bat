@echo off
cd /d "%~dp0"
mvn test-compile
mvn exec:java -Dexec.mainClass="com.plant.backend.PasswordGeneratorTest" -Dexec.classpathScope=test
pause
