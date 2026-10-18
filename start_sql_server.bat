@echo off
chcp 65001 > nul
echo ============================================================
echo   Đang khởi động dịch vụ Microsoft SQL Server...
echo ============================================================
echo.

:: Tự động đặt chế độ khởi động cùng Windows
sc config MSSQLSERVER start= auto > nul 2>&1

:: Khởi chạy dịch vụ SQL Server
net start MSSQLSERVER

echo.
if %errorlevel% equ 0 (
    echo [THÀNH CÔNG] Dịch vụ SQL Server đã chạy và được đặt tự động bật khi khởi động máy!
    echo Bây giờ bạn có thể quay lại VS Code và bấm Run ứng dụng.
) else (
    echo [LƯU Ý] Hãy chắc chắn bạn đã CHUỘT PHẢI vào file này và chọn "Run as administrator"!
)

echo.
pause

