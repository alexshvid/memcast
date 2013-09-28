
@set BIN=%~dp0
@cd %BIN%\..

set JAVA_OPTS=-server
set JAVA_OPTS=%AVA_OPTS% -Xms2048m -Xmx2048m -XX:MaxPermSize=256m
set JAVA_OPTS=%JAVA_OPTS% -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,suspend=n,server=y
set JAVA_OPTS=%JAVA_OPTS% -Dcom.sun.management.jmxremote.port=8000 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
set JAVA_OPTS=%JAVA_OPTS% -Xss412k

@set CLASSPATH=conf

@for %%a in (lib\*.jar) do @call bin\add2cp.bat %%a

@java -classpath %CLASSPATH% %JAVA_OPTS% com.shvid.memcast.MemcastMain %1

pause
