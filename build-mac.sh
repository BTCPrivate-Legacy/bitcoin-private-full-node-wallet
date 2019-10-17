#!/bin/bash

############################################
#    Created by @Pega88 && @Jon_S_Layton   #
#         v1.0.1 06 March 2018		   	   #
############################################

# set up your app name, version number, and background image file name
APP_NAME="BitcoinPrivateDesktopWallet"
APP_DISPLAY_NAME="Bitcoin Private Desktop Wallet"
VERSION="1.1.4"
APP_EXE="${APP_DISPLAY_NAME}.app/Contents/MacOS/JavaAppLauncher"
VOL_NAME="${APP_NAME}_${VERSION}"
DMG_TMP="${VOL_NAME}-temp.dmg"
DMG_FINAL="${VOL_NAME}.dmg"
STAGING_DIR="./Install"
DMG_BACKGROUND_IMG="background.png"

echo "*****************"
echo "|| Cleaning up ||"
echo "*****************"
echo ""
#required to build JAR. If not done, can only build once. to investigate.
rm -rf .gradle

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
./gradlew clean fatJar

echo ""
echo "*******************"
echo "|| Packaging App ||"
echo "*******************"
echo ""
#package jar to app
jar2app build/libs/"${APP_NAME}"-*.jar  -i ./src/main/resources/images/btcp.icns -b org.btcprivate.wallets.fullnode -v "${VERSION}" -s "${VERSION}"
mv "${APP_NAME}-${VERSION}.app" "${APP_DISPLAY_NAME}.app"

#support automatic graphics switching
plutil -replace NSSupportsAutomaticGraphicsSwitching -bool true "${APP_DISPLAY_NAME}.app"/Contents/Info.plist

#create copies for link modification
cp ./btcpd ./btcpd-dylib
cp ./btcp-cli ./btcp-cli-dylib

#ensure permissions allow for execution
chmod +x "./btcpd-dylib"
chmod +x "./btcp-cli-dylib"

echo ""
echo "**********************************"
echo "|| Statically linking libraries ||"
echo "**********************************"
echo ""

#statically build required libraries
dylibbundler -od -b -x "./btcpd-dylib" \
                    -x "./btcp-cli-dylib" \
                    -d "./libs" \
                    -p @executable_path/libs

#add btcpd, btcp-cli, and libs into the required Contents folder of the App
cp ./btcpd-dylib "./${APP_DISPLAY_NAME}.app/Contents/btcpd"
cp ./btcp-cli-dylib "./${APP_DISPLAY_NAME}.app/Contents/btcp-cli"
cp -R ./libs "./${APP_DISPLAY_NAME}.app/Contents/libs"

#remove modified copies
rm ./btcpd-dylib
rm ./btcp-cli-dylib

rm -rf "${STAGING_DIR}" "${DMG_TMP}" "${DMG_FINAL}"
mkdir -p "${STAGING_DIR}"
cp -rpf "${APP_DISPLAY_NAME}.app" "${STAGING_DIR}"

# figure out how big our DMG needs to be
#  assumes our contents are at least 5M!
SIZE=`du -sh "${STAGING_DIR}" | sed 's/\([0-9]*\)M\(.*\)/\1/'`
SIZE=`echo "${SIZE} + 5.0" | bc | awk '{print int($1+0.5)}'`

if [ $? -ne 0 ]; then
   echo "Error: Cannot compute size of staging dir"
   exit
fi

# create the temp DMG file
hdiutil create -srcfolder "${STAGING_DIR}" -volname "${VOL_NAME}" -fs HFS+ \
      -fsargs "-c c=64,a=16,e=16" -format UDRW -size ${SIZE}M "${DMG_TMP}"

echo "Created DMG: ${DMG_TMP}"

# mount it and save the device
DEVICE=$(hdiutil attach -readwrite -noverify "${DMG_TMP}" | \
         egrep '^/dev/' | sed 1q | awk '{print $1}')

sleep 2

# add a link to the Applications dir
echo "Add link to /Applications"
pushd /Volumes/"${VOL_NAME}"
ln -s /Applications
popd

# add a background image
mkdir /Volumes/"${VOL_NAME}"/.background
cp "${DMG_BACKGROUND_IMG}" /Volumes/"${VOL_NAME}"/.background/

# tell the Finder to resize the window, set the background,
#  change the icon size, place the icons in the right position, etc.
echo '
   tell application "Finder"
     tell disk "'${VOL_NAME}'"
           open
           set current view of container window to icon view
           set toolbar visible of container window to false
           set statusbar visible of container window to false
           set the bounds of container window to {400, 100, 1220, 647}
           set viewOptions to the icon view options of container window
           set arrangement of viewOptions to not arranged
           set icon size of viewOptions to 144
           set background picture of viewOptions to file ".background:'${DMG_BACKGROUND_IMG}'"
           set position of item "'${APP_DISPLAY_NAME}'.app" of container window to {205, 238}
           set position of item "Applications" of container window to {615, 238}
           close
           open
           update without registering applications
           delay 2
     end tell
   end tell
' | osascript

sync

# unmount it
hdiutil detach "${DEVICE}"

# now make the final image a compressed disk image
echo "Creating compressed image"
hdiutil convert "${DMG_TMP}" -format UDZO -imagekey zlib-level=9 -o "${DMG_FINAL}"

# clean up
rm -rf "${DMG_TMP}"
rm -rf "${STAGING_DIR}"
# rm -rf "${APP_DISPLAY_NAME}.app"

echo 'Done.'

exit
