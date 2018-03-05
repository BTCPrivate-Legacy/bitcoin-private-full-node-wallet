#!/bin/bash

############################################
#    Created by @Pega88 && @Jon_S_Layton   #
#         v1.0.0 16 Feb 2018		   	   #
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

#fetch + install dylibbundler
if [ -e /usr/local/bin/dylibbundler ]
then
    echo "dylibbundler already installed - OK"
else
	git clone https://github.com/auriamg/macdylibbundler
	cd macdylibbundler
	sudo make install
  cd ..
  rm -rf macdylibbundler
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
ant jar -f src/build/build.xml

echo ""
echo "*******************"
echo "|| Packaging App ||"
echo "*******************"
echo ""
#package jar to app
jar2app build/jars/BitcoinPrivateDesktopWallet.jar -n BitcoinPrivateDesktopWallet  -i ./src/resources/images/btcp.icns

#add btcpd and btcp-cli into the required Contents folder of the App
cp ./btcpd ./BitcoinPrivateDesktopWallet.app/Contents/btcpd
cp ./btcp-cli ./BitcoinPrivateDesktopWallet.app/Contents/btcp-cli


chmod +x ./BitcoinPrivateDesktopWallet.app/Contents/btcpd
chmod +x ./BitcoinPrivateDesktopWallet.app/Contents/btcp-cli
echo ""
echo "**********************************"
echo "|| Statically linking libraries ||"
echo "**********************************"
echo ""

#statically build required libraries
dylibbundler -od -b -x ./BitcoinPrivateDesktopWallet.app/Contents/btcpd \
                    -x ./BitcoinPrivateDesktopWallet.app/Contents/btcp-cli \
                    -d ./BitcoinPrivateDesktopWallet.app/Contents/libs \
                    -p @executable_path/libs
