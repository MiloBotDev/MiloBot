package io.github.milobotdev.milobot.utility.chart;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ChartFunctions {

    public static void styleChart(@NotNull JFreeChart chart) {
        // Set the font for the chart title
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));

        Plot plot = chart.getPlot();
        if(plot instanceof  CategoryPlot) {
            // Set the font for the axis tick labels
            ((CategoryPlot) plot).getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            ((CategoryPlot) plot).getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
            // Set the color of the axis tick labels
            ((CategoryPlot) plot).getDomainAxis().setTickLabelPaint(Color.WHITE);
            ((CategoryPlot) plot).getRangeAxis().setTickLabelPaint(Color.WHITE);
            // Make the text underneath the x-axis vertical
            ((CategoryPlot) plot).getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            // Set the color of the text on the x-axis
            ((CategoryPlot) plot).getDomainAxis().setLabelPaint(Color.WHITE);
            // Set the font of the text on the x-axis
            ((CategoryPlot) plot).getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
            // Set the color of the text on the y-axis
            ((CategoryPlot) plot).getRangeAxis().setLabelPaint(Color.WHITE);
            // Set the font of the text on the y-axis
            ((CategoryPlot) plot).getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 16));
        }


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
    }

    public static byte @NotNull [] chartToByteArray(JFreeChart chart, int width, int height) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ChartUtilities.writeChartAsPNG(os, chart, width, height);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }
}
