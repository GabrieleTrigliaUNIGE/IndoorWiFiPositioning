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
        seriesAPs.setSize(20f);

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
        graphMap.getViewport().setMinX(- 2);
        graphMap.getViewport().setMaxX(roomMaxX + 3);
        graphMap.getViewport().setMinY(- 2);
        graphMap.getViewport().setMaxY(roomMaxY + 2);

        graphMap.getGridLabelRenderer().setNumHorizontalLabels(15);
        graphMap.getGridLabelRenderer().setNumVerticalLabels(15);
    }

    public void updatePositions(double[] posLog, double[] posPoly) {
        if (posLog != null) {
            double xLog = posLog[0];
            double yLog = posLog[1];
            seriesPosLog.resetData(new DataPoint[] { new DataPoint(xLog, yLog) });
        } else {
            // Passo array vuoto
            seriesPosLog.resetData(new DataPoint[] {});
        }

        if (posPoly != null) {
            double xPoly = posPoly[0];
            double yPoly = posPoly[1];
            seriesPosPoly.resetData(new DataPoint[] { new DataPoint(xPoly, yPoly) });
        } else {
            // Passo array vuoto
            seriesPosPoly.resetData(new DataPoint[] {});
        }
    }
}
