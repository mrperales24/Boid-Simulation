import java.util.ArrayList;
import java.awt.*;

public class Cube {
    ArrayList<Vec3> verts;
    Vec3 center;
    double width;
    ArrayList<Vec3> quadCenters;
    public Cube(Vec3 center, double width) {
        verts = new ArrayList<>();
        quadCenters = new ArrayList<>();
        this.center = center;
        this.width = width;
        verts.add(new Vec3(1, 1, -1));
        verts.add(new Vec3(1, -1, -1));
        verts.add(new Vec3(1, 1, 1));
        verts.add(new Vec3(1, -1, 1));
        verts.add(new Vec3(-1, 1, -1));
        verts.add(new Vec3(-1, -1, -1));
        verts.add(new Vec3(-1, 1, 1));
        verts.add(new Vec3(-1, -1, 1));
        for (int i = 0; i < 8; i++) {
            Vec3 q = verts.get(i).copy();
            q.mult(0.5);
            q.mult(width); q.add(center);
            quadCenters.add(q);
            verts.get(i).mult(width);
            verts.get(i).add(center);
        }
    }

    public Vec3 getV(int i) { return verts.get(i).copy(); }

    public Vec3 getQuadCenter(int i) { return quadCenters.get(i).copy(); }

    public boolean contains(Vec3 point) {
        Vec3 diff = Vector.sub(point, center);
        diff.div(width);
        return Math.abs(diff.x) <= 1 && Math.abs(diff.y) <= 1 && Math.abs(diff.z) <= 1;
    }

    public void draw(Graphics2D g, PerspectiveCamera camera) {
        ArrayList<Vec3> P = new ArrayList<>();
        for (Vec3 vert : verts) {
            Vec3 a = vert.copy();
            a = camera.transformAndProject(a);
            if (a.z < 0) return;
            Vec3 p = new Vec3();
            p.set(((a.x / a.z) + 1 )*camera.width/2, (-(a.y / a.z)+1)*camera.height/2, 1);
            P.add(p);
        }
        Vec3 one = P.get(0), two = P.get(1), three = P.get(2), four = P.get(3);
        Vec3 five = P.get(4), six = P.get(5), seven = P.get(6), eight = P.get(7);
        int[][] top = Vector.to2DInt(one, five, seven, three);
        int[][] bottom = Vector.to2DInt(six, two, four, eight);
        int[][] line1 = Vector.to2DInt(eight, seven);
        int[][] line2 = Vector.to2DInt(four, three);
        int[][] line3 = Vector.to2DInt(two, one);
        int[][] line4 = Vector.to2DInt(six, five);
        g.drawPolygon(top[0], top[1], 4);
        g.drawPolygon(bottom[0], bottom[1], 4);
        g.drawPolyline(line1[0], line1[1], 2);
        g.drawPolyline(line2[0], line2[1], 2);
        g.drawPolyline(line3[0], line3[1], 2);
        g.drawPolyline(line4[0], line4[1], 2);
    }
}
