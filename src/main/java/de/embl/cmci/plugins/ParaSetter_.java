package de.embl.cmci.plugins;

import de.embl.cmci.mayumi.DotdetectorByObj3DParameter;
import ij.plugin.PlugIn;


public class ParaSetter_ implements PlugIn {

		@Override
		public void run(String arg) {
			DotdetectorByObj3DParameter para = new DotdetectorByObj3DParameter();
			para.showDialog();
		}

}
