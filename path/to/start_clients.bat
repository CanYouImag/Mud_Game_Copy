@echo off
REM 检查是否提供了客户端数量参数
if "%1"=="" (
  echo 用法: %0 ^<客户端数量^>
  exit /b 1
)

REM 定义服务器地址和端口
set SERVER_ADDRESS=localhost
set SERVER_PORT=3080

REM 获取客户端数量
set CLIENT_COUNT=%1

REM 启动指定数量的客户端
for /L %%i in (1,1,%CLIENT_COUNT%) do (
  echo 启动客户端 %%i...
  start java -cp "C:\Users\11831\IdeaProjects\Former\Client\target\classes" Client.Main %SERVER_ADDRESS% %SERVER_PORT%
)

echo 已启动 %CLIENT_COUNT% 个客户端。