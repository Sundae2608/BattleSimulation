package view.utils;

import model.utils.MathUtils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Contains utility function related to image processing.
 */
public class ImageProcessingUtils {
    public static PImage transformImage(PImage image, double[][] points) {
        // Convert PImage to Mat
        int[] iArray = new int[image.pixels.length];
        byte[] bArray = new byte[image.pixels.length * 4];
        System.out.println(image.pixels.length);
        System.out.println(image.width);
        System.out.println(image.height);
        PApplet.arrayCopy(image.pixels, iArray);
        ByteBuffer bBuf = ByteBuffer.allocate(image.pixels.length * 4);
        IntBuffer iBuf = bBuf.asIntBuffer();
        iBuf.get(iArray);
        bBuf.put(bArray);
        Mat input = new Mat(image.height, image.width, CvType.CV_8UC4);
        input.put(0, 0, bArray);

        // Set up before and after anchor points.
        MatOfPoint2f pts1 = new MatOfPoint2f(
            new Point(0, 0),
            new Point(image.width, 0),
            new Point(0, image.height),
            new Point(image.width, image.height)
        );
        double leftShift = MathUtils.findMin(new double[] {
                points[0][0], points[1][0], points[2][0], points[3][0]
        });
        double topShift = MathUtils.findMin(new double[] {
                points[0][1], points[1][1], points[2][1], points[3][1]
        });
        MatOfPoint2f pts2 = new MatOfPoint2f(
            new Point(points[0][0] - leftShift, points[0][1] - topShift),
            new Point(points[1][0] - leftShift, points[1][1] - topShift),
            new Point(points[2][0] - leftShift, points[2][1] - topShift),
            new Point(points[3][0] - leftShift, points[3][1] - topShift)
        );
        Size newSize = new Size((int) Math.ceil(MathUtils.findMax(new double[] {
                points[0][0] - leftShift,
                points[1][0] - leftShift,
                points[2][0] - leftShift,
                points[3][0] - leftShift,
        })), (int) Math.ceil(MathUtils.findMax(new double[] {
                points[0][1] - topShift,
                points[1][1] - topShift,
                points[2][1] - topShift,
                points[3][1] - topShift,
        })));

        // Create the perspective transformation
        Mat transform = Imgproc.getPerspectiveTransform(pts1, pts2);
        Mat dst = new Mat();
        Imgproc.warpPerspective(input, dst, transform, newSize);

        // Put the data back into a new Image.
        PImage ret = new PImage((int) newSize.width, (int) newSize.height, PConstants.ARGB);
        int[] returnIArray = new int[(int) newSize.width * (int) newSize.height];
        byte[] returnBArray = new byte[(int) newSize.width * (int) newSize.height * 4];
        ByteBuffer returnBBuf = ByteBuffer.allocate((int) newSize.width * (int) newSize.height * 4);
        IntBuffer returnIBuf = bBuf.asIntBuffer();
        dst.get(0, 0, returnBArray);
        ByteBuffer.wrap(returnBArray).asIntBuffer().get(returnIArray);
        PApplet.arrayCopy(iArray, ret.pixels);
        System.out.println(ret.pixels.length);
        for (int i = 0; i < ret.pixels.length; i++) {
            System.out.println(PApplet.hex(ret.pixels[i]));
        }
        return ret;
    }
}
