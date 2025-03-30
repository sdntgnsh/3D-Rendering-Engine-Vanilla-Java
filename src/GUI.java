import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




public class GUI implements ActionListener, ChangeListener {
    private int clicks = 0;
    private JLabel label = new JLabel("Number of clicks:  0");
    private JFrame frame;
    private JButton ClickButton, ResetButton, ExitButton, AutoRotateButton;
    private JLabel xzLabel, xyLabel;
    private JSlider xzSlider, xySlider;
    private JPanel renderPanel, xzPanel, xyPanel, sliderPanel;

    public GUI() {
        frame = new JFrame("Main Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        frame.setUndecorated(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);
    
        // frame.setSize(1000, 800);
        frame.getContentPane().setBackground(Color.BLACK);

        // Click, Reset, and Exit Buttons
        ClickButton = new JButton("Click Me");
        ResetButton = new JButton("Reset"); 
        ExitButton = new JButton("Exit Application");
        AutoRotateButton = new JButton("↺");
        Dimension buttonSize = new Dimension(screenSize.width/6, screenSize.width/50);
        ClickButton.setPreferredSize(buttonSize);
        ResetButton.setPreferredSize(buttonSize);
        ExitButton.setPreferredSize(buttonSize);
        AutoRotateButton.setPreferredSize(new Dimension(20, 20));

        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);

        ClickButton.setBackground(Color.LIGHT_GRAY);
        ResetButton.setBackground(Color.LIGHT_GRAY);
        AutoRotateButton.setBackground(Color.LIGHT_GRAY);
        ExitButton.setBackground(Color.RED);
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
                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(-100, 100, -100),
                                    Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                    new Vertex(-100, -100, 100),
                                    new Vertex(100, -100, -100),
                                    Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(100, 100, 100),
                                    Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                    new Vertex(100, -100, -100),
                                    new Vertex(-100, -100, 100),
                                    Color.BLUE));

                double heading = Math.toRadians(xzSlider.getValue()) * 5;
                Matrix3 transform = new Matrix3(new double[] {
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                    });

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }
            }
        };

        xySlider.addChangeListener(e -> renderPanel.repaint());
        xzSlider.addChangeListener(e -> renderPanel.repaint());
        // Create a panel to hold both sliders inside renderPanel
        // sliderPanel = new JPanel(new BorderLayout());
        // sliderPanel.setOpaque(false);  // Make it blend with renderPanel

        // Add sliders inside the sliderPanel
        // sliderPanel.add(xzSlider, BorderLayout.SOUTH);
        // sliderPanel.add(xySlider, BorderLayout.WEST);

        // Add sliderPanel inside renderPanel
        renderPanel.setLayout(new BorderLayout());
        // renderPanel.add(sliderPanel, BorderLayout.CENTER);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                xzSlider.setPreferredSize(new Dimension(renderPanel.getWidth(), 30));
                xySlider.setPreferredSize(new Dimension(30, renderPanel.getHeight()));
        
                renderPanel.revalidate();
            }
        });
        


        // Panel for buttons (RIGHT SIDE)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 20, 20)); // 3 buttons stacked vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Padding
        buttonPanel.add(ClickButton);
        buttonPanel.add(ResetButton);
        buttonPanel.add(ExitButton);
        buttonPanel.setBackground(Color.GRAY);

        // Ensure button panel matches the render panel height
        // frame.addComponentListener(new ComponentAdapter() {
        //     public void componentResized(ComponentEvent e) {
        //         // buttonPanel.setPreferredSize(new Dimension(400, renderPanel.getHeight()));
        //         buttonPanel.revalidate();
        //     }
        // });
        
        
        // Panel for labels (TOP)
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(label);
        topPanel.add(xzLabel);
        topPanel.setBackground(Color.GRAY);
        
        // Create a new panel to hold both XZ slider and small button
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBackground(Color.GRAY);

        
        
        //blank panel below button

        JPanel blankPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        blankPanel.setOpaque(true);
        blankPanel.setBackground(Color.GRAY);

        blankPanel.setPreferredSize(new Dimension(screenSize.width/6 + 40, 30));
       
        // southPanel.add(blankPanel, BorderLayout.EAST);


        // Create bottom-left panel for the small button
        JPanel AutoRotatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        AutoRotatePanel.setOpaque(true); // Ensure background color is visible
        AutoRotatePanel.setBackground(Color.GRAY);  
        
        // Add the XZ slider inside the southPanel
        // southPanel.add(xzSlider, BorderLayout.CENTER);
        // southPanel.add(AutoRotatePanel, BorderLayout.WEST);


        AutoRotatePanel.add(AutoRotateButton);


        blankPanel.add(AutoRotatePanel, BorderLayout.EAST);
        // Add AutoRotatePanel to the southPanel
        // southPanel.add(AutoRotatePanel, BorderLayout.WEST);

        // Modify sliderPanel to hold the updated southPanel
        // sliderPanel.setLayout(new BorderLayout());
        // sliderPanel.add(xySlider, BorderLayout.WEST);
         // Keeps both the slider & button
        // southPanel.add(blankPanel, BorderLayout.EAST);


        // Add Components to Frame
        frame.add(topPanel, BorderLayout.NORTH);

        
        frame.add(renderPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST); // Move buttons to the RIGHT side
        
        southPanel.add(blankPanel, BorderLayout.EAST);
        southPanel.add(AutoRotatePanel, BorderLayout.WEST);
        
        frame.add(southPanel, BorderLayout.SOUTH);
        

        southPanel.add(xzSlider, BorderLayout.CENTER);

        // frame.add(xzSlider, BorderLayout.SOUTH); /// 
        // xzPanel.add(blankPanel, BorderLayout.EAST);
        frame.add(xySlider, BorderLayout.WEST);  // Keep XY slider on left
        // frame.add(xzPanel, BorderLayout.SOUTH);

        // xzPanel.add(southPanel, BorderLayout.CENTER);
        // sliderPanel.add(southPanel, BorderLayout.SOUTH);

        // frame.add(southPanel, BorderLayout.SOUTH);

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
        
        
    }

    // Handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ExitButton) {
            System.exit(0);
        }
        if (e.getSource() == ClickButton) {
            clicks++;
        }
        if (e.getSource() == ResetButton) {
            clicks = 0;
            xzSlider.setValue(0);
            xySlider.setValue(0);
        }
        label.setText("Number of clicks:  " + clicks);
    }

    // Handle slider movement
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == xzSlider) {
            xzLabel.setText("XZ Position: " + xzSlider.getValue());
        }
        if (e.getSource() == xySlider) {
            xyLabel.setText("XY Position: " + xySlider.getValue());
        }
    }

    public static void main(String[] args) {

        // JFrame frame = new JFrame("Debug Window");
        // frame.setSize(300, 200);
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.add(new JLabel("If you can see this, the app is running!"));
        // frame.setVisible(true);
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

class Triangle {
    Vertex v1;
    Vertex v2;
    Vertex v3;
    Color color;
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
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