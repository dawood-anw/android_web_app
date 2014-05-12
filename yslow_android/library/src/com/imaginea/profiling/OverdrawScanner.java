/*******************************************************************************
 * Copyright 2014 Asha
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Owner : Asha - initial API and implementation
 * Project Name : Lib_PerformanceMonitoring
 * FileName :OverdrawScanner
 ******************************************************************************/
package com.imaginea.profiling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import com.imaginea.instrumentation.Utils;
import com.imaginea.instrumentation.Utils.DeviceDensity;

/**
 * The Class OverdrawScanner. OverdrawScanner: is used to scan the number of
 * Overdraws per activity.
 */
public final class OverdrawScanner {

    /** The Constant LANDSCAPE. */
    private final static int LANDSCAPE = 1;

    private final static int IMAGE_BACKGROUND_RGB_COLOR = 150;

    private static int SUB_IMAGE_WIDTH = 0;

    private static int SUB_IMAGE_HEIGHT = 0;

    private static int OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT = 0;

    /**
     * Color to RGB. This method is used for Image Processing and gets the RGB
     * value of each Image Pixel
     * 
     * @param alpha
     *            the alpha
     * @param red
     *            the red
     * @param green
     *            the green
     * @param blue
     *            the blue
     * @return the int
     */
    private static int colorToRGB(final int alpha, final int red,
            final int green, final int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;

    }

    /**
     * Gets the image rgb. This method is used to get the Image background color
     * 
     * @param image
     *            the image
     * @return the image rgb
     */
    private static int getImageRGB(final BufferedImage image) {
        int rgbdata[][];
        int background_color = 0;
        rgbdata = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                rgbdata[i][j] = image.getRGB(i, j);
            }
        }
        int howmanydone = 0;
        int prevcolor, newcolor;
        prevcolor = rgbdata[0][0];

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                newcolor = rgbdata[i][j];
                new Color(newcolor);

                if (howmanydone == 0 && newcolor != prevcolor) {
                    background_color = prevcolor;
                    prevcolor = newcolor;
                    howmanydone = 1;
                }
                if (newcolor != prevcolor && howmanydone == 1) {
                }
            }
        }
        return background_color;
    }

    /**
     * Sets the sub image box.
     * 
     * @param isBlackPatchEnable
     *            ( For device like nexus 5 Extra black patch for soft keys are
     *            available). the new sub image box
     */
    private static void setSubImageBox(final boolean isBlackPatchEnable) {
        if (!isBlackPatchEnable) {
            SUB_IMAGE_WIDTH = 80;
            SUB_IMAGE_HEIGHT = 40;
            OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT = 105;
        } else {
            final DeviceDensity deviceDensity = Utils.getDeviceDensity();
            if (deviceDensity == Utils.DeviceDensity.MDPI) {
                OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT = 160;
            } else if (deviceDensity == Utils.DeviceDensity.HDPI) {
                OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT = 230;
            } else {
                OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT = 105;
            }
            SUB_IMAGE_WIDTH = 160;
            SUB_IMAGE_HEIGHT = 80;

        }
    }

    /**
     * Gets the no of overdraws. This method crop the screen shot captured
     * Activity Image and store left Bottom corner(only overdraw part).
     * 
     * @param dir
     *            the dir
     * @param mode
     *            the mode
     * @return the no of overdraws
     */
    public static String getNoOfOverdraws(final String dir, final int mode,
            final boolean isBlackPatchEnable) {
        BufferedImage image = null;
        FileInputStream in = null;
        String noOfOverdraw = null;
        setSubImageBox(isBlackPatchEnable);
        try {
            in = new FileInputStream(dir + "/screen.png");
            image = ImageIO.read(in);
            in.close();
            if (mode == LANDSCAPE) {
                image = rotateImage(image);
            }
            if (image != null) {
                final int height = image.getHeight();
                // crop only overdraw part(left Bottom corner).
                image = image.getSubimage(0, height
                        - OVERDRAW_BOTTOM_LEFT_CORNER_HEIGHT, SUB_IMAGE_WIDTH,
                        SUB_IMAGE_HEIGHT);
                final String outputImagePath = dir + "/screenout.png";
                try {
                    final FileOutputStream out = new FileOutputStream(
                            outputImagePath);
                    // perform Image Processing based on Image Background color
                    final int color = getImageRGB(image);
                    final Color imageColor = new Color(color);
                    if (imageColor.getRed() > IMAGE_BACKGROUND_RGB_COLOR
                            && imageColor.getGreen() > IMAGE_BACKGROUND_RGB_COLOR
                            && imageColor.getBlue() > IMAGE_BACKGROUND_RGB_COLOR) {
                        image = rgb(image, 2);
                    } else {
                        image = rgb(image, 1);
                    }

                    ImageIO.write(image, "PNG", out);
                    out.close();
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                noOfOverdraw = performOCR(outputImagePath);
            }
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return noOfOverdraw;

    }

    /**
     * Perform OCR. This method performs the OCR on cropped Captured screen
     * shot(left bottom corner of Image which displays the overdraws)
     * 
     * @param outputImagePath
     *            the output image path
     * @return the string
     */
    public static String performOCR(final String outputImagePath) {
        final File file = new File(outputImagePath);
        String result = null;
        try {
            final Tesseract tesseract = Tesseract.getInstance();
            final Utils.OSType os = Utils.getOperatingSystemType();
            if (os == Utils.OSType.Windows) {
                tesseract.setDatapath(Utils.getFullPath("Tesseract"));
            } else {
                tesseract.setDatapath("/usr/local/share/");
            }
            result = tesseract.doOCR(file);
            file.delete();// delete the file once OCR done
        } catch (final TesseractException e) {
        }

        return result;

    }

    /**
     * rgb : This method is used for Image Processing and gets the RGB value of
     * each Image Pixel
     * 
     * @param original
     *            the original
     * @param color
     *            the color
     * @return the buffered image
     */
    private static BufferedImage rgb(final BufferedImage original,
            final int color) {

        int alpha, red, green, blue;
        int newPixel;

        final int[] pixel = new int[3];

        final BufferedImage rgb = new BufferedImage(original.getWidth(),
                original.getHeight(), original.getType());

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels by R, G, B
                alpha = new Color(original.getRGB(i, j)).getAlpha();
                red = new Color(original.getRGB(i, j)).getRed();
                green = new Color(original.getRGB(i, j)).getGreen();
                blue = new Color(original.getRGB(i, j)).getBlue();

                pixel[0] = red;
                pixel[1] = green;
                pixel[2] = blue;

                final int newval = pixel[color];

                // Return back to original format
                newPixel = colorToRGB(alpha, newval, newval, newval);

                // Write pixels into image
                rgb.setRGB(i, j, newPixel);

            }

        }

        return rgb;

    }

    /**
     * Rotate image 90 degree to the left.
     * 
     * @param image
     *            the image
     * @return the buffered image
     */
    private static BufferedImage rotateImage(final BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final BufferedImage returnImage = new BufferedImage(height, width,
                image.getType());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                returnImage.setRGB(y, width - x - 1, image.getRGB(x, y));
            }
        }

        return returnImage;
    }

}
