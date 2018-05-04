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
    pirateship rename treehouses # rename the raspberry pi so we could easily distinguish the bluetooth device
    pirateship bluetooth on # enable bluetooth and our bluetooth-server on the Raspberry Pi
    # pirateship wifi ssid password # if you do not have ethernet cable/access to router, uncomment this line and replace with your wifi ssid and password 
    ```

4. Unmount and remove the microSD card from the card reader and place it into the RPi.

5. Connect a RJ45 network cable to the Ethernet port on the RPi (skip this step if you have Wi-Fi configured in step 3.)

6. Connect the RPi to power.

7. Wait for a minute or two and look for `treehouses` in your Android device's Bluetooth pairing screen.

## Setting Up the Android Device

The basic requirements of setting an android environment up is firstly, download [Android Studio](https://open-learning-exchange.github.io/#!./pages/robots/rbts-takehome-android-studio-setup.md) and later go through the basic requirements of [Android Device](https://open-learning-exchange.github.io/#!./pages/robots/rbts-takehome-device-setup.md)

After you set up the above steps. Please go through the following inorder to run the `remote` application in your mobile device

In the android device, once you come across `Treehouses` icon, please click that to open the application

1. After the application is opened, you will get to the dashboard where pirateship, docker and cmd buttons will be displayed. 

2. Inorder to connect with raspsberry pi wiith bluetooth. Please click the three vertical dots or menu icon visible at the right handside of the application. 

3. Please click on the three vertical dots visible to display a drop down list of `insecure-connect`. Please click on that to display the devices that are on bluetooth around you. 

4. Please select `raspberry pi` or whatever name that is given to your raspberry pi device (raspberry pi being default) to connect to the raspberry pi via bluetooth.

5. If it does not work out, please click on cmd button and repeat steps 3 & 4.

6. Click on the buttons present in cmd activity to check the functionality of raspberry pi and for testing.

7. We can check the connectivity status by the circle present on the left hand side of the screen below action bar where `green` indicates successful connection and `red` indicates not connected to raspberry pi. The connection status can also be seen on the action bar messages.

8. Alternatively, we can connect to raspberry pi with the help of wifi. Click the wifi icon present in the action bar to open up the dialog fragment.

9. Enter `SSID` and `password` to connect to raspberry pi via wifi.

10. Click on the back button present at the bottom to go back to the dashboard.

11. Additionally, we can use [Vysor](https://www.vysor.io/) to display the working/ testing of an issue or the whole app. This helps in explaning the issues or discussing further about a particular subject.

