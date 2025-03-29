
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GUI implements ActionListener {
    private int clicks = 0;
    private JLabel label = new JLabel("Number of clicks:  0     ");
    private JFrame frame = new JFrame();
    private JButton ClickButton, ResetButton, ExitButton;

    public GUI() {

        // the clickable button
        ClickButton = new JButton("Click Me");
        ResetButton = new JButton("Reset");
        ExitButton = new JButton("Exit Application");
        ClickButton.addActionListener(this);
        ResetButton.addActionListener(this);
        ExitButton.addActionListener(this);

        // the panel with the button and text
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        panel.setLayout(new GridLayout(0, 1, 0, 10));
        panel.add(ClickButton);
        panel.add(ResetButton);
        panel.add(ExitButton);
        panel.add(label);

        // set up the frame and display it
        frame = new JFrame("Main Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 250);
        frame.setMinimumSize(new Dimension(350,200));
        frame.setLocationRelativeTo(null);
        frame.add(panel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("GUI");
        frame.pack();
        frame.setVisible(true);
    }

    // process the button clicks
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == ExitButton){
            System.exit(0);
        }
        if(e.getSource() == ClickButton){
            clicks++;
        }
        if(e.getSource() == ResetButton){
            clicks = 0;
        }
        label.setText("Number of clicks:  " + clicks);
    }

    // create one Frame
    public static void main(String[] args) {
        // Create a frame for the start screen
        JFrame startFrame = new JFrame("Welcome");
        startFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startFrame.setSize(300, 200);
        startFrame.setLayout(new FlowLayout());

        // Create the start button
        JButton startButton = new JButton("Start Application");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Add action listener to open the main GUI
        startButton.addActionListener(e -> {
            startFrame.dispose(); // Close the start window
            new GUI(); // Open the main application
        });

        // Add button to frame and display it
        startFrame.add(startButton);
        startFrame.setVisible(true);
        startFrame.setLocationRelativeTo(null);
    }
}
