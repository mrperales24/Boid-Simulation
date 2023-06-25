import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
public class Boid {
    
    public Vec3 position, velocity, acceleration;
    private Vec3 alignment, separation, cohesion, vnorm;
    private Vec3 w, v, u, color;
    public double maxSpeed;
    public double distance;
    public int flockNumber = (int) (Math.random() * 5);

    static int count = 0;
    public Boid(Vec3 maxBounds, Vec3 minBounds, double maxSpeed) {
        position = Vector.random(minBounds, maxBounds);
        velocity = Vector.random(-maxSpeed, maxSpeed);
        acceleration = Vector.random();
        this.acceleration = new Vec3(Math.random(), Math.random(), Math.random());
        this.maxSpeed = maxSpeed;
        this.color = Vector.random(255);
        w = new Vec3(); v = new Vec3(); u = new Vec3();
        alignment = new Vec3(); separation = new Vec3(); cohesion = new Vec3();
        vnorm = velocity.copy();
    } // Constructor

    public void updateAxis() {
        u.set(velocity);
        v.set(0, 1, 0);
        v.sub(Vector.mult(u, v.dot(u)/u.dot(u)));
        w = Vector.cross(u, v);
        u.normalize(); v.normalize(); w.normalize(); 
    } // Updates the u, v, w vectors based on the velocity (direction of travel) using Gram-Schmidt Algorithm

    public Matrix3 rotationMatrix() {
        return new Matrix3(
            new double[]{u.x, v.x, -w.x},
            new double[]{u.y, v.y, -w.y},
            new double[]{u.z, v.z, -w.z}
        );
    } // Change-of-coordinates matrix from the standard basis {i, j, k}, to the basis {u, v, w}

    
    public void update(int[] radii, double[] forces, ArrayList<Boid> boids, boolean flocking) {
        if (flocking) {
            alignment(radii[0] * 2, forces[0], boids, flocking);
            cohesion(radii[1] * 2, forces[1], boids, flocking);
            flockAvoidance(radii[2] * 2, forces[2] * 2, boids);
        } else {
            alignment(radii[0], forces[0], boids, flocking);
            cohesion(radii[1], forces[1], boids, flocking);
        }
        separation(radii[2], forces[2], boids, flocking);
        move();
        updateAxis();
    } // updates the boid 

    public void move() {
        position.add(velocity);
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        acceleration.setToZero();
    } // translates the boid based on velocity

    public void updateAllForces(int[] radii, double[] forces, ArrayList<Boid> boids, boolean flocking) {
        alignment.setToZero();
        cohesion.setToZero();
        separation.setToZero();
        Vec3 fSeparation = new Vec3();
        int aTotal = 0, cTotal = 0, sTotal = 0, fTotal = 0;
        vnorm.set(velocity);
        vnorm.normalize();
        double aRadius = radii[0], cRadius = radii[1], sRadius = radii[2], fRadius = radii[2]*2;
        if (flocking) {
            aRadius *= 2; cRadius *= 2;
        }
        for (Boid boid : boids) {
            double dist = position.dist(boid.position);
            if (boid == this || !canSee(boid, vnorm)) continue;
            if (flocking && this.flockNumber != boid.flockNumber) {
                if (dist < fRadius) {
                    Vec3 diff = Vector.sub(position, boid.position);
                    if (dist != 0) diff.div(dist * dist);
                    fSeparation.add(diff);
                    fTotal++;
                }
            }
            if (flocking && this.flockNumber != boid.flockNumber) continue;
            if (dist < aRadius) {
                alignment.add(boid.velocity);
                aTotal++;
            }
            if (dist < cRadius) {
                cohesion.add(boid.position);
                cTotal++;
            }
            if (dist < sRadius) {
                Vec3 diff = Vector.sub(position, boid.position);
                if (dist != 0) diff.div(dist * dist);
                separation.add(diff);
                sTotal++;
            }
        }
        if (aTotal > 0) {
            alignment.div(aTotal);
            alignment.setMag(maxSpeed);
            alignment.sub(velocity);
            alignment.limit(forces[0]);
            acceleration.add(alignment);
        }
        if (cTotal > 0) {
            cohesion.div(cTotal);
            cohesion.sub(position);
            cohesion.setMag(maxSpeed);
            cohesion.sub(velocity);
            cohesion.limit(forces[1]);
            acceleration.add(cohesion);
        }
        if (sTotal > 0) {
            separation.div(sTotal);
            separation.setMag(maxSpeed);
            separation.sub(velocity);
            separation.limit(forces[2]);
            acceleration.add(separation);
        }
        if (fTotal > 0) {
            fSeparation.div(fTotal);
            fSeparation.setMag(maxSpeed);
            fSeparation.sub(velocity);
            fSeparation.limit(forces[2]*2);
            acceleration.add(fSeparation);
        }
    }

    public void alignment(int radius, double force, ArrayList<Boid> boids, boolean flocking) {
        alignment.setToZero();
        int total = 0;
        vnorm.set(velocity.x, velocity.y, velocity.z);
        vnorm.normalize();
        for (Boid boid : boids) {
            if (flocking && boid.flockNumber != this.flockNumber) continue;
            double dist = position.dist(boid.position);
            if (boid != this && dist < radius && this.canSee(boid, vnorm)) {
                alignment.add(boid.velocity);
                total++;
            }
        }
        if (total > 0) {
            alignment.div(total);
            alignment.setMag(maxSpeed);
            alignment.sub(velocity);
            alignment.limit(force);
            acceleration.add(alignment);
        }
    } // calculates the alignment force from boids within the radius and applies it to the acceleration

    public void separation(int radius, double force, ArrayList<Boid> boids, boolean flocking) {
        separation.setToZero();
        int total = 0;
        vnorm.set(velocity);
        vnorm.normalize();
        for (Boid boid : boids) {
            if (flocking && boid.flockNumber != this.flockNumber) continue;
            double dist = position.dist(boid.position);
            if (boid != this && dist < radius && this.canSee(boid, vnorm)) {
                Vec3 diff = Vector.sub(position, boid.position);
                if (dist != 0) diff.div(dist * dist);
                separation.add(diff);
                total++;
            }
        }
        if (total > 0) {
            separation.div(total);
            separation.setMag(maxSpeed);
            separation.sub(velocity);
            separation.limit(force);
            acceleration.add(separation);
        }
    } // calculates the separation force from boids within the radius and applies it to the acceleration

    public void flockAvoidance(int radius, double force, ArrayList<Boid> boids) {
        separation.setToZero();
        int total = 0;
        vnorm.set(velocity);
        vnorm.normalize();
        for (Boid boid : boids) {
            if (boid.flockNumber == this.flockNumber) continue;
            double dist = position.dist(boid.position);
            if (boid != this && dist < radius && this.canSee(boid, vnorm)) {
                Vec3 diff = Vector.sub(position, boid.position);
                if (dist != 0) diff.div(dist * dist);
                separation.add(diff);
                total++;
            }
        }
        if (total > 0) {
            separation.div(total);
            separation.setMag(maxSpeed);
            separation.sub(velocity);
            separation.limit(force);
            acceleration.add(separation);
        }
    } // calculates the flock avoidance force from boids in other flocks and applies it to the acceleration

    public void cohesion(int radius, double force, ArrayList<Boid> boids, boolean flocking) {
        cohesion.setToZero();
        int total = 0;
        vnorm.set(velocity);
        vnorm.normalize();
        for (Boid boid : boids) {
            if (flocking && boid.flockNumber != this.flockNumber) continue;
            double dist = position.dist(boid.position);
            if (boid != this && dist < radius && canSee(boid, vnorm)) {
                cohesion.add(boid.position);
                total++;
            }
        }
        if (total > 0) {
            cohesion.div(total);
            cohesion.sub(position);
            cohesion.setMag(maxSpeed);
            cohesion.sub(velocity);
            cohesion.limit(force);
            acceleration.add(cohesion);
        }
    } // calculates the cohesion force from boids within the radius and applies it to the acceleration

    public void attraction(Vec3 pos, double mag, double forceLimit) {
        Vec3 attraction = new Vec3();
        attraction.add(pos);
        attraction.sub(position);
        attraction.setMag(mag);
        attraction.sub(velocity);
        attraction.limit(forceLimit);
        acceleration.add(attraction);
    } // Sends boid to the center (0, 0, 0)

    public boolean canSee(Boid boid, Vec3 vnorm) {
        Vec3 dnorm = Vector.sub(boid.position, position);
        dnorm.normalize();
        double dp = vnorm.dot(dnorm);
        return dp > 0;
    } // returns true if this boid can see the other boid 
    
    public void reflect(Vec3 maxBounds, Vec3 minBounds) {
        double val, max, min;
        for (int i = 0; i < 3; i++) {
            val = position.get(i) + velocity.get(i); max = maxBounds.get(i); min = minBounds.get(i);
            if (val > max) {
                position.set(i, max);
                velocity.changeSign(i);
            } else if (val < min) {
                position.set(i, min);
                velocity.changeSign(i);
            }
        }
    } // reflects boid off the boundary when reached

    public void wrap(Vec3 maxBounds, Vec3 minBounds) {
        double val, max, min;
        for (int i = 0; i < 3; i++) {
            val = position.get(i) + velocity.get(i); max = maxBounds.get(i); min = minBounds.get(i);
            if (val > max) position.set(i, min);
            else if (val < min) position.set(i, max);
        }
    } // sends boid to the opposite side of the area when one boundary is reached

    public Vec3 transform(Vec3 point, double size) {
        Vec3 a = point.copy();
        // a.x = point.dot(u);
        // a.y = point.dot(v);
        // a.z = point.dot(w);
        a.matMult(rotationMatrix());
        a.mult(size);
        a.add(position);
        return a;
    }

    public void setDistance(Vec3 pos) {
        distance = position.dist(pos);
    } // sets the distance from a position, used for sorting

    public Color getColor(double alpha) {
        return Vector.mult(color, alpha).toColor();
    } // Returns the color of this boid times alpha for shading (not transparency)

    public Color getColor(double shading, double alpha) {
        return Vector.mult(color, shading).toColor(alpha*255);
    }

    public Color getColor() {
        return color.toColor();
    } // Returns the color of this boid

    public static Comparator<Boid> distComparator = new Comparator<>() {
        public int compare(Boid b1, Boid b2) {
            return Double.compare(b2.distance, b1.distance);
        }
    }; // sorts the boids from largest to smallest distance (for the painters method)
}
