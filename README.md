# Treehouse Remote

An Android app that communicates with headless Raspberry Pi mobile server runing [treehouses images](https://github.com/treehouses/builder) via bluetooth.
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

3. Create a file named `autorunonce` and place it in the `boot` partition of the microSD card:

    ```
    pirateship rename ole # rename the raspberry pi so we could easily distinguish the bluetooth device
    pirateship bluetooth on # enable bluetooth and our bluetooth-server on the Rasoberry Pi
    # pirateship wifi ssid password # if you do not have ethernet cable/access to router, uncomment this line and replace with your wifi ssid and password 
    ```

4. Unmount and remove the microSD card from the card reader and place it into the RPi.

5. Connect a RJ45 network cable to the Ethernet port on the RPi. (Skip this step if you have Wi-Fi configured in step 3)

6. Connect the RPi to power.

7. Wait for a minute or two and look for `ole` in your Android device's bluetooth setting

## Setting Up The Android Device

* Steps to be determined
