package mihailris.lmpacker;

public class Size {
    int width;
    int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
