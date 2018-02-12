# Treehouse Remote

is an Android app to communicate with headless Raspberry Pi driven mobile server.
Trough a Bluetooth connection it gives you detailed information about the Soft- and Hardware.
On top of that there is an interface to run a cli command and other stuff to remote control this RPI server.


## Prerequisites
<!---
#please move to builder
--->
In order to complete this installation, we will need a few hardware and software components as follows:

* Raspberry Pi 3 (or zero W) and 5V 2.4A power supply with microUSB connector

* A microSD card reader

* A [Class 10](https://www.sdcard.org/developers/overview/speed_class/index.html) microSD card (minimal 8GB, but we recommend 16GB or greater)

* Software for burning OS image to microSD card. We recommend [Etcher](https://etcher.io), but there are many from which to choose

* The latest version of [Treehouse image](http://dev.ole.org/)

* Wi-Fi or a ethernet connection

## Installation Steps

1. Burn the treehouse image to the microSD card. This is a simple process with Etcher - select the treehouse image, select the microSD card and burn the image;

2. Once it's done burning, remount the microSD card if its unmounted by Etcher, so that you can view the contents in the `boot` partition. You will see a long list of files as follows:

3. In order for the installation to complete, we must create a small text file called `autorunonce` and place it in the `boot` partition of the microSD card.

  * Open code editor of your choice

    ```
    pirateship rename ole
    pirateship bluetooth on
    # if you do not have ethernet cable/access to router, uncomment next line and replace yours wifi ssid and password 
    # pirateship wifi ssid password
    ```

  * Save the file with name `autorunonce` and place it in the `boot` partition of the microSD card.

4. Unmount and remove the microSD card from the card reader and place it into the RPi.

5. Connect a RJ45 network cable to the Ethernet port on the RPi. (Skip this step if you have Wi-Fi configured in step 3)

6. Connect the RPi to power.

7. Wait for a minute or two and look for `ole` in your Android device's bluetooth setting

## Setting Up The Android Device

* Steps to be determined
