/*     */ package emblcmci.pt3d;
/*     */ 
/*     */ import ij.IJ;
/*     */ import ij.ImagePlus;
/*     */ import ij.ImageStack;
/*     */ import ij.gui.GenericDialog;
/*     */ import ij.gui.ImageWindow;
/*     */ import ij.measure.ResultsTable;
/*     */ import ij.plugin.filter.PlugInFilter;
/*     */ import ij.process.ImageProcessor;
/*     */ import ij.process.StackStatistics;
/*     */ import java.awt.Insets;
/*     */ import java.awt.Point;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Vector;
/*     */ import mosaic.core.detection.FeaturePointDetector;
/*     */ import mosaic.core.detection.MyFrame;
/*     */ import mosaic.core.detection.Particle;
/*     */ import mosaic.core.detection.PreviewCanvas;
/*     */ import mosaic.core.detection.PreviewInterface;
/*     */ 
/*     */ public class Dot_Detector_3D
/*     */   implements PlugInFilter, PreviewInterface
/*     */ {
/*     */   protected ImagePlus original_imp;
/*     */   private ImageStack stack;
/*     */   private String title;
/*     */   private int frames_number;
/*     */   private int slices_number;
/*     */   private FeaturePointDetector detector;
/*     */   private GenericDialog gd;
/*     */   private PreviewCanvas preview_canvas;
/*  35 */   private boolean frames_processed = false;
/*     */   private MyFrame[] frames;
/*     */   private int preview_slice_calculated;
/*     */ 
/*     */   public int setup(String arg, ImagePlus imp)
/*     */   {
/*  41 */     if (IJ.versionLessThan("1.38u")) {
/*  42 */       return 4096;
/*     */     }
/*     */ 
/*  49 */     this.original_imp = imp;
/*     */ 
/*  51 */     if (imp == null) {
/*  52 */       IJ.error("You must load an Image Sequence or Movie first");
/*  53 */       return 4096;
/*     */     }
/*  55 */     if (this.original_imp.getStackSize() == 1) {
/*  56 */       return 223;
/*     */     }
/*  58 */     return 32991;
/*     */   }
/*     */ 
/*     */   public void run(ImageProcessor ip) {
/*  62 */     initializeMembers();
/*  63 */     this.preview_canvas = this.detector.generatePreviewCanvas(this.original_imp);
/*  64 */     if (!getUserDefinedParams()) return;
/*  65 */     if (!processFrames()) return;
/*  66 */     transferParticlesToResultsTable();
/*     */   }
/*     */ 
/*     */   private void initializeMembers()
/*     */   {
/*  73 */     this.stack = this.original_imp.getStack();
/*  74 */     this.title = this.original_imp.getTitle();
/*     */ 
/*  77 */     StackStatistics stack_stats = new StackStatistics(this.original_imp);
/*  78 */     float global_max = (float)stack_stats.max;
/*  79 */     float global_min = (float)stack_stats.min;
/*  80 */     this.frames_number = this.original_imp.getNFrames();
/*  81 */     this.slices_number = this.original_imp.getNSlices();
/*     */ 
/*  83 */     this.detector = new FeaturePointDetector(global_max, global_min);
/*     */   }
/*     */ 
/*     */   boolean getUserDefinedParams()
/*     */   {
/*  88 */     this.gd = new GenericDialog("Particle Tracker...", IJ.getInstance());
/*     */ 
/*  90 */     boolean convert = false;
/*     */ 
/*  92 */     this.detector.addUserDefinedParametersDialog(this.gd);
/*     */ 
/*  94 */     this.gd.addPanel(this.detector.makePreviewPanel(this, this.original_imp), 10, new Insets(5, 0, 0, 0));
/*     */ 
/*  97 */     if ((this.original_imp.getType() != 0) && 
/*  98 */       (this.original_imp.getType() != 1) && 
/*  99 */       (this.original_imp.getType() != 2)) {
/* 100 */       this.gd.addCheckbox("Convert to Gray8 (recommended)", true);
/* 101 */       convert = true;
/*     */     }
/*     */ 
/* 104 */     this.gd.showDialog();
/*     */ 
/* 108 */     Boolean changed = this.detector.getUserDefinedParameters(this.gd);
/*     */ 
/* 111 */     if ((changed.booleanValue()) && 
/* 112 */       (this.frames_processed)) {
/* 113 */       this.frames = null;
/* 114 */       this.frames_processed = false;
/*     */     }
/*     */ 
/* 119 */     if (convert) convert = this.gd.getNextBoolean();
/*     */ 
/* 122 */     return true;
/*     */   }
/*     */ 
/*     */   public void preview(ActionEvent e)
/*     */   {
/* 127 */     this.original_imp.getWindow().setLocation((int)this.gd.getLocationOnScreen().getX() + this.gd.getWidth(), (int)this.gd.getLocationOnScreen().getY());
/*     */ 
/* 129 */     preview();
/* 130 */     this.preview_canvas.repaint();
/*     */   }
/*     */ 
/*     */   public synchronized void preview()
/*     */   {
/* 135 */     if (this.original_imp == null) return;
/*     */ 
/* 138 */     this.stack = this.original_imp.getStack();
/*     */ 
/* 141 */     this.preview_slice_calculated = this.original_imp.getCurrentSlice();
/*     */ 
/* 143 */     this.detector.getUserDefinedPreviewParams(this.gd);
/*     */ 
/* 145 */     int first_slice = (getFrameNumberFromSlice(this.preview_slice_calculated) - 1) * this.slices_number + 1;
/*     */ 
/* 147 */     MyFrame preview_frame = new MyFrame(FeaturePointDetector.GetSubStackCopyInFloat(this.stack, first_slice, first_slice + this.slices_number - 1), getFrameNumberFromSlice(this.preview_slice_calculated) - 1);
/*     */ 
/* 150 */     this.detector.featurePointDetection(preview_frame);
/* 151 */     this.detector.setPreviewLabel("#Particles: " + preview_frame.getParticles().size());
/*     */ 
/* 153 */     this.preview_canvas.setPreviewFrame(preview_frame);
/* 154 */     this.preview_canvas.setPreviewParticleRadius(this.detector.getRadius());
/* 155 */     this.preview_canvas.setPreviewSliceCalculated(this.preview_slice_calculated);
/*     */   }
/*     */ 
/*     */   public int getFrameNumberFromSlice(int sliceIndex)
/*     */   {
/* 163 */     return (sliceIndex - 1) / this.slices_number + 1;
/*     */   }
/*     */ 
/*     */   public void saveDetected(ActionEvent e)
/*     */   {
/* 169 */     this.detector.getUserDefinedPreviewParams(this.gd);
/*     */ 
/* 172 */     if (processFrames()) {
/* 173 */       this.detector.saveDetected(this.frames);
/*     */     }
/* 175 */     this.preview_canvas.repaint();
/*     */   }
/*     */ 
/*     */   public boolean processFrames()
/*     */   {
/* 181 */     if (this.frames_processed) return true;
/*     */ 
/* 184 */     this.frames = new MyFrame[this.frames_number];
/* 185 */     MyFrame current_frame = null;
/*     */ 
/* 187 */     int frame_i = 0; for (int file_index = 0; frame_i < this.frames_number; file_index++)
/*     */     {
/* 191 */       current_frame = new MyFrame(FeaturePointDetector.GetSubStackInFloat(this.stack, frame_i * this.slices_number + 1, (frame_i + 1) * this.slices_number), frame_i);
/*     */ 
/* 194 */       IJ.showStatus("Detecting Particles in Frame " + (frame_i + 1) + "/" + this.frames_number);
/* 195 */       this.detector.featurePointDetection(current_frame);
/* 196 */       this.frames[current_frame.frame_number] = current_frame;
/* 197 */       IJ.freeMemory();
/*     */ 
/* 187 */       frame_i++;
/*     */     }
/*     */ 
/* 199 */     this.frames_processed = true;
/* 200 */     return true;
/*     */   }
/*     */   public void transferParticlesToResultsTable() {
/* 203 */     ResultsTable rt = null;
/*     */     try {
/* 205 */       rt = ResultsTable.getResultsTable(); } catch (Exception localException) {
/*     */     }
/* 207 */     if ((rt.getCounter() != 0) || (rt.getLastColumn() != -1)) {
/* 208 */       if (IJ.showMessageWithCancel("Results Table", "Reset Results Table?"))
/* 209 */         rt.reset();
/*     */       else {
/* 211 */         return;
/*     */       }
/*     */     }
/* 214 */     int rownum = 0;
/* 215 */     for (int i = 0; i < this.frames.length; i++) {
/* 216 */       Vector particles = this.frames[i].getParticles();
/* 217 */       for (Particle p : particles) {
/* 218 */         rt.incrementCounter();
/* 219 */         rownum = rt.getCounter() - 1;
/* 220 */         rt.setValue("frame", rownum, p.getFrame());
/* 221 */         rt.setValue("x", rownum, p.x);
/* 222 */         rt.setValue("y", rownum, p.y);
/* 223 */         rt.setValue("z", rownum, p.z);
/* 224 */         rt.setValue("m0", rownum, p.m0);
/* 225 */         rt.setValue("m1", rownum, p.m1);
/* 226 */         rt.setValue("m2", rownum, p.m2);
/* 227 */         rt.setValue("m3", rownum, p.m3);
/* 228 */         rt.setValue("m4", rownum, p.m4);
/* 229 */         rt.setValue("NPscore", rownum, p.score);
/*     */       }
/*     */     }
/* 232 */     rt.show("Results");
/*     */   }
/*     */ }

/* Location:           /Users/miura/Dropbox/people/mayumi/Dot_Detector3D.jar
 * Qualified Name:     emblcmci.pt3d.Dot_Detector_3D
 * JD-Core Version:    0.6.2
 */