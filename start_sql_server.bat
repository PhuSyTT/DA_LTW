@echo off
title Khoi Dong SQL Server (Bookstore)
color 0A

:: Kiem tra quyen Administrator
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [THONG BAO] Dang yeu cau quyen Administrator de bat SQL Server...
    powershell -Command "Start-Process cmd -ArgumentList '/c \"\"%~f0\"\"' -Verb RunAs"
    exit /b
)

echo ======================================================================
echo    DANG KHOI DONG DICH VU SQL SERVER CHO DU AN BOOKSTORE...
echo ======================================================================
echo.

net start MSSQLSERVER

echo.
echo ----------------------------------------------------------------------
echo Trang thai hien tai cua SQL Server:
sc query MSSQLSERVER | findstr "STATE"
echo ----------------------------------------------------------------------
echo.
echo [THANH CONG] SQL Server da san sang! 
echo Ban co the bat dau chay ung dung Bookstore ma khong bi loi 1433 nua.
echo.
pause
