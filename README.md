# Treehouse Remote

An Android app that communicates with headless Raspberry Pi mobile server running [treehouses image](https://github.com/treehouses/builder) via Bluetooth.
- Get detailed software and hardware information of a Raspberry Pi.
- Configure a Raspberry Pi through user-friendly interface.

Check our [issues](https://github.com/treehouses/remote/issues) to see what features we are working on.

## Setting Up the Raspberry Pi

### Prerequisites
<!---
#please move to builder
--->
We will need a few hardware and software components as follows:

* Raspberry Pi 3 (or Zero W) and 5V 2.4A (1.2A for Zero) power supply with microUSB connector

* A microSD card reader

* A [Class 10](https://www.sdcard.org/developers/overview/speed_class/index.html) microSD card (minimal 8GB, but we strongly recommend 16GB or greater)

* Software for burning OS image to microSD card. We recommend [Etcher](https://etcher.io), but there are many from which to choose

* The latest version of [Treehouse image](http://dev.ole.org/)

* Wi-Fi or a ethernet connection

### Get It Up and Running

1. Burn the treehouse image to the microSD card. This is a simple process with Etcher - select the treehouse image, select the microSD card and burn the image;

2. Once it's done burning, remount the microSD card if its unmounted by Etcher, so that you can view the contents in the `boot` partition. You will see a long list of files.

3. Create a file named `autorunonce.txt` and place it in the `boot` partition of the microSD card:

    ```
    treehouses rename treehouses # rename the raspberry pi so we could easily distinguish the bluetooth device
    treehouses bluetooth on # enable bluetooth and our bluetooth-server on the Raspberry Pi
    # treehouses wifi ssid password # if you do not have ethernet cable/access to router, uncomment this line and replace with your wifi ssid and password 
    ```

4. Unmount and remove the microSD card from the card reader and place it into the RPi.

5. Connect a RJ45 network cable to the Ethernet port on the RPi (skip this step if you have Wi-Fi configured in step 3.)

6. Connect the RPi to power.

7. Wait for a minute or two and look for `treehouses` in your Android device's Bluetooth pairing screen.

## Setting Up the Android Device

We assume you've already installed [Android Studio](https://developer.android.com/studio/install) on your machine and [enabled USB debugging](https://developer.android.com/studio/command-line/adb#Enabling) on your Android device.

## Usage

Launch `Treehouses Remote` app, you'll see a dashboard with `pirateship`, `docker`, and `cmd` button.

### Connect to Raspsberry Pi via Bluetooth

1. Click on the three dots (menu icon) at the upper right corner of the app. 
1. Click on `Connect a device - Insecure` to display paired Bluetooth devices or scan for new devices around you.
1. Select `raspberrypi` or whatever name that is given to your Raspberry Pi device to connect to the Raspberry Pi.
1. If it does not work, please click on `cmd` button and repeat steps 1 through 3.

### Features

- The connection status is shown on the action bar.
- In `cmd`
  - Use the Wi-Fi icon on the action bar to comfigure the Raspberry Pi to connect to a Wi-Fi network.
  - Use buttons on the lower part of the screen to performe various commands.
  - The circle on the upper left corner shows the Raspberry Pi's internet connectivity status using color green and red.
  - To return to dashboard, click on the back button at the bottom of the device.

## Tools

#### Vysor

[Vysor](https://www.vysor.io/) lets us view and control your Android on our computerto. It might help us exlpain issues better or discuss further about a particular subject when we remotely work together.
