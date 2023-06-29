package io.github.milobotdev.milobot.utility.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class LineChart {

    private final String title;
    private final String xAxisLabel;
    private final String yAxisLabel;
    private final DefaultCategoryDataset dataset;

    public LineChart(String title, String xAxisLabel, String yAxisLabel) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.dataset = new DefaultCategoryDataset();
    }

    public byte[] createLineChart() {
        JFreeChart chart = ChartFactory.createLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        ChartFunctions.styleChart(chart);
        return ChartFunctions.chartToByteArray(chart, 640, 480);
    }

    public void addPlotPoint(int value, String yAxisLabel, String xAxisLabel) {
        dataset.addValue(value, yAxisLabel, xAxisLabel);
    }

}
