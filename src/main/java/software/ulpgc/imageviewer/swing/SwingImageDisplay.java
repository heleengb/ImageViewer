package software.ulpgc.imageviewer.swing;

import software.ulpgc.imageviewer.ImageDisplay;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwingImageDisplay extends JPanel implements ImageDisplay {
    private Shift shift = Shift.Null;
    private Released released = Released.Null;
    private int initShift;
    private List<Paint> paints = new ArrayList<>();
    // CAMBIO: Un caché para guardar las imágenes cargadas y no leerlas constantemente del disco
    private Map<String, BufferedImage> imageCache = new HashMap<>();

    public SwingImageDisplay() {
        this.addMouseListener(mouseListener());
        this.addMouseMotionListener(mouseMotionListener());
    }

    private MouseListener mouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                initShift = e.getX();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                released.offset(e.getX() - initShift);
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) { }
        };
    }

    private MouseMotionListener mouseMotionListener() {
        return new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                shift.offset(e.getX() - initShift);
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        };
    }

    @Override
    public void paint(String id, int offset) {
        paints.add(new Paint(id, offset));
        repaint();
    }

    @Override
    public void clear() {
        paints.clear();
    }

    // CAMBIO: Método load para leer la imagen o sacarla del caché
    private BufferedImage load(String name) {
        if (imageCache.containsKey(name)) {
            return imageCache.get(name);
        }
        try {
            // Intenta cargar la imagen desde la raíz del proyecto
            BufferedImage image = ImageIO.read(new File(name));
            imageCache.put(name, image);
            return image;
        } catch (IOException e) {
            // Si falla (ej. no encuentra el archivo), retorna null o imprime error
            System.err.println("No se pudo cargar la imagen: " + name);
            return null;
        }
    }

    @Override
    public void paint(Graphics g) {
        for (Paint paint : paints) {
            BufferedImage img = load(paint.id);
            if (img != null) {
                // CAMBIO: Usamos drawImage en lugar de fillRect
                // Se dibuja la imagen ocupando todo el panel (getWidth() y getHeight())
                g.drawImage(img, paint.offset, 0, getWidth(), getHeight(), null);
            } else {
                // Si no hay imagen, pintamos un cuadro negro de error
                g.setColor(Color.BLACK);
                g.fillRect(paint.offset, 0, getWidth(), getHeight());
            }
        }
    }

    @Override
    public void on(Shift shift) {
        this.shift = shift != null ? shift : Shift.Null;
    }

    @Override
    public void on(Released released) {
        this.released = released != null ? released : Released.Null;
    }

    private record Paint(String id, int offset) {
    }
}