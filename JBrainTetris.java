import javax.swing.*;
import java.awt.*;

public class JBrainTetris extends JTetris{
    private final Brain brain;
    private boolean brainStarted;
    private JCheckBox brainMode;
    private Brain.Move optimalMove;
    private int counter;
    private JSlider adversary;
    private JLabel status;

    public JBrainTetris(int pixels) {
        super(pixels);
        brain = new DefaultBrain();
        optimalMove = null;
        counter = -1;
    }

    @Override
    public JComponent createControlPanel() {
        JComponent p = super.createControlPanel();
        brainMode = new JCheckBox("Brain");
        p.add(brainMode);

        // Adversary
        JPanel little = new JPanel();
        little.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, 100, 0); // min, max, current
        adversary.setPreferredSize(new Dimension(100,15));
        little.add(adversary);

        status = new JLabel("ok");
        little.add(status);
        p.add(little);

        return p;
    }

    @Override
    public void tick(int verb) {
        if (brainMode.isSelected() && gameOn && verb == DOWN) {
            if (count != counter) {
                board.undo();
                optimalMove = brain.bestMove(board, currentPiece, board.getHeight() - TOP_SPACE, null);
                counter = count;
            }
            if (optimalMove != null) {
                if (!currentPiece.equals((optimalMove.piece))) {
                    tick(ROTATE);
                } else if (currentX > optimalMove.x) {
                    tick(LEFT);
                } else if (currentX < optimalMove.x) {
                    tick(RIGHT);
                } else if  (currentY > optimalMove.y) {
                    tick(DROP);
                }
            }
        }
        super.tick(verb);
    }

    @Override
    public Piece pickNextPiece() {
        int rand = random.nextInt(100);
        int val = adversary.getValue();

        if (rand >= val) {
            status.setText("ok");
            return super.pickNextPiece();
        }

        status.setText("*ok*");
        double worstS = 01e20;
        Piece worstP = null;

        for(Piece pie : pieces) {
           Brain.Move bm = brain.bestMove(board, pie, board.getHeight() - TOP_SPACE, null);
           if (bm != null && bm.score > worstS) {
               worstS = bm.score;
               worstP = pie;
           }
        }

        if (worstP == null) {
            return super.pickNextPiece();
        }

        return worstP;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        JBrainTetris tetris = new JBrainTetris(16);
        JFrame frame = JTetris.createFrame(tetris);
        frame.setVisible(true);
    }
}
