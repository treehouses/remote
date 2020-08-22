# Treehouse Remote

[![Build Status](https://travis-ci.org/treehouses/remote.svg?branch=master)](https://travis-ci.org/treehouses/remote) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/treehouses/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


An Android app that communicates with headless Raspberry Pi mobile server running [treehouses image](https://github.com/treehouses/builder) via Bluetooth.
- Get detailed software and hardware information of a Raspberry Pi.
- Configure a Raspberry Pi through user-friendly interface.

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="40">](https://play.google.com/store/apps/details?id=io.treehouses.remote)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="40">](https://f-droid.org/packages/io.treehouses.remote/)

Check our [issues](https://github.com/treehouses/remote/issues) to see what features we are working on.

## Setting Up the Raspberry Pi
Within this section we will cover the required hardware and software you will need to contribute to the System's Engineering team, then explore the first steps you should take as a Virtual Intern.

### Prerequisites

We will need a few hardware and software components as follows:

* officially supported Raspberry Pi versions:
  - Pi 3b/3b+
  - Pi 4b
  - Zero W
  - and Pi 3a+

* power supply
  - 5V 2.4A with microUSB connector for 3b/3+ and 3a+
  - 5V 1.2A with microUSB connector for Zero W
  - 5V 3A with usb-c connector for 4b

* A microSD card reader (check to see if your computer has an SD slot)  

* Three or more [Class 10](https://www.sdcard.org/developers/overview/speed_class/index.html) microSD card (minimal 8GB, but we strongly recommend 32GB or greater)

* Software for burning OS image to microSD card. We recommend [Etcher](https://etcher.io), but there are many from which to choose

* The latest version of [Treehouse image](http://dev.ole.org/)

* Wi-Fi or a ethernet connection


## Get It Up and Running
After getting your microSD cards and card reader/adapter, our first step will be to burn an image of the treehouse onto the a microSD card which we will later insert into our Rasperry Pi.

### Prepping the microSD card
1. Download the newest treehouse image that is available [here](https://treehouses.io/#!pages/download.md)

2. Download and install [Etcher](https://etcher.io) or another software.

3. Insert a microSD card into the card reader and connect this to your computer 

4.   Burn the treehouses image onto the microSD card, this is done by:
* Opening Etcher
* Clicking "Select Image" -This is the downloaded image
* Selecting the "Target" - This is your microSD card
* Clicking "Flash!"

The process will take approximately 10 min to complete.

5.  Once it's done burning,  if you chose you can view the contents in the `boot` partition. You will see a long list of files.

6. Unmount and safely eject the microSD card from your computer. 

### Starting up the Raspberry Pi
1. Place the microSD card into the RPi.

2. Connect the RPi to power, if there in an "on" switch or button ensure the Rpi has power.

3. If you possess an android phone, find the Treehouses Remote app on [Google Play](https://play.google.com/store/apps/details?id=io.treehouses.remote) or on [F-Droid](https://f-droid.org/packages/io.treehouses.remote/).

4. Wait for a minute or two and look for `treehouses` in your Android device's Bluetooth pairing screen.


## Setting Up the Android Device

We assume you've already installed [Android Studio](https://developer.android.com/studio/install) on your machine and [enabled USB debugging](https://developer.android.com/studio/command-line/adb#Enabling) on your Android device.

### Connect to Raspsberry Pi via Bluetooth

1. In the android bluetooth settings, scan and pair with the Pi device (look for `treehouses`)

2. Open the `treehouses remote` app 

3. Click on "Connect to RPI"  

4. Select your Raspberry Pi (`treehouses-<4-digit-number>`)i.e. `treehouses-8930` 

5. Once you have connected to your Raspberry Pi, tap the menu button on the top left of your screen to view a whole host of options to interact with your Raspberry Pi  

6. Go to Terminal in the treehouses app 

7. In the Terminal window type `treehouses detectrpi` and send it 

8. Now type `treehouses default network`  

9. Reboot the system by entering the command `reboot` and go back to the home screen to re-connect to your Pi.  

10. Back in the terminal, type `treehouses bridge "wifiname" treehouses "wifipassword"`, and replace `wifiname` with the name of your wifi network, and `wifipassword` with the password 

11. Reboot once again. 

### Features

- The connection status is shown on the action bar.
- In `cmd`
  - Use the Wi-Fi icon on the action bar to comfigure the Raspberry Pi to connect to a Wi-Fi network.
  - Use buttons on the lower part of the screen to perform various commands.
  - The circle on the upper left corner shows the Raspberry Pi's internet connectivity status using color green and red.
  - To return to dashboard, click on the back button at the bottom of the device.


## Tools

#### Scrcpy

[Scrcpy](https://github.com/Genymobile/scrcpy) allows screen sharing over USB or Internet for Android to PC. This runs on Windows/Mac/Linux and has no advertisements. No root access is required. 

Checkout our tutorial for [Android Screen Sharing](https://treehouses.io/#!./pages/blog/20190925-mobilescreenshare.md) using *Scrcpy*.
