import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class EditorLauncher {

    public static void main(String[] args) {
        JFrame f = new JFrame("Level Editor");
        LevelEditorPanel editor = new LevelEditorPanel();

        JPanel tools = new JPanel();
        tools.setLayout(new GridLayout(1, 5));

        for (EditorTool tool : EditorTool.values()) {
            JButton b = new JButton(tool.name());
            b.addActionListener(e -> editor.setTool(tool));
            tools.add(b);
        }

        JButton play = new JButton("PLAY");
        play.addActionListener(e -> {
            f.dispose(); // Close the editor window

            RaycastingEngine3D game = new RaycastingEngine3D(
                    editor.walls,
                    editor.enemies,
                    editor.playerX,
                    editor.playerY
            );

            JFrame gameFrame = new JFrame("3D Game");
            gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            gameFrame.setUndecorated(true);
            gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gameFrame.setContentPane(game);
            gameFrame.pack();
            gameFrame.setVisible(true);
        });

        tools.add(play);

        f.setLayout(new BorderLayout());
        f.add(editor, BorderLayout.CENTER);
        f.add(tools, BorderLayout.SOUTH);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
