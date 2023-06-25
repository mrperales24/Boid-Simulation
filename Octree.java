import java.util.ArrayList;
import java.awt.*;
public class Octree {
    public ArrayList<Boid> boids;
    public int size, capacity;
    public ArrayList<Octree> children;
    public Vec3 center;
    public boolean divided;

    public Cube cube;
    public Octree(Vec3 center, double width, int capacity) {
        this.cube = new Cube(center, width);
        boids = new ArrayList<>();
        this.center = center;
        this.capacity = capacity;
        size = 0;
        divided = false;
    }

    public void reset() {
        boids = new ArrayList<>();
        size = 0;
        divided = false;
    }

    public void addBoid(Boid boid) {
        if (!this.contains(boid)) return;
        if (size < capacity) {
            boids.add(boid);
            size++;
        } else {
            if (!divided) {
                subDivide();
                divided = true;
            }
            for (Octree octree : children) {
                octree.addBoid(boid);
            }
        }
    }

    public void subDivide() {
        children = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            children.add(new Octree(cube.getQuadCenter(i), cube.width/2, capacity));
        }
        for (Boid b1 : boids) {
            for (Octree octree : children) {
                octree.addBoid(b1);
            }
        }
        boids = new ArrayList<>();
    }

    public boolean contains(Boid boid) {
        return cube.contains(boid.position);
    }

    public void draw(Graphics2D g, PerspectiveCamera camera) {
        double alpha = (1-(Vector.sub(center, camera.light).mag()/camera.far));
        int c = (int) (200 * alpha);
        c = c > 255 ? 255 : c < 0 ? 0 : c;
        g.setColor(new Color(c,  c, c, c));
        cube.draw(g, camera);
    }
}
