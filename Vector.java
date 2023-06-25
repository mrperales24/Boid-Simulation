import java.text.DecimalFormat;
import java.awt.*;
public final class Vector {
    static long addCount = 0, subCount = 0, dotProducts = 0, multCount = 0, divCount = 0, crossCount = 0;
    private Vector() { // Utility class for Vector operations, no instances are allowed
        throw new UnsupportedOperationException();
    }

    public static Vec3 add(Vec3 v1, Vec3 v2) {
        addCount++;
        return new Vec3(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    } // returns the sum of two vectors without altering the two

    public static Vec3 sub(Vec3 v1, Vec3 v2) {
        subCount++;
        return new Vec3(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    } // returns the difference of two vectors without altering the two

    public static Vec3 mult(Vec3 v1, Vec3 v2) {
        multCount++;
        return new Vec3(v1.x * v2.x, v1.y * v2.y, v1.z * v2.z);
    } // returns the product of two vectors without altering the two

    public static Vec3 mult(Vec3 v, double s) {
        multCount++;
        return new Vec3(v.x * s, v.y * s, v.z * s);
    } // returns the product of a vector with a scalar without alteration

    public static Vec3 div(Vec3 v1, Vec3 v2) {
        divCount++;
        return new Vec3(v1.x / v2.x, v1.y / v2.y, v1.z / v2.z);
    } // returns the result of one vector divided by another without alteration

    public static Vec3 div(Vec3 v, double d) {
        divCount++;
        return new Vec3(v.x / d, v.y / d, v.z / d);
    } // returns the result of a vector divided by a scalar without alteration

    public static double dot(Vec3 v1, Vec3 v2) {
        dotProducts++;
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    } // returns the dot product of two vectors

    public static Vec3 cross(Vec3 v1, Vec3 v2) {
        crossCount++;
        return new Vec3(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);
    } // returns the cross product of two vectors

    public static Vec3 random() {
        return new Vec3(Math.random(), Math.random(), Math.random());
    } // returns a random vector

    public static Vec3 random(double max) {
        return new Vec3(Math.random() * max, Math.random() * max, Math.random() * max);
    }

    public static Vec3 random(double min, double max) {
        return new Vec3((Math.random() * (max - min)) + min, (Math.random() * (max - min)) + min, (Math.random() * (max - min)) + min);
    }

    public static Vec3 random(Vec3 min, Vec3 max) {
        return new Vec3(
            (Math.random() * (max.x - min.x)) + min.x, 
            (Math.random() * (max.y - min.y)) + min.y, 
            (Math.random() * (max.z - min.z)) + min.z);
    }

    public static Vec3 average(Vec3...vecs) {
        Vec3 sum = new Vec3();
        for (Vec3 v : vecs) {
            sum.add(v);
        }
        sum.div(vecs.length);
        return sum;
    }
    
    public static int[][] to2DInt(Vec3...points) {
        int[][] poly = new int[2][points.length];
        for (int i = 0; i < points.length; i++) {
            poly[0][i] = (int) points[i].x;
            poly[1][i] = (int) points[i].y;
        }
       return new int[][]{poly[0], poly[1]};
    }

}
class Vec3 {

    public double x, y, z, w = 1;

    // CONSTRUCTORS //
    public static long vectorCount = 0;
    public Vec3(double x, double y, double z) {
        vectorCount++;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vec3() { this(0, 0, 0); }
    public Vec3(double n) { this(n, n, n); }
    public Vec3(double x, double y) { this(x, y, 0); }
    public Vec3(double[] array) { this(array[0], array[1], array[2]); }
    public Vec3(float[] array) { this(array[0], array[1], array[2]); }

    // SETTERS //
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void set(int index, double value) {
        if (index == 0) this.x = value;
        if (index == 1) this.y = value;
        if (index == 2) this.z = value;
    }
    public void setToZero() { set(0); }
    public void set(double n) { this.x = n; this.y = n; this.z = n;}
    public void set(double x, double y) { this.x = x; this.y = y; }
    public void set(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    public void set(Vec3 v) { this.x = v.x; this.y = v.y; this.z = v.z; }

    // GETTERS //
    public double get(int index) {
        if (index == 0) return this.x;
        if (index == 1) return this.y;
        if (index == 2) return this.z;
        else throw new IndexOutOfBoundsException();
    }
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    public double getZ() { return this.z; }

    // ADDITION (+) //
    public void add(double x, double y, double z) { Vector.addCount++; this.x += x; this.y += y; this.z += z; }
    public void add(double n) { this.add(n, n, n); }
    public void add(Vec3 v) { this.add(v.x, v.y, v.z); }

    // SUBTRACTION (-) //
    public void sub(double x, double y, double z) { Vector.subCount++; this.x -= x; this.y -= y; this.z -= z; }
    public void sub(double n) { this.sub(n, n, n); }
    public void sub(Vec3 v) { this.sub(v.x, v.y, v.z); }

    // MULTIPLICATION (*) //
    public void mult(double x, double y, double z) { Vector.multCount++; this.x *= x; this.y *= y; this.z *= z; }
    public void mult(double s) { this.mult(s, s, s); }
    public void mult(Vec3 v) { this.mult(v.x, v.y, v.z); }

    // DIVISION (/) //
    public void div(double x, double y, double z) {
        Vector.divCount++;
        if (x!= 0) this.x /= x; else throw new IllegalArgumentException("Cannot Divide Vec3 X by Zero");
        if (y!= 0) this.y /= y; else throw new IllegalArgumentException("Cannot Divide Vec3 Y by Zero");
        if (z!= 0) this.z /= z; else throw new IllegalArgumentException("Cannot Divide Vec3 Z by Zero");
    }
    public void div(double d) { this.div(d, d, d); }
    public void div(Vec3 v) { this.div(v.x, v.y, v.z); }

    // DOT PRODUCT //
    public double dot(Vec3 v) { Vector.dotProducts++; return (this.x * v.x) + (this.y * v.y) + (this.z * v.z); }

    // CROSS PRODUCT // *Alters this Vec3* *Use the Vec3Util Static Method to cross two vectors without changing them*
    public void cross(Vec3 v) {
        Vector.crossCount++;
        double a = this.x, b = this.y, c = this.z;
        this.x = b * v.z - c * v.y;
        this.y = c * v.x - a * v.z;
        this.z = a * v.y - b * v.x;
    }

    // UNIT VECTOR //
    public void normalize() {
        double div = this.mag();
        if (div != 0) this.mult(1/div);
    }

    public Vec3 getNormal() {
        Vec3 norm = this.copy();
        norm.normalize();
        return norm;
    }

    // CLONE //
    public Vec3 copy() { return new Vec3(this.x, this.y, this.z); }

    public void toInt() {
        x = (int) x;
        y = (int) y;
        z = (int) z;
    }

    public void xSignSwitch() { this.x *= -1; }
    public void ySignSwitch() { this.y *= -1; }
    public void zSignSwitch() { this.z *= -1; }
    public void changeSign(int i) {
        if (i == 0) this.x *= -1;
        if (i == 1) this.y *= -1;
        if (i == 2) this.z *= -1;
    }

    // MAGNITUDE //
    public double mag() { return Math.sqrt(this.dot(this)); }
    public double magSq() { return this.dot(this); }
    public void setMag(double mag) {
        this.normalize();
        this.mult(mag);
    }

    // LIMIT //
    public void limit(double limit) {
        double magSq = this.magSq();
        if (magSq > limit * limit) {
            this.normalize();
            this.mult(limit);
        }
    }

    // ANGLE //
    public double getAngle() { return Math.atan2(this.x, this.y); }

    // DISTANCE //
    public double dist(Vec3 v) {
        double dx = v.x - x;
        double dy = v.y - y;
        double dz = v.z - z;
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }

    // SUM OF COMPONENTS //
    public double sum() { return this.x + this.y + this.z; }

    public void divideByW() {
        this.x /= w;
        this.y /= w;
        this.z /= w;
    }

    // Matrix Transformations
    public void matMult(Matrix3 m) {
        Matrix.matrixMultCount++;
        double x1, y1, z1;
        x1 = this.x * m.m[0][0] + this.y * m.m[0][1] + this.z * m.m[0][2];
        y1 = this.x * m.m[1][0] + this.y * m.m[1][1] + this.z * m.m[1][2];
        z1 = this.x * m.m[2][0] + this.y * m.m[2][1] + this.z * m.m[2][2];
        this.x = x1; this.y = y1; this.z = z1;
    } // multiplies this vector by a 3x3 matrix

    public void matMult(Matrix4 m) {
        Matrix.matrixMultCount++;
        double x1, y1, z1, w1;
        x1 = this.x * m.m[0][0] + this.y * m.m[0][1] + this.z * m.m[0][2] + this.w * m.m[0][3];
        y1 = this.x * m.m[1][0] + this.y * m.m[1][1] + this.z * m.m[1][2] + this.w * m.m[1][3];
        z1 = this.x * m.m[2][0] + this.y * m.m[2][1] + this.z * m.m[2][2] + this.w * m.m[2][3];
        w1 = this.x * m.m[3][0] + this.y * m.m[3][1] + this.z * m.m[3][2] + this.w * m.m[3][3];
        this.x = x1; this.y = y1; this.z = z1; this.w = w1;
    } // multiplies this vector by a 4x4 matrix

    public void clamp(int min, int max) {
        this.x = x < min ? min : (x > max ? max : x);
        this.y = y < min ? min : (y > max ? max : y);
        this.z = z < min ? min : (z > max ? max : z);
    }

    // CREATE AN ARRAY FROM Vec3 //
    public double[] toArray() {
        return new double[]{this.x, this.y, this.z};
    }

    public int[] toIntArray() {
        return new int[]{(int) this.x, (int) this.y, (int) this.z};
    }

    public Color toColor() {
        this.clamp(0, 255);
        return new Color((int) this.x, (int) this.y, (int) this.z);
    }

    public Color toColor(double alpha) {
        this.clamp(0, 255);
        alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;
        return new Color((int) this.x, (int) this.y, (int) this.z, (int) alpha);
    }

    // For Printing //
    public String toString() {
        return "[" + decimal.format(x) + "," + decimal.format(y) + "," + decimal.format(z) + "]";
    }
    static DecimalFormat decimal = new DecimalFormat("#.###");
    public String xyString() {
        return "[" + decimal.format(x) + "," + decimal.format(y) + "]";
    }

    public boolean equals(Object object) {
        if (object != null && object instanceof Vec3) {
            Vec3 v = (Vec3) object;
            return x == v.x && y == v.y && z == v.z;
        } else return false;
    }
}