<img align="left" src="/app/src/main/res/mipmap-xxhdpi/ic_launcher.png?raw=true" alt="Application icon" />
## OwnTrack - Tracking software respecting privacy

### What is OwnTrack?
_OwnTrack is an android location tracking application_. The goal is to develop an application to be able to track all movements of you phone and access this information remotely. The idea is to get it back after it was stole or just for curiosity of ones movement paths, but you are free to use it for any usecase you can think of. 

__But there are thousands of applications like this!?__ 

No. There are thousand of tracking applications, which store your movements on a third party server and who knows what they do with your data. So this app tries to __give you back the control over your data__. There are two parts: The app itself, to install on your phone and the server part to host on your own server.

The app part gets your location at a given time interval, which you are able to choose yourself and this data is then send encrypted to your server

The server checks authenticity of every data send to it and stores it in a database.

__One main difference__ to other tracking apps is also, the fact, that to view the location data on a map, you don't have to give the data away to a third party, like google maps. There is a small python script, which downloads the openstreetmap tile that fits the given time frame you want to see and then draws the location points itself, without openstreetmap knowing where these points are.

__Your location data does never leave your control. You don't have to trust any third party.__

### What platforms are supported by OwnTrack?
Currently OwnTrack supports only __Android 4.1 and above__ (API 16). _Windows Phone and iOS are not supported_. This is due to the fact, that they don't really allow the user to control the OS, like custom roms without gApps do. Therefore a privacy application for these platforms would be rather useless.

### What is the current development state?
OwnTrack is currently __not working__. Please be patient until the development for the first version is finished. Thank you!

## Screenshots
<img align="left" src="/screenshots/2016-03-05.png?raw=true" alt="Latest screenshot" />
