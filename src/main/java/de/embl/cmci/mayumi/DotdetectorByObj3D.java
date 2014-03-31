package de.embl.cmci.mayumi;

import java.util.Vector;

import Utilities.Counter3D;
import Utilities.Object3D;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.ZProjector;

public class DotdetectorByObj3D {

	// Object3D counter related variables
	boolean excludeOnEdges, showObj, showSurf, 
			showCentro, showCOM, showNb, 
			whiteNb, newRT, showStat, 
			showMaskedImg, closeImg, 
			showSummary, redirect;

	DotdetectorByObj3DParameter para = new DotdetectorByObj3DParameter();	

	int maxspotvoxels = para.getMaxspotvoxels();
	
	/**Object volume minimum for volume-based segmentation*/
	int minspotvoxels = para.getMinspotvoxels();	
	
	/**object volume minimum for measurement
	 *  (maybe 7 is too small)*/
	int minspotvoxels_measure = para.getMinspotvoxels_measure();	
	
	/** maximum loop for exiting optimum threshold searching	 */
	int maxloops = para.getMaxloops();
	
	int thadj_volmin = para.getThadj_volmin();
	
	int thadj_volmax = para.getThadj_volmax();
	
	int thadj_nummin = para.getThadj_nummin();
	
	int thadj_nummax = para.getThadj_nummax();	
	
	public ImagePlus segmentaitonByObjectSize(ImagePlus imp){

		Duplicator bin = new Duplicator();	//this duplication may not be necessary
		ImagePlus binimp = bin.run(imp);		
		int nSlices = imp.getImageStackSize();
		int zframes = imp.getNSlices();
		int tframes = nSlices/zframes;
		double minth =0.0;
		int adjth =0;
		Duplicator dup = new Duplicator();	//this duplication may not be necessary
		ImagePlus impcopy = null;
		int maxth = (int) Math.pow(2,imp.getBitDepth());
		for(int i =0; i<tframes; i++){
			impcopy = dup.run(imp, (i*zframes+1), (i+1)*zframes);
			
			//second argument is cutoff pixel area in histogram upper part
			//--> this method is pretty much manual. should think about some way to 
			// automaticcally set the valu
			minth = initializeThresholdLevel(impcopy, 25); 
			
			IJ.log(Integer.toString(i)+": initial threshold set to "+Double.toString(minth));
			adjth = (int) ThresholdAdjusterBy3Dobj(imp, (int)minth, this.thadj_volmin, this.thadj_volmax, this.thadj_nummin, this.thadj_nummax);
			IJ.log("... ... Adjusted to "+Integer.toString(adjth));
			
			for (int j=0; j<zframes; j++)
				binimp.getStack().getProcessor(i*zframes+1+j).threshold(adjth);
		}	
		return binimp;
	}
	
	/**Initializes the threshold value of 3D stack with dark background 
	 * <ul>
	 * <li>1. z-projection by maximum intensity
	 * <li>2. get histogram of z-projection
	 * <li>3. then find a pixel value that the upper area of histogram becomes larger than setting value
	 * </ul>
	 * <br>tried using Shanbhag autothreshold but later suppressed. 
	 * <br> 
	 * @param imp	grayscale 3D stack
	 * @param cutoff_upperArea pixel area of upper part of histogram 
	 * @return
	 */
	public int initializeThresholdLevel(ImagePlus imp, int cutoff_upperArea){
		ZProjector zpimp = new ZProjector(imp);
		zpimp.setMethod(1); //1 is max intensity projection
		zpimp.doProjection();
			//zpimp.getProjection().show();
			//IJ.setAutoThreshold(zpimp.getProjection(), "Shanbhag dark");
			//IJ.setAutoThreshold(zpimp.getProjection(), "Minimum dark");
			//double minth = zpimp.getProjection().getProcessor().getMinThreshold();
		int[] hist = zpimp.getProjection().getProcessor().getHistogram();	//simpler strategy
		int sumpixels =0;
		int i = hist.length-1;
		while (sumpixels < cutoff_upperArea){
			sumpixels += hist[i--];
		}
		return i;
	}
	
	/** Explore different threshold levels to find out an optimum threshold level for segmenting 
	 * 3D dots.<br><br>
	 * <b>updates</b>
	 * <ul>
	 * <li>20101117 added a line to suppress error messages from Counter3D
	 * when no 3D objects were found. 
	 * </ul>
	 * 
	 * @param imp	gray scale 3D stack
	 * @param initTh	initial level of threshold to start with exploration
	 * @param thadj_volmin
	 * @param thadj_volmax
	 * @param thadj_nummin
	 * @param thadj_nummax
	 * @return optimized threshold level for segmentation of 3D dots. 
	 */	
	public double ThresholdAdjusterBy3Dobj(ImagePlus imp, 
			int initTh, 
			int thadj_volmin, 
			int thadj_volmax, 
			int thadj_nummin,
			int thadj_nummax){
		// somehow this part causes error in osx
		//TODO implement initial setting more directly
//		IJ.run("3D OC Options", "volume surface nb_of_obj._voxels nb_of_surf._voxels " +
//				"integrated_density mean_gray_value std_dev_gray_value median_gray_value " +
//				"minimum_gray_value maximum_gray_value centroid mean_distance_to_surface " +
//				"std_dev_distance_to_surface median_distance_to_surface centre_of_mass " +
//				"bounding_box dots_size=5 font_size=10 redirect_to=none");


		Duplicator dup = new Duplicator();	//this duplication may not be necessary
		ImagePlus impcopy = dup.run(imp);

		//initial conditions
		int localthres =0;
		excludeOnEdges = false;
		redirect = false;
		double expectedDotSize =  100;	// this is in unit [voxlel]. Find threshold level that is close to this value.  
		
		Counter3D OC = new Counter3D(imp, initTh, minspotvoxels, (int) maxspotvoxels*2, excludeOnEdges, redirect);
		Vector<Object3D> obj = OC.getObjectsList();
		int nobj = obj.size();
		
		int volumesum = 0; 
		for (int i=0; i<nobj; i++){
			 Object3D currObj=obj.get(i);
			 volumesum += currObj.size;
		}
		double volumeavg = (double) volumesum / nobj;
		
		IJ.log("Threshold Adjuster initial th: "+ Integer.toString(initTh));
		IJ.log("... " + "Number of Objects: "+Integer.toString(nobj));
		IJ.log("... " +"Total Volume Sum: "+Integer.toString(volumesum));
		IJ.log("... " + "Average Volume: " + Double.toString(volumeavg));
		IJ.log("-> threshold level will be optimized for the average volume to" + Double.toString(expectedDotSize));
		
		localthres = initTh;
		int loopcount =0;
		while ( 
				(nobj < thadj_nummin || nobj > thadj_nummax || volumesum > thadj_volmax || volumesum <thadj_volmin) 
				&& 
				(loopcount<maxloops)
				) {

			if ((nobj<thadj_nummin) && (volumesum < thadj_volmin)) localthres--;
			if ((nobj<thadj_nummin) && (volumesum > thadj_volmax)) localthres++;			
			if ((nobj>thadj_nummax) && (volumesum > thadj_volmax)) localthres--;
			if ((nobj>thadj_nummax) && (volumesum < thadj_volmin)) localthres++;
			if ((nobj >= thadj_nummin) && (nobj <= thadj_nummax)){
				if (volumesum < thadj_volmin) localthres--;
				else localthres++;
			}
			// this part is a bit not clear
			if ((volumesum >= thadj_volmin) && (volumesum <= thadj_volmax)){
				if (nobj < thadj_nummin) localthres++;
				else localthres--;
			}			
			IJ.redirectErrorMessages(true);	//20101117
			OC = new Counter3D(impcopy, localthres, minspotvoxels, (int) (maxspotvoxels*1.5), excludeOnEdges, redirect);
			obj = OC.getObjectsList();
			nobj = obj.size();
			volumesum=0;
			for (int i=0; i<nobj; i++){
				 Object3D currObj=obj.get(i);
				 volumesum += currObj.size;
			}
			loopcount++;
		}
		if (loopcount>0) IJ.log("... New Th="+ Integer.toString(localthres)+" Iter="+Integer.toString(loopcount)+ " ObjNo:"+Integer.toString(nobj)+"Volume Sum:"+Integer.toString(volumesum));
		
		return localthres;
	}

	public void runThresholdStudyBy3Dobj(){
	}
	/** Scan through Threshold level step by Step, measure Obj3D.
	 * - number of detected 3D objects
	 * - average of 3D object volume
	 * - variance (or standard deviation) of volume
	 * - print results in a table. 
	 * @param imp		grayscale image stack (3D)
	 * @param initTh
	 * @return
	 */
	public void ThresholdStudyBy3Dobj(ImagePlus imp, int initTh,  int finTh, int stepTh, int maxTh, int minspotvoxels, int maxspotvoxels){

		//Duplicator dup = new Duplicator();	//this duplication may not be necessary
		//ImagePlus impcopy = dup.run(imp);
		
		//initial conditions
		int localthres =0;
		excludeOnEdges = false;
		redirect = false;
		int volumesum = 0; 
		double volumeavg = 0;
		double volumeVariance = 0;
		double devsum = 0;
		int nobj = 0;
		Vector<Object3D> obj;
		Counter3D OC;
		int iter = (int) Math.floor((finTh-initTh) / stepTh);
		int[] volumesumA = new int[iter];
		double[] volumeavgA = new double[iter];
		double[] volumevarianceA = new double[iter];
		int[] nobjA = new int[iter];
		
		for (int j = 0; j < iter; j++){		
			localthres = initTh + (j*stepTh);
			IJ.redirectErrorMessages(true);	//20101117
			OC = new Counter3D(imp, localthres, minspotvoxels, 
					(int) (maxspotvoxels*1.5), excludeOnEdges, redirect);
			obj = OC.getObjectsList();
			nobj = obj.size();
			volumesum = 0;
			devsum = 0;
			if (nobj>0) {
					for (int i=0; i<nobj; i++){
						 Object3D currObj=obj.get(i);
						 volumesum += currObj.size;
					}
					volumeavg = volumesum / nobj;
		
					for (int i=0; i<nobj; i++){
						 Object3D currObj=obj.get(i);
						 devsum += Math.pow(volumeavg - currObj.size, 2);
					}
					volumeVariance = devsum / nobj;
			}
			volumesumA[j] = volumesum;
			volumeavgA[j] = volumeavg;
			volumevarianceA[j] = volumeVariance;
			nobjA[j] =nobj;

		}
		showObjStatistics(initTh, stepTh, volumesumA, volumeavgA, volumevarianceA, nobjA);
	}
		/** For printing scan results to a table. 
		 * 
		 * @param initTh
		 * @param vsA
		 * @param vaA
		 * @param vvA
		 * @param nA
		 */
	   public void showObjStatistics(int initTh, int stepTh, int[] vsA, double[] vaA, double[] vvA, int[] nA){
	        ResultsTable rt;        
	        rt=new ResultsTable();	        
	        for (int i=0; i<vsA.length; i++){
	            	rt.incrementCounter();           	
	            	rt.setValue("Threshold", i, initTh + i*stepTh);
	            	rt.setValue("ObjCount", i,nA[i]);
	            	rt.setValue("AvgVolume", i, vaA[i]);
	            	rt.setValue("VolumeVariance", i, vvA[i]);
	            	rt.setValue("totalVolume", i, vsA[i]);
	        }
	       
	        rt.show("Statistics_Thresholded_Objects");
	        
	    }
	

}


