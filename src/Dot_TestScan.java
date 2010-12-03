import emblcmci.mayumi.DotdetectorByObj3D;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Dot_TestScan implements PlugIn {

	int initTh = 1000;
	int finTh = 2500;
	int stepTh = 5;
	int maxTh = 3000;
	
	int minspotvoxels = 10;
	int maxspotvoxels = 3000;
	
	
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		DotdetectorByObj3D at = new DotdetectorByObj3D();
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null)
			at.ThresholdStudyBy3Dobj(imp, initTh, finTh, stepTh, maxTh, minspotvoxels, maxspotvoxels);

	}
}
