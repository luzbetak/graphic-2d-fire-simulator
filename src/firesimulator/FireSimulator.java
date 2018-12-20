/*
 * Fire 2D Graphic Simulator
 * Advanced Graphics
 * http://kevinluzbetak.com
 */
package firesimulator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FireSimulator {

    public static void main(String[] args) {
        int height = 200;
        int width = 300;
        int[][][] fire = new int[2][height][width];

        int coolant = 1;
        int front = 0;
        int back = 1;

        // create the color palette 
        int[][] palette = new int[256][3];
        for (int i = 1; i <= 64; ++i) {
            palette[i - 1][0] = (i << 2) - 1;
            palette[i - 1][1] = 0;
            palette[i - 1][2] = 0;
            palette[i - 1 + 64][0] = 255;
            palette[i - 1 + 64][1] = (i << 2) - 1;
            palette[i - 1 + 64][2] = 0;
            palette[i - 1 + 128][0] = 255;
            palette[i - 1 + 128][1] = 255;
            palette[i - 1 + 128][2] = (i << 2) - 1;
            palette[i - 1 + 192][0] = 255;
            palette[i - 1 + 192][1] = 255;
            palette[i - 1 + 192][2] = 255;
        }

        // fuel
        int[] fuel = new int[width];
        for (int j = 0; j < width; ++j) {
            fuel[j] = (int) (Math.random() * 255);
        }

        int iterations = 300;
        for (int iter = 0; iter < iterations; ++iter) {

            // generate flames, bottom row gets created from fuel source
            fire[back][0][0] = 0;
            fire[back][0][width - 1] = 0;
            for (int j = 1; j < width - 1; ++j) {
                int neighbors = fuel[j] + fire[front][1][j] + fire[front][0][j - 1] + fire[front][0][j + 1];
                neighbors >>= 2;
                neighbors -= coolant;
                fire[back][0][j] = (neighbors < 0) ? 0 : neighbors;
            }

            // other rows get created from 4-connected neighbors
            for (int i = 1; i < height - 1; ++i) {
                fire[back][i][0] = 0;
                fire[back][i][width - 1] = 0;
                for (int j = 1; j < width - 1; ++j) {
                    int neighbors = (int) fire[front][i - 1][j]
                            + (int) fire[front][i + 1][j]
                            + (int) fire[front][i][j - 1]
                            + (int) fire[front][i][j + 1];
                    neighbors >>= 2;
                    neighbors -= coolant;
                    fire[back][i][j] = (neighbors < 0) ? 0 : neighbors;
                }
            }

            // shift rows upward
            for (int i = height - 1; i >= 1; --i) {
                for (int j = 0; j < width; ++j) {
                    fire[back][i][j] = fire[back][i - 1][j];
                }
            }

            // add/subtract fuel to the source row
            for (int j = 0; j < width; ++j) {
                int f = (int) (Math.random() * 64) - 32;
                fuel[j] += f;
                if (fuel[j] < 0) {
                    fuel[j] = 0;
                } else if (fuel[j] > 255) {
                    fuel[j] = 255;
                }
            }

            // render image
            int[][] img = new int[height][width];
            if (iter > -1) {
                for (int i = 0; i < height; ++i) {
                    for (int j = 0; j < width; ++j) {
                        img[i][j] = ((palette[fire[back][height - i - 1][j]][0] & 0xFF) << 16)
                                | ((palette[fire[back][height - i - 1][j]][1] & 0xFF) << 8)
                                | ((palette[fire[back][height - i - 1][j]][2] & 0xFF) << 0);
                    }
                }
                saveImage(String.format("/Users/kevin/tmp/img/frame%03d.png", iter), img);
            }

            // -- swap buffers
            back = (back == 1) ? 0 : 1;
            front = (front == 1) ? 0 : 1;
        }
    }

    public static void saveImage(String name, int[][] img) {
        int height = img.length;
        int width = img[0].length;

        // -- write image to file
        System.out.format("%s", name);
        try {
            // move image into BufferedImage object
            BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    int color = img[i][j];
                    bi.setRGB(j, i, color);
                }
            }
            File outputfile = new File(name);
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            System.out.println("image write error");
        }

        System.out.println(" - done");
    }
}
