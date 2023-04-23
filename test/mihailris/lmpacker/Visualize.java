package mihailris.lmpacker;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class Visualize {
    public static void main(String[] args) throws IOException {
        Random random = new Random(46);

        int count = 3000;
        int[] sizes = new int[count * 2];
        for (int i = 0; i < count; i++) {
            int w;
            int h;
            if (i < 90) {
                w = (random.nextInt(120) + 1);
                h = (random.nextInt(120) + 1);
            } else {
                w = (random.nextInt(20) + 1);
                h = (random.nextInt(20) + 1);
            }
            sizes[i*2] = w;
            sizes[i*2 + 1] = h;
        }
        LMPacker packer = new LMPacker(sizes);

        int width = 1024;
        int height = 1024;

        Runtime runtime = Runtime.getRuntime();

        long tm;
        tm = System.currentTimeMillis();
        if (!packer.buildCompact(width, height, 1)) {
            System.err.println("Could not to pack");
        }
        System.out.println("Compact: "+(System.currentTimeMillis()-tm)+" ms");
        ImageIO.write(visualize(width, height, packer.getResult()), "PNG", new File("output_compact.png"));

        tm = System.currentTimeMillis();
        if (!packer.buildFast(width, height, 1)) {
            System.err.println("Could not to pack");
        }
        System.out.println("Fast: "+(System.currentTimeMillis()-tm)+" ms");
        ImageIO.write(visualize(width, height, packer.getResult()), "PNG", new File("output_fast.png"));

        runtime.exec("xdg-open output_compact.png");
    }

    private static BufferedImage visualize(int width, int height, List<Rect> rects) {
        BufferedImage image = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        for (Rect rect : rects) {
            g2d.fillRect(rect.x-1, rect.y-1, rect.width+2, rect.height+2);
        }
        g2d.setColor(Color.BLACK);
        for (Rect rect : rects) {
            g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
        return image;
    }
}
