<img align="left" src="/app/src/main/res/mipmap-xxhdpi/ic_launcher.png?raw=true" alt="Application icon" />
## OwnTrack - Tracking software respecting privacy

### What is OwnTrack?
_OwnTrack is an android location tracking application_. The goal is to develop an application to track all movements of your phone and all gathered data remotely. The idea is for example, to get it back after it was stolen or just out of curiosity, to visualize one movement paths. But there are a lot of use cases, which you are free to explore.

__But there are thousands of applications like this!?__

No. There are thousand of tracking applications, which store your movements on a second or third party server and who knows what they do with your data. If you don't care about that, this app is not the right choice for you. This app needs some technical knowledge once, to set it up. But if done you can __have back the control over your most private data__. After this initial setup this app is as convenient as the popular tracking solutions, completely for free. The tracking service has two parts: The app itself, to install on your phone and the remote server, which can be installed on almost any web server, including your own.

The app will check the phones location every few minutes, based on your settings. After a custom number of received locations, the app will establish an encrypted connection with your remote server, send all received locations and forget them. Received locations are not lost on reboot, or crash, until they are successfully sent.

The server checks the authenticity of every received location to prevent spam from third parties.

__Another major difference__ between this tracking app and the currently established is, that even if you want
the server to visualize the phones movement path it doesn't need to use APIs from e.g. Google, which would be also
a leakage your location history. This app includes a python script, which will download the openstreetmap tile that fits the time frame you want to see and then draws the location points locally, without openstreetmap knowing where these points are.

__Your most private informations do never leave your control. You do not have to trust any second or third party.__

### What platforms are supported by OwnTrack?
Currently OwnTrack supports only __Android 4.1 or above__ (API 16). _Windows Phone and iOS are not supported_. This is due to the fact, that they don't really allow the user to control the OS, like custom roms without gApps do. Therefore a privacy application for these platforms would be rather useless.

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
