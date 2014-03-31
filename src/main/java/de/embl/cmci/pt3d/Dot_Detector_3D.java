package de.embl.cmci.pt3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Vector;
import mosaic.core.detection.FeaturePointDetector;
import mosaic.core.detection.MyFrame;
import mosaic.core.detection.Particle;
import mosaic.core.detection.PreviewCanvas;
import mosaic.core.detection.PreviewInterface;
import mosaic.core.utils.MosaicUtils;

public class Dot_Detector_3D
  implements PlugInFilter, PreviewInterface
{
  protected ImagePlus original_imp;
  private ImageStack stack;
  private String title;
  private int frames_number;
  private int slices_number;
  private FeaturePointDetector detector;
  private GenericDialog gd;
  private PreviewCanvas preview_canvas;
  private boolean frames_processed = false;
  private MyFrame[] frames;
  private int preview_slice_calculated;

  public int setup(String arg, ImagePlus imp)
  {
    if (IJ.versionLessThan("1.38u")) {
      return 4096;
    }

    this.original_imp = imp;

    if (imp == null) {
      IJ.error("You must load an Image Sequence or Movie first");
      return 4096;
    }
    if (this.original_imp.getStackSize() == 1) {
      return 223;
    }
    return 32991;
  }

  public void run(ImageProcessor ip) {
    initializeMembers();
    this.preview_canvas = this.detector.generatePreviewCanvas(this.original_imp);
    if (!getUserDefinedParams()) return;
    if (!processFrames()) return;
    transferParticlesToResultsTable();
  }

  private void initializeMembers()
  {
    this.stack = this.original_imp.getStack();
    this.title = this.original_imp.getTitle();

    StackStatistics stack_stats = new StackStatistics(this.original_imp);
    float global_max = (float)stack_stats.max;
    float global_min = (float)stack_stats.min;
    this.frames_number = this.original_imp.getNFrames();
    this.slices_number = this.original_imp.getNSlices();

    this.detector = new FeaturePointDetector(global_max, global_min);
  }

  boolean getUserDefinedParams()
  {
    this.gd = new GenericDialog("Particle Tracker...", IJ.getInstance());

    boolean convert = false;

    this.detector.addUserDefinedParametersDialog(this.gd);

    this.gd.addPanel(this.detector.makePreviewPanel(this, this.original_imp), 10, new Insets(5, 0, 0, 0));

    if ((this.original_imp.getType() != 0) && 
      (this.original_imp.getType() != 1) && 
      (this.original_imp.getType() != 2)) {
      this.gd.addCheckbox("Convert to Gray8 (recommended)", true);
      convert = true;
    }

    this.gd.showDialog();

    Boolean changed = this.detector.getUserDefinedParameters(this.gd);

    if ((changed.booleanValue()) && 
      (this.frames_processed)) {
      this.frames = null;
      this.frames_processed = false;
    }

    if (convert) convert = this.gd.getNextBoolean();

    return true;
  }

  public void preview(ActionEvent e)
  {
    this.original_imp.getWindow().setLocation((int)this.gd.getLocationOnScreen().getX() + this.gd.getWidth(), (int)this.gd.getLocationOnScreen().getY());

    preview();
    this.preview_canvas.repaint();
  }

  public synchronized void preview()
  {
    if (this.original_imp == null) return;

    this.stack = this.original_imp.getStack();

    this.preview_slice_calculated = this.original_imp.getCurrentSlice();

    this.detector.getUserDefinedPreviewParams(this.gd);

    int first_slice = (getFrameNumberFromSlice(this.preview_slice_calculated) - 1) * this.slices_number + 1;

    MyFrame preview_frame = new MyFrame(MosaicUtils.GetSubStackCopyInFloat(this.stack, first_slice, first_slice + this.slices_number - 1), getFrameNumberFromSlice(this.preview_slice_calculated) - 1, 2);

    this.detector.featurePointDetection(preview_frame);
    this.detector.setPreviewLabel("#Particles: " + preview_frame.getParticles().size());

    this.preview_canvas.setPreviewFrame(preview_frame);
    this.preview_canvas.setPreviewParticleRadius(this.detector.getRadius());
    this.preview_canvas.setPreviewSliceCalculated(this.preview_slice_calculated);
  }

  public int getFrameNumberFromSlice(int sliceIndex)
  {
    return (sliceIndex - 1) / this.slices_number + 1;
  }

  public void saveDetected(ActionEvent e)
  {
    this.detector.getUserDefinedPreviewParams(this.gd);

    if (processFrames()) {
      this.detector.saveDetected(this.frames);
    }
    this.preview_canvas.repaint();
  }

  public boolean processFrames()
  {
    if (this.frames_processed) return true;

    this.frames = new MyFrame[this.frames_number];
    MyFrame current_frame = null;

    int frame_i = 0; for (int file_index = 0; frame_i < this.frames_number; file_index++)
    {
      current_frame = new MyFrame(MosaicUtils.GetSubStackInFloat(this.stack, frame_i * this.slices_number + 1, (frame_i + 1) * this.slices_number), frame_i, 2);

      IJ.showStatus("Detecting Particles in Frame " + (frame_i + 1) + "/" + this.frames_number);
      this.detector.featurePointDetection(current_frame);
      this.frames[current_frame.frame_number] = current_frame;
      IJ.freeMemory();

      frame_i++;
    }

    this.frames_processed = true;
    return true;
  }
  public void transferParticlesToResultsTable() {
    ResultsTable rt = null;
    try {
      rt = ResultsTable.getResultsTable(); } catch (Exception localException) {
    }
    if ((rt.getCounter() != 0) || (rt.getLastColumn() != -1)) {
      if (IJ.showMessageWithCancel("Results Table", "Reset Results Table?"))
        rt.reset();
      else {
        return;
      }
    }
    int rownum = 0;
    for (int i = 0; i < this.frames.length; i++) {
      Vector<Particle> particles = this.frames[i].getParticles();
      for (Particle p : particles) {
        rt.incrementCounter();
        rownum = rt.getCounter() - 1;
        rt.setValue("frame", rownum, p.getFrame());
        rt.setValue("x", rownum, p.x);
        rt.setValue("y", rownum, p.y);
        rt.setValue("z", rownum, p.z);
        rt.setValue("m0", rownum, p.m0);
        rt.setValue("m1", rownum, p.m1);
        rt.setValue("m2", rownum, p.m2);
        rt.setValue("m3", rownum, p.m3);
        rt.setValue("m4", rownum, p.m4);
        rt.setValue("NPscore", rownum, p.score);
      }
    }
    rt.show("Results");
  }
}

/* Location:           /Users/miura/Dropbox/people/mayumi/Dot_Detector3D.jar
 * Qualified Name:     emblcmci.pt3d.Dot_Detector_3D
 * JD-Core Version:    0.6.2
 */
