::
:: clean-blockchain.bat
::
:: ------------------------------------------------------------------
:: [Niels Buekers] 	Clean blockchain folders
::          		Deletes existing blockchain from user's default
::					installation directory. Should only be used in
::					case of a corrupted chainstate. User will need
::					to redownload entire blockchain if he proceeds.
::
::					'wallet', 'peers', and 'config' remain untouched.
:: ------------------------------------------------------------------

@echo off
setlocal
:PROMPT
ECHO "This operation will delete your current blockchain - blocks and chainstate."
ECHO "You will need to re-download it completely."
ECHO "! Only use this in case of a corrupted chainstate."
SET /P AREYOUSURE=Are you sure (Y/[N])?
IF /I "%AREYOUSURE%" NEQ "Y" GOTO END

 @RD /S /Q "C:\Users\%USERNAME%\AppData\Roaming\BitcoinPrivate\blocks"
 @RD /S /Q "C:\Users\%USERNAME%\AppData\Roaming\BitcoinPrivate\chainstate"

:END
endlocal
