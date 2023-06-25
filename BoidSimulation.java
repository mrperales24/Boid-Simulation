import java.awt.event.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

public class BoidSimulation implements ActionListener, ChangeListener {
    public ArrayList<Boid> boids;
    private final Color[] flockColors = {Color.red, Color.green, Color.blue, Color.cyan, Color.magenta};
    public Vec3 maxBounds, minBounds;
    public Mesh boidMesh, boxMesh;
    public PerspectiveCamera camera;
    public int boidCount; double boidSize;
    public int[] radii = {40, 35, 20};
    public double[] forces = {0.7, 0.2, 1};
    public boolean canMove = true, group = false, wrap = true, enableFlocks = false;
    public boolean displayControls = false, displayValues = true, showVelocity = false, showOctree = false;
    int width, height;
    public Clipper clipper;
    public Octree boidTree;
    
    public BoidSimulation(int boidCount, double boidSize, Vec3 maxBounds, Vec3 minBounds, int width, int height) {
        //System.setProperty("sun.java2d.opengl", "True");
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        this.boidCount = boidCount;
        this.boidSize = boidSize;
        this.maxBounds = maxBounds;
        this.minBounds = minBounds;
        boids = new ArrayList<>();
        boidTree = new Octree(Vector.average(maxBounds, minBounds), maxBounds.x, boidCount/50);
        while (boids.size() < boidCount) boids.add(new Boid(maxBounds, minBounds, 8));
        boidMesh = new Mesh("crane.txt");
        boxMesh = new Mesh("cubeSub.txt");
        this.width = width; this.height = height;
        clipper = new Clipper(width, height);
    } // Constructor

    public void setCamera(double near, double far, int fov, Vec3 position) {
        camera = new PerspectiveCamera(near, far, fov, width, height);
        camera.position = position;
    } // sets the camera fields and position

    public void setForces(double...forces) { this.forces = forces; } // Sets the forces in the simulation
    public void setRadii(int...radii) { this.radii = radii; } // sets the radii for the forces

    private BufferedImage toCompatibleImage (BufferedImage image) {
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        if (image.getColorModel().equals(gfxConfig.getColorModel())) return image;
        BufferedImage newImage = gfxConfig.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return newImage;
    }
    Graphics2D g;

    JPanel drawPanel, sliderPanel, buttonPanel;
    public void start() {
        Color background = new Color(255, 255, 255, 255);
        JFrame frame = new JFrame();
        BufferedImage offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        offscreenImage = toCompatibleImage(offscreenImage);
        BufferedImage onscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        onscreenImage = toCompatibleImage(onscreenImage);
        Graphics2D offscreen = offscreenImage.createGraphics(), onscreen = onscreenImage.createGraphics();
        offscreen.setColor(Color.BLACK);
        offscreen.fillRect(0, 0, width, height);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);
        offscreen.setFont(DEFAULT_FONT);
        fontMetrics = offscreen.getFontMetrics();
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);
        
        drawPanel = new JPanel();
        drawPanel.setBackground(background);
        drawPanel.add(draw);
        drawPanel.setPreferredSize(new Dimension(width, height));
        
        buttonPanel = new JPanel();
        buttonPanel.setBackground(background);
        buttonPanel.setLayout(new GridLayout());
        buttonPanel.setPreferredSize(new Dimension(width, 30));

        sliderPanel = new JPanel();
        sliderPanel.setBackground(background);
        sliderPanel.setLayout(new GridLayout());
        sliderPanel.setPreferredSize(new Dimension(width, 30));

        initSliders(sliderPanel, offscreen, width, height);
        initButtons(buttonPanel, offscreen, width, height);   
        initLabels(draw, offscreen, width, height);
        
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(width, height + 90));
        box.setBackground(background);
        box.add(buttonPanel);
        box.add(drawPanel); 
        box.add(sliderPanel);
        
        frame.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        frame.setContentPane(box);

        //frame.setContentPane(draw);
        frame.addKeyListener(camera);
        frame.addMouseListener(camera);
        frame.addMouseMotionListener(camera);
        frame.addKeyListener(camera);
        frame.setBackground(Color.black);
        frame.setFocusTraversalKeysEnabled(false);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("Boid Simulation");
        frame.pack(); frame.requestFocusInWindow();
        frame.setVisible(true);

        double previousTime = System.currentTimeMillis()*1000, currentTime;
        int frames = 0;
        int cX = width/2, cY = height/2;
        g = offscreen;
        while (frame.isShowing()) {
            Matrix.matrixMultCount = 0; Matrix3.matrixCount = 0;
            Matrix4.matrixCount = 0; Vec3.vectorCount = 0;
            Vector.addCount = 0; Vector.subCount = 0; Vector.dotProducts = 0; 
            Vector.multCount = 0; Vector.divCount = 0; Vector.crossCount = 0;
            if (frames == 0) previousTime = System.currentTimeMillis()*0.001;
            offscreen.setColor(new Color(0, 0, 100, 255));
            offscreen.fillRect(0, 0, width, height);
            camera.updateControls();
            drawBox(offscreen);
            updateOctree();
            updateBoids(boidTree);
            //updateBoids();
            //System.out.println(boidscount);
            boidscount = 0;
            drawBoids(offscreen);
            offscreen.setColor(Color.black);
            updateCameraLabel();
            offscreen.drawString("+", cX, cY);
            onscreen.drawImage(offscreenImage, 0, 0, null);
            frame.repaint();
            currentTime = System.currentTimeMillis()*0.001;
            frames++;
            if (currentTime - previousTime >= 1) {
                fList.add(frames);
                displayFrames = (int) frames;
                frames = 0;
            }
        }
    } // this starts the simulation and loops until window is closed
    public ArrayList<Integer> fList = new ArrayList<>();
    public void updateOctree() {
        boidTree.reset();
        for (Boid boid : boids) boidTree.addBoid(boid);
    }
    int displayFrames = 0;
    int boidscount = 0;
    public void updateBoids(Octree octree) {
        if (octree.boids.size() > 0 && showOctree) octree.draw(g, camera);
        if (octree.divided) for (Octree o : octree.children) updateBoids(o);
        else {
            for (Boid boid : octree.boids) {
                boidscount++;
                if (canMove && !group) {
                    boid.update(radii, forces, octree.boids, enableFlocks);
                    if (wrap) boid.wrap(maxBounds, minBounds);
                    else boid.reflect(maxBounds, minBounds);
                } else if (group && canMove) {
                    boid.attraction(new Vec3(), 100, 100);
                    if (wrap) boid.wrap(maxBounds, minBounds);
                    else boid.reflect(maxBounds, minBounds);
                    boid.move();
                }
                boid.setDistance(camera.position);
            }
        }
    }

    public void updateBoids() {
        for (Boid boid : boids) {
            if (canMove && !group) {
                boid.update(radii, forces, boids, enableFlocks);
                if (wrap) boid.wrap(maxBounds, minBounds);
                else boid.reflect(maxBounds, minBounds);
            } else if (group && canMove) {
                boid.attraction(new Vec3(0, 0, 0), 100, 100);
                boid.move();
            }
            boid.setDistance(camera.position);
        }
    } // updates each boid
    
    public void drawBoids(Graphics2D graphics2d) {
        Collections.sort(boids, Boid.distComparator);
        Vec3 a, p = new Vec3();
        int behindCount;
        for (Boid boid : boids) {
            if (showVelocity) drawVelocityLine(boid, graphics2d, width, height);
            for (Triangle triangle : boidMesh.triangles) {
                int[] tempX = new int[3], tempY = new int[3];
                double alpha = 0;
                behindCount = 0;
                for (int i = 0; i < triangle.verts.size(); i++) {
                    a = boid.transform(triangle.verts.get(i), boidSize);
                    alpha += (1-(Vector.sub(a, camera.light).mag()/camera.far));
                    a = camera.transformAndProject(a);
                    if (a.z < 0) behindCount++;
                    a.divideByW();
                    p.set(((a.x / a.z) + 1 )*width/2, (-(a.y / a.z)+1)*height/2, 1);
                    tempX[i] = (int) p.x;
                    tempY[i] = (int) p.y;
                }
                alpha /= 3;
                if (behindCount != 0) continue;
                alpha = alpha < 0 ? 0 : alpha > 1 ? 1 : alpha;
                if (enableFlocks) graphics2d.setColor(getFlockColor(boid.flockNumber, alpha));
                else graphics2d.setColor(boid.getColor(alpha, alpha));
                graphics2d.fillPolygon(tempX, tempY, 3);
            }
        }
    } // Draws the boids in the simulation using change of coordinates and perspective projection

    public void drawBox(Graphics2D graphics2d) {
        Vec3 a, p = new Vec3();
        int behindCount;
        for (Triangle triangle : boxMesh.triangles) {
            int[] tempX = new int[3], tempY = new int[3];
            behindCount = 0;
            double alpha = 0;
            for (int i = 0; i < triangle.verts.size(); i++) {
                a = triangle.verts.get(i).copy();
                a.mult((maxBounds.x - minBounds.x) / 2, (maxBounds.y - minBounds.y) / 2, (maxBounds.z - minBounds.z) / 2);
                alpha += (1-(Vector.sub(a, camera.light).mag()/camera.far));
                a = camera.transformAndProject(a);
                if (a.z < 0) behindCount++;
                a.divideByW();
                p.set(((a.x / a.z) + 1 )*width/2, (-(a.y / a.z)+1)*height/2, 1);
                tempX[i] = (int) p.x;
                tempY[i] = (int) p.y;
            }
            alpha/=3;
            if (behindCount != 0 || alpha == 0) continue;
            Vec3 color = new Vec3(255, 255, 255);
            color.mult(alpha);
            if (triangle.verts.get(0).y == -1 && triangle.verts.get(1).y == -1 && triangle.verts.get(2).y == -1)
                graphics2d.setColor(color.toColor(70*alpha));
            else if (triangle.verts.get(0).y == 1 && triangle.verts.get(1).y == 1 && triangle.verts.get(2).y == 1)
                graphics2d.setColor(color.toColor(50*alpha));
            else graphics2d.setColor(color.toColor(10*alpha));
            graphics2d.fillPolygon(tempX, tempY, 3);
        }
    } // Draws a box that surrounding the bounding area of the boid simulation

    public void drawVelocityLine(Boid boid, Graphics2D graphics2d, int width, int height) {
        graphics2d.setColor(Color.white);
        Vec3 p2 = Vector.add(boid.position, boid.velocity);
        Vec3 p1 = boid.position.copy();
        p1 = camera.transform(p1);
        if (p1.z > 0) return;
        p1 = Matrix.vecMatMult(camera.projection(width, height), p1);
        p1.divideByW();
        p1 = new Vec3(p1.x/p1.z, p1.y/p1.z, 1);
        p1.x = (p1.x + 1) * width/2;
        p1.y = (-p1.y + 1) * height/2;
        p2 = camera.transform(p2);
        if (p2.z > 0) return;
        p2 = Matrix.vecMatMult(camera.projection(width, height), p2);
        p2.divideByW();
        p2 = new Vec3(p2.x/p2.z, p2.y/p2.z, 1);
        p2.x = (p2.x + 1) * width/2;
        p2.y = (-p2.y + 1) * height/2;
        boolean out1 = clipper.outside(p1), out2 = clipper.outside(p2);
        if (out1 && out2) return;
        if (!out2 && out1) p1 = clipper.clip(p1, p2);
        if (out2 && !out1) p2 = clipper.clip(p2, p1);
        graphics2d.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
    } // draws the line of velocity for the given boid

    static DecimalFormat df = new DecimalFormat("0.00");
    public void initButtons(JPanel draw, Graphics2D graphics2d, int width, int height) {
        String[] buttonStrings = {
            "Toggle Simulation Values","Wrap/Reflect","Group Boids","Toggle Movement",
            "Toggle Flocks","Show Velocity","Show Octree","Toggle Control Display"
        };
        for (int i = 0; i < buttonStrings.length; i++) {
            String bString = buttonStrings[i];
            JButton button = new JButton(bString);
            button.addActionListener(this);
            button.setFocusable(false);
            button.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "none");
            button.setActionCommand(String.valueOf(i));
            button.setBounds(0, 0, 100, 30);
            buttonPanel.add(button);
        }
    } // initializes the buttons that toggle the boolean values for the simulation

    ArrayList<JSlider> radiusSliders = new ArrayList<>();
    ArrayList<JSlider> forceSliders = new ArrayList<>();
    public void initSliders(JPanel draw, Graphics2D graphics2d, int width, int height) {
        for (int i = 0; i < 3; i++) {
            JSlider slider = new JSlider(0, 100, radii[i]);
            slider.setFont(graphics2d.getFont());
            slider.setBorder(BorderFactory.createTitledBorder(radiusTitles[i] + radii[i]));
            slider.setSize(90, 30);
            slider.addChangeListener(this);
            slider.setFocusable(false);
            slider.setPaintLabels(true);
            radiusSliders.add(slider);
            draw.add(slider);
        }
        for (int i = 0; i < 3; i++) {
            JSlider slider = new JSlider(0, 50, (int) (forces[i] * 10));
            slider.setBorder(BorderFactory.createTitledBorder(forceTitles[i] + forces[i]));
            slider.setFont(graphics2d.getFont());
            slider.setSize(90, 30);
            slider.addChangeListener(this);
            slider.setFocusable(false);
            slider.setPaintLabels(true);
            forceSliders.add(slider);
            draw.add(slider);
        }
        for (JSlider slider : forceSliders) slider.setVisible(displayValues);
        for (JSlider slider : radiusSliders) slider.setVisible(displayValues);
    } // initializes the sliders that will control the forces and radii for the simulation

    JLabel cameraLabel, sizeLabel, countLabel, FPSLabel; //titleLabel;
    ArrayList<JLabel> controlLabels;
    public void initLabels(JLabel draw, Graphics2D graphics2d, int width, int height) {
        int xPos = (int) (width * 0.01);
        int yPos = (int) (height * 0.03);
        cameraLabel = new JLabel("X: "+df.format(camera.position.x) + "  Y: "+df.format(camera.position.y)+"  Z: "+df.format(camera.position.z));
        cameraLabel.setSize(200, 40); cameraLabel.setLocation(xPos, yPos);
        cameraLabel.setForeground(Color.white); cameraLabel.setFont(graphics2d.getFont());
        countLabel = new JLabel("Boids: " + boidCount);
        countLabel.setSize(200, 40); countLabel.setLocation(xPos, yPos + 20);
        countLabel.setForeground(Color.white); countLabel.setFont(graphics2d.getFont());
        sizeLabel = new JLabel("Boid Size: " + boidSize);
        sizeLabel.setSize(200, 40); sizeLabel.setForeground(Color.white);
        sizeLabel.setFont(graphics2d.getFont()); sizeLabel.setLocation(xPos, yPos + 40);
        FPSLabel = new JLabel("FPS: " + displayFrames);
        FPSLabel.setSize(200, 40); FPSLabel.setForeground(Color.white);
        FPSLabel.setFont(graphics2d.getFont()); FPSLabel.setLocation(xPos, yPos + 60);
        cameraLabel.setVisible(displayValues); sizeLabel.setVisible(displayValues); 
        countLabel.setVisible(displayValues); FPSLabel.setVisible(displayValues);
        draw.add(cameraLabel); draw.add(countLabel); draw.add(sizeLabel); draw.add(FPSLabel);
        controlLabels = new ArrayList<>();
        int wOffset = (int) (width * 0.98);
        int hOffset = (int) (height * 0.03);
        String[] controlStrings = {
            "Move Forward: (W)","Move Left: (A)","Move Backward: (S)","Move Right: (D)",
            "Move Up: (Spacebar)","Move Down: (Left Shift)", "Rotate: Click and Drag"};
        int[] controlOffsets = {0, 20, 40, 60, 80, 100, 120, 150};
        for (int i = 0; i < controlStrings.length; i++) {
            JLabel jLabel = new JLabel(controlStrings[i]);
            jLabel.setSize(200, 40);
            int stringWidth = graphics2d.getFontMetrics().stringWidth(controlStrings[i]);
            jLabel.setLocation(wOffset - stringWidth, controlOffsets[i] + hOffset);
            jLabel.setFont(graphics2d.getFont()); jLabel.setForeground(Color.white);
            controlLabels.add(jLabel); draw.add(jLabel);
        }
        for (JLabel label : controlLabels) label.setVisible(displayControls);
    } // Initializes the Labels that will be on screen

    public void updateCameraLabel() {
        cameraLabel.setText("X: "+df.format(camera.position.x) + "  Y: "+df.format(camera.position.y)+"  Z: "+df.format(camera.position.z));
        cameraLabel.setSize(fontMetrics.stringWidth(cameraLabel.getText()) + 200, 40);
        FPSLabel.setText("FPS: " + displayFrames);
    } // Updates the camera position label

    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);
    public static FontMetrics fontMetrics;
    private static void textLeft(Graphics2D graphics2d, double x, double y, String text) {
        int hs = fontMetrics.getDescent();
        graphics2d.drawString(text, (int) (x), (int) (y + hs));
    } // Draws text aligned to the right of indicated x value, used for drawing text on the left side of the frame

    private static void textCenter(Graphics2D graphics2d, double x, double y, String text) {
        int ws = fontMetrics.stringWidth(text);
        int hs = fontMetrics.getDescent();
        graphics2d.drawString(text, (int) (x - (ws/2)), (int) (y + hs));
    }

    public Color getFlockColor(int flockNumber, double alpha) {
        int Red = (int) (flockColors[flockNumber].getRed() * alpha); 
        int Green = (int) (flockColors[flockNumber].getGreen() * alpha); 
        int Blue = (int) (flockColors[flockNumber].getBlue() * alpha); 
        return new Color(Red, Green, Blue, (int) (alpha * 255));
    } // returns the color of each boid using it's flockID and multiplies each component by alpha for shading
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "0" -> {
                displayValues = !displayValues;
                sliderPanel.setVisible(displayValues);
                cameraLabel.setVisible(displayValues);
                sizeLabel.setVisible(displayValues);
                countLabel.setVisible(displayValues);
                FPSLabel.setVisible(displayValues);
            }
            case "1" -> wrap = !wrap;
            case "2" -> group = !group;
            case "3" -> canMove = !canMove;
            case "4" -> enableFlocks = !enableFlocks;
            case "5" -> showVelocity = !showVelocity;
            case "7" -> {
                displayControls = !displayControls;
                for (JLabel label : controlLabels) label.setVisible(displayControls);
            }
            case "6" -> showOctree = !showOctree;
        }
    }
    String[] radiusTitles = {"Alignment Radius: ", "Cohesion Radius: ", "Separation Radius: "};
    String[] forceTitles = {"Alignment Force: ", "Cohesion Force: ", "Separation Force: "};
    @Override
    public void stateChanged(ChangeEvent e) {
        for (int i = 0; i < radiusSliders.size(); i++) {
            int value = radiusSliders.get(i).getValue();
            if (value == radii[i]) continue;
            radii[i] = value;
            radiusSliders.get(i).setBorder(BorderFactory.createTitledBorder(radiusTitles[i] + radii[i]));
        }
        for (int i = 0; i < forceSliders.size(); i++) {
            double value = forceSliders.get(i).getValue();
            if (value * 0.1 == forces[i]) continue;
            forces[i] = value * 0.1;
            forceSliders.get(i).setBorder(BorderFactory.createTitledBorder(forceTitles[i] + forces[i]));
        }
    }
}