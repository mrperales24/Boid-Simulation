import java.awt.*;
public class Main {
    static int boidCount = 750;
    static double boidSize = 2.5;
    static Vec3 maxBounds = new Vec3(200, 200, 200);
    static Vec3 minBounds = Vector.mult(maxBounds, -1);
    static Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    static int height = (int) size.getHeight() - 200;
    static int width = (int) (size.getWidth()) - 110;
    
    public static void main(String[] args) {
        BoidSimulation sim = new BoidSimulation(boidCount, boidSize, maxBounds, minBounds, width, height);
        sim.setCamera(0.1, 1000, 120, new Vec3(0, 0, minBounds.z*1.5));
        sim.start();
    }
}
