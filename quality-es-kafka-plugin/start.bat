@ECHO OFF
SET APP_PATH=.
SET PARAMER=-Dfile.encoding=utf-8
SET JAVA_MEM=-Xmx2048m -Xms1024m -Xmn712m
SET JAVA_FILE=%APP_PATH%\quality-es-kafka-plugin-1.0.0-SNAPSHOT.jar
java %PARAMER% %JAVA_MEM% -jar %JAVA_FILE%