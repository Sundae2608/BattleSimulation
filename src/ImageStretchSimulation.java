import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import processing.core.PApplet;
import processing.core.PImage;
import view.utils.ImageProcessingUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * This runs a simulation that tests the image stretching capability. Since the entire engine also optimizes for
 * drawing, we need a way to be able to stretch a square image into 4 corners effectively.
 */
public class ImageStretchSimulation extends PApplet {

    Mat dst;
    byte[] bArray;
    int pxCount;
    int[] iArray;
    PImage inputImage;
    PImage pimage;
    public void settings() {
        size(225, 225);
        return;
    }
    public void setup() {
        inputImage = loadImage("imgs/TestImages/images.png");
        pimage = ImageProcessingUtils.transformImage(inputImage, new double[][] {
                {0, 0}, {180, 12}, {20, 200},  {225, 225}
        });
    }
    private void convertBGRAtoRAGB(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            print(hex(pixels[i]));
            print("\n");
            print("\n");
            print("\n");
            pixels[i] = (
                ((pixels[i] & 0xFF000000) >> 24) |
                ((pixels[i] & 0x00FF0000) >> 8) |
                ((pixels[i] & 0x0000FF00) << 8) |
                ((pixels[i] & 0x000000FF) << 24)
            );
        }
    }
    public void draw() {
        background(128);
        image(pimage, 0, 0, 225, 225);
        return;
    }

    public static void main(String... args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        PApplet.main("ImageStretchSimulation");
    }
}
