#!/bin/bash

############################################
#    Created by @Pega88 && @Jon_S_Layton   #
#         v1.0.1 06 March 2018		   	   #
############################################

echo "***************************"
echo "|| Checking dependencies ||"
echo "***************************"
echo ""

#fetch + install jar2app
if [ -e /usr/local/bin/jar2app ]
then
    echo "jar2app already installed - OK"
else
	git clone https://github.com/Jorl17/jar2app
	cd jar2app
	chmod +x install.sh uninstall.sh
	sudo ./install.sh /usr/local/bin
  cd ..
  rm -rf jar2app
fi

if [ ! -e ./btcpd ]
then
	echo "please provide btcpd in the root directory"
else
	echo "found btcpd - OK"
fi

if [ ! -e ./btcp-cli ]
then
	echo "please provide btcp-cli in the root directory"
else
	echo "found btcp-cli - OK"
fi
echo ""
echo "******************"
echo "|| building JAR ||"
echo "******************"
echo ""

#build the jar from source
./gradlew clean fatJar 
#./gradlew clean build #includes tests

echo ""
echo "*******************"
echo "|| Packaging App ||"
echo "*******************"
echo ""
#package jar to app
jar2app build/libs/BitcoinPrivateDesktopWallet-*.jar -n BitcoinPrivateDesktopWallet  -i ./src/main/resources/images/btcp.icns

#add btcpd and btcp-cli into the required Contents folder of the App
cp ./btcpd ./BitcoinPrivateDesktopWallet.app/Contents/btcpd
cp ./btcp-cli ./BitcoinPrivateDesktopWallet.app/Contents/btcp-cli


chmod +x ./BitcoinPrivateDesktopWallet.app/Contents/btcpd
chmod +x ./BitcoinPrivateDesktopWallet.app/Contents/btcp-cli

VERSION=1.0.2
sudo hdiutil create -fs "HFS+" -volname "BTCP Full-Node GUI Wallet - Installer" -srcfolder "." btcp-desktop-wallet-$VERSION-macosx.dmg

