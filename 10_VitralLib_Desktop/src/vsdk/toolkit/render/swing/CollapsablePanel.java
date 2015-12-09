//===========================================================================
package vsdk.toolkit.render.swing;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JLabel;

class _CollapsablePanelMouseListener implements MouseListener {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20140314L;

    private CollapsablePanel parent;

    public _CollapsablePanelMouseListener(CollapsablePanel parent) {
        this.parent = parent;
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        parent.toggleSelection();
    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseReleased(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent e) {
    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent e) {
    }
}

class _CollapsablePanelHeader extends JPanel {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20140314L;

    private String text_;
    private Font font;
    private BufferedImage open, closed;
    private final int OFFSET = 30, PAD = 5;
    private CollapsablePanel parent;

    public _CollapsablePanelHeader(String text, CollapsablePanel parent) {
        super();
        this.parent = parent;
        addMouseListener(new _CollapsablePanelMouseListener(parent));
        text_ = text;
        font = new Font("sans-serif", Font.PLAIN, 12);
        // setRequestFocusEnabled(true);
        setPreferredSize(new Dimension(200, 20));
        int w = getWidth();
        int h = getHeight();


        /*
         * try { open = ImageIO.read(new File("images/arrow_down_mini.png"));
         * closed = ImageIO.read(new File("images/arrow_right_mini.png")); }
         * catch (IOException e) { e.printStackTrace(); }
         */

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int h = getHeight();
        /*
         * if (selected) g2.drawImage(open, PAD, 0, h, h, this); else
         * g2.drawImage(closed, PAD, 0, h, h, this);
         */ // Uncomment once you have your own images
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics(text_, frc);
        float height = lm.getAscent() + lm.getDescent();
        float x = OFFSET;
        float y = (h + height) / 2 - lm.getDescent();
        g2.drawString(text_, x, y);

        g2.drawLine(5, 10, 15, 10);

        if (!parent.selected) {
            g2.drawLine(10, 5, 10, 15);
        }
    }
}

/**
*/
public class CollapsablePanel extends JPanel {
    /// Check the general attribute description in superclass Entity.
    public static final long serialVersionUID = 20140314L;

    public boolean selected;
    private JPanel contentPanel;
    private _CollapsablePanelHeader headerPanel;
    public CollapsablePanel(String text, JPanel panel) {
        super(new GridBagLayout());

        this.setBackground(Color.red);
        this.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.insets = new Insets(1, 3, 0, 3);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        selected = false;
        headerPanel = new _CollapsablePanelHeader(text, this);

        contentPanel = panel;

        add(headerPanel, gbc);
        
        add(contentPanel, gbc);
        contentPanel.setVisible(false);

        JLabel padding = new JLabel();
        padding.setBackground(Color.green);
        gbc.weighty = 1.0;
        add(padding, gbc);
        

    }

    public void toggleSelection() {
        selected = !selected;

        if (contentPanel.isShowing()) {
            //contentPanel.setSize(0, 0);
            //contentPanel.setPreferredSize(new Dimension(0, 0));
            contentPanel.setVisible(false);
            
        } else {
            contentPanel.setVisible(true);
            
        }

        validate();

        headerPanel.repaint();
    }
}

//===========================================================================
//= EOF                                                                     =
//===========================================================================
