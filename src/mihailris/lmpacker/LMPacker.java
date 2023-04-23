package mihailris.lmpacker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lightmaps packer (atlas UV generator for huge amount of small rectangles)
 * <br><br>
 * <sub>note: mpbit removal does no any performance improvement</sub>
 */
public class LMPacker {
    private final List<Rect> rects;
    private final List<Rect> placed;
    private final Size size;
    private Rect[][] matrix;
    private int mpbit; // 1 -> 2px, 2 -> 4px, 3 -> 8px, ...
    public LMPacker(int[] sizes, Comparator<Rect> comparator) {
        rects = new ArrayList<>(sizes.length/2);
        for (int i = 0; i < sizes.length/2; i++) {
            rects.add(new Rect(0, 0, sizes[i * 2], sizes[i * 2 + 1]));
        }
        rects.sort(comparator);
        placed = new ArrayList<>();
        size = new Size(0, 0);
    }

    public LMPacker(int[] sizes) {
        this(sizes, Comparator.comparingInt(Rect::getPackerScore).reversed());
    }

    public boolean buildFast(int width, int height, int extension) {
        return build(width, height, extension, 1, 2);
    }

    public boolean buildCompact(int width, int height, int extension) {
        return build(width, height, extension, 0, 1);
    }

    /**
     * Accuracy - minimization of unused space
     * @param width     target atlas width
     * @param height    target atlas height
     * @param extension extra border size (used to minimize mip-mapping artifacts)
     * @param mpbit     collisions matrix size is calculated as (width >> mpbit, height >> mpbit).
     *                  0 is the best, but the slowest.
     * @param vstep     lines scanning step. 1 is accurate scanning
     * @return true if success (image size is enough to pack all images)
     */
    public boolean build(int width, int height, int extension, int mpbit, int vstep) {
        cleanup();
        this.mpbit = mpbit;
        int mpix = 1 << mpbit;
        matrix = new Rect[height >> mpbit][width >> mpbit];
        size.set(width, height);
        for (Rect rect : rects) {
            rect.x = 0;
            rect.y = 0;
            rect.width += extension * 2;
            rect.height += extension * 2;
            rect.extX = 0;
            rect.extY = 0;
            if (mpix > 1) {
                if (rect.width % mpix > 0) {
                    rect.extX = mpix - (rect.width % mpix);
                }
                if (rect.height % mpix > 0) {
                    rect.extY = mpix - (rect.height % mpix);
                }
            }
            rect.width += rect.extX;
            rect.height += rect.extY;
        }
        try {
            for (Rect rect : rects) {
                if (!place(rect, vstep))
                    return false;
            }
        } finally {
            for (Rect rect : rects) {
                rect.x += extension;
                rect.y += extension;
                rect.width -= extension * 2 + rect.extX;
                rect.height -= extension * 2 + rect.extY;
            }
        }
        return true;
    }

    private void cleanup() {
        placed.clear();
    }

    private boolean place(Rect rect, int vstep) {
        int rw = rect.width >> mpbit;
        int rh = rect.height >> mpbit;
        if (vstep > 1) {
            vstep = Math.max(vstep, rh);
        }
        for (int y = 0; y + rh < matrix.length; y += vstep) {
            Rect[] line = matrix[y];

            boolean skiplines = true;
            Rect[] lower = matrix[y + rh - 1];

            for (int x = 0; x + rw < line.length; x++) {
                Rect prect = line[x];
                if (prect == null) {
                    if (skiplines) {
                        int lfree = 0;
                        while (lfree + x < line.length && lower[x + lfree] == null && lfree < rw) {
                            lfree++;
                        }
                        if (lfree >= rw)
                            skiplines = false;
                    }
                    prect = findCollision(x, y, rw, rh);
                    if (prect != null) {
                        x = (prect.x >> mpbit) + (prect.width >> mpbit) - 1;
                        continue;
                    }
                    fill(rect, x, y, rw, rh);
                    rect.x = x << mpbit;
                    rect.y = y << mpbit;
                    placed.add(rect);
                    return true;
                } else {
                    x = (prect.x >> mpbit) + (prect.width >> mpbit) - 1;
                }
            }
            if (skiplines) {
                y += rh - vstep;
            }
        }
        return false;
    }

    private Rect findCollision(int x, int y, int w, int h) {
        for (int row = y; row < y+h; row++) {
            for (int col = x; col < x+w; col++) {
                Rect rect = matrix[row][col];
                if (rect != null) {
                    return rect;
                }
            }
        }
        return null;
    }

    private void fill(Rect rect, int x, int y, int w, int h) {
        for (int row = y; row < y+h; row++) {
            for (int col = x; col < x+w; col++) {
                matrix[row][col] = rect;
            }
        }
    }

    public List<Rect> getResult() {
        return placed;
    }

    public Size getSize() {
        return size;
    }
}
