#3D Dot Detector Plugin for ImageJ

Kota Miura (<miura@embl.de>)

## Installation

### Fiji

Please download and install Fiji.

<http://fiji.sc>

### This plugin

Install jar file `target/DotDetect3D-2.0.0.jar` as a plugin for ImageJ / Fiji. This plugin analyzes a 3D image stack and lists dots in results table. 

The command is located at

[Plugins > EMBLTools > Mayumi > Dot Detect 3D]

### Script

The actual analysis for Mayumi's project is done by running the script 

[src/main/scripts/Dot3Danalysis_2_MI.py](https://github.com/cmci/MayumiProject/blob/master/src/main/scripts/Dot3Danalysis_2_MI.py)

from within Fiji. 

### Dependencies

Please download the MosaicSuit ImageJ plugin from the URL below and install it to your local ImageJ or Fiji:

<http://mosaic.mpi-cbg.de/?q=downloads/imageJ>

If you want to be always updated with changes, use the update site functionality and install "MOSAIC ToolSuite" using ImageJ updater. 

<http://fiji.sc/List_of_update_sites>

Please read their conditions and terms, and if you use this tool in your paper, please also site the following paper. 

```
I. F. Sbalzarini and P. Koumoutsakos. 
Feature Point Tracking and Trajectory Analysis for Video Imaging in Cell Biology
Journal of Structural Biology 151(2):182-195, 2005.
```

### To Compile

Install the jar file to your local Maven repository by following command:

```
mvn install:install-file -Dfile=/PATH/TO/Mosaic_ToolSuite.jar -DgroupId=de.mpi-cbg.mosaic  -DartifactId=mosaicSuit -Dversion=1.0.0 -Dpackaging=jar
```

Then compile java files as usual. 

```
mvn compile
```

##Usage

## Acknowledgements

This plugin uses FeaturePoint detection library from MosaicSuit plugin developed by Mosaic group (Ivo Sbalzarini, CBG-MPI, Dresden). We thank their great implementation and offering them as an open source library! 
