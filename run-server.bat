@echo off
title Game O Chu - Server
cd server
echo ======= Dang build server... =======
call mvn clean package -q
echo ======= Khoi dong server... =======
cd target
for %%f in (*.jar) do (
    echo Chay file %%f
    start "Game O Chu Server" java -jar "%%f"
)
pause