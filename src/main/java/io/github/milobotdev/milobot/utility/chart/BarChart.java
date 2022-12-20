package io.github.milobotdev.milobot.utility.chart;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static io.github.milobotdev.milobot.utility.ImgurFunctions.uploadImageToImgur;

/**
 * A class that represents a bar chart with multiple bars.
 * The bar chart can be saved as a PNG image file and uploaded to Imgur.
 */
public class BarChart {

    private final ArrayList<ChartSection> sections;
    private final String title;
    private final String xAxisLabel;
    private final String yAxisLabel;
    private final String filename;

    /**
     * Creates a new bar chart with the specified title, x-axis label, y-axis label, and filename.
     *
     * @param title      the title of the bar chart
     * @param xAxisLabel the label for the x-axis
     * @param yAxisLabel the label for the y-axis
     * @param filename   the name of the image file that the bar chart will be saved as
     */
    public BarChart(String title, String xAxisLabel, String yAxisLabel, String filename) {
        this.sections = new ArrayList<>();
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.filename = filename;
    }

    /**
     * Adds a new bar to the bar chart.
     *
     * @param key   the key for the bar
     * @param value the value for the bar
     * @param color the color of the bar
     */
    public void addBar(String key, int value, Color color) {
        sections.add(new ChartSection(key, value, color));
    }

    /**
     * Creates the bar chart and saves it as a PNG image file.
     * The image file is then uploaded to Imgur and the URL for the uploaded image is returned.
     *
     * @return the URL of the uploaded image on Imgur
     * @throws IOException           if there is an error reading or writing to the image file
     * @throws IllegalStateException if the bar chart has no bars
     */
    public InputStream createBarChart() throws IllegalStateException {
        if (sections.size() == 0) {
            throw new IllegalStateException("BarChart must have at least one bar");
        }

        // Create a bar chart dataset
        DefaultCategoryDataset dataset = createDataset();

        // Create a bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Set the font for the chart title
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));

        // Get the plot object for the bar chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        Color blackBackgroundColor = Color.decode("#2f2f2f");
        Color blackShadowCoLor = Color.decode("#696969");

        // Set the font for the chart title
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        // Set the title text white
        chart.getTitle().setPaint(Color.WHITE);
        // Set the color of the background
        chart.setBackgroundPaint(blackBackgroundColor);
        // Set the background color of the legend
        LegendTitle legend = chart.getLegend();
        legend.setBackgroundPaint(blackBackgroundColor);
        // Set the padding of the legend
        legend.setItemLabelPadding(new RectangleInsets(2, 10, 2, 10));
        // Set the outline of the legend
        legend.setFrame(new BlockBorder(blackBackgroundColor));
        // Set the text color of the legend
        legend.setItemPaint(Color.WHITE);
        // Set the font of the legend
        legend.setItemFont(new Font("SansSerif", Font.PLAIN, 16));

        // Set the opacity
        plot.setForegroundAlpha(0.8f);
        // Set the background color
        plot.setBackgroundPaint(blackBackgroundColor);
        // Change the color of the outline
        plot.setOutlinePaint(blackBackgroundColor);

        // Set the color of the text on the x-axis
        plot.getDomainAxis().setLabelPaint(Color.WHITE);
        // Set the font of the text on the x-axis
        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        // Set the color of the text on the y-axis
        plot.getRangeAxis().setLabelPaint(Color.WHITE);
        // Set the font of the text on the y-axis
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));


        // Set the colors for the bars
        sections.forEach(section -> plot.getRenderer().setSeriesPaint(sections.indexOf(section), section.color()));

        // Save the bar chart as an image file
        int width = 640;
        int height = 480;

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ChartUtilities.writeChartAsPNG(os, chart, width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ByteArrayInputStream(os.toByteArray());
    }

    /**
     * Creates a {@link DefaultCategoryDataset} object with data from the sections of the bar chart.
     *
     * @return a {@link DefaultCategoryDataset} object with data from the sections of the bar chart
     */
    private @NotNull DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        sections.forEach(section -> dataset.addValue(section.value(), section.key(), ""));
        return dataset;
    }

}
