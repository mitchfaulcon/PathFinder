package imageParser;

import controller.PathFinderController;
import controller.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageParser {

    private int[][] map;

    /** Constructor that converts an image to an array of int values.
     * The image is first converted to grayscale, then the darkest pixels are set as
     * walls, the lightest are set as standard tiles, and the rest are weighted walls
     * depending on where they lie between the lightest and darkest pixels.
     * @param imageFile The image file to convert to a tilemap
     * @param minRows The minimum possible rows in the map
     * @param minCols The minimum possible columns in the map
     * @param maxRows The maximum possible rows in the map
     * @param maxCols The maximum possible columns in the map
     */
    public ImageParser(File imageFile, int minRows, int minCols, int maxRows, int maxCols) {

        BufferedImage image = null;

        try {
            image = ImageIO.read(imageFile);

            int imgHeight = image.getHeight();
            int imgWidth = image.getWidth();

            //Resize image if it is too small/too large
            boolean resize = false;
            if (imgHeight < minRows) {
                imgHeight = minRows;
                resize = true;
            } else if (imgHeight > maxRows) {
                imgHeight = maxRows;
                resize = true;
            }
            if (imgWidth < minCols) {
                imgWidth = minCols;
                resize = true;
            } else if (imgWidth > maxCols) {
                imgWidth = maxCols;
                resize = true;
            }
            if (resize) {
                Image resizedImage = image.getScaledInstance(imgWidth, imgHeight, Image.SCALE_DEFAULT);
                image = convertToBufferedImage(resizedImage);
            }

            //Remove opacity from image & change to grayscale
            BufferedImage grayImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
            Graphics gr = grayImage.getGraphics();
            gr.setColor(Color.WHITE);
            gr.fillRect(0, 0, imgWidth, imgHeight);
            gr.drawImage(image, 0, 0, null);
            gr.dispose();

            //Put all RGB values of image into array
            int[] pixels = image.getRGB(0, 0, imgWidth, imgHeight, null, 0, imgWidth);

            //Get darkest & lightest pixel values
            int darkest = 0;
            int lightest = 255;
            for (int pixel : pixels) {
                int grayVal = pixel & 0xFF;
                if (grayVal > darkest) darkest = grayVal;
                if (grayVal < lightest) lightest = grayVal;
            }

            map = new int[imgHeight][imgWidth];

            if (darkest == lightest) {  //All pixels are the same -> set everything to default tile weight and return early
                for (int row = 0; row < map.length; row++) {
                    for (int col = 0; col < map[0].length; col++){
                        map[row][col] = 1;
                    }
                }
                return;
            }

            //Start writing values to the int map of the image that will later be displayed
            int rowIndex = 0;
            int colIndex = 0;
            for (int pixel : pixels) {
                //Check if we need to bump down to the next row
                if (colIndex >= imgWidth) {
                    colIndex = 0;
                    rowIndex++;
                }

                //Get gray value of pixel by averaging rbg values
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel & 0xFF);
                int grayVal = (r + g + b) / 3;
                grayVal = 255 - grayVal;    //invert so white = 0 and black = 255

                int tileWeight = (int)((grayVal - lightest) * (100.0 / (darkest - lightest)) + 1);   //Convert values to between 1-101

                map[rowIndex][colIndex] = tileWeight;

                colIndex++;
            }
        }
        catch (IOException ex) {
            Logger.getLogger(PathFinderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private BufferedImage convertToBufferedImage(Image image)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    /**
     * Updates the tiles in the tilemap depending on what was calculated in the constructor
     */
    public void imageToMap(Tile[][] tileMap) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                switch (map[row][col]) {
                    case 101:   //Wall
                        tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.WALL);
                        break;
                    case 1:     //Default/empty tile
                        tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.NONE);
                        break;
                    default:    //Weighted tiles
                        tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.WEIGHTED, map[row][col]);
                        break;
                }
            }
        }
    }

    public int getRows() {
        return map.length;
    }

    public int getCols() {
        return map[0].length;
    }
}
