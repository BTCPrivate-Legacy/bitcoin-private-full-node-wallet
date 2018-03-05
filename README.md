# [BitcoinPrivate](https://btcprivate.org/) Full-Node Desktop Wallet

### Supports B and Z addresses. Read [here](https://github.com/BTCPrivate/bitcoin-private-full-node-wallet/blob/master/claim-btcp.md) on how to claim your BTCP from Zclassic and/or Bitcoin.

### Download the latest release:
https://github.com/BTCPrivate/bitcoin-private-full-node-wallet/releases


**Platforms:** Windows, Mac, Linux

[Java](https://java.com/en/download/) must be installed.


![Bitcoin Private Desktop Wallet](https://github.com/BTCPrivate/bitcoin-private-full-node-wallet/raw/master/docs/WalletPreviewWin.png "Bitcoin Private Desktop Wallet")


#### WARNING: Careful with 'watch-only' imports! They will appear in 'addresses', but ***do not*** receive funds to them because you have not fully imported the private key! This UX issue will be fixed.


## Building, Installing, and Running the Wallet

*To run the Bitcoin Private Full-Node Desktop Wallet from source, you need to have [BitcoinPrivate](https://github.com/BTCPrivate/BitcoinPrivate) built and ready to go. Effectively, this means having a copy of `btcpd` and `btcp-cli` in the .jar's directory.*

#### For best security, it is recommended to build the entire Bitcoin Private wallet by yourself, directly from GitHub.

##### 1. Operating System and Tools

   You will need Git, Java (JDK7 or later), and Ant.  

   **MacOS -**

   Download & Install [JDK 9](http://www.oracle.com/technetwork/java/javase/downloads/jdk9-downloads-3848520.html)

   **Ubuntu Linux -**
   ```
   sudo apt-get install git default-jdk ant
   ```
   RedHat/CentOS/Fedora Linux -
   ```
   sudo yum install java-1.8.0-openjdk git ant
   ```
   The name of the JDK package (`java-1.8.0-openjdk`) may vary depending on the Linux system, so look around if name `java-1.8.0-openjdk` can't be found or doesn't work.

   Commands `git`, `java`, `javac`, `ant` need to be runnable from command line
   before proceeding with build.

##### 2 - Building from Source Code

   First, clone this Git repository:
   ```
   git clone https://github.com/BTCPrivate/bitcoin-private-full-node-wallet
   ```
   Enter:
   ```
   cd bitcoin-private-full-node-wallet/
   ```
   Build:
   ```
   ant -buildfile ./src/build/build.xml
   ```
   This may take a few seconds. When it finishes, you will now see `build/jars/BitcoinPrivateDesktopWallet.jar`.

   You need to make this file executable:
   ```
   chmod u+x ./build/jars/BitcoinPrivateDesktopWallet.jar
   ```
   At this point the build process is finished! The final product is the GUI Wallet Java JAR: `build/jars/BitcoinPrivateDesktopWallet.jar`

##### 3. Installing the Base Wallet ([BitcoinPrivate](https://github.com/BTCPrivate/BitcoinPrivate))

Once you've built Bitcoin Private from source code, `btcpd` and `btcp-cli` will appear in `~/BitcoinPrivate/src`.

You will need to copy them beside the jar, and rename them to `btcpd` and `btcp-cli`:

```
cp ~/BitcoinPrivate/src/btcpd build/jars/btcpd
cp ~/BitcoinPrivate/src/btcp-cli build/jars/btcp-cli
```

You can now run the Desktop GUI Wallet:

```
java -jar build/jars/BitcoinPrivateDesktopWallet.jar
```

Or just double-click it!


If you are using Ubuntu or another Linux, you may need to
right-click `BitcoinPrivateDesktopWallet.jar` file and choose "Open with OpenJDK 8 Runtime".


### `btcprivate.conf`
Running the .jar will automatically set up `~/.btcprivate/btcprivate.conf` for you. In some cases, you may need to edit it manually. It should contain:
```
rpcuser=ENTER-RANDOM-ALPHANUMERICAL-PASSWORD
rpcpassword=ENTER-RANDOM-ALPHANUMERICAL-PASSWORD
rpcport=7932
#addnode=...

```

### Notes from ZENCash - Known Issues and Limitations

1. **Issue:** The Bitcoin Private Desktop GUI Wallet is not compatible with applications that modify the BTCP `wallet.dat` file. The wallet should not be used
with such applications on the same PC. For instance some distributed exchange applications are known to create watch-only addresses in the
`wallet.dat` file that cause the GUI wallet to display a wrong balance and/or display addresses that do not belong to the wallet.
1. **Limitation:** if two users exchange text messages via the messaging UI TAB and one of them has a system clock, substantially running slow or fast by more than 1 minute, it is possible that this user will see text messages appearing out of order.
1. **Limitation:** if a messaging identity has been created (happens on first click on the messaging UI tab), then replacing the `wallet.dat` or changing the node configuration between mainnet and testnet will make the identity invalid. This will result in a wallet update error. To remove the error the directory `~/.BitcoinPrivateDesktopWallet/messaging` may be manually renamed or deleted (when the wallet is stopped). **CAUTION: all messaging history will be lost in this case!**
1. **Limitation:** Wallet encryption has been temporarily disabled in Bitcoin Private due to stability problems. A corresponding issue
[#1552](https://github.com/zcash/zcash/issues/1552) has been opened by the Zcash developers. Correspondingly,
wallet encryption has been temporarily disabled in the Bitcoin Private Desktop GUI Wallet.
The latter needs to be disabled.
1. **Limitation:** The list of transactions does not show all outgoing ones (specifically outgoing Z address
transactions). A corresponding issue [#1438](https://github.com/zcash/zcash/issues/1438) has been opened
for the Zcash developers.
1. **Limitation:** The CPU percentage shown to be taken by btcpd on Linux is the average for the entire lifetime
of the process. This is not very useful. This will be improved in future versions.
1. **Limitation:** When using a natively compiled wallet version (e.g. `BitcoinPrivateDesktopWallet.exe` for Windows) on a
very high resolution monitor with a specifically configured DPI scaling (enlargement) factor to make GUI
elements look larger, the GUI elements of the wallet actually do not scale as expected. To correct this on
Windows you need to right-click on `BitcoinPrivateDesktopWallet.exe` and choose option:
```
Properties >> Compatibility >> Override high DPI scaling behavior >> Scaling performed by: (Application)
```

![DPI Scaling](https://github.com/BTCPrivate/bitcoin-private-full-node-wallet/raw/master/docs/EXEScalingSettings.png "DPI Scaling")


### License
Originally forked from the [ZENCash Swing Wallet](https://github.com/ZencashOfficial/zencash-swing-wallet-ui).

This program is distributed under an [MIT License](https://github.com/BTCPrivate/bitcoin-private-full-node-wallet/raw/master/LICENSE).

### Disclaimer

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.