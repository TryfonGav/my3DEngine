import javax.swing.*;
import java.awt.*;

public class EditorLauncher {
    public static void main(String[] args) {
        JFrame f = new JFrame("Level Editor");
        LevelEditorPanel editor = new LevelEditorPanel();

        JPanel tools = new JPanel();
        tools.setLayout(new GridLayout(2, 4));

        for (EditorTool tool : EditorTool.values()) {
            JButton b = new JButton(tool.name());
            b.addActionListener(e -> editor.setTool(tool));
            tools.add(b);
        }

        JButton play = new JButton("PLAY");
        play.setBackground(Color.GREEN);
        play.addActionListener(e -> {
            f.dispose(); // Close editor
            RaycastingEngine3D game = new RaycastingEngine3D(
                    editor.map, editor.enemies, editor.playerX, editor.playerY
            );
            JFrame gameFrame = new JFrame("Raycasting 3D");
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
