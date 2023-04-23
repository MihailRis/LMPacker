package mihailris.lmpacker;

public class Rect {
    int x;
    int y;
    int width;
    int height;

    int extX;
    int extY;

    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "["+x+", "+y+" "+width+"x"+height+"]";
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPackerScore() {
        if (width * height > 100)
            return height * height * 1000;
        return (width * height * height);
    }

    public int getArea() {
        return width * height;
    }
}
