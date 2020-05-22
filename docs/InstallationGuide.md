# Installation Guide

This document will guide you how to set up the development environment to run the software on IntelliJ.

## Step 1: Download the software

If git is not installed in your machine, please download and install git [here](https://git-scm.com/downloads). Check to
make sure git is installed by running `git` in your command line. Then clone the software to your machine by running.

```aidl
git clone https://github.com/Sundae2608/BattleSimulation.git
```

You will also need to download the following Java packages to resolve dependencies.

* [colt](https://dst.lbl.gov/ACSSoftware/colt/)
* [commons-math3-3.6.1](https://mvnrepository.com/artifact/org.apache.commons/commons-math3/3.6.1)
* [disiutils-2.6.3](http://fastutil.di.unimi.it/)
* [javafx](https://openjfx.io/)
* [parallel_colt](https://sites.google.com/site/piotrwendykier/software/parallelcolt)
* [processing-3.5.4](https://processing.org/) core library
* [processing-sound-2.2.3](https://github.com/processing/processing-sound/releases/tag/v2.2.3)

## Step 2: Setting up IntellJ development environment

IntelliJ is the author's preferred development environment for the project. You can install IntelliJ Community Version
for free [here](https://www.jetbrains.com/idea/download/#section=windows). Once this is done, Open IntelliJ and follow 
these steps to get the project up and running.

1. Go to `File > Open` and open the project folder. It should be the same location in which you have `clone` the project 
into.
2. Go to `File > Project Structure`. 
3. In `Project Settings > Project`, change SDK to version 11.
4. In `Libraries`, import all the downloaded Java packages mentioned above.
   * `colt`, `common-math`, `disiutils`, `javafx`, `parallel_colt` and `processing-sound` should exist as a `jar` file. 
   You can simply import them by pressing `+ > New Java Library > Java` and select the folder containing these `.jar`
   files.
   *  To find `processing-3.5.4` core library, go to `processing-3.5.4` folder and to `core\library`. You should import
   `core.jar` and all other `.jar` files within the folder.