import java.awt.*;
import java.util.ArrayList;
public class Clipper {
    int width, height;
    Vec3 p1, p2, p3;
    public Clipper(int width, int height) {
        this.width = width; this. height = height;
        this.p1 = new Vec3();
        this.p2 = new Vec3();
        this.p3 = new Vec3();
    }

    public Polygon clip(int[] X, int[] Y, int count) {
        if (count == 0) return new Polygon(X, Y, X.length);
        p1.set((double) X[0],(double) Y[0], 0);
        p2.set((double) X[1],(double) Y[1], 0);
        p3.set((double) X[2],(double) Y[2], 0);
        if (count == 1) return clip1();
        return clip2();
    }

    public int clipSquare(Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4) {
        int count = getOutOfBoundsCount(p1, p2, p3, p4);
        if (outside(p1)) {
            Vec3 a1 = clip(p1.copy(), p2);
            Vec3 a2 = clip(p1.copy(), p4);
            p1 = Vector.average(a1, a2);
        }
        if (outside(p2)) {
            Vec3 b1 = clip(p2.copy(), p1);
            Vec3 b2 = clip(p2.copy(), p3);
            p2 = Vector.average(b1, b2);
        }
        if (outside(p3)) {
            Vec3 c1 = clip(p3.copy(), p2);
            Vec3 c2 = clip(p3.copy(), p4);
            p3 = Vector.average(c1, c2);
        } 
        if (outside(p4)) {
            Vec3 d1 = clip(p4.copy(), p1);
            Vec3 d2 = clip(p4.copy(), p3);
            p4 = Vector.average(d1, d2);
        }
        return count;
    }
    public ArrayList<Integer> clipIterations = new ArrayList<>();
    public Vec3 clipAlt(Vec3 a, Vec3 b) {
        a.sub(b);
        double max = 0.5, min = 0;
        Vec3 sum = Vector.add(a, b);
        for (int i = 0; i < 100; i++) {
            sum.set(a.x*max + b.x, a.y*max + b.y);
            if (inside(sum)) {
                double tmax = max;
                max += (min + max)/2;
                min = tmax;
            } else if (outside(sum)) {
                max = (min + max)/2;
            } else {
                clipIterations.add(i);
                break; 
            }
        }
        return sum;
    } // clip vector A, using B as the point inside the triangle

    public Vec3 clip(Vec3 a, Vec3 b) {
        a.sub(b); a.normalize();
        Vec3 sum = Vector.add(a, b);
        int count = 0;
        while (inside(sum)) {
            sum.add(a);
            count++;
        }
        clipIterations.add(count);
        return sum;
    }

    public Polygon clip1() {
        boolean out1 = outside(p1), out2 = outside(p2), out3 = outside(p3);
        Vec3 a = p1.copy(), b = p2.copy(), c = p3.copy(), d = p1.copy();
        if (out1) {
            a = clip(a, p2); 
            d = clip(d, p3);
        } else if (out2) {
            b = clip(b, p1); 
            c = clip(p2.copy(), p3);
            d = p3.copy();
        } else if (out3) {
            c = clip(c, p2); 
            d = clip(p3.copy(), p1);
        }
        return new Polygon(new int[]{(int) a.x, (int) b.x, (int) c.x, (int) d.x}, new int[]{(int) a.y, (int) b.y, (int) c.y, (int) d.y}, 4);
    }
    
    public Polygon clip2() {
        boolean out1 = outside(p1), out2 = outside(p2), out3 = outside(p3);
        Vec3 a = p1.copy(), b = p2.copy(), c = p3.copy();
        if (!out3) {
            a = clip(a, p3); 
            b = clip(b, p3);
        } else if (!out2) {
            a = clip(a, p2);
            c = clip(c, p2);
        } else if (!out1) {
            b = clip(b, p1); 
            c = clip(c, p1);
        }
        return new Polygon(new int[]{(int) a.x,(int) b.x,(int)c.x}, new int[]{(int) a.y,(int) b.y,(int)c.y}, 3);
    }

    public boolean outside(Vec3 v) { return v.x > width || v.x < 0 || v.y > height || v.y < 0; }

    public boolean inside(Vec3 v) { return v.x < width && v.x > 0 && v.y < height && v.y > 0; }

    public boolean isInside(Polygon poly) {
        int[] X = poly.xpoints, Y = poly.ypoints;
        return getOutOfBoundsCount(X, Y) == 0;
    }

    public int getOutOfBoundsCount(int[] X, int[] Y) {
        int count = 0;
        for (int i = 0; i < X.length; i++)
            if (X[i] > width || X[i] < 0 || Y[i] > height || Y[i] < 0) count++;
        return count;
    } // Returns the number of boids that are outside of the specificed width and height

    public int getOutOfBoundsCount(Vec3...vecs) {
        int count = 0;
        for (Vec3 v : vecs) {
            if (v.x < 0 || v.x > width || v.y < 0 || v.y > height) count++;
        }
        return count;
    }
}
