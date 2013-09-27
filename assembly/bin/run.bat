
@set BIN=%~dp0
@cd %BIN%\..

@set CLASSPATH=conf

@for %%a in (lib\*.jar) do @call bin\add2cp.bat %%a

@java -classpath %CLASSPATH% com.shvid.memcast.MemcastMain %1

pause