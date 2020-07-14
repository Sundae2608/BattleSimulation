# Installation Guide

This document will guide you how to set up the development environment to run the software on IntelliJ.

## Step 1: Download the software and required dependencies

If git is not installed in your machine, please download and install git [here](https://git-scm.com/downloads). Check to
make sure git is installed by running `git` in your command line. Then clone the software to your machine by running.

```aidl
git clone https://github.com/Sundae2608/BattleSimulation.git
```

You will also need to download the following Java packages to resolve dependencies.

* [colt](https://dst.lbl.gov/ACSSoftware/colt/)
* [commons-math3-3.6.1](https://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.6.1)
* [disiutils-2.6.5](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22it.unimi.dsi%22)
* [fastutils-8.3.1](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22it.unimi.dsi%22)
* [javafx](https://openjfx.io/)
* [parallel_colt](https://sites.google.com/site/piotrwendykier/software/parallelcolt)
* [processing-3.5.4](https://processing.org/) core library
* [processing-sound-2.2.3](https://github.com/processing/processing-sound/releases/tag/v2.2.3). Remember to get 
`sound.zip` which contains the .jar files.

## Step 2: Import the necessary dependencies.

IntelliJ is the author's preferred development environment for the project. You can install IntelliJ Community Version
for free [here](https://www.jetbrains.com/idea/download/#section=windows). Once this is done, Open IntelliJ and follow 
these steps to get the project up and running.

1. Go to `File > Open` and open the project folder. It should be the same location in which you have `clone` the project 
into.
2. Go to `File > Project Structure`. 
3. In `Project Settings > Project`, change SDK to version 11.
4. In `Libraries`, import all the downloaded Java packages mentioned above.
   * `colt`, `common-math`, `disiutils`, `fastutils`, `javafx`, `parallel_colt` should exist as a `jar` file. 
   You can simply import them by pressing `+ > New Java Library > Java` and select the folder containing these `.jar`
   files.
   * To find `processing-3.5.4` core library, go to `processing-3.5.4` folder and to `core\library`. Make sure import
   `core.jar` and all other `.jar` files within the folder.
   * When you import `processing-sound` libraries, be sure to also import all of `sound.jar`, `javamp` and `jsyn`
   libraries.
   
## Step 3: Set up running configuration.

1. Go to `Run > Edit Configuration`.
2. Press the `+` button and add a new configuration called `Application`.
3. Type in `Name` whatever name you wish to call the configuration.
4. In `Main class`, change it to `MainSimulation`.
5. In Working directory, change it to the root folder `BattleSimulation` which contains all of your files.
6. In `Use classpath of module`, choose `BattleSimulation`
7. Make sure JRE version is 11 or above.
8. Click `Apply` / `OK`.

In the top right corner, you should now see your configuration name to the left of a green Play button. Press Play to
test run the software.

If a Processing window opens up and you can zoom in and out of the battlefield, you have successfully set up
the development environment.

If you struggle to set up your environment, please create an Issue 
[here](https://github.com/Sundae2608/BattleSimulation/issues).
