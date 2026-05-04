package com.subcafae.finantialtracker.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Reemplaza el contenido del JDialog "loading" con un panel moderno y animado:
 * titulo en bold, mensaje contextual con puntos animados ("Cargando."
 * "Cargando.." "Cargando...") y barra de progreso indeterminada (FlatLaf
 * la pinta como shimmer suave).
 *
 * Se instala una sola vez sobre el JDialog existente. Los call sites
 * preexistentes que hacen setModal/setLocationRelativeTo/setVisible/dispose
 * siguen funcionando sin cambios.
 *
 * Para mostrar un mensaje contextual diferente al "Cargando" por defecto:
 *   LoadingOverlay.setMessage("Cargando abonos");
 *   ViewMain.loading.setVisible(true);
 */
public final class LoadingOverlay {

    private static final Color BG = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TITLE_COLOR = new Color(45, 55, 72);
    private static final Color ACCENT = new Color(99, 102, 241);

    private static JLabel messageLabel;
    private static Timer dotsTimer;
    private static String currentMessage = "Cargando";
    private static boolean installed = false;

    private LoadingOverlay() {}

    /**
     * Reemplaza el contenido del JDialog con el panel moderno y registra
     * los listeners para arrancar/parar la animacion al show/hide.
     */
    public static void install(JDialog loading) {
        if (installed || loading == null) return;

        JLabel title = new JLabel("Procesando");
        title.setFont(new Font("Roboto", Font.BOLD, 16));
        title.setForeground(TITLE_COLOR);

        messageLabel = new JLabel(currentMessage);
        messageLabel.setFont(new Font("Roboto", Font.PLAIN, 13));
        messageLabel.setForeground(ACCENT);

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setBorderPainted(false);
        progress.setPreferredSize(new Dimension(0, 6));

        JPanel textBlock = new JPanel(new BorderLayout(0, 4));
        textBlock.setOpaque(false);
        textBlock.add(title, BorderLayout.NORTH);
        textBlock.add(messageLabel, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(BG);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)));
        content.add(textBlock, BorderLayout.CENTER);
        content.add(progress, BorderLayout.SOUTH);

        loading.setUndecorated(true);
        loading.getContentPane().removeAll();
        loading.getContentPane().setLayout(new BorderLayout());
        loading.getContentPane().add(content, BorderLayout.CENTER);
        loading.setSize(new Dimension(360, 130));
        loading.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        loading.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                startDotsAnimation();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                stopDotsAnimation();
            }
        });

        installed = true;
    }

    /**
     * Cambia el mensaje contextual. Llamar antes de setVisible(true).
     * Si no se llama, se usa el ultimo mensaje seteado (default "Cargando").
     */
    public static void setMessage(String message) {
        currentMessage = (message == null || message.isBlank()) ? "Cargando" : message;
        if (messageLabel != null) {
            updateMessageOnEdt(currentMessage);
        }
    }

    private static void updateMessageOnEdt(String text) {
        if (SwingUtilities.isEventDispatchThread()) {
            messageLabel.setText(text);
        } else {
            SwingUtilities.invokeLater(() -> messageLabel.setText(text));
        }
    }

    private static void startDotsAnimation() {
        stopDotsAnimation();
        if (messageLabel == null) return;
        final String base = currentMessage;
        final String[] suffixes = {"", ".", "..", "..."};
        final int[] frame = {0};
        dotsTimer = new Timer(400, e -> {
            messageLabel.setText(base + suffixes[frame[0]]);
            frame[0] = (frame[0] + 1) % suffixes.length;
        });
        dotsTimer.setInitialDelay(0);
        dotsTimer.start();
    }

    private static void stopDotsAnimation() {
        if (dotsTimer != null) {
            dotsTimer.stop();
            dotsTimer = null;
        }
    }

    /**
     * Helper opcional para call sites nuevos que prefieran un API mas simple.
     * Equivalente a: setMessage + setLocationRelativeTo + setVisible(true).
     */
    public static void show(JDialog loading, Component parent, String message) {
        if (loading == null) return;
        setMessage(message);
        if (parent != null) loading.setLocationRelativeTo(parent);
        loading.setVisible(true);
    }

    public static void hide(JDialog loading) {
        if (loading == null) return;
        if (loading.isVisible()) loading.setVisible(false);
    }
}
