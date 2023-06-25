import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.*;

public class Mesh {
    public ArrayList<Triangle> triangles;
    public Vec3 position, color = new Vec3(255);
    public int alpha = 255;
    public double scale = 1;

    public Mesh(String fileName) {
        triangles = new ArrayList<>();
        loadMesh(new File(fileName));
    } // Constructor

    public Mesh(URI uri) {
        triangles = new ArrayList<>();
        loadMesh(uri);
    } // Constructor primarily when packaging into a jar file

    public void loadMesh(URI uri) {
        ArrayList<Vec3> vertices = new ArrayList<>();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(uri.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        Scanner inFile = new Scanner(inputStreamReader);
        while (inFile.hasNextLine()) {
            String next = inFile.next();
            if (next.equals("v")) {
                vertices.add(new Vec3(inFile.nextDouble(), inFile.nextDouble(), inFile.nextDouble()));
            } else if (next.equals("f")) {
                triangles.add(new Triangle(vertices.get(inFile.nextInt() - 1), vertices.get(inFile.nextInt() - 1), vertices.get(inFile.nextInt() - 1)));
            }
        }
        inFile.close();
    } // Loads a mesh using a uri, necessary when packaging into a jar file

    public void loadMesh(File objFile) {
        ArrayList<Vec3> vertices = new ArrayList<>();
        FileReader reader = null;
        try {
            reader = new FileReader(objFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert reader != null;
        Scanner inFile = new Scanner(reader);
        while (inFile.hasNextLine()) {
            String next = inFile.next();
            if (next.equals("v")) {
                vertices.add(new Vec3(inFile.nextDouble(), inFile.nextDouble(), inFile.nextDouble()));
            } else if (next.equals("f")) {
                triangles.add(new Triangle(vertices.get(inFile.nextInt() - 1), vertices.get(inFile.nextInt() - 1), vertices.get(inFile.nextInt() - 1)));
            }
        }
        inFile.close();
    } // loads a mesh from a obj formatted file

    public Color getColor() {
        return color.toColor();
    } // returns this color

    public Color getColor(Triangle triangle) {
        return Vector.mult(color, triangle.alpha).toColor();
    } // returns this color multiplied by the alpha value for shading (not transparency)

    public void setColor(int red, int green, int blue) {
        color = new Vec3(red, green, blue);
    } // sets the red, green, and blue fields

    public void setColor(Color color) {
        this.color = new Vec3(color.getComponents(null));
    } // sets the red, green, and blue fields from a color
}
class Triangle {
    public double alpha = 255;
    private Color color;
    private Vec3 position;
    private double distance, scale;

    public ArrayList<Vec3> verts;
    static long triangleCount;
    public Triangle() {
        triangleCount++;
        verts = new ArrayList<>();
        verts.add(new Vec3());
        verts.add(new Vec3());
        verts.add(new Vec3());
    } // Default Constructor

    public Triangle(Vec3 A, Vec3 B, Vec3 C) {
        triangleCount++;
        verts = new ArrayList<>(3);
        verts.add(A); verts.add(B); verts.add(C);
    } // Constructs a triangle from 3 vectors

    public Triangle(ArrayList<Vec3> verts) {
        triangleCount++;
        this.verts = verts;
    } // Constructs triangle from an ArrayList of vectors

    public Triangle(Vec3[] verts) {
        triangleCount++;
        this.verts = new ArrayList<>();
        this.verts.add(verts[0]);
        this.verts.add(verts[1]);
        this.verts.add(verts[2]);
    } // Constructs a triangle from an array of vectors

    // public int[] toArrayX() { return new int[]{(int) A.x, (int) B.x, (int) C.x}; } // Used for drawing
    // public int[] toArrayY() { return new int[]{(int) A.y, (int) B.y, (int) C.y}; } // Used for drawing

    public Vec3 getNormalToSurface() {
        Vec3 normal, line1, line2;
        line1 = Vector.sub(verts.get(1).copy(), verts.get(0).copy());
        line2 = Vector.sub(verts.get(2).copy(), verts.get(0).copy());
        normal = Vector.cross(line1.copy(), line2.copy());
        normal.normalize();
        return normal.copy();
    } // Returns the normal vector to the surface of this triangle

    public void setAlpha(Vec3 light) {
        Vec3 ray = Vector.sub(verts.get(0), light);
        ray.normalize();
        alpha = (getNormalToSurface().dot(ray) * 255);
        alpha = (getNormalToSurface().dot(ray) + 1) / 2;
    } // sets the alpha value from the position of the light source for shading

    public void setColor(Color color) {
        this.color = color;
    } // Sets this color

    public Color getColor(double alpha) {
        return new Color((int) (color.getRed() * alpha), (int) (color.getGreen() * alpha), (int) (color.getBlue() * alpha));
    } // returns this color multiplied by an alpha value for shading (not transparency)

    public void setPosition(Vec3 position) {
        this.position = position.copy();
    } // sets this position

    public Vec3 getPosition() {
        return this.position;
    } // returns this position

    public void setDistance(double distance) {
        this.distance = distance;
    } // sets this distance

    public double getDistance() {
        return this.distance;
    } // returns this distance

    public void setScale(double scale) {
        this.scale = scale;
    } // sets the scale of the triangle

    public double getScale() {
        return this.scale;
    } // return the scale of this triangle

    public static Comparator<Triangle> distComparator = new Comparator<Triangle>() {
        public int compare(Triangle t1, Triangle t2) {
            return Double.compare(t2.getDistance(), t1.getDistance());
        }
    }; // Comparator for sorting triangles from largest to smallest distance (painters method)
}