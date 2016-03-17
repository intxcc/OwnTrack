<img align="left" src="/app/src/main/res/mipmap-xxhdpi/ic_launcher.png?raw=true" alt="Application icon" />
## OwnTrack - Tracking software respecting privacy

### IMPORTANT: Please read the next paragraph carefully, before trying to use this app!
This app will NOT work "out of the box". This app is one of two components to make location tracking possible, without leaking personal information. The second part is a web application, which you have to setup manually on a webserver.

If you do not know how to set up a PHP script on a server, setup a Database, execute a Python script or do not know what any of these words mean, this app is not what you came here for.


### What is OwnTrack?
_OwnTrack lets you track the location of your phone remotely, without surrendering your privacy._ OwnTrack is an android location tracking application with great attention to respecting your privacy. This application tracks all movements of your phone and saves all gathered data remotely, but privately. The idea is to - for example - get it back after it was stolen or just out of curiosity, to visualize your movement paths. But there are a lot of use cases, which you are free to explore; all without giving away any private information to second or third parties.

__But there are thousands of applications like this!?__

No. There are thousand of tracking applications, which store your movements on a second or third party server and who knows what they do with your data. If you don't care about that, this app is not the right choice for you. This app needs some technical expertise before the first use. But if once done you will __have back the control over one of your most private information__, your location. After the initial setup this app is as convenient as the more popular tracking solutions, completely for free and open source.

__How does it work?__

The tracking service has two parts: The app itself, to install on your phone and the remote server, which can be installed on almost any web server, including your own.

The app will check the phones location every few minutes, based on your settings. After a custom number of received locations, the app will establish an encrypted connection with your remote server and send all received locations.

__Another major difference__

between this tracking app and the currently popular ones is, that even if you want the server to visualize the phones movement path, it doesn't need to send that path to a second party. The Python script - which can be found in the GitHub repository for this app - will download the openstreetmap map-tile that fits the location range of the time frame you want to have a look at and draws the location points locally. without even openstreetmap knowing where these points are.

__Your most private informations do never leave your control. You do not have to trust any second or third party__

### What platforms are supported by OwnTrack?
Currently OwnTrack supports only __Android 4.1 or above__ (API 16). _Windows Phone and iOS are not supported_. This is due to the fact, that they don't really allow the user to control the OS, like custom roms without gApps do. Therefore a privacy application for these platforms would be rather useless.

## HowTo - Install the client
Installing the all (client) is very easy. You have to options, you become a beta tester and receive all updates
automatically. If you don't want or can use the Play Store, you can also look into the releases tab here on
GitHub or look into the folder /master/apks/ for the newest build. Or - of course - you can just compile the app yourself.

### Good news! From now on anyone can become an open beta tester and help speeding up development and fix issues
OwnTrack has just arrived open beta stage. Want to help some developer and get free ice cream? I would have choosen that as well. Sadly I don't have ice cream, but you could still help fix some bugs if you become a beta tester and be one of the first ones to download OwnTrack from the official Google Play Store. Thank you!

To start just follow this link: https://play.google.com/apps/testing/cc.intx.owntrack

## HowTo - Install the server
This is a little more tricky and therefore in its own file. Check out [INSTALL.md](/INSTALL.md) for the instructions.

## Development

### What is the current development state?
OwnTrack is currently __working__. OwnTrack is still beta software and has some bugs, but the main functionality should work flawlessly.

### Features
- Option to enable auto starting of the tracking service after reboot.
- Already received locations are never lost, but will just wait for the next upload attempt
- Completely independent server, to setup by yourself
- Not secure connections are prevented
- Certificates can be pinned to prevent MITM and to allow verifying servers with self signed certificates

### What you need to use OwnTrack
- A smartphone with Android 4.1 or above
- A webserver with the ability to:
	- Handle HTTPS requests (Self signed certificates may be allowed trough app settings)
	- Execute PHP scripts
	- (Optional) Execute python scripts

### Road map
- Implementing the PHP script in Phyton or vice versa
- Plenty of small TODOs in the source code
- Make visualizing the location paths easier: A small "CMS" or looking up the map in the app

## Latest Screenshots
<img align="left" src="/screenshots/2016-03-17_00.png?raw=true" width="30%" alt="Latest screenshots" />
<img align="left" src="/screenshots/2016-03-17_02.png?raw=true" width="30%" />
<img align="left" src="/screenshots/2016-03-17_01.png?raw=true" width="30%" />
<img align="left" src="/screenshots/2016-03-17_03.png?raw=true" width="30%" />
<img align="left" src="/screenshots/2016-03-15_02.png?raw=true" width="30%" />
