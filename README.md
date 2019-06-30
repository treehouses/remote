# Treehouse Remote

[![Build Status](https://travis-ci.org/treehouses/remote.svg?branch=master)](https://travis-ci.org/treehouses/remote) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/treehouses/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


An Android app that communicates with headless Raspberry Pi mobile server running [treehouses image](https://github.com/treehouses/builder) via Bluetooth.
- Get detailed software and hardware information of a Raspberry Pi.
- Configure a Raspberry Pi through user-friendly interface.

Check our [issues](https://github.com/treehouses/remote/issues) to see what features we are working on.

## Setting Up the Raspberry Pi
Within this section we will cover the required hardware and software you will need to contribute to the System's Engineering team, then explore the first steps you should take as a Virtual Intern.

### Prerequisites
<!---
#please move to builder
--->
We will need a few hardware and software components as follows:

* Raspberry versions:
	• Pi 3 
	• Zero W
	• Pi 3b+

* 5V 2.4A (1.2A for Zero) power supply with microUSB connector

**NOTE**: A Raspberry Pi 4 would be a bonus purchase as of recently our latest images are not supported yet with the device

* A microSD card reader (check to see if your computer has an SD slot)  

* Three or more [Class 10](https://www.sdcard.org/developers/overview/speed_class/index.html) microSD card (minimal 8GB, but we strongly recommend 32GB or greater)

* Software for burning OS image to microSD card. We recommend [Etcher](https://etcher.io), but there are many from which to choose

* The latest version of [Treehouse image](http://dev.ole.org/)

* Wi-Fi or a ethernet connection


##Get It Up and Running
After getting your microSD cards and card reader/adapter, our first step will be to burn an image of the treehouse onto the a microSD card which we will later insert into our Rasperry Pi.

### Prepping the microSD card
1. Download the treehouse image that is available [here](http://download.treehouses.io/treehouse-88img.gz)

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

3. If you possess an android phone, find the Treehouses Remote app on Google Play Store and [download](https://play.google.com/store/apps/details?id=io.treehouses.remote) it.

4. Wait for a minute or two and look for `treehouses` in your Android device's Bluetooth pairing screen.


## Setting Up the Android Device

We assume you've already installed [Android Studio](https://developer.android.com/studio/install) on your machine and [enabled USB debugging](https://developer.android.com/studio/command-line/adb#Enabling) on your Android device.

### Connect to Raspsberry Pi via Bluetooth

1. In the android bluetooth settings, scan and pair with the Pi device.

2. Click on the three dots (menu icon) at the upper right corner of the app. 

3. Click on `Connect a device - Insecure` to display paired Bluetooth devices or scan for new devices around you.

4. Select `raspberrypi` or whatever name that is given to your Raspberry Pi device to connect to the Raspberry Pi.

5. If it does not work, please click on `cmd` button and repeat steps 1 through 3.

### Features

- The connection status is shown on the action bar.
- In `cmd`
  - Use the Wi-Fi icon on the action bar to comfigure the Raspberry Pi to connect to a Wi-Fi network.
  - Use buttons on the lower part of the screen to performe various commands.
  - The circle on the upper left corner shows the Raspberry Pi's internet connectivity status using color green and red.
  - To return to dashboard, click on the back button at the bottom of the device.



## Tools

#### Vysor

[Vysor](https://www.vysor.io/) A software that helps display your Android screen into your computer. You will find this software very helpful as it helps you explain the issue more in detail. Plus, everybody in the team can see what is happening on your screen, therfore we can help each other in debugging.
