@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  WAT-Vis startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and WAT_VIS_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\WAT-Vis-1.5.0.jar;%APP_HOME%\lib\flatlaf-2.3.jar;%APP_HOME%\lib\gson-2.9.0.jar;%APP_HOME%\lib\mc_math-d73ac7cc644c67628ade0effd7136e11eb00bb76.jar;%APP_HOME%\lib\mc_seed-5518e3ba3ee567fb0b51c15958967f70a6a19e02.jar;%APP_HOME%\lib\mc_core-706e4f1b7aa6b42b3627f682a311d06280d80b5c.jar;%APP_HOME%\lib\mc_noise-a6ab8e6c688491829f8d2adf845392da22ef8e9c.jar;%APP_HOME%\lib\mc_biome-b2271807a047bb43ac60c8c20ad47e315f19b9a6.jar;%APP_HOME%\lib\mc_terrain-9e937ddb838e28e79423c287fa18b1ce66f061d7.jar;%APP_HOME%\lib\latticg-1.06.jar;%APP_HOME%\lib\commons-lang3-3.12.0.jar;%APP_HOME%\lib\commons-io-2.11.0.jar;%APP_HOME%\lib\junit-platform-commons-1.8.2.jar;%APP_HOME%\lib\junit-jupiter-api-5.8.2.jar;%APP_HOME%\lib\xchart-3.8.1.jar;%APP_HOME%\lib\junit-4.13.2.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.3.72.jar;%APP_HOME%\lib\opentest4j-1.2.0.jar;%APP_HOME%\lib\VectorGraphics2D-0.13.jar;%APP_HOME%\lib\graphics2d-0.32.jar;%APP_HOME%\lib\animated-gif-lib-1.4.jar;%APP_HOME%\lib\hamcrest-core-1.3.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.3.72.jar;%APP_HOME%\lib\kotlin-stdlib-1.3.72.jar;%APP_HOME%\lib\pdfbox-2.0.24.jar;%APP_HOME%\lib\kotlin-stdlib-common-1.3.72.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\fontbox-2.0.24.jar;%APP_HOME%\lib\commons-logging-1.2.jar


@rem Execute WAT-Vis
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %WAT_VIS_OPTS%  -classpath "%CLASSPATH%" com.anipgames.WAT_Vis.PlayerTrackerDecoder %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable WAT_VIS_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%WAT_VIS_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
