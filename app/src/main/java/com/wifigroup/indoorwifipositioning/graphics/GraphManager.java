package com.wifigroup.indoorwifipositioning.graphics;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

/**
 * Manages the 2D graphical representation of the indoor positioning system.
 * <p>
 * This class wraps a {@link GraphView} component to visually plot the physical room boundaries,
 * the fixed locations of the Access Points (rendered as blue rectangles), and the real-time
 * estimated positions of the device calculated by the mathematical models (rendered as red and green points).
 * </p>
 *
 * @author WiFiGroup
 * @version 1.1.0-alpha
 */
public class GraphManager {

    private final GraphView graphMap;
    private PointsGraphSeries<DataPoint> seriesAPs;
    private PointsGraphSeries<DataPoint> seriesPosLog;
    private PointsGraphSeries<DataPoint> seriesPosPoly;

    private double roomMaxX = 9.0;
    private double roomMaxY = 10.0;

    /**
     * Initializes a new GraphManager and sets up the internal graph series.
     *
     * @param graphMap the {@link GraphView} UI component used to render the plot
     */
    public GraphManager(GraphView graphMap) {
        this.graphMap = graphMap;
        setupGraph();
    }

    private void setupGraph() {
        seriesAPs = new PointsGraphSeries<>();
        seriesAPs.setColor(Color.BLUE);
        seriesAPs.setShape(PointsGraphSeries.Shape.RECTANGLE);
        seriesAPs.setSize(15f);

        seriesPosLog = new PointsGraphSeries<>();
        seriesPosLog.setColor(Color.RED);
        seriesPosLog.setShape(PointsGraphSeries.Shape.POINT);
        seriesPosLog.setSize(20f);

        seriesPosPoly = new PointsGraphSeries<>();
        seriesPosPoly.setColor(Color.GREEN);
        seriesPosPoly.setShape(PointsGraphSeries.Shape.POINT);
        seriesPosPoly.setSize(20f);

        graphMap.addSeries(seriesAPs);
        graphMap.addSeries(seriesPosLog);
        graphMap.addSeries(seriesPosPoly);

        graphMap.getViewport().setScrollable(false);
        graphMap.getViewport().setScalable(false);
    }

    /**
     * Configures the physical dimensions of the room and plots the Access Points.
     * <p>
     * This method resets the view boundaries to match the provided room dimensions,
     * adding a 1-meter padding on all sides to ensure points on the edge remain visible.
     * </p>
     *
     * @param apPoints an array of {@link DataPoint} representing the [X, Y] coordinates of the Access Points
     * @param maxX the physical maximum width of the room in meters
     * @param maxY the physical maximum length of the room in meters
     */
    public void drawRoomAndAPs(DataPoint[] apPoints, double maxX, double maxY) {
        this.roomMaxX = maxX;
        this.roomMaxY = maxY;

        seriesAPs.resetData(apPoints);

        graphMap.getViewport().setXAxisBoundsManual(true);
        graphMap.getViewport().setYAxisBoundsManual(true);
        graphMap.getViewport().setMinX(- 1);
        graphMap.getViewport().setMaxX(roomMaxX + 1);
        graphMap.getViewport().setMinY(- 1);
        graphMap.getViewport().setMaxY(roomMaxY + 1);
    }

    /**
     * Updates the plotted real-time position of the device for both mathematical models.
     * <p>
     * The estimated coordinates are clamped to the physical boundaries of the room
     * (between 0 and the maximum dimensions) to prevent the points from being drawn
     * outside the graph area in case of extreme mathematical outliers.
     * </p>
     *
     * @param posLog a {@code double} array containing the [X, Y] coordinates estimated
     * by the Logarithmic model, or {@code null} to skip updating it
     * @param posPoly a {@code double} array containing the [X, Y] coordinates estimated
     * by the Polynomial model, or {@code null} to skip updating it
     */
    public void updatePositions(double[] posLog, double[] posPoly) {
        if (posLog != null) {
            double xLog = Math.max(0, Math.min(posLog[0], roomMaxX));
            double yLog = Math.max(0, Math.min(posLog[1], roomMaxY));
            seriesPosLog.resetData(new DataPoint[] { new DataPoint(xLog, yLog) });
        }

        if (posPoly != null) {
            double xPoly = Math.max(0, Math.min(posPoly[0], roomMaxX));
            double yPoly = Math.max(0, Math.min(posPoly[1], roomMaxY));
            seriesPosPoly.resetData(new DataPoint[] { new DataPoint(xPoly, yPoly) });
        }
    }
}