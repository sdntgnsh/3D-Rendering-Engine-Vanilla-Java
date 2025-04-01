import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI implements ActionListener, ChangeListener {
    private final int size = 200;
    private int clicks = 0, colorCounter = 0;
    private int INFLATE_COUNTER = 1;
    private JLabel label = new JLabel("Number of clicks:  0");
    private JLabel WireFramelabel = new JLabel("Toggle WireFrame:  0");
    private JLabel ChangeColorLabel = new JLabel("Change Color:  0");
    private JFrame frame;
    private JButton ClickButton, ResetButton, ExitButton, AutoRotateButton, ToggleWireframeButton, ChangeColorButton;
    private JLabel xzLabel, xyLabel;
    private JSlider xzSlider, xySlider;
    private JPanel renderPanel, xzPanel, xyPanel, sliderPanel;

    private Timer autoRotateTimer;
    private Timer idleCheckTimer;
    private long lastUserInputTime;
    private static final long IDLE_TIMEOUT = 500;

    private double totalRotationAngleXZ = 0.0;
    private double totalRotationAngleXY = 0.0;
    private static final double AUTO_ROTATION_SPEED = 1.0; 
    private boolean isAutoRotating = false; // Flag to block slider feedback

    private boolean autoRotationEnabled = true;
    private boolean ToggleWireframe = true;

    private JButton InflateButton;
    private boolean isInflated = false;

    public GUI() {
        frame = new JFrame("Main Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setUndecorated(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
    
        frame.getContentPane().setBackground(Color.BLACK);

        // Click, Reset, and Exit Buttons
        ClickButton = new JButton("Toggle Shape (F)");
        ResetButton = new JButton("Reset (R)"); 
        ExitButton = new JButton("Exit Application (Esc)");
        ToggleWireframeButton = new JButton("Toggle WireFrame (Q)");
        ChangeColorButton = new JButton("Change Color (E)");
        AutoRotateButton = new JButton("");
        Dimension buttonSize = new Dimension(screenSize.width/6, screenSize.width/50);
        ClickButton.setPreferredSize(buttonSize);
        ResetButton.setPreferredSize(buttonSize);
        ExitButton.setPreferredSize(buttonSize);
        ChangeColorButton.setPreferredSize(buttonSize);
        ToggleWireframeButton.setPreferredSize(buttonSize);
        AutoRotateButton.setPreferredSize(new Dimension(20, 20));

        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);
        ToggleWireframeButton.addActionListener(this);
        ChangeColorButton.addActionListener(this);

        ClickButton.setBackground(Color.LIGHT_GRAY);
        ResetButton.setBackground(Color.LIGHT_GRAY);
        ToggleWireframeButton.setBackground(Color.LIGHT_GRAY);
        ChangeColorButton.setBackground(Color.LIGHT_GRAY);
        AutoRotateButton.setBackground(Color.GREEN);
        ExitButton.setBackground(Color.RED);

        AutoRotateButton.addActionListener(this);
        // AutoRotateButton.setText("↺");

        // Sliders for XZ and XY movement
        xzSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 0);
        xySlider = new JSlider(JSlider.VERTICAL, -50, 50, 0);
        
        xzPanel = new JPanel(new BorderLayout());
        xzPanel.setBackground(Color.GRAY);
        xzPanel.add(xzSlider, BorderLayout.CENTER);
        
        xyPanel = new JPanel(new BorderLayout());
        xyPanel.setBackground(Color.GRAY);
        xyPanel.add(xySlider, BorderLayout.CENTER);

        // Slider properties for smooth scrolling feel
        xzSlider.setMajorTickSpacing(10);
        xzSlider.setPaintTicks(false);
        xzSlider.setPaintTrack(true);

        xySlider.setMajorTickSpacing(10);
        xySlider.setPaintTicks(false);
        xySlider.setPaintTrack(true);

        // Add listeners
        xzSlider.addChangeListener(this);
        xySlider.addChangeListener(this);

        //Keyboard Bindings
        InputMap inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = frame.getRootPane().getActionMap();
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        
        
        inputMap.put(KeyStroke.getKeyStroke("F"), "toggleShape");
        inputMap.put(KeyStroke.getKeyStroke("R"), "reset");
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "exitApp");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "toggleWireframe");
        inputMap.put(KeyStroke.getKeyStroke("T"), "toggleAutoRotate");
        inputMap.put(KeyStroke.getKeyStroke("E"), "changeColor");
        inputMap.put(KeyStroke.getKeyStroke("I"), "inflate");
        actionMap.put("inflate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InflateButton.doClick();
            }
        });
        
        actionMap.put("toggleShape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ClickButton.doClick();
            }
        });
        
        actionMap.put("reset", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ResetButton.doClick();
            }
        });
        actionMap.put("changeColor", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangeColorButton.doClick();
            }
        });
        
        actionMap.put("exitApp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExitButton.doClick();
            }
        });
        
        actionMap.put("toggleWireframe", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ToggleWireframeButton.doClick();
            }
        });
        
        actionMap.put("toggleAutoRotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AutoRotateButton.doClick();
            }
        });

        // Add mouse wheel listener for XZ slider
        xzSlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int value = xzSlider.getValue();
                int notches = e.getWheelRotation();
                xzSlider.setValue(value - (notches * 2)); // Adjust sensitivity as needed
            }
        });

        // Add mouse wheel listener for XY slider
        xySlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int value = xySlider.getValue();
                int notches = e.getWheelRotation();
                xySlider.setValue(value - (notches * 2)); // Adjust sensitivity as needed
            }
        });
  
        xzSlider.setBackground(Color.GRAY);
        xySlider.setBackground(Color.GRAY);
        xzSlider.setPreferredSize(new Dimension(400, 30)); 
        xySlider.setPreferredSize(new Dimension(25, 400));
        
        // Labels for sliders
        xzLabel = new JLabel("XZ Position: 0", SwingConstants.CENTER);
        xyLabel = new JLabel("XY Position: 0", SwingConstants.CENTER);

        // Render Panel to show 3D Render
        renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Rendering magic will happen here
                List<Polygon> polygon_list = new ArrayList<>();

                List<Vertex[]> Shape_Coords = new ArrayList<>(); 

                
            // Adding coordinates to Shape_Coords

                
                if(clicks % 5 == 0){
                    Shape_Coords = CoordinateCreator.create_triangle_coords(size);
                }
                else if(clicks % 5 == 1){
                    Shape_Coords = CoordinateCreator.create_square_coords(size);
                }
                else if(clicks % 5 == 2){
                    Shape_Coords = CoordinateCreator.create_octahedron_coords(size*2);
                   
                }
                else if(clicks % 5 == 3){
                     Shape_Coords = CoordinateCreator.create_icosahedron_coords(size);
                }
                else{
                    Shape_Coords = CoordinateCreator.create_torus_coords(size);
                }
                Color colorArr[] = {new Color(205, 180, 219), new Color(255, 200, 221), new Color(255, 175, 204), new Color(189, 224, 254), new Color(162, 210, 255), new Color(202, 240, 248)};
                Color colorArr2[] = {new Color(255, 173, 173), new Color(255, 214, 165), new Color(253, 255, 182), new Color(202, 255, 191),new Color(155, 246, 255),new Color(160, 196, 255), new Color(189, 178, 255)};

                Color Colors[][] = {colorArr,colorArr2};

                int inx  = 0;
                for(Vertex[] coords : Shape_Coords){

                    polygon_list.add(new Polygon(coords, Colors[colorCounter%2][inx % Colors[colorCounter%2].length]));
                    inx++;
                }

                if (isInflated) {
                    for (int i = 0; i < INFLATE_COUNTER; i ++)
                        polygon_list = inflate(polygon_list);
                }


                double heading = Math.toRadians(totalRotationAngleXZ); // Use tracked angle
                double pitch = Math.toRadians(totalRotationAngleXY);    // Use tracked angle

                Matrix3 headingTransform = new Matrix3(new double[] {
                    Math.cos(heading), 0, Math.sin(heading),
                    0, 1, 0,
                    -Math.sin(heading), 0, Math.cos(heading)
                });

                Matrix3 pitchTransform = new Matrix3(new double[] {
                    1, 0, 0,
                    0, Math.cos(pitch), Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });

                Matrix3 transform = headingTransform.multiply(pitchTransform);
                if(ToggleWireframe){
                    g2.translate(getWidth() / 2, getHeight() / 2);
                    g2.setColor(Color.WHITE);
                    
                    for (Polygon poly : polygon_list) {


                        for(int i = 0; i < poly.number_of_sides; i++){

                            poly.vertex_array.set(i, transform.transform(poly.vertex_array.get(i)) );
                        }


                        Path2D path = new Path2D.Double();
                        Vertex prevVertex = poly.vertex_array.get(0);
                        for(Vertex v : poly.vertex_array){ 
                            path.moveTo(prevVertex.x, prevVertex.y);
                            path.lineTo(v.x, v.y);
                            prevVertex = v;
                        }
                        path.moveTo(prevVertex.x, prevVertex.y);
                        path.lineTo(poly.vertex_array.get(0).x, poly.vertex_array.get(0).y);

                        path.closePath();
                        g2.draw(path);
                    }
                }
                else{

                    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                    Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                    for (Polygon poly : polygon_list) {
                        List<Vertex> rotatedVertices = new ArrayList<>();
                        for (int i = 0; i < poly.number_of_sides; i++) {
                            Vertex original = poly.vertex_array.get(i);
                            Vertex rotated = transform.transform(original);
                            rotatedVertices.add(rotated);
                            // Translate the rotated vertex
                            Vertex translated = new Vertex(
                                rotated.x + getWidth()/2,
                                rotated.y + getHeight()/2,
                                rotated.z
                            );
                            poly.vertex_array.set(i, translated);
                        }

                        // Compute normal using rotated vertices (before translation)
                        Vertex v0 = rotatedVertices.get(0);
                        Vertex v1 = rotatedVertices.get(1);
                        Vertex v2 = rotatedVertices.get(2);

                        Vertex ab = new Vertex(v1.x - v0.x, v1.y - v0.y, v1.z - v0.z);
                        Vertex ac = new Vertex(v2.x - v0.x, v2.y - v0.y, v2.z - v0.z);

                        Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x
                        );

                        double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                        if (normalLength != 0) {
                            norm.x /= normalLength;
                            norm.y /= normalLength;
                            norm.z /= normalLength;
                        } else {
                            norm.x = 0; norm.y = 0; norm.z = 0;
                        }

                        // Compute shade factor (light direction: towards the viewer)
                        double lightDirX = 0, lightDirY = 0, lightDirZ = 1;
                        double dotProduct = norm.x * lightDirX + norm.y * lightDirY + norm.z * lightDirZ;
                        double shade = Math.abs(dotProduct);
                        Color shadedColor = getShade(poly.color, shade);

                        // Determine bounding box
                        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
                        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                        for (Vertex v : poly.vertex_array) {
                            minX = Math.min(minX, (int) v.x);
                            maxX = Math.max(maxX, (int) v.x);
                            minY = Math.min(minY, (int) v.y);
                            maxY = Math.max(maxY, (int) v.y);
                        }
                        minX = Math.max(0, (int) Math.ceil(minX));
                        maxX = Math.min(img.getWidth()-1, (int) Math.floor(maxX));
                        minY = Math.max(0, (int) Math.ceil(minY));
                        maxY = Math.min(img.getHeight()-1, (int) Math.floor(maxY));

                        // Check each pixel in the bounding box
                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {
                                boolean inTriangle = false, inTriangle2 = false;
                                double[] inside = null, inside2 = null;
                                double epsilon = 1e-6;

                                if (poly.number_of_sides == 3) {
                                    inside = isPointInsideTriangle(x, y, poly.vertex_array.get(0), poly.vertex_array.get(1), poly.vertex_array.get(2));
                                    inTriangle = Math.abs(inside[0] - (inside[1] + inside[2] + inside[3])) < epsilon;
                                } else if (poly.number_of_sides == 4) {
                                    inside = isPointInsideTriangle(x, y, poly.vertex_array.get(0), poly.vertex_array.get(1), poly.vertex_array.get(2));
                                    inTriangle = Math.abs(inside[0] - (inside[1] + inside[2] + inside[3])) < epsilon;
                                    inside2 = isPointInsideTriangle(x, y, poly.vertex_array.get(0), poly.vertex_array.get(2), poly.vertex_array.get(3));
                                    inTriangle2 = Math.abs(inside2[0] - (inside2[1] + inside2[2] + inside2[3])) < epsilon;
                                }

                                if (inTriangle || inTriangle2) {
                                    double depth = 0.0;
                                    if (inTriangle) {
                                        double b1 = inside[1]/inside[0], b2 = inside[2]/inside[0], b3 = inside[3]/inside[0];
                                        depth = b1*poly.vertex_array.get(0).z + b2*poly.vertex_array.get(1).z + b3*poly.vertex_array.get(2).z;
                                    } else {
                                        double b1 = inside2[1]/inside2[0], b2 = inside2[2]/inside2[0], b3 = inside2[3]/inside2[0];
                                        depth = b1*poly.vertex_array.get(0).z + b2*poly.vertex_array.get(2).z + b3*poly.vertex_array.get(3).z;
                                    }

                                    int zinx = y * img.getWidth() + x;
                                    if (zBuffer[zinx] <= depth) {
                                        zBuffer[zinx] = depth;
                                        img.setRGB(x, y, shadedColor.getRGB());
                                    }
                                }
                            }
                        }
                    }
                    g2.drawImage(img, 0, 0, null);                
                }
            }
        
        };
        xySlider.addChangeListener(e -> renderPanel.repaint());
        xzSlider.addChangeListener(e -> renderPanel.repaint());
        renderPanel.setLayout(new BorderLayout());
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                xzSlider.setPreferredSize(new Dimension(renderPanel.getWidth(), 30));
                xySlider.setPreferredSize(new Dimension(30, renderPanel.getHeight()));
                renderPanel.revalidate();
            }
        });
        
        // Panel for buttons (RIGHT SIDE)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 20, 20)); // 3 buttons stacked vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Padding
        InflateButton = new JButton("Inflate (I)");
        InflateButton.setPreferredSize(buttonSize);
        InflateButton.addActionListener(this);
        InflateButton.setBackground(Color.LIGHT_GRAY);
        buttonPanel.add(ClickButton);
        buttonPanel.add(ToggleWireframeButton);
        buttonPanel.add(ChangeColorButton);
        buttonPanel.add(ResetButton);
        buttonPanel.add(InflateButton);
        buttonPanel.add(ExitButton);
        buttonPanel.setBackground(Color.GRAY);
        
        // Panel for labels (TOP)
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        topPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7)); // Padding
        topPanel.add(label);
        topPanel.add(WireFramelabel);
        topPanel.add(ChangeColorLabel);
        // topPanel.add(xzLabel);
        // topPanel.add(xyLabel);
        topPanel.setBackground(Color.GRAY);
        
        // Create a new panel to hold both XZ slider and small button
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBackground(Color.GRAY);

        JPanel blankPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        blankPanel.setOpaque(true);
        blankPanel.setBackground(Color.GRAY);
        blankPanel.setPreferredSize(new Dimension(screenSize.width/6 + 40, 30));
       
        JPanel AutoRotatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        AutoRotatePanel.setOpaque(true);
        AutoRotatePanel.setBackground(Color.GRAY);  
        AutoRotatePanel.add(AutoRotateButton);
        blankPanel.add(AutoRotatePanel, BorderLayout.EAST);

        southPanel.add(blankPanel, BorderLayout.EAST);
        southPanel.add(AutoRotatePanel, BorderLayout.WEST);
        frame.add(southPanel, BorderLayout.SOUTH);
        southPanel.add(xzSlider, BorderLayout.CENTER);
        frame.add(xySlider, BorderLayout.WEST);  // Keep XY slider on left

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(renderPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST); // Buttons on the RIGHT side
        
        // Add key bindings for arrow keys and WASD
        addKeyBindings();

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                xzSlider.setPreferredSize(new Dimension(renderPanel.getWidth(), 30));
                xySlider.setPreferredSize(new Dimension(30, renderPanel.getHeight()));
                xzPanel.revalidate();
                xyPanel.revalidate();
            }
        });

            autoRotateTimer = new Timer(30, e -> {
                renderPanel.repaint();
                isAutoRotating = true; // Block stateChanged updates during auto-rotation
                
                // Increment angles continuously
                totalRotationAngleXZ += AUTO_ROTATION_SPEED;
                totalRotationAngleXY += AUTO_ROTATION_SPEED;
                
                // Map angles to slider range (-50 to 50) using modulo
                xzSlider.setValue((int) ((totalRotationAngleXZ / 5) % 100 - 50));
                xySlider.setValue((int) ((totalRotationAngleXY / 5) % 100 - 50));
                
                isAutoRotating = false;
                renderPanel.repaint();
            });

        idleCheckTimer = new Timer(500, e -> {
            if (System.currentTimeMillis() - lastUserInputTime > IDLE_TIMEOUT) {
                if (autoRotationEnabled && !autoRotateTimer.isRunning()) {
                    autoRotateTimer.start();
                }
            }
        });

        autoRotateTimer.setInitialDelay(0);
        idleCheckTimer.setInitialDelay(0);
        idleCheckTimer.start();
        lastUserInputTime = System.currentTimeMillis();

        // Add mouse listeners to sliders for user input detection
        xzSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onUserInput();
            }
        });

        xySlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onUserInput();
            }
        });

        // Add onUserInput to mouse wheel listeners
        xzSlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                onUserInput();
                int value = xzSlider.getValue();
                int notches = e.getWheelRotation();
                xzSlider.setValue(value - (notches * 2));
            }
        });

        xySlider.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                onUserInput();
                int value = xySlider.getValue();
                int notches = e.getWheelRotation();
                xySlider.setValue(value - (notches * 2));
            }
        });
    }

    private void onUserInput() {
        lastUserInputTime = System.currentTimeMillis();
        if (autoRotateTimer.isRunning()) {
            autoRotateTimer.stop();
        }
    }

   
    private final Set<Integer> pressedKeys = new HashSet<>();
    private Timer movementTimer;

    private void addKeyBindings() {
        JRootPane rootPane = frame.getRootPane();
        InputMap im = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPane.getActionMap();

        // Disable focus so sliders don't capture arrow keys
        xzSlider.setFocusable(false);
        xySlider.setFocusable(false);

        // Key Press Actions
        int[] keys = {KeyEvent.VK_LEFT, KeyEvent.VK_A, KeyEvent.VK_RIGHT, KeyEvent.VK_D,
                    KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_DOWN, KeyEvent.VK_S};
        
        for (int key : keys) {
            im.put(KeyStroke.getKeyStroke(key, 0, false), "pressed-" + key);
            am.put("pressed-" + key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.add(key);
                    onUserInput();
                }
            });

            im.put(KeyStroke.getKeyStroke(key, 0, true), "released-" + key);
            am.put("released-" + key, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pressedKeys.remove(key);
                }
            });
        }

        // Timer to update sliders based on pressed keys
        movementTimer = new Timer(30, e -> {
            if (pressedKeys.contains(KeyEvent.VK_LEFT) || pressedKeys.contains(KeyEvent.VK_A)) {
                xzSlider.setValue(xzSlider.getValue() - 1);
            }
            if (pressedKeys.contains(KeyEvent.VK_RIGHT) || pressedKeys.contains(KeyEvent.VK_D)) {
                xzSlider.setValue(xzSlider.getValue() + 1);
            }
            if (pressedKeys.contains(KeyEvent.VK_UP) || pressedKeys.contains(KeyEvent.VK_W)) {
                xySlider.setValue(xySlider.getValue() + 1);
            }
            if (pressedKeys.contains(KeyEvent.VK_DOWN) || pressedKeys.contains(KeyEvent.VK_S)) {
                xySlider.setValue(xySlider.getValue() - 1);
            }
        });

        movementTimer.start();
    }

    private List<Polygon> inflate(List<Polygon> polys) {
        System.out.println("Inflating " + polys.size() + " polygons"); // Debug
        List<Polygon> result = new ArrayList<>();
        for (Polygon poly : polys) {
            List<Vertex> vertices = poly.vertex_array;
            Color color = poly.color;
            
            if (vertices.size() == 3) { // Triangle subdivision
                Vertex v1 = vertices.get(0);
                Vertex v2 = vertices.get(1);
                Vertex v3 = vertices.get(2);

                Vertex m1 = midPoint(v1, v2);
                Vertex m2 = midPoint(v2, v3);
                Vertex m3 = midPoint(v1, v3);

                result.add(new Polygon(new Vertex[]{v1, m1, m3}, color));
                result.add(new Polygon(new Vertex[]{v2, m1, m2}, color));
                result.add(new Polygon(new Vertex[]{v3, m2, m3}, color));
                result.add(new Polygon(new Vertex[]{m1, m2, m3}, color));
            } 
            else if (vertices.size() == 4) { // Square subdivision
                Vertex v1 = vertices.get(0);
                Vertex v2 = vertices.get(1);
                Vertex v3 = vertices.get(2);
                Vertex v4 = vertices.get(3);

                Vertex m1 = midPoint(v1, v2);
                Vertex m2 = midPoint(v2, v3);
                Vertex m3 = midPoint(v3, v4);
                Vertex m4 = midPoint(v4, v1);
                Vertex center = midPoint(m1, m3);

                result.add(new Polygon(new Vertex[]{v1, m1, center, m4}, color));
                result.add(new Polygon(new Vertex[]{m1, v2, m2, center}, color));
                result.add(new Polygon(new Vertex[]{center, m2, v3, m3}, color));
                result.add(new Polygon(new Vertex[]{m4, center, m3, v4}, color));
            }
        }

        // Normalize vertices
        double targetSize = Math.sqrt(30000);
        for (Polygon poly : result) {
            for (Vertex v : poly.vertex_array) {
                double length = Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
                if (length > 0) {
                    double scale = targetSize / length;
                    v.x *= scale;
                    v.y *= scale;
                    v.z *= scale;
                }
            }
        }
        System.out.println("Created " + result.size() + " inflated polygons"); // Debug
        return result;
    }

    private Vertex midPoint(Vertex a, Vertex b) {
        return new Vertex(
            (a.x + b.x)/2,
            (a.y + b.y)/2,
            (a.z + b.z)/2
        );
    }

    // Handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ExitButton) {
            System.exit(0);
        } 
        if (e.getSource() == ClickButton) {
            renderPanel.repaint();
            clicks++;
        } 
        if (e.getSource() == ChangeColorButton) {
            // renderPanel.repaint();
            colorCounter++;
            ChangeColorLabel.setText("Change Color:  " + colorCounter);
        } 
        if (e.getSource() == ResetButton) {
            clicks = 0;
            xzSlider.setValue(0);
            xySlider.setValue(0);
        } 
        if (e.getSource() == AutoRotateButton) {
            // Toggle auto-rotation state
            autoRotationEnabled = !autoRotationEnabled;
            // AutoRotateButton.setText(autoRotationEnabled ? "↺" : "▶");
            if (!autoRotationEnabled) {
                AutoRotateButton.setBackground(Color.RED);
                // Stop auto-rotation immediately when disabled
                autoRotateTimer.stop();
            } else {
                AutoRotateButton.setBackground(Color.GREEN);
                // If enabled and idle, start auto-rotation
                if (System.currentTimeMillis() - lastUserInputTime > IDLE_TIMEOUT) {
                    autoRotateTimer.start();
                }
            }
        }
        if (e.getSource() == ToggleWireframeButton) {
            renderPanel.repaint();
            // Toggle auto-rotation state
           ToggleWireframe  = !ToggleWireframe;
            // AutoRotateButton.setText(ToggleWireframe ? "↺" : "▶");
            if (!ToggleWireframe) {
            } else {
            }
            WireFramelabel.setText("Toggle WireFrame:  " + ToggleWireframe);
        }
        if (e.getSource() == InflateButton) {
            isInflated = !isInflated;
            System.out.println("Inflate toggled: " + isInflated); // Debug
            renderPanel.repaint();
        }
        label.setText("Number of clicks:  " + clicks);
    }

    //HERE LIES GPT

     private double[] isPointInsideTriangle(int px, int py, Vertex v1, Vertex v2, Vertex v3) {
        double areaOrig = triangleArea(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        double area1 = triangleArea(px, py, v2.x, v2.y, v3.x, v3.y);
        double area2 = triangleArea(v1.x, v1.y, px, py, v3.x, v3.y);
        double area3 = triangleArea(v1.x, v1.y, v2.x, v2.y, px, py);
        double arr[] = {areaOrig, area1, area2, area3};
        return arr;
    }

    private double triangleArea(double x1, double y1, double x2, double y2, double x3, double y3) {
        return Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
    }

    private Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return new Color(red, green, blue);
    }



    // Handle slider movement
    public void stateChanged(ChangeEvent e) {
        if (isAutoRotating) return; // Ignore auto-rotation updates
        
        if (e.getSource() == xzSlider) {
            // Directly set angle based on slider position
            totalRotationAngleXZ = xzSlider.getValue() * 5.0;
            xzLabel.setText("XZ Position: " + xzSlider.getValue());
            onUserInput(); // Stop auto-rotation
        }
        if (e.getSource() == xySlider) {
            totalRotationAngleXY = xySlider.getValue() * 5.0;
            xyLabel.setText("XY Position: " + xySlider.getValue());
            onUserInput(); // Stop auto-rotation
        }
    }    
    public static void main(String[] args) {
            new GUI();
        }
}

class Vertex {
    double x;
    double y;
    double z;
    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}



class Polygon {
    int number_of_sides = 3; //Defaut Triangle

    public List<Vertex> vertex_array = new ArrayList<>();

    Color color;
    Polygon(Vertex inp[], Color color) {
        for(Vertex v : inp){
            this.vertex_array.add(new Vertex(v.x, v.y, v.z));
        }
        number_of_sides = vertex_array.size();
        this.color = color;
    }
}

class CoordinateCreator {


    static List<Vertex[]> create_square_coords(int size){
        List<Vertex[]> square_coords = new ArrayList<>(); 
        int baseSize = (int)(size * 0.6);
                // Adding coordinates to square_coords
                square_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, baseSize), // window wall
                                                new Vertex(baseSize, baseSize, -baseSize),
                                                new Vertex(baseSize, -baseSize, -baseSize),
                                                new Vertex(baseSize, -baseSize, baseSize)});

                square_coords.add(new Vertex[]{new Vertex(-baseSize, baseSize, baseSize), // door wall
                                                new Vertex(-baseSize, baseSize, -baseSize),
                                                new Vertex(-baseSize, -baseSize, -baseSize),
                                                new Vertex(-baseSize, -baseSize, baseSize)});

                square_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, baseSize), // bathroom wall
                                                new Vertex(baseSize, -baseSize, baseSize),
                                                new Vertex(-baseSize, -baseSize, baseSize),
                                                new Vertex(-baseSize, baseSize, baseSize)});

                square_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, -baseSize), // opposite to bathroom
                                                new Vertex(baseSize, -baseSize, -baseSize),
                                                new Vertex(-baseSize, -baseSize, -baseSize),
                                                new Vertex(-baseSize, baseSize, -baseSize)});

                square_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, baseSize), // top
                                                new Vertex(baseSize, baseSize, -baseSize),
                                                new Vertex(-baseSize, baseSize, -baseSize),
                                                new Vertex(-baseSize, baseSize, baseSize)});

                square_coords.add(new Vertex[]{new Vertex(baseSize, -baseSize, baseSize), // bottom
                                                new Vertex(baseSize, -baseSize, -baseSize),
                                                new Vertex(-baseSize, -baseSize, -baseSize),
                                                new Vertex(-baseSize, -baseSize, baseSize)});


                return square_coords;

        
    }

    static List<Vertex[]> create_triangle_coords(int size){

        List<Vertex[]> Tetrahedron_coords = new ArrayList<>();
        int baseSize = (int)(size * 0.6);
        // Adding coordinates to Tetrahedron_coords
        Tetrahedron_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, baseSize),
                                        new Vertex(-baseSize, -baseSize, baseSize),
                                        new Vertex(-baseSize, baseSize, -baseSize)});

        Tetrahedron_coords.add(new Vertex[]{new Vertex(baseSize, baseSize, baseSize),
                                        new Vertex(-baseSize, -baseSize, baseSize),
                                        new Vertex(baseSize, -baseSize, -baseSize)});

        Tetrahedron_coords.add(new Vertex[]{new Vertex(-baseSize, baseSize, -baseSize),
                                        new Vertex(baseSize, -baseSize, -baseSize),
                                        new Vertex(baseSize, baseSize, baseSize)});

        Tetrahedron_coords.add(new Vertex[]{new Vertex(-baseSize, baseSize, -baseSize),
                                        new Vertex(baseSize, -baseSize, -baseSize),
                                        new Vertex(-baseSize, -baseSize, baseSize)});
        return Tetrahedron_coords;
    }


        static List<Vertex[]> create_icosahedron_coords(int size){
        List<Vertex[]> icosahedron_coords = new ArrayList<>();
        double phi = (1 + Math.sqrt(5)) / 2; // Golden ratio
        int baseSize = (int)(size * 0.6);
        
        // Defining the 12 vertices of an icosahedron
        Vertex[] vertices = new Vertex[]{
            new Vertex(-baseSize, phi * baseSize, 0), new Vertex(baseSize, phi * baseSize, 0),
            new Vertex(-baseSize, -phi * baseSize, 0), new Vertex(baseSize, -phi * baseSize, 0),
            new Vertex(0, -baseSize, phi * baseSize), new Vertex(0, baseSize, phi * baseSize),
            new Vertex(0, -baseSize, -phi * baseSize), new Vertex(0, baseSize, -phi * baseSize),
            new Vertex(phi * baseSize, 0, -baseSize), new Vertex(phi * baseSize, 0, baseSize),
            new Vertex(-phi * baseSize, 0, -baseSize), new Vertex(-phi * baseSize, 0, baseSize)
        };
        
        // Defining the 20 triangular faces
        int[][] faces = {
            {0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
            {1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
            {3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
            {4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
        };
        
        // Adding faces to icosahedron_coords
        for (int[] face : faces) {
            icosahedron_coords.add(new Vertex[]{vertices[face[0]], vertices[face[1]], vertices[face[2]]});
        }
        
        return icosahedron_coords;
    }

    static List<Vertex[]> create_octahedron_coords(int size) {
    List<Vertex[]> octaFaces = new ArrayList<>();
    int baseSize = (int)(size * 0.6);

    // Define vertices of the octahedron.
    Vertex top    = new Vertex(0, 0, baseSize);
    Vertex bottom = new Vertex(0, 0, -baseSize);
    Vertex right  = new Vertex(baseSize, 0, 0);
    Vertex left   = new Vertex(-baseSize, 0, 0);
    Vertex front  = new Vertex(0, baseSize, 0);
    Vertex back   = new Vertex(0, -baseSize, 0);

    // Define the 8 triangular faces.
    octaFaces.add(new Vertex[]{ top, front, right });
    octaFaces.add(new Vertex[]{ top, right, back });
    octaFaces.add(new Vertex[]{ top, back, left });
    octaFaces.add(new Vertex[]{ top, left, front });
    octaFaces.add(new Vertex[]{ bottom, right, front });
    octaFaces.add(new Vertex[]{ bottom, back, right });
    octaFaces.add(new Vertex[]{ bottom, left, back });
    octaFaces.add(new Vertex[]{ bottom, front, left });
    
    return octaFaces;
}

static List<Vertex[]> create_torus_coords(int size) {
    List<Vertex[]> polygons = new ArrayList<>();
    int baseSize = (int)(size * 0.6);
    int segMajor = 20, segMinor = 20; // resolutions along major and minor circles
    double R = baseSize;      // Major radius
    double r = baseSize / 2.0;  // Minor radius

    double majorStep = 2 * Math.PI / segMajor;
    double minorStep = 2 * Math.PI / segMinor;

    // Create a grid of vertices.
    Vertex[][] grid = new Vertex[segMajor][segMinor];
    for (int i = 0; i < segMajor; i++) {
        double phi = i * majorStep;
        for (int j = 0; j < segMinor; j++) {
            double theta = j * minorStep;
            double x = (R + r * Math.cos(theta)) * Math.cos(phi);
            double y = (R + r * Math.cos(theta)) * Math.sin(phi);
            double z = r * Math.sin(theta);
            grid[i][j] = new Vertex(x, y, z);
        }
    }

    // Build quadrilateral faces from the grid, wrapping indices as needed.
    for (int i = 0; i < segMajor; i++) {
        int nextI = (i + 1) % segMajor;
        for (int j = 0; j < segMinor; j++) {
            int nextJ = (j + 1) % segMinor;
            // Quad face: grid[i][j], grid[nextI][j], grid[nextI][nextJ], grid[i][nextJ]
            polygons.add(new Vertex[]{
                grid[i][j],
                grid[nextI][j],
                grid[nextI][nextJ],
                grid[i][nextJ]
            });
        }
    }

    return polygons;
}


    


}





class Matrix3 {
    double[] values;
    Matrix3(double[] values) {
        this.values = values;
    }
    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] +=
                        this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }
    Vertex transform(Vertex in) {
        return new Vertex(
            in.x * values[0] + in.y * values[3] + in.z * values[6],
            in.x * values[1] + in.y * values[4] + in.z * values[7],
            in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
}
