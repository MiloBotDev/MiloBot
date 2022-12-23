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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static io.github.milobotdev.milobot.utility.ImgurFunctions.uploadImageToImgur;

/**
 * A class that represents a pie chart with multiple sections.
 * The pie chart can be saved as a PNG image file and uploaded to Imgur.
 */
public class PieChart {

    private final ArrayList<ChartSection> sections;
    private final String title;
    private final String filename;

    /**
     * Creates a new pie chart with the specified title and filename.
     *
     * @param title    the title of the pie chart
     * @param filename the name of the image file that the pie chart will be saved as
     */
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

    /**
     * Creates the pie chart and saves it as a PNG image file.
     * The image file is then uploaded to Imgur and the URL for the uploaded image is returned.
     *
     * @return the URL of the uploaded image on Imgur
     * @throws IOException           if there is an error reading or writing to the image file
     * @throws IllegalStateException if the pie chart has no sections
     * @see io.github.milobotdev.milobot.utility.ImgurFunctions
     */
    public @NotNull InputStream createCircleDiagram() throws IOException, IllegalStateException {
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

        // Get the plot object for the pie chart
        PiePlot plot = (PiePlot) chart.getPlot();

        // Set the label font
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        // Make the label color white
        plot.setLabelBackgroundPaint(Color.WHITE);
        // Make the text on the label white
        plot.setLabelPaint(Color.BLACK);
        // Set the label generator
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
        // Set the shadow color of the label
        plot.setLabelShadowPaint(blackShadowCoLor);
        // Set the opacity
        plot.setForegroundAlpha(0.8f);
        // Set the background color
        plot.setBackgroundPaint(blackBackgroundColor);
        // Change the color of the outline
        plot.setOutlinePaint(blackBackgroundColor);

        // Set the colors for the sections
        sections.forEach(section -> plot.setSectionPaint(section.key(), section.color()));

        // Save the pie chart as an image file
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

    private @NotNull PieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        sections.forEach(section -> dataset.setValue(section.key(), section.value()));
        return dataset;
    }

}
