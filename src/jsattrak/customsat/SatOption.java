package jsattrak.customsat;

import java.awt.Color;

import name.gano.worldwind.modelloader.WWModel3D_new;

public class SatOption {

	// plot options
	private boolean plot2d = true;
	
	private Color trueSatColor = Color.RED;
	private Color EventSatColor = Color.LIGHT_GRAY;
	
	
	private Color satColor = trueSatColor; // randomize in future
	private Color groundTrackColor = trueSatColor;
	
	private boolean plot2DFootPrint = true;
	private boolean fillFootPrint = true;
	private int numPtsFootPrint = 101; // number of points in footprint

	// ground track options -- grounds tracks draw to asending nodes,
	// re-calculated at acending nodes
	private boolean showGroundTrack = true;
	private int grnTrkPointsPerPeriod = 121; // equally space in time >=2
	private double groundTrackLeadPeriodMultiplier = 2.0; // how far forward to
															// draw ground track
															// - in terms of
															// periods
	private double groundTrackLagPeriodMultiplier = 1.0; // how far behind to
															// draw ground track
															// - in terms of
															// periods
	private boolean groundTrackIni = false; // if ground track has been initialized

	private boolean showName2D = true; // show name in 2D plots

	// 3D Options
	private boolean show3DOrbitTrace = true;
	private boolean show3DFootprint = true;
	private boolean show3DName = true; // not implemented to change yet
	private boolean show3D = true; // no implemented to change yet, or to modify
									// showing of sat
	private boolean showGroundTrack3d = false;
	private boolean show3DOrbitTraceECI = true; // show orbit in ECI mode
												// otherwise , ECEF

	private boolean showConsoleOnPropogate = true;

	// 3D model parameters
	private boolean use3dModel = false; // use custom 3D model (or default
										// sphere)
	
	private String threeDModelPath = "globalstar/Globalstar.3ds"; // path to the
	// custom
	// model,
	// default=
	// globalstar/Globalstar.3ds
	// ?

	private transient WWModel3D_new threeDModel; // DO NOT STORE when saving --
	// need to reload this --
	// TOO MUCH DATA!
	private double threeDModelSizeFactor = 300000;

	public SatOption(){
		
	}

	
	//get & set
	public boolean isPlot2d() {
		return plot2d;
	}

	public void setPlot2d(boolean plot2d) {
		this.plot2d = plot2d;
	}

	public Color getSatColor() {
		return satColor;
	}

	public void setSatColor(Color satColor) {
		this.satColor = satColor;
	}

	public Color getGroundTrackColor() {
		return groundTrackColor;
	}

	public void setGroundTrackColor(Color groundTrackColor) {
		this.groundTrackColor = groundTrackColor;
	}

	public boolean isPlot2DFootPrint() {
		return plot2DFootPrint;
	}

	public void setPlot2DFootPrint(boolean plot2dFootPrint) {
		plot2DFootPrint = plot2dFootPrint;
	}

	public boolean isFillFootPrint() {
		return fillFootPrint;
	}

	public void setFillFootPrint(boolean fillFootPrint) {
		this.fillFootPrint = fillFootPrint;
	}

	public int getNumPtsFootPrint() {
		return numPtsFootPrint;
	}

	public void setNumPtsFootPrint(int numPtsFootPrint) {
		this.numPtsFootPrint = numPtsFootPrint;
	}

	public boolean isShowGroundTrack() {
		return showGroundTrack;
	}

	public void setShowGroundTrack(boolean showGroundTrack) {
		this.showGroundTrack = showGroundTrack;
	}

	public int getGrnTrkPointsPerPeriod() {
		return grnTrkPointsPerPeriod;
	}

	public void setGrnTrkPointsPerPeriod(int grnTrkPointsPerPeriod) {
		this.grnTrkPointsPerPeriod = grnTrkPointsPerPeriod;
	}

	public double getGroundTrackLeadPeriodMultiplier() {
		return groundTrackLeadPeriodMultiplier;
	}

	public void setGroundTrackLeadPeriodMultiplier(
			double groundTrackLeadPeriodMultiplier) {
		this.groundTrackLeadPeriodMultiplier = groundTrackLeadPeriodMultiplier;
	}

	public double getGroundTrackLagPeriodMultiplier() {
		return groundTrackLagPeriodMultiplier;
	}

	public void setGroundTrackLagPeriodMultiplier(
			double groundTrackLagPeriodMultiplier) {
		this.groundTrackLagPeriodMultiplier = groundTrackLagPeriodMultiplier;
	}

	public boolean isGroundTrackIni() {
		return groundTrackIni;
	}

	public void setGroundTrackIni(boolean groundTrackIni) {
		this.groundTrackIni = groundTrackIni;
	}

	public boolean isShowName2D() {
		return showName2D;
	}

	public void setShowName2D(boolean showName2D) {
		this.showName2D = showName2D;
	}

	public boolean isShow3DOrbitTrace() {
		return show3DOrbitTrace;
	}

	public void setShow3DOrbitTrace(boolean show3dOrbitTrace) {
		show3DOrbitTrace = show3dOrbitTrace;
	}

	public boolean isShow3DFootprint() {
		return show3DFootprint;
	}

	public void setShow3DFootprint(boolean show3dFootprint) {
		show3DFootprint = show3dFootprint;
	}

	public boolean isShow3DName() {
		return show3DName;
	}

	public void setShow3DName(boolean show3dName) {
		show3DName = show3dName;
	}

	public boolean isShow3D() {
		return show3D;
	}

	public void setShow3D(boolean show3d) {
		show3D = show3d;
	}

	public boolean isShowGroundTrack3d() {
		return showGroundTrack3d;
	}

	public void setShowGroundTrack3d(boolean showGroundTrack3d) {
		this.showGroundTrack3d = showGroundTrack3d;
	}

	public boolean isShow3DOrbitTraceECI() {
		return show3DOrbitTraceECI;
	}

	public void setShow3DOrbitTraceECI(boolean show3dOrbitTraceECI) {
		show3DOrbitTraceECI = show3dOrbitTraceECI;
	}

	public boolean isShowConsoleOnPropogate() {
		return showConsoleOnPropogate;
	}

	public void setShowConsoleOnPropogate(boolean showConsoleOnPropogate) {
		this.showConsoleOnPropogate = showConsoleOnPropogate;
	}

	public boolean isUse3dModel() {
		return use3dModel;
	}

	public void setUse3dModel(boolean use3dModel) {
		this.use3dModel = use3dModel;
	}

	public WWModel3D_new getThreeDModel() {
		return threeDModel;
	}

	public void setThreeDModel(WWModel3D_new threeDModel) {
		this.threeDModel = threeDModel;
	}

	public double getThreeDModelSizeFactor() {
		return threeDModelSizeFactor;
	}

	public void setThreeDModelSizeFactor(double threeDModelSizeFactor) {
		this.threeDModelSizeFactor = threeDModelSizeFactor;
	}
	
	public void setGroundTrackIni2False() {
		// forces repaint of ground track next update
		groundTrackIni = false;
	}


	public String getThreeDModelPath() {
		return threeDModelPath;
	}


	public void setThreeDModelPath(String threeDModelPath) {
		this.threeDModelPath = threeDModelPath;
	}


	public Color getTrueSatColor() {
		return trueSatColor;
	}


	public Color getEventSatColor() {
		return EventSatColor;
	}

}
