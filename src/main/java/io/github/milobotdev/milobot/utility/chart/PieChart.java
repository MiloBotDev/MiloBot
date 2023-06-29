package io.github.milobotdev.milobot.utility.chart;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A class that represents a pie chart with multiple sections.
 * The pie chart can be saved as a PNG image file and uploaded to Imgur.
 */
public class PieChart {

    private final ArrayList<ChartSection> sections;
    private final String title;
    private final String filename;

    public PieChart(String title, String filename) {
        this.sections = new ArrayList<>();
        this.title = title;
        this.filename = filename;
    }

    /**
     * Adds a new section to the pie chart.
     *
     * @param key   the key for the section
     * @param value the value for the section
     * @param color the color of the section
     */
    public void addSection(String key, int value, Color color) {
        this.sections.add(new ChartSection(key, value, color));
    }

    public byte @NotNull [] createCircleDiagram() throws IOException, IllegalStateException {
        if (this.sections.size() == 0) {
            throw new IllegalStateException("PieChart must have at least one section");
        }
        // Create a pie chart dataset
        PieDataset dataset = createDataset();

        // Create a pie chart
        JFreeChart chart = ChartFactory.createPieChart(
                title,  // chart title
                dataset,                      // data
                true,                         // include legend
                true,
                false);

        ChartFunctions.styleChart(chart);

        // Get the plot object for the pie chart
        PiePlot plot = (PiePlot) chart.getPlot();

        // Set the colors for the sections
        sections.forEach(section -> plot.setSectionPaint(section.key(), section.color()));

        return ChartFunctions.chartToByteArray(chart, 640, 480);
    }

    private @NotNull PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        sections.forEach(section -> dataset.setValue(section.key(), section.value()));
        return dataset;
    }

}
