
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI implements ActionListener, ChangeListener {

    private final int size = 200;
    private int clicks = 0, colorCounter = 0;
    private int INFLATE_COUNTER = 1;
    private JLabel label = new JLabel("Clicks:  0");
    private JLabel WireFramelabel = new JLabel("Toggle WireFrame:  0");
    private JLabel ChangeColorLabel = new JLabel("Change Color:  0");
    private JLabel AutoRotateLabel = new JLabel("Toggle Auto Rotate (T)");
    private JLabel MoveInstructionsLabel = new JLabel("Move (WASD or Arrow Keys)");
    private JLabel ShapesListLabel;
    private JFrame frame;
    private JButton ClickButton, ResetButton, ExitButton, AutoRotateButton, ToggleWireframeButton, ChangeColorButton;
    private JComboBox<String> ShapesListBox;
    private JLabel xzLabel, xyLabel;
    private JSlider xzSlider, xySlider;
    private JPanel renderPanel, xzPanel, xyPanel;
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

    private boolean wasAutoRotating = false;

    // Shapes list
    public String[] ShapesList = {"TetraHedron", "Cube", "Sphere", "Octahedron", "Icosahedron", "Torus", "Mobius Strip", "DNA", "Tesseract", "Saturn"};

    public GUI() {
        frame = new JFrame("Main Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setUndecorated(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);

        frame.getContentPane().setBackground(Color.BLACK);

        // Click, Reset, and Exit Buttons
        ClickButton = new JButton("Toggle Shape (Q/E)");
        ResetButton = new JButton("Reset (R)");
        ExitButton = new JButton("Exit Application (Esc)");
        ToggleWireframeButton = new JButton("Toggle WireFrame (F)");
        ChangeColorButton = new JButton("Change Color (C)");
        InflateButton = new JButton("Inflate (I)");
        AutoRotateButton = new JButton("");
        Dimension buttonSize = new Dimension(screenSize.width / 6, screenSize.width / 50);
        ClickButton.setPreferredSize(new Dimension(buttonSize.width, buttonSize.height * 3 / 4));
        ResetButton.setPreferredSize(buttonSize);
        ExitButton.setPreferredSize(buttonSize);
        ChangeColorButton.setPreferredSize(buttonSize);
        ToggleWireframeButton.setPreferredSize(buttonSize);
        InflateButton.setPreferredSize(buttonSize);
        AutoRotateButton.setPreferredSize(new Dimension(20, 20)); // Keep AutoRotate small

        ClickButton.setBackground(Color.LIGHT_GRAY);
        ResetButton.setBackground(Color.LIGHT_GRAY);
        ToggleWireframeButton.setBackground(Color.LIGHT_GRAY);
        ChangeColorButton.setBackground(Color.LIGHT_GRAY);
        AutoRotateButton.setBackground(Color.GREEN);
        InflateButton.setBackground(Color.LIGHT_GRAY);
        ExitButton.setBackground(Color.RED);

        AutoRotateButton.addActionListener(this);
        // AutoRotateButton.setText("↺");

        // Shapes Drop Down list
        ShapesListBox = new JComboBox<>(ShapesList);
        ShapesListBox.setPreferredSize(new Dimension(buttonSize.width, buttonSize.height / 2));
        ShapesListBox.setMinimumSize(new Dimension(buttonSize.width, buttonSize.height / 2));
        ShapesListBox.setMaximumSize(new Dimension(buttonSize.width, buttonSize.height / 2));

        ShapesListLabel = new JLabel("Select Shape:");
        ShapesListBox.setBackground(Color.LIGHT_GRAY);
        ShapesListBox.setForeground(Color.BLACK);
        ShapesListBox.setFont(new Font("Arial", Font.BOLD, 14)); // Adjust font size and style
        ((JLabel) ShapesListBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        //Action Listeners
        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);
        ToggleWireframeButton.addActionListener(this);
        InflateButton.addActionListener(this);
        ChangeColorButton.addActionListener(this);
        ShapesListBox.addActionListener(this);
        // Sliders for XZ and XY movement
        xzSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        xySlider = new JSlider(JSlider.VERTICAL, -0, 100, 50);

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

        ShapesListBox.addActionListener(e -> {
            ShapesListBox.setFocusable(false);  // Remove focus so keybindings work separately
            frame.requestFocusInWindow();  // Give focus back to the main window
        });

        inputMap.put(KeyStroke.getKeyStroke("E"), "forwardToggleShape");
        inputMap.put(KeyStroke.getKeyStroke("Q"), "backwardToggleShape");
        inputMap.put(KeyStroke.getKeyStroke("R"), "reset");
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "exitApp");
        inputMap.put(KeyStroke.getKeyStroke("F"), "toggleWireframe");
        inputMap.put(KeyStroke.getKeyStroke("T"), "toggleAutoRotate");
        inputMap.put(KeyStroke.getKeyStroke("pressed SHIFT"), "pauseAutoRotate");
        inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), "resumeAutoRotate");
        inputMap.put(KeyStroke.getKeyStroke("C"), "changeColor");
        inputMap.put(KeyStroke.getKeyStroke("I"), "inflate");

        actionMap.put("inflate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InflateButton.doClick();
            }
        });

        actionMap.put("forwardToggleShape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renderPanel.repaint();
                // Cycle through shapes
                clicks = (clicks + 1) % ShapesList.length;
                ShapesListBox.setSelectedIndex(clicks); // update dropdown selection
            }
        });
        actionMap.put("backwardToggleShape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renderPanel.repaint();
                // Cycle through shapes
                clicks = (clicks - 1 + ShapesList.length) % ShapesList.length;
                ShapesListBox.setSelectedIndex(clicks); // update dropdown selection
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

        // FIX THISSSS!!!!!!!!!!!!!!!!!
        {
            actionMap.put("pauseAutoRotate", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (autoRotationEnabled) { // If auto-rotate was ON, store its state
                        wasAutoRotating = true;
                        autoRotateTimer.stop();
                    }
                }
            });

            // Action when SHIFT is released (Resume Auto-Rotate if it was ON)
            actionMap.put("resumeAutoRotate", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (wasAutoRotating) { // If auto-rotate was ON before Shift, resume it
                        autoRotateTimer.start();
                    }
                    wasAutoRotating = false; // Reset state
                }
            });
        }

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
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Rendering magic will happen here
                List<Polygon> polygon_list = new ArrayList<>();

                List<Vertex[]> Shape_Coords;

                //List for all shapes coords creator
                List<Supplier<List<Vertex[]>>> shapeFunctions = Arrays.asList(
                        () -> CoordinateCreator.create_triangle_coords(size),
                        () -> CoordinateCreator.create_square_coords(size),
                        () -> CoordinateCreator.create_sphere_coords(WIDTH * 150, SOMEBITS * 3),
                        () -> CoordinateCreator.create_octahedron_coords(size * 2),
                        () -> CoordinateCreator.create_icosahedron_coords(size),
                        () -> CoordinateCreator.create_torus_coords(size),
                        () -> CoordinateCreator.create_mobius_strip_coords(size * 2),
                        () -> CoordinateCreator.create_dna_coords(size),
                        () -> CoordinateCreator.create_tesseract_coords(size),
                        () -> CoordinateCreator.create_saturn_coords(size)
                );

                // Get the shape based on the number of clicks
                // Adding coordinates to Shape_Coords
                Shape_Coords = shapeFunctions.get(clicks % shapeFunctions.size()).get();

                //Colors
                Color colorArr[] = {new Color(205, 180, 219), new Color(255, 200, 221), new Color(255, 175, 204), new Color(189, 224, 254), new Color(162, 210, 255), new Color(202, 240, 248)};
                Color colorArr2[] = {new Color(255, 173, 173), new Color(255, 214, 165), new Color(253, 255, 182), new Color(202, 255, 191), new Color(155, 246, 255), new Color(160, 196, 255), new Color(189, 178, 255)};

                Color Colors[][] = {colorArr, colorArr2};

                int inx = 0;
                for (Vertex[] coords : Shape_Coords) {

                    polygon_list.add(new Polygon(coords, Colors[colorCounter % 2][inx % Colors[colorCounter % 2].length]));
                    inx++;
                }

                if (isInflated) {
                    for (int i = 0; i < INFLATE_COUNTER; i++) {
                        polygon_list = inflate(polygon_list);
                    }
                }

                double heading = Math.toRadians(totalRotationAngleXZ); // Use tracked angle
                double pitch = Math.toRadians(totalRotationAngleXY);    // Use tracked angle

                Matrix3 headingTransform = new Matrix3(new double[]{
                    Math.cos(heading), 0, Math.sin(heading),
                    0, 1, 0,
                    -Math.sin(heading), 0, Math.cos(heading)
                });

                Matrix3 pitchTransform = new Matrix3(new double[]{
                    1, 0, 0,
                    0, Math.cos(pitch), Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });

                Matrix3 transform = headingTransform.multiply(pitchTransform);
                if (ToggleWireframe) {
                    g2.translate(getWidth() / 2, getHeight() / 2);
                    g2.setColor(Color.WHITE);

                    for (Polygon poly : polygon_list) {

                        for (int i = 0; i < poly.number_of_sides; i++) {

                            poly.vertex_array.set(i, transform.transform(poly.vertex_array.get(i)));
                        }

                        Path2D path = new Path2D.Double();
                        Vertex prevVertex = poly.vertex_array.get(0);
                        for (Vertex v : poly.vertex_array) {
                            path.moveTo(prevVertex.x, prevVertex.y);
                            path.lineTo(v.x, v.y);
                            prevVertex = v;
                        }
                        path.moveTo(prevVertex.x, prevVertex.y);
                        path.lineTo(poly.vertex_array.get(0).x, poly.vertex_array.get(0).y);

                        path.closePath();
                        g2.draw(path);
                    }
                } else {

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
                                    rotated.x + getWidth() / 2,
                                    rotated.y + getHeight() / 2,
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
                            norm.x = 0;
                            norm.y = 0;
                            norm.z = 0;
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
                        maxX = Math.min(img.getWidth() - 1, (int) Math.floor(maxX));
                        minY = Math.max(0, (int) Math.ceil(minY));
                        maxY = Math.min(img.getHeight() - 1, (int) Math.floor(maxY));

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
                                        double b1 = inside[1] / inside[0], b2 = inside[2] / inside[0], b3 = inside[3] / inside[0];
                                        depth = b1 * poly.vertex_array.get(0).z + b2 * poly.vertex_array.get(1).z + b3 * poly.vertex_array.get(2).z;
                                    } else {
                                        double b1 = inside2[1] / inside2[0], b2 = inside2[2] / inside2[0], b3 = inside2[3] / inside2[0];
                                        depth = b1 * poly.vertex_array.get(0).z + b2 * poly.vertex_array.get(2).z + b3 * poly.vertex_array.get(3).z;
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
        // Add change listeners for wrap-around effect
        xzSlider.addChangeListener(e -> {
            // int value = xzSlider.getValue();
            // xzSlider.setValue((value + 101) % 101); // Wraps around using mod
            renderPanel.repaint();
        });

        xySlider.addChangeListener(e -> {
            // int value = xySlider.getValue();
            // xySlider.setValue((value + 101) % 101);
            renderPanel.repaint();
        });

        renderPanel.setLayout(new BorderLayout());
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                xzSlider.setPreferredSize(new Dimension(renderPanel.getWidth(), 30));
                xySlider.setPreferredSize(new Dimension(30, renderPanel.getHeight()));
                renderPanel.revalidate();
            }
        });

        // Panel for buttons (RIGHT SIDE)
        // 1) Your main button panel with GridLayout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(6, 1, 20, 20));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        buttonPanel.setBackground(Color.GRAY);
        // Create a sub-panel for Toggle Shape and the dropdown with a 1:3 ratio vertically
        JPanel shapePanel = new JPanel(new GridBagLayout());
        shapePanel.setBackground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // Add the Toggle Shape button (1 part)
        gbc.gridy = 0;
        gbc.weighty = 3;  // 1/4 of the height
        shapePanel.add(ClickButton, gbc);

        // Add the dropdown (3 parts)
        gbc.gridy = 1;
        gbc.weighty = 1;  // 3/4 of the height
        shapePanel.add(ShapesListBox, gbc);

        // 3) Add all components to buttonPanel
        // First cell: shapePanel (toggle button + dropdown)
        buttonPanel.add(shapePanel);

        // Next cells: your other buttons
        buttonPanel.add(ToggleWireframeButton);
        buttonPanel.add(ChangeColorButton);
        buttonPanel.add(ResetButton);
        buttonPanel.add(InflateButton);
        buttonPanel.add(ExitButton);

        // Panel for labels (TOP)
        // AutoRotateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        MoveInstructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        topPanel.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7)); // Padding
        topPanel.add(MoveInstructionsLabel);
        topPanel.add(AutoRotateLabel);
        // topPanel.add(label);
        // topPanel.add(WireFramelabel);
        // topPanel.add(ChangeColorLabel);
        // topPanel.add(ShapesListLabel);
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
        blankPanel.setPreferredSize(new Dimension(screenSize.width / 6 + 40, 30));

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
            xzSlider.setValue((int) ((totalRotationAngleXZ / 5) % 100));
            xySlider.setValue((int) ((totalRotationAngleXY / 5) % 100));

            isAutoRotating = false;
            renderPanel.repaint();
        });

        idleCheckTimer = new Timer(1000, e -> {
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
            } else if (vertices.size() == 4) { // Square subdivision
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
                double length = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
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
                (a.x + b.x) / 2,
                (a.y + b.y) / 2,
                (a.z + b.z) / 2
        );
    }

    // Handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ExitButton) {
            System.exit(0);
        }
        if (e.getSource() == ClickButton) {
            renderPanel.repaint();
            // Cycle through shapes
            clicks = (clicks + 1) % ShapesList.length;
            ShapesListBox.setSelectedIndex(clicks); // update dropdown selection
        }
        if (e.getSource() == ChangeColorButton) {
            // renderPanel.repaint();
            colorCounter++;
            ChangeColorLabel.setText("Change Color:  " + colorCounter);
        }
        if (e.getSource() == ResetButton) {
            // clicks = 0;
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
            ToggleWireframe = !ToggleWireframe;
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
        label.setText("Clicks:  " + clicks);
        if (e.getSource() == ShapesListBox) {
            String selectedShape = (String) ShapesListBox.getSelectedItem();

            if (selectedShape.equals("TetraHedron")) {
                clicks = 0;
            } else if (selectedShape.equals("Cube")) {
                clicks = 1;
            } else if (selectedShape.equals("Sphere")) {
                clicks = 2;
            } else if (selectedShape.equals("Octahedron")) {
                clicks = 3;
            } else if (selectedShape.equals("Icosahedron")) {
                clicks = 4;
            } else if (selectedShape.equals("Torus")) {
                clicks = 5;
            } else if (selectedShape.equals("Mobius Strip")) {
                clicks = 6;
            } else if (selectedShape.equals("DNA")) {
                clicks = 7;
            } else if (selectedShape.equals("Tesseract")) {
                clicks = 8;
            } else if (selectedShape.equals("Saturn")) {
                clicks = 9;
            }

            renderPanel.repaint(); // Refresh to display selected shape
        }
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

        int red = (int) Math.pow(redLinear, 1 / 2.4);
        int green = (int) Math.pow(greenLinear, 1 / 2.4);
        int blue = (int) Math.pow(blueLinear, 1 / 2.4);

        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return new Color(red, green, blue);
    }

    // Handle slider movement
    public void stateChanged(ChangeEvent e) {
        if (isAutoRotating) {
            return; // Ignore auto-rotation updates
        }
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
        for (Vertex v : inp) {
            this.vertex_array.add(new Vertex(v.x, v.y, v.z));
        }
        number_of_sides = vertex_array.size();
        this.color = color;
    }
}

class CoordinateCreator {

    static List<Vertex[]> create_square_coords(int size) {
        List<Vertex[]> square_coords = new ArrayList<>();
        int baseSize = (int) (size * 0.6);
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

    static List<Vertex[]> create_triangle_coords(int size) {

        List<Vertex[]> Tetrahedron_coords = new ArrayList<>();
        int baseSize = (int) (size * 0.6);
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

    static List<Vertex[]> create_icosahedron_coords(int size) {
        List<Vertex[]> icosahedron_coords = new ArrayList<>();
        double phi = (1 + Math.sqrt(5)) / 2; // Golden ratio
        int baseSize = (int) (size * 0.6);

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
        int baseSize = (int) (size * 0.6);

        // Define vertices of the octahedron.
        Vertex top = new Vertex(0, 0, baseSize);
        Vertex bottom = new Vertex(0, 0, -baseSize);
        Vertex right = new Vertex(baseSize, 0, 0);
        Vertex left = new Vertex(-baseSize, 0, 0);
        Vertex front = new Vertex(0, baseSize, 0);
        Vertex back = new Vertex(0, -baseSize, 0);

        // Define the 8 triangular faces.
        octaFaces.add(new Vertex[]{top, front, right});
        octaFaces.add(new Vertex[]{top, right, back});
        octaFaces.add(new Vertex[]{top, back, left});
        octaFaces.add(new Vertex[]{top, left, front});
        octaFaces.add(new Vertex[]{bottom, right, front});
        octaFaces.add(new Vertex[]{bottom, back, right});
        octaFaces.add(new Vertex[]{bottom, left, back});
        octaFaces.add(new Vertex[]{bottom, front, left});

        return octaFaces;
    }

    static List<Vertex[]> create_tesseract_coords(int size) {
        List<Vertex[]> tesseractFaces = new ArrayList<>();

        int halfSize = size / 2;

        // Define the 16 vertices (Projecting from 4D to 3D)
        Vertex[] vertices = new Vertex[16];

        // 8 vertices for the first cube (w = -1)
        for (int i = 0; i < 8; i++) {
            int x = (i & 1) == 0 ? -halfSize : halfSize;
            int y = (i & 2) == 0 ? -halfSize : halfSize;
            int z = (i & 4) == 0 ? -halfSize : halfSize;
            vertices[i] = new Vertex(x, y, z);
        }

        // 8 vertices for the second cube (w = +1)
        for (int i = 0; i < 8; i++) {
            vertices[i + 8] = new Vertex(vertices[i].x * 0.7, vertices[i].y * 0.7, vertices[i].z * 0.7);
        }

        // Connect corresponding vertices between two cubes
        for (int i = 0; i < 8; i++) {
            tesseractFaces.add(new Vertex[]{vertices[i], vertices[i + 8]});
        }

        // Connect edges within both cubes
        int[] edges = {0, 1, 0, 2, 0, 4, 1, 3, 1, 5, 2, 3, 2, 6, 3, 7,
            4, 5, 4, 6, 5, 7, 6, 7};
        for (int i = 0; i < edges.length; i += 2) {
            tesseractFaces.add(new Vertex[]{vertices[edges[i]], vertices[edges[i + 1]]});
            tesseractFaces.add(new Vertex[]{vertices[edges[i] + 8], vertices[edges[i + 1] + 8]});
        }

        return tesseractFaces;
    }

    static List<Vertex[]> create_torus_coords(int size) {
        List<Vertex[]> polygons = new ArrayList<>();
        int baseSize = (int) (size * 0.6);
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

    static List<Vertex[]> create_mobius_strip_coords(int size) {
        List<Vertex[]> polygons = new ArrayList<>();
        int segU = 40; // resolution along the u-direction (around the circle)
        int segV = 10; // resolution along the v-direction (across the width)
        double R = size * 0.6;  // major radius (circle center)
        double w = R / 3.0;     // half-width of the strip (you can adjust this)
        double du = 2 * Math.PI / segU;
        double dv = 2 * w / segV;  // v runs from -w to +w

        // Create a grid of vertices.
        // We store segU rows (for different u values) and segV+1 columns (to include both ends of v).
        Vertex[][] grid = new Vertex[segU][segV + 1];
        for (int i = 0; i < segU; i++) {
            double u = i * du;
            for (int j = 0; j <= segV; j++) {
                double v = -w + j * dv;
                double x = (R + v * Math.cos(u / 2)) * Math.cos(u);
                double y = (R + v * Math.cos(u / 2)) * Math.sin(u);
                double z = v * Math.sin(u / 2);
                grid[i][j] = new Vertex(x, y, z);
            }
        }

        // Create quadrilateral faces from the grid.
        // For each face, the vertices are chosen from adjacent grid points.
        // The twist appears when wrapping around from the last u-segment to the first.
        for (int i = 0; i < segU; i++) {
            int nextI = (i + 1) % segU; // For the last row, nextI wraps around to 0.
            for (int j = 0; j < segV; j++) {
                // Standard case: if we're not wrapping (i < segU-1), use grid as-is.
                Vertex v00 = grid[i][j];
                Vertex v01 = grid[i][j + 1];
                Vertex v10, v11;
                if (i != segU - 1) {
                    v10 = grid[nextI][j];
                    v11 = grid[nextI][j + 1];
                } else {
                    // For the wrap-around row, apply the twist.
                    // At u = 2π (i = segU - 1), a point at v corresponds to a point at u = 0 with -v.
                    // We simulate this by reversing the v-index for the wrap-around row.
                    v10 = grid[nextI][segV - j];
                    v11 = grid[nextI][segV - (j + 1)];
                }
                // Add the quadrilateral face.
                polygons.add(new Vertex[]{v00, v01, v11, v10});
            }
        }
        return polygons;
    }

    static List<Vertex[]> create_dna_coords(int size) {
        List<Vertex[]> dnaCoords = new ArrayList<>();
        double radius = size * 0.3; // Radius of the DNA strands
        double height = size * 10; // Total height of the DNA
        int numTurns = 10; // Fixed number of turns for proportionate scaling
        int numBasePairs = numTurns * 10; // Proportional base pairs count

        int numSteps = numBasePairs * 2; // Steps for smooth curves
        double angleStep = (2 * Math.PI * numTurns) / numSteps; // Rotation per step
        double zStep = height / numSteps; // Height per step

        Vertex[] strandA = new Vertex[numSteps + 1];
        Vertex[] strandB = new Vertex[numSteps + 1];

        // Generate two helices
        for (int i = 0; i <= numSteps; i++) {
            double angle = i * angleStep;
            double z = i * zStep - (height / 2); // Center DNA at origin

            double xA = radius * Math.cos(angle);
            double yA = radius * Math.sin(angle);
            strandA[i] = new Vertex(xA, yA, z);

            double xB = radius * Math.cos(angle + Math.PI);
            double yB = radius * Math.sin(angle + Math.PI);
            strandB[i] = new Vertex(xB, yB, z);
        }

        // Add helices to DNA structure
        for (int i = 0; i < numSteps; i++) {
            dnaCoords.add(new Vertex[]{strandA[i], strandA[i + 1]});
            dnaCoords.add(new Vertex[]{strandB[i], strandB[i + 1]});
        }

        // Generate base pairs
        for (int i = 0; i < numSteps; i += 2) { // Every second step
            dnaCoords.add(new Vertex[]{strandA[i], strandB[i]});
        }

        return dnaCoords;
    }

    static List<Vertex[]> create_saturn_coords(int size) {
        List<Vertex[]> saturnShape = new ArrayList<>();

        int planetRadius = size / 2;
        int ringMajorRadius = (int) (size * 1);  // Wider ring
        int ringMinorRadius = (int) (size * 0.1); // Much thinner ring

        int sphereSegments = 24;  // Smoother sphere
        int ringSegments = 64;    // Higher resolution for smooth rings

        // Generate Sphere (Planet)
        for (int i = 0; i < sphereSegments; i++) {
            for (int j = 0; j < sphereSegments; j++) {
                double theta1 = 2 * Math.PI * i / sphereSegments;
                double theta2 = 2 * Math.PI * (i + 1) / sphereSegments;
                double phi1 = Math.PI * j / sphereSegments;
                double phi2 = Math.PI * (j + 1) / sphereSegments;

                Vertex v1 = new Vertex(
                        planetRadius * Math.cos(theta1) * Math.sin(phi1),
                        planetRadius * Math.sin(theta1) * Math.sin(phi1),
                        planetRadius * Math.cos(phi1)
                );
                Vertex v2 = new Vertex(
                        planetRadius * Math.cos(theta2) * Math.sin(phi1),
                        planetRadius * Math.sin(theta2) * Math.sin(phi1),
                        planetRadius * Math.cos(phi1)
                );
                Vertex v3 = new Vertex(
                        planetRadius * Math.cos(theta1) * Math.sin(phi2),
                        planetRadius * Math.sin(theta1) * Math.sin(phi2),
                        planetRadius * Math.cos(phi2)
                );
                Vertex v4 = new Vertex(
                        planetRadius * Math.cos(theta2) * Math.sin(phi2),
                        planetRadius * Math.sin(theta2) * Math.sin(phi2),
                        planetRadius * Math.cos(phi2)
                );

                saturnShape.add(new Vertex[]{v1, v2, v3});
                saturnShape.add(new Vertex[]{v3, v2, v4});
            }
        }

        // Generate a Flatter Torus (Rings)
        for (int i = 0; i < ringSegments; i++) {
            for (int j = 0; j < ringSegments; j++) {
                double theta1 = 2 * Math.PI * i / ringSegments;
                double theta2 = 2 * Math.PI * (i + 1) / ringSegments;
                double phi1 = 2 * Math.PI * j / ringSegments;
                double phi2 = 2 * Math.PI * (j + 1) / ringSegments;

                // Keep Z value very small to flatten the torus
                Vertex v1 = new Vertex(
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta1)) * Math.cos(phi1),
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta1)) * Math.sin(phi1),
                        ringMinorRadius * 0.2 * Math.sin(theta1) // Flattened Z
                );
                Vertex v2 = new Vertex(
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta2)) * Math.cos(phi1),
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta2)) * Math.sin(phi1),
                        ringMinorRadius * 0.2 * Math.sin(theta2) // Flattened Z
                );
                Vertex v3 = new Vertex(
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta1)) * Math.cos(phi2),
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta1)) * Math.sin(phi2),
                        ringMinorRadius * 0.2 * Math.sin(theta1) // Flattened Z
                );
                Vertex v4 = new Vertex(
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta2)) * Math.cos(phi2),
                        (ringMajorRadius + ringMinorRadius * Math.cos(theta2)) * Math.sin(phi2),
                        ringMinorRadius * 0.2 * Math.sin(theta2) // Flattened Z
                );

                saturnShape.add(new Vertex[]{v1, v2, v3});
                saturnShape.add(new Vertex[]{v3, v2, v4});
            }
        }

        return saturnShape;
    }

    static List<Vertex[]> create_sphere_coords(int radius, int segments) {
        List<Vertex[]> sphereFaces = new ArrayList<>();

        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < segments; j++) {
                double theta1 = 2 * Math.PI * i / segments;
                double theta2 = 2 * Math.PI * (i + 1) / segments;
                double phi1 = Math.PI * j / segments;
                double phi2 = Math.PI * (j + 1) / segments;

                Vertex v1 = new Vertex(
                        radius * Math.cos(theta1) * Math.sin(phi1),
                        radius * Math.sin(theta1) * Math.sin(phi1),
                        radius * Math.cos(phi1)
                );
                Vertex v2 = new Vertex(
                        radius * Math.cos(theta2) * Math.sin(phi1),
                        radius * Math.sin(theta2) * Math.sin(phi1),
                        radius * Math.cos(phi1)
                );
                Vertex v3 = new Vertex(
                        radius * Math.cos(theta1) * Math.sin(phi2),
                        radius * Math.sin(theta1) * Math.sin(phi2),
                        radius * Math.cos(phi2)
                );
                Vertex v4 = new Vertex(
                        radius * Math.cos(theta2) * Math.sin(phi2),
                        radius * Math.sin(theta2) * Math.sin(phi2),
                        radius * Math.cos(phi2)
                );

                // Create two triangles per quad
                sphereFaces.add(new Vertex[]{v1, v2, v3});
                sphereFaces.add(new Vertex[]{v3, v2, v4});
            }
        }

        return sphereFaces;
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
                    result[row * 3 + col]
                            += this.values[row * 3 + i] * other.values[i * 3 + col];
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

