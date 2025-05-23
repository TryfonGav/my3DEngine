
import javax.swing.*;
        import java.awt.*;
        import java.awt.event.*;

public class EditorLauncher {

    public static void main(String[] args) {
        JFrame f = new JFrame("Level Editor");
        LevelEditorPanel editor = new LevelEditorPanel();

        JPanel tools = new JPanel();
        tools.setLayout(new GridLayout(1, 5));
        for (EditorTool t : EditorTool.values()) {
            JButton b = new JButton(t.name());
            b.addActionListener(e -> editor.setTool(t));
            tools.add(b);
        }

        JButton play = new JButton("PLAY");
        play.addActionListener(e -> {
            f.dispose(); // close editor
            RaycastingEngine3D game = new RaycastingEngine3D(
                    editor.walls,
                    editor.enemies,
                    editor.playerX,
                    editor.playerY
            );
            JFrame gf = new JFrame("3D Game");
            gf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gf.setContentPane(game);
            gf.pack();
            gf.setVisible(true);
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
