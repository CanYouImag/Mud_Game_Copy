@echo off
REM 脚本说明：启动指定数量的客户端
REM 调用方式：.\start_clients.bat <客户端数量>

REM 检查是否提供了客户端数量参数
if "%1"=="" (
  echo 用法: .\start_clients.bat ^<客户端数量^>
  exit /b 1
)

REM 调试输出：显示提供的客户端数量
echo 启动 %1 个客户端...

REM 检查 Java 是否可用
java -version >nul 2>&1
if %error level% neq 0 (
  echo 请确保已安装 Java 并正确配置环境变量。
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
  REM 调试输出：显示正在启动的客户端命令
  echo 正在启动客户端 %%i: java -cp "Former\Client\target\classes\Client" Client.Main %SERVER_ADDRESS% %SERVER_PORT%
  start java -cp "Former\Client\target\classes\Client" Client.Main %SERVER_ADDRESS% %SERVER_PORT%
)

echo 已启动 %CLIENT_COUNT% 个客户端。