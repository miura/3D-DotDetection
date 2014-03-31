#3D Dot Detector Plugin for ImageJ

## Requirements

### Fiji

Please download and install Fiji.

<http://fiji.sc>

### This plugin

Install jar file target/DotDetect3D-2.0.0.jar as a plugin for ImageJ / Fiji.

### Script

The actual analysis is done by running the script src/main/scripts/Dot3Danalysis_2_MI.py from within Fiji. 

### Dependencies

Please download the MosaicSuit ImageJ plugin from the URL below and install it to your local ImageJ or Fiji:

<http://mosaic.mpi-cbg.de/?q=downloads/imageJ>

Please read their conditions and terms, and if you use this tool in your paper, please also site the following paper. 

>I. F. Sbalzarini and P. Koumoutsakos. Feature Point Tracking and Trajectory Analysis for Video Imaging in Cell Biology, Journal of Structural Biology 151(2):182-195, 2005.

### To Compile

Install the jar file to your local Maven repository by following command:

```
mvn install:install-file -Dfile=/PATH/TO/Mosaic_ToolSuite.jar -DgroupId=de.mpi-cbg.mosaic  -DartifactId=mosaicSuit -Dversion=1.0.0 -Dpackaging=jar
```

Then compile as usual. 

```
mvn compile
```


## Acknowledgement

This plugin uses FeaturePoint detection library from MosaicSuit plugin developed by Mosaic group (Ivo Sbalzarini, CBG-MPI, Dresden). We thank their great implementation and offereing them as an opne source library! 
