import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class View extends JPanel {
    private Model model;
    private static final Color BG_COLOR = new Color(0xbbada0);
    private static final String FONT_NAME = "Arial";
    private static final int TILE_SIZE = 96;
    private static final int TILE_MARGIN = 12;

    private Controller controller;

    boolean isGameWon = false;
    boolean isGameLost = false;

    public View(Controller controller) {
        setFocusable(true);
        this.controller = controller;
        addKeyListener(controller);

        // Добавляем фокус для JPanel
        setFocusable(true);
        requestFocusInWindow();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (isGameWon || isGameLost) {
            // Создаем размытое изображение фона
            BufferedImage blurredBackground = createBlurredBackground();
            g.drawImage(blurredBackground, 0, 0, this);

            // Выводим текст на размытом фоне
            g.setColor(Color.WHITE);
            g.setFont(new Font(FONT_NAME, Font.BOLD, 36));
            if (isGameWon) {
                g.drawString("You've won!", 130, 100);
            } else {
                g.setFont(new Font(FONT_NAME,Font.BOLD,44));
                g.drawString("Game over!", 120, 100);
                g.setFont(new Font("Times New Roman",Font.BOLD,30));
                g.drawString("Made by Jambo", 200, 430);
                g.setFont(new Font(FONT_NAME,Font.BOLD,25));
                g.drawString("Press SPACE to start a new game!", 20, 250);
            }
        } else {
            g.setColor(BG_COLOR);
            g.fillRect(0, 0, this.getSize().width, this.getSize().height);
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    drawTile(g, controller.getGameTiles()[y][x], x, y);
                }
            }
            g.drawString("Score: " + controller.getScore(), 140, 465);
        }
    }

    // Метод для создания размытого фона

    private BufferedImage createBlurredBackground() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(BG_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Используем ConvolveOp для размытия фона
        float[] blurKernel = {
                0.0625f, 0.125f, 0.0625f,
                0.125f, 0.25f, 0.125f,
                0.0625f, 0.125f, 0.0625f
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, blurKernel));
        BufferedImage blurredImage = op.filter(image, null);

        return blurredImage;
    }


    private static int offsetCoors(int arg) {
        return arg * (TILE_MARGIN + TILE_SIZE) + TILE_MARGIN;
    }

    private void drawTile(Graphics g2, Tile tile, int x, int y) {
        Graphics2D g = ((Graphics2D) g2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int value = tile.value;
        int xOffset = offsetCoors(x);
        int yOffset = offsetCoors(y);
        g.setColor(tile.getTileColor());
        g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 8, 8);
        g.setColor(tile.getFontColor());
        final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
        final Font font = new Font(FONT_NAME, Font.BOLD, size);
        g.setFont(font);

        String s = String.valueOf(value);
        final FontMetrics fm = getFontMetrics(font);

        final int w = fm.stringWidth(s);
        final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

        if (value != 0)
            g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);
    }
}
