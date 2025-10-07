@echo off
title Game O Chu - 2 Clients
cd client
echo ======= Dang build client... =======
call mvn clean package -q
cd target

echo ======= Mo 2 cua so client =======
for %%f in (*.jar) do (
    echo Chay client 1...
    start "Client 1" java -jar "%%f"
    timeout /t 2 >nul
    echo Chay client 2...
    start "Client 2" java -jar "%%f"
)
pause