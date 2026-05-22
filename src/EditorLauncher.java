import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class EditorLauncher {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Level Editor");
            LevelEditorPanel editor = new LevelEditorPanel();

            JPanel tools = new JPanel();
            tools.setLayout(new GridLayout(1, 7));

            for (EditorTool tool : EditorTool.values()) {
                JButton b = new JButton(tool.name());
                b.addActionListener(e -> editor.setTool(tool));
                tools.add(b);
            }

            JButton saveBtn = new JButton("SAVE");
            saveBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                int res = chooser.showSaveDialog(f);
                if (res == JFileChooser.APPROVE_OPTION) {
                    try {
                        editor.saveToFile(chooser.getSelectedFile());
                        JOptionPane.showMessageDialog(f, "Saved successfully");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(f, "Save failed: " + ex.getMessage());
                    }
                }
            });

            JButton loadBtn = new JButton("LOAD");
            loadBtn.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                int res = chooser.showOpenDialog(f);
                if (res == JFileChooser.APPROVE_OPTION) {
                    try {
                        editor.loadFromFile(chooser.getSelectedFile());
                        JOptionPane.showMessageDialog(f, "Loaded successfully");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(f, "Load failed: " + ex.getMessage());
                    }
                }
            });

            JButton play = new JButton("PLAY");
            play.addActionListener(e -> {
                f.dispose(); // Close the editor window

                RaycastingEngine3D game = new RaycastingEngine3D(
                        editor.getWalls(),
                        editor.getEnemies(),
                        editor.getPlayerX(),
                        editor.getPlayerY()
                );

                JFrame gameFrame = new JFrame("3D Game");
                gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                gameFrame.setUndecorated(true);
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.setContentPane(game);
                gameFrame.pack();
                gameFrame.setVisible(true);
                game.start();
            });

            tools.add(saveBtn);
            tools.add(loadBtn);
            tools.add(play);

            f.setLayout(new BorderLayout());
            f.add(editor, BorderLayout.CENTER);
            f.add(tools, BorderLayout.SOUTH);
            f.pack();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}
