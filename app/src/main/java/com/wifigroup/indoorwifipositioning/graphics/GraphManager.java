package com.wifigroup.indoorwifipositioning.graphics;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class GraphManager {

    private final GraphView graphMap;
    private PointsGraphSeries<DataPoint> seriesAPs;
    private PointsGraphSeries<DataPoint> seriesPosLog;
    private PointsGraphSeries<DataPoint> seriesPosPoly;

    private double roomMaxX = 9.0;
    private double roomMaxY = 10.0;

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

    public void drawRoomAndAPs(DataPoint[] apPoints, double maxX, double maxY) {
        this.roomMaxX = maxX;
        this.roomMaxY = maxY;

        seriesAPs.resetData(apPoints);

        graphMap.getViewport().setXAxisBoundsManual(true);
        graphMap.getViewport().setYAxisBoundsManual(true);
        graphMap.getViewport().setMinX(0 - 1);
        graphMap.getViewport().setMaxX(roomMaxX + 1);
        graphMap.getViewport().setMinY(0 - 1);
        graphMap.getViewport().setMaxY(roomMaxY + 1);
    }

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
