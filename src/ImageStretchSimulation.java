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
                {0, 0}, {20, 200}, {180, 12}, {225, 225}
        });
//        Imgcodecs imageCodecs = new Imgcodecs();
//        Mat img = imageCodecs.imread("imgs/TestImages/images.png");
//        System.out.println(img);
//        bArray = new byte[img.height() * img.width() * 4];
//        iArray = new int[img.height() * img.width()];
//        dst = imageCodecs.imread("imgs/TestImages/images.png");
//        MatOfPoint2f pts1 = new MatOfPoint2f(
//                new Point(0, 0),
//                new Point(0,225),
//                new Point(225,0),
//                new Point(225,225));
//        MatOfPoint2f pts2 = new MatOfPoint2f(
//                new Point(0, 0),
//                new Point(20,200),
//                new Point(180,12),
//                new Point(225,225));
//        Mat transform = Imgproc.getPerspectiveTransform(pts1, pts2);
//        Imgproc.warpPerspective(img, dst, transform, new Size(225, 225));
//        pimage = createImage(img.width(), img.height(), ARGB);
//        ByteBuffer bBuf = ByteBuffer.allocate(pxCount);
//        IntBuffer iBuf = bBuf.asIntBuffer();
//        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2BGRA);
//        dst.get(0, 0, bArray);
//        ByteBuffer.wrap(bArray).asIntBuffer().get(iArray);
//        arrayCopy(iArray, pimage.pixels);
//        convertBGRAtoRAGB(pimage.pixels);
//        return;
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
