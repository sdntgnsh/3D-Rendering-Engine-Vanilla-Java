import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class GUI implements ActionListener, ChangeListener {
    private int clicks = 0;
    private JLabel label = new JLabel("Number of clicks:  0");
    private JLabel WireFramelabel = new JLabel("Toggle WireFrame:  0");
    private JFrame frame;
    private JButton ClickButton, ResetButton, ExitButton, AutoRotateButton, ToggleWireframeButton;
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
        AutoRotateButton = new JButton("");
        Dimension buttonSize = new Dimension(screenSize.width/6, screenSize.width/50);
        ClickButton.setPreferredSize(buttonSize);
        ResetButton.setPreferredSize(buttonSize);
        ExitButton.setPreferredSize(buttonSize);
        ToggleWireframeButton.setPreferredSize(buttonSize);
        AutoRotateButton.setPreferredSize(new Dimension(20, 20));

        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);
        ToggleWireframeButton.addActionListener(this);

        ClickButton.setBackground(Color.LIGHT_GRAY);
        ResetButton.setBackground(Color.LIGHT_GRAY);
        ToggleWireframeButton.setBackground(Color.LIGHT_GRAY);
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
        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "toggleAutoRotate");
        
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
                if(clicks % 2 == 0){
                    Shape_Coords = CoordinateCreator.create_triangle_coords(100);
                }
                else{
                    Shape_Coords = CoordinateCreator.create_square_coords(100);
                }
                Color colorArr[] = {new Color(205, 180, 219), new Color(255, 200, 221), new Color(255, 175, 204), new Color(189, 224, 254), new Color(162, 210, 255), new Color(202, 240, 248)};
                Color colorArr2[] = {new Color(255, 173, 173), new Color(255, 214, 165), new Color(253, 255, 182), new Color(202, 255, 191),new Color(155, 246, 255),new Color(160, 196, 255), new Color(189, 178, 255)};

                int inx  = 0;
                for(Vertex[] coords : Shape_Coords){
                    polygon_list.add(new Polygon(coords, colorArr2[inx % colorArr2.length]));
                    inx++;
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
                    int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                    Arrays.fill(pixels, 0); // Clear to transparent
                    
                    double[] zBuffer = new double[pixels.length];
                    Arrays.fill(zBuffer, Double.NEGATIVE_INFINITY);

                    // Create a thread pool with optimal threads for CPU
                    int processors = Runtime.getRuntime().availableProcessors();
                    ExecutorService executor = Executors.newFixedThreadPool(processors);
                    
                    List<Future<?>> futures = new ArrayList<>();

                    for (Polygon poly : polygon_list) {
                        // Pre-transform all vertices first
                        List<Vertex> transformed = new ArrayList<>(poly.number_of_sides);
                        for (int i = 0; i < poly.number_of_sides; i++) {
                            Vertex v = transform.transform(poly.vertex_array.get(i));
                            transformed.add(new Vertex(
                                v.x + getWidth()/2, 
                                v.y + getHeight()/2, 
                                v.z
                            ));
                        }

                        // Calculate bounds
                        int minX = transformed.stream().mapToInt(v -> (int) v.x).min().orElse(0);
                        int maxX = transformed.stream().mapToInt(v -> (int) v.x).max().orElse(img.getWidth());
                        int minY = transformed.stream().mapToInt(v -> (int) v.y).min().orElse(0);
                        int maxY = transformed.stream().mapToInt(v -> (int) v.y).max().orElse(img.getHeight());

                        // Split Y range into chunks for parallel processing
                        int yChunkSize = (maxY - minY + 1) / processors + 1;
                        
                        for (int yStart = minY; yStart <= maxY; yStart += yChunkSize) {
                            final int yEnd = Math.min(yStart + yChunkSize, maxY);
                            final int yStartFinal = yStart;
                            
                            futures.add(executor.submit(() -> {
                                for (int y = yStartFinal; y <= yEnd; y++) {
                                    for (int x = minX; x <= maxX; x++) {
                                        boolean inTriangle = false;
                                        boolean inTriangle2 = false;
                                        double depth = Double.NEGATIVE_INFINITY;
                                        final int idx = y * img.getWidth() + x;

                                        if (poly.number_of_sides == 3) {
                                            inTriangle = isInsideTriangle(x, y, transformed.get(0), transformed.get(1), transformed.get(2));
                                            if (inTriangle) {
                                                depth = calculateDepth(x, y, transformed.get(0), 
                                                    transformed.get(1), transformed.get(2));
                                            }
                                        } 
                                        else if (poly.number_of_sides == 4) {
                                            inTriangle = isInsideTriangle(x, y, 
                                                transformed.get(0), transformed.get(1), transformed.get(2));
                                            inTriangle2 = isInsideTriangle(x, y, 
                                                transformed.get(0), transformed.get(2), transformed.get(3));
                                            
                                            if (inTriangle) {
                                                depth = calculateDepth(x, y, 
                                                    transformed.get(0), transformed.get(1), transformed.get(2));
                                            }
                                            if (inTriangle2) {
                                                double depth2 = calculateDepth(x, y, 
                                                    transformed.get(0), transformed.get(2), transformed.get(3));
                                                depth = Math.max(depth, depth2);
                                            }
                                        }

                                        if ((inTriangle || inTriangle2) && depth > zBuffer[idx]) {
                                            synchronized(zBuffer) {
                                                if (depth > zBuffer[idx]) {
                                                    zBuffer[idx] = depth;
                                                    pixels[idx] = poly.color.getRGB();
                                                }
                                            }
                                        }
                                    }
                                }
                            }));
                        }
                    }

                    // Wait for all tasks to complete
                    for (Future<?> f : futures) {
                        try { f.get(); } catch (Exception e) { /* Handle exception */ }
                    }
                    executor.shutdown();

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
        buttonPanel.setLayout(new GridLayout(4, 1, 20, 20)); // 3 buttons stacked vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Padding
        buttonPanel.add(ClickButton);
        buttonPanel.add(ResetButton);
        buttonPanel.add(ToggleWireframeButton);
        buttonPanel.add(ExitButton);
        buttonPanel.setBackground(Color.GRAY);

        // Panel for labels (TOP)
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(label);
        topPanel.add(WireFramelabel);
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

    // Handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ExitButton) {
            System.exit(0);
        } 
        if (e.getSource() == ClickButton) {
            renderPanel.repaint();
            clicks++;
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

    private boolean isInsideTriangle(int x, int y, Vertex v1, Vertex v2, Vertex v3) {
        double d1 = (x - v2.x) * (v1.y - v2.y) - (v1.x - v2.x) * (y - v2.y);
        double d2 = (x - v3.x) * (v2.y - v3.y) - (v2.x - v3.x) * (y - v3.y);
        double d3 = (x - v1.x) * (v3.y - v1.y) - (v3.x - v1.x) * (y - v1.y);
        return (d1 >= 0 && d2 >= 0 && d3 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0);
    }

    private double calculateDepth(int x, int y, Vertex v1, Vertex v2, Vertex v3) {
        double det = (v2.y - v3.y) * (v1.x - v3.x) + (v3.x - v2.x) * (v1.y - v3.y);
        double l1 = ((v2.y - v3.y) * (x - v3.x) + (v3.x - v2.x) * (y - v3.y)) / det;
        double l2 = ((v3.y - v1.y) * (x - v3.x) + (v1.x - v3.x) * (y - v3.y)) / det;
        double l3 = 1.0 - l1 - l2;
        return l1 * v1.z + l2 * v2.z + l3 * v3.z;
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
        List<Vertex[]> Shape_Coords = new ArrayList<>(); 
                // Adding coordinates to Shape_Coords
                Shape_Coords.add(new Vertex[]{new Vertex(size, size, size), // window wall
                                                new Vertex(size, size, -size),
                                                new Vertex(size, -size, -size),
                                                new Vertex(size, -size, size)});

                Shape_Coords.add(new Vertex[]{new Vertex(-size, size, size), // door wall
                                                new Vertex(-size, size, -size),
                                                new Vertex(-size, -size, -size),
                                                new Vertex(-size, -size, size)});

                Shape_Coords.add(new Vertex[]{new Vertex(size, size, size), // bathroom wall
                                                new Vertex(size, -size, size),
                                                new Vertex(-size, -size, size),
                                                new Vertex(-size, size, size)});

                Shape_Coords.add(new Vertex[]{new Vertex(size, size, -size), // opposite to bathroom
                                                new Vertex(size, -size, -size),
                                                new Vertex(-size, -size, -size),
                                                new Vertex(-size, size, -size)});

                Shape_Coords.add(new Vertex[]{new Vertex(size, size, size), // top
                                                new Vertex(size, size, -size),
                                                new Vertex(-size, size, -size),
                                                new Vertex(-size, size, size)});

                Shape_Coords.add(new Vertex[]{new Vertex(size, -size, size), // bottom
                                                new Vertex(size, -size, -size),
                                                new Vertex(-size, -size, -size),
                                                new Vertex(-size, -size, size)});


                return Shape_Coords;

        
    }

    static List<Vertex[]> create_triangle_coords(int size){

        List<Vertex[]> Triangle_coords = new ArrayList<>();
        // Adding coordinates to Triangle_coords
        Triangle_coords.add(new Vertex[]{new Vertex(size, size, size),
                                        new Vertex(-size, -size, size),
                                        new Vertex(-size, size, -size)});

        Triangle_coords.add(new Vertex[]{new Vertex(size, size, size),
                                        new Vertex(-size, -size, size),
                                        new Vertex(size, -size, -size)});

        Triangle_coords.add(new Vertex[]{new Vertex(-size, size, -size),
                                        new Vertex(size, -size, -size),
                                        new Vertex(size, size, size)});

        Triangle_coords.add(new Vertex[]{new Vertex(-size, size, -size),
                                        new Vertex(size, -size, -size),
                                        new Vertex(-size, -size, size)});
        return Triangle_coords;
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
