package de.embl.cmci.mayumi;

import ij.gui.GenericDialog;

public class DotdetectorByObj3DParameter {

		private static int maxspotvoxels = 300000000;
		
		/**Object volume minimum for volume-based segmentation*/
		private static int minspotvoxels = 3;	
		
		/**object volume minimum for measurement
		 *  (maybe 7 is too small)*/
		private static int minspotvoxels_measure = 7;
		
		/** maximum loop for exiting optimum threshold searching	 */
		private static int maxloops =50;
		
		private static int thadj_volmin = 5;
		
		private static int thadj_volmax = 80;

		private static int thadj_nummin = 1;
		
		private static int thadj_nummax = 4;

		void ParamSetter(){}

		void ParamSetter(
				int maxspotvoxels, 
				int minspotvoxels, 
				int minspotvoxels_measure, 
				int maxloops,
				int thadj_volmin,
				int thadj_volmax,
				int thadj_nummin,
				int thadj_nummax
				)
		{
			this.setMaxspotvoxels(maxspotvoxels);
			this.setMinspotvoxels(minspotvoxels);
			this.setMinspotvoxels_measure(minspotvoxels_measure);
			this.setMaxloops(maxloops);
			this.setThadj_volmin(thadj_volmin);
			this.setThadj_volmax(thadj_volmax);
			this.setThadj_nummin(thadj_nummin);
			this.setThadj_nummax(thadj_nummax); 	
		}

		public boolean showDialog()	{
			GenericDialog gd = new GenericDialog("Kinetochore Dots Measurement");
			gd.addNumericField("Segmentation_Min Spot Size  (3Dobject) :", this.getMinspotvoxels(), 0);
			gd.addNumericField("Measurmeents Min Spot Size  (3Dobject) :", this.getMinspotvoxels_measure(), 0);
			gd.addNumericField("Segmentation_Max Spot Size for  (3Dobject) :", this.getMaxspotvoxels(), 0);

			gd.addMessage("------ Adjustment Loop ------");
			
			gd.addNumericField("Min_Volume Sum for Segmentation :", this.getThadj_volmin(), 0);
			gd.addNumericField("Max_Volume Sum for Segmentation :", this.getThadj_volmax(), 0);
			gd.addNumericField("Min_Object Number for Segmentation :", this.getThadj_nummin(), 0);
			gd.addNumericField("Max_Object Number for Segmentation :", this.getThadj_nummax(), 0);
			
			gd.addMessage("------ Advanced Options ------");
			gd.addNumericField("Maximum_Loop_exit for threshold adjustment :", this.getMaxloops(), 0);
			
			gd.showDialog();
			if (gd.wasCanceled()) 
				return false;
			this.setMinspotvoxels((int) gd.getNextNumber());
			this.setMinspotvoxels_measure((int) gd.getNextNumber());
			this.setMaxspotvoxels((int) gd.getNextNumber());	

			this.setThadj_volmin((int) gd.getNextNumber());
			this.setThadj_volmax((int) gd.getNextNumber());
			this.setThadj_nummin((int) gd.getNextNumber());
			this.setThadj_nummax((int) gd.getNextNumber());

			this.setMaxloops((int) gd.getNextNumber());

			return true;
		}

		/**
		 * @param maxspotvoxels the maxspotvoxels to set
		 */
		public void setMaxspotvoxels(int maxspotvoxels) {
			this.maxspotvoxels = maxspotvoxels;
		}

		/**
		 * @return the maxspotvoxels
		 */
		public int getMaxspotvoxels() {
			return maxspotvoxels;
		}

		/**
		 * @param minspotvoxels the minspotvoxels to set
		 */
		public void setMinspotvoxels(int minspotvoxels) {
			this.minspotvoxels = minspotvoxels;
		}

		/**
		 * @return the minspotvoxels
		 */
		public int getMinspotvoxels() {
			return minspotvoxels;
		}

		/**
		 * @param minspotvoxels_measure the minspotvoxels_measure to set
		 */
		public void setMinspotvoxels_measure(int minspotvoxels_measure) {
			this.minspotvoxels_measure = minspotvoxels_measure;
		}

		/**
		 * @return the minspotvoxels_measure
		 */
		public int getMinspotvoxels_measure() {
			return minspotvoxels_measure;
		}

		/**
		 * @param maxloops the maxloops to set
		 */
		public void setMaxloops(int maxloops) {
			this.maxloops = maxloops;
		}

		/**
		 * @return the maxloops
		 */
		public int getMaxloops() {
			return maxloops;
		}

		/**
		 * @param thadj_volmin the thadj_volmin to set
		 */
		public void setThadj_volmin(int thadj_volmin) {
			DotdetectorByObj3DParameter.thadj_volmin = thadj_volmin;
		}

		/**
		 * @return the thadj_volmin
		 */
		public int getThadj_volmin() {
			return thadj_volmin;
		}

		/**
		 * @param thadj_volmax the thadj_volmax to set
		 */
		public void setThadj_volmax(int thadj_volmax) {
			DotdetectorByObj3DParameter.thadj_volmax = thadj_volmax;
		}

		/**
		 * @return the thadj_volmax
		 */
		public int getThadj_volmax() {
			return thadj_volmax;
		}

		/**
		 * @param thadj_nummin the thadj_nummin to set
		 */
		public void setThadj_nummin(int thadj_nummin) {
			DotdetectorByObj3DParameter.thadj_nummin = thadj_nummin;
		}

		/**
		 * @return the thadj_nummin
		 */
		public int getThadj_nummin() {
			return thadj_nummin;
		}

		/**
		 * @param thadj_nummax the thadj_nummax to set
		 */
		public void setThadj_nummax(int thadj_nummax) {
			DotdetectorByObj3DParameter.thadj_nummax = thadj_nummax;
		}

		/**
		 * @return the thadj_nummax
		 */
		public int getThadj_nummax() {
			return thadj_nummax;
		}	
		

	}

