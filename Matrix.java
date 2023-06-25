public final class Matrix {

    private Matrix() { // Utility class for Matrix/Vector operations, no instances allowed
        throw new UnsupportedOperationException();
    }
    public static long matrixMultCount = 0;
    public static Vec3 vecMatMult(Matrix4 m, Vec3 v) {
        matrixMultCount++;
        Vec3 v1 = new Vec3();
        v1.x = v.x * m.m[0][0] + v.y * m.m[0][1] + v.z * m.m[0][2] + v.w * m.m[0][3];
        v1.y = v.x * m.m[1][0] + v.y * m.m[1][1] + v.z * m.m[1][2] + v.w * m.m[1][3];
        v1.z = v.x * m.m[2][0] + v.y * m.m[2][1] + v.z * m.m[2][2] + v.w * m.m[2][3];
        v1.w = v.x * m.m[3][0] + v.y * m.m[3][1] + v.z * m.m[3][2] + v.w * m.m[3][3];
        return v1;
    } // returns the vector resulting from multiplying v by the 4x4 matrix m

    public static Vec3 vec3MatMult(Matrix3 m, Vec3 v) {
        matrixMultCount++;
        Vec3 v1 = new Vec3();
        v1.x = v.x * m.m[0][0] + v.y * m.m[0][1] + v.z * m.m[0][2];
        v1.y = v.x * m.m[1][0] + v.y * m.m[1][1] + v.z * m.m[1][2];
        v1.z = v.x * m.m[2][0] + v.y * m.m[2][1] + v.z * m.m[2][2];
        return v1;
    } // returns the vector resulting from multiplying v by the 3x3 matrix m

    public static Matrix4 identity() {
        return new Matrix4(
                new double[]{1, 0, 0, 0},
                new double[]{0, 1, 0, 0},
                new double[]{0, 0, 1, 0},
                new double[]{0, 0, 0, 1});
    } // returns the 4x4 identity matrix

    public static Matrix4 rotationX(double theta) {
        return new Matrix4(
                new double[]{1, 0, 0, 0},
                new double[]{0, Trig.cos(theta), Trig.sin(theta), 0},
                new double[]{0, -Trig.sin(theta), Trig.cos(theta), 0},
                new double[]{0, 0, 0, 1}
        );
    } // returns the 4x4 x-axis rotational matrix about an angle theta (in degrees)

    public static Matrix4 rotationY(double theta) {
        return new Matrix4(
                new double[]{Trig.cos(theta), 0, -Trig.sin(theta), 0},
                new double[]{0, 1, 0, 0},
                new double[]{Trig.sin(theta), 0, Trig.cos(theta), 0},
                new double[]{0, 0, 0, 1}
        );
    }  // returns the 4x4 y-axis rotational matrix about an angle theta (in degrees)

    public static Matrix4 rotationZ(double theta) {
        return new Matrix4(
            new double[]{Trig.cos(theta), Trig.sin(theta), 0, 0},
            new double[]{-Trig.sin(theta), Trig.cos(theta), 0, 0},
            new double[]{0, 0, 1, 0},
            new double[]{0, 0, 0, 1}
        );
    } // returns the 4x4 z-axis rotational matrix about an angle theta (in degrees)

    public static Matrix4 scale(double x, double y, double z) {
        return new Matrix4(
            new double[]{x, 0, 0, 0},
            new double[]{0, y, 0, 0},
            new double[]{0, 0, z, 0},
            new double[]{0, 0, 0, 1}
        );
    } // returns the 4x4 scaling matrix (except w is maintained)

    public static Matrix4 translation(double x, double y, double z) {
        return new Matrix4(
                new double[]{1, 0, 0, x},
                new double[]{0, 1, 0, y},
                new double[]{0, 0, 1, z},
                new double[]{0, 0, 0, 1}
        );
    } // returns the 4x4 translational matrix

    public static Matrix4 projection(double fov, double near, double far) {
        double f = 1 / Trig.tang(fov * 0.5);
        return new Matrix4(
            new double[]{f, 0, 0, 0},
            new double[]{0, f, 0, 0},
            new double[]{0, 0, far / (far - near), 1},
            new double[]{0, 0, (-far * near) / (far - near), 1}
        );
    } // returns the 4x4 projection matrix

    public static Matrix4 matMult(Matrix4 m1, Matrix4 m2) {
        Matrix4 product = new Matrix4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                product.m[j][i] = m1.m[i][0] * m2.m[j][0] + m1.m[i][1] * m2.m[j][1] + m1.m[i][2] * m2.m[j][2] + m1.m[i][3] * m2.m[j][3];
            }
        }
        return product;
    } // returns a 4x4 matrix product of two matrices
}
class Matrix4 {
    public double[][] m;
    static long matrixCount = 0;

    public Matrix4() {
        matrixCount++;
        m = new double[4][4];
    } // Default Constructor

    public Matrix4(double[] row0, double[] row1, double[] row2, double[] row3) {
        matrixCount++;
        m = new double[][]{row0, row1, row2, row3};
    } // Contructor


}
class Matrix3 {
    public double[][] m = new double[3][3];
    static long matrixCount = 0;
    public Matrix3() {
        matrixCount++;
        m = new double[3][3];
    } // Default Constructor

    public Matrix3(double[] row0, double[] row1, double[] row2) {
        matrixCount++;
        m = new double[][]{row0, row1, row2};
    } // Constructor

    public Matrix3 getInverse() {
        double[][] a = new double[][]{
            {cof(1,1,2,2),-cof(1,0,2,2),cof(1,0,2,1)},
            {-cof(0,1,2,2),cof(0,0,2,2),-cof(0,0,2,1)},
            {cof(0,1,1,2),-cof(0,0,1,2),cof(0,0,1,1)}
        };
        double det = m[0][0]*a[0][0]+m[0][1]*a[0][1]+m[0][2]*a[0][2];
        return new Matrix3(
            new double[] {a[0][0]/det,a[1][0]/det,a[2][0]/det},
            new double[] {a[0][1]/det,a[1][1]/det,a[2][1]/det},
            new double[] {a[0][2]/det,a[1][2],a[2][2]/det}
        );
    } // returns the 3x3 inverse of this

    private double cof(int a, int b, int c, int d) {
        return m[a][b]*m[c][d] - m[c][b]*m[a][d];
    } // helper method for getting the inverse (det of minor of m)
}