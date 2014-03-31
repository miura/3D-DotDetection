import emblcmci.mayumi.DotdetectorByObj3DParameter;
import ij.plugin.PlugIn;


public class ParaSetter_ implements PlugIn {

		@Override
		public void run(String arg) {
			DotdetectorByObj3DParameter para = new DotdetectorByObj3DParameter();
			para.showDialog();
		}

}
