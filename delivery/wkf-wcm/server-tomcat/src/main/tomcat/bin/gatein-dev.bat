@REM
@REM Copyright (C) 2009 eXo Platform SAS.
@REM 
@REM This is free software; you can redistribute it and/or modify it
@REM under the terms of the GNU Lesser General Public License as
@REM published by the Free Software Foundation; either version 2.1 of
@REM the License, or (at your option) any later version.
@REM 
@REM This software is distributed in the hope that it will be useful,
@REM but WITHOUT ANY WARRANTY; without even the implied warranty of
@REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
@REM Lesser General Public License for more details.
@REM 
@REM You should have received a copy of the GNU Lesser General Public
@REM License along with this software; if not, write to the Free
@REM Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
@REM 02110-1301 USA, or see the FSF site: http://www.fsf.org.
@REM

@echo off

rem Computes the absolute path of eXo
setlocal ENABLEDELAYEDEXPANSION
for %%i in ( !%~f0! ) do set BIN_DIR=%%~dpi
cd %BIN_DIR%

rem Sets some variables
BONITA_OPTS="-Dorg.ow2.bonita.environment=../conf/bonita.environnement.xml"
set LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
set SECURITY_OPTS="-Djava.security.auth.login.config=..\conf\jaas.conf"
set EXO_OPTS="-Dexo.product.developing=false -Dexo.conf.dir.name=gatein/conf"
set EXO_CONFIG_OPTS="-Dorg.exoplatform.container.configuration.debug"
set JAVA_OPTS=-Xshare:auto -Xms128m -Xmx512m -XX:MaxPermSize=512m %LOG_OPTS% %SECURITY_OPTS% %EXO_OPTS% %EXO_CONFIG_OPTS% %BONITA_OPTS%
set JPDA_TRANSPORT=dt_socket
set JPDA_ADDRESS=8000

rem Launches the server
call catalina.bat %*
