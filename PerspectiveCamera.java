import java.awt.event.*;
import java.awt.*;
import java.util.TreeSet;
class PerspectiveCamera implements KeyListener, MouseListener, MouseMotionListener {

    public Vec3 position, u, v, w, theta, prevTheta;
    public double near, far, FOV, aspectRatio;
    public int fov;
    public Vec3 light = new Vec3(0, 1, 0);
    public int width, height;
    private Matrix4 projection;
    public boolean canRotate;
    public Robot robot = null;
    public PerspectiveCamera(double near, double far, int fov, int width, int height) {
        prevTheta = new Vec3();
        theta = new Vec3();
        this.near = near;
        this.far = far;
        this.fov = fov;
        FOV = Trig.tang(fov/2);
        this.position = new Vec3();
        w = new Vec3(0, 0, 1);
        v = new Vec3(0, 1, 0);
        u = new Vec3(1, 0, 0);
        keysDown = new TreeSet<>();
        this.aspectRatio = width/height;
        this.width = width; this.height = height;
        canRotate = false;
        try { robot = new Robot(); }
        catch (Exception e) { e.printStackTrace(); }
        setProjectionMatrix();
    } // Constructor

    private void setProjectionMatrix() {
        projection = new Matrix4(
            new double[]{1/(FOV*aspectRatio), 0, 0, 0},
            new double[]{0, 1/(FOV), 0, 0},
            new double[]{0, 0, (far + near) / (near - far), (2 * near * far)/(near - far)},
            new double[]{0, 0, -1, 0});
    }

    public Vec3 transform(Vec3 a) {
        return Matrix.vec3MatMult(rotationMatrix(), Vector.sub(a, position));
    } // performs a camera transform on the given vector

    public Vec3 transformAndProject(Vec3 a) {
        return Matrix.vecMatMult(projection, transform(a));
    } // transforms the given point and then projects onto the projection plane

    public void updateAxis() {
        if (theta.equals(prevTheta)) return;
        w.set(0, 0, 1);
        w.matMult(Matrix.rotationY(-theta.x));
        w.matMult(Matrix.rotationX(-theta.y));
        v.set(0, 1, 0);
        v.matMult(Matrix.rotationX(-theta.y));
        u = Vector.cross(v, w);
        prevTheta.set(theta);
        // w.normalize(); v.normalize(); u.normalize();
    } // updates the axis of rotation based on theta (changes by keyboard input)

    private Matrix3 rotationMatrix() {
        return new Matrix3(
            new double[]{u.x, v.x, -w.x},
            new double[]{u.y, v.y, -w.y},
            new double[]{u.z, v.z, -w.z}
    );
    } // change-of-coordinates matrix from the standard basis to the rotational basis

    public Matrix4 projection(int width, int height) {
        double a = width/height;
        return new Matrix4(
                new double[]{1/(FOV*a), 0, 0, 0},
                new double[]{0, 1/(FOV), 0, 0},
                new double[]{0, 0, -(far + near) / (far - near), -(2 * near * far)/(far - near)},
                new double[]{0, 0, -1, 0});
    } // returns a projection matrix based on the aspect ratio, fov, and near and far planes

    public boolean isBehind(Vec3 point) {
        Vec3 n = Vector.sub(point, position);
        n.normalize();
        Vec3 camNorm = w.copy();
        camNorm.normalize();
        return n.dot(camNorm) < 0;
    } // returns whether or not the point is behind this camera


    Vec3 forward = new Vec3(0, 0, 1), lateral = new Vec3(0, 0, 1);
    public void updateControls() {
        forward.set(0, 0, 4);
        forward.matMult(Matrix.rotationY(-theta.x));
        lateral.set(4, 0, 0);
        lateral.matMult(Matrix.rotationY(-theta.x));
        if (isKeyPressed(KeyEvent.VK_W)) position.add(forward);
        if (isKeyPressed(KeyEvent.VK_S)) position.sub(forward);
        if (isKeyPressed(KeyEvent.VK_A)) position.sub(lateral);
        if (isKeyPressed(KeyEvent.VK_D)) position.add(lateral);
        if (isKeyPressed(KeyEvent.VK_SHIFT)) position.y -= 4;
        if (isKeyPressed(KeyEvent.VK_SPACE)) position.y += 4;
        if (isKeyPressed(KeyEvent.VK_ESCAPE)) canRotate = !canRotate;
        if (isKeyPressed(KeyEvent.VK_UP)) theta.y -= 3;
        if (isKeyPressed(KeyEvent.VK_DOWN)) theta.y += 3;
        if (isKeyPressed(KeyEvent.VK_LEFT)) theta.x -= 3;
        if (isKeyPressed(KeyEvent.VK_RIGHT)) theta.x += 3;
        updateAxis();
        light.set(position.x, position.y, position.z);
    } // updates the camera fields through the corresponding keyboard input

    private static final Object keyLock = new Object();
    private static TreeSet<Integer> keysDown;

    private static boolean isKeyPressed(int keyCode) {
        synchronized (keyLock) {
            return keysDown.contains(keyCode);
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyPressed(KeyEvent e) { synchronized (keyLock) { keysDown.add(e.getKeyCode()); } }
    @Override
    public void keyReleased(KeyEvent e) { synchronized (keyLock) { keysDown.remove(e.getKeyCode()); } }
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseDragged(MouseEvent e) {
        mousePrev.set(mousePos.x, mousePos.y);
        mousePos.set((double) e.getX(), (double) e.getY());
        mouseChange = Vector.sub(mousePos, mousePrev);
        mouseChange.normalize();
        theta.add(mouseChange);
        if (theta.y > 90) {
            theta.y = 90;
        } if (theta.y < -90) {
            theta.y = -90;
        }
    }
    Vec3 mousePrev = new Vec3();
    Vec3 mousePos = new Vec3(width/2, height/2);
    Vec3 mouseChange = new Vec3();
    @Override
    public void mouseMoved(MouseEvent e) {
        if (!canRotate) return;
        mousePos.set((double) e.getX(), (double) e.getY());
        mousePos.div(width, height, 1);
        mousePos.sub(0.5, 0.5, 0);
        mousePos.mult(360, 180, 1);
        theta.set(mousePos);
        if (theta.y > 90) {
            theta.y = 90;
        } if (theta.y < -90) {
            theta.y = -90;
        }
    }
}