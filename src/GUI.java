import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI implements ActionListener, ChangeListener {
    private int clicks = 0;
    private JLabel label = new JLabel("Number of clicks:  0");
    private JFrame frame;
    private JButton ClickButton, ResetButton, ExitButton;
    private JLabel xzLabel, xyLabel;
    private JSlider xzSlider, xySlider;
    private JPanel renderPanel, xzPanel, xyPanel, sliderPanel;

    public GUI() {
        frame = new JFrame("Main Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());
        // frame.setUndecorated(true);
        frame.setSize(1000, 800);
        frame.getContentPane().setBackground(Color.BLACK);

        // Click, Reset, and Exit Buttons
        ClickButton = new JButton("Click Me");
        ResetButton = new JButton("Reset");
        ExitButton = new JButton("Exit Application");
        Dimension buttonSize = new Dimension(400, 150);
        ClickButton.setPreferredSize(buttonSize);
        ResetButton.setPreferredSize(buttonSize);
        ExitButton.setPreferredSize(buttonSize);

        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);

        ClickButton.setBackground(Color.LIGHT_GRAY);
        ResetButton.setBackground(Color.LIGHT_GRAY);
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
            }
        };
        // Create a panel to hold both sliders inside renderPanel
        sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false);  // Make it blend with renderPanel

        // Add sliders inside the sliderPanel
        sliderPanel.add(xzSlider, BorderLayout.SOUTH);
        sliderPanel.add(xySlider, BorderLayout.WEST);

        // Add sliderPanel inside renderPanel
        renderPanel.setLayout(new BorderLayout());
        renderPanel.add(sliderPanel, BorderLayout.CENTER);
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                xzSlider.setPreferredSize(new Dimension(renderPanel.getWidth(), 30));
                xySlider.setPreferredSize(new Dimension(30, renderPanel.getHeight()));
        
                renderPanel.revalidate();
            }
        });
        


        // Panel for buttons (RIGHT SIDE)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 buttons stacked vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding
        buttonPanel.add(ClickButton);
        buttonPanel.add(ResetButton);
        buttonPanel.add(ExitButton);
        buttonPanel.setBackground(Color.GRAY);
        
        
        // Panel for labels (TOP)
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(label);
        topPanel.add(xzLabel);
        topPanel.setBackground(Color.GRAY);

        // Add Components to Frame
        frame.add(topPanel, BorderLayout.NORTH);
        // frame.add(xzSlider, BorderLayout.SOUTH);
        // frame.add(xySlider, BorderLayout.WEST);  // Keep XY slider on left
        frame.add(renderPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST); // Move buttons to the RIGHT side

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
