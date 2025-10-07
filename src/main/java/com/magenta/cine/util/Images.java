package com.magenta.cine.util;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class Images {

    private static final long MAX_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT);

    private Images() {
    }

    public static String copyPosterToAppFolder(File origen) throws IOException {
        Objects.requireNonNull(origen, "El archivo de origen no puede ser null");
        if (!origen.exists() || !origen.isFile()) {
            throw new IOException("El archivo de portada no existe o no es válido");
        }
        if (origen.length() > MAX_SIZE_BYTES) {
            throw new IOException("El archivo supera el tamaño máximo de 5MB");
        }

        String extension = obtenerExtension(origen.getName());
        if (extension == null || !(extension.equalsIgnoreCase("jpg") ||
                extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png"))) {
            throw new IOException("Formato no soportado. Usa imágenes JPG o PNG");
        }

        Path destinoDir = Path.of(System.getProperty("user.home"), "magenta", "posters");
        Files.createDirectories(destinoDir);

        String nombreLimpio = limpiarNombre(origen.getName());
        String timestamp = TS_FORMATTER.format(LocalDateTime.now());
        String nuevoNombre = timestamp + "-" + UUID.randomUUID() + (nombreLimpio.isBlank() ? "" : "-" + nombreLimpio);
        Path destino = destinoDir.resolve(nuevoNombre + "." + extension.toLowerCase(Locale.ROOT));

        Files.copy(origen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        return destino.toAbsolutePath().toString();
    }

    public static Icon loadThumb(String path, int width, int height) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235, 235, 235));
        g.fillRect(0, 0, width, height);
        g.setColor(new Color(210, 210, 210));
        g.drawRect(0, 0, width - 1, height - 1);

        if (path != null && !path.isBlank()) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    if (img != null) {
                        double scale = Math.min((double) width / img.getWidth(), (double) height / img.getHeight());
                        int newW = Math.max(1, (int) Math.round(img.getWidth() * scale));
                        int newH = Math.max(1, (int) Math.round(img.getHeight() * scale));
                        Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                        int x = (width - newW) / 2;
                        int y = (height - newH) / 2;
                        g.drawImage(scaled, x, y, null);
                    }
                } catch (IOException e) {
                    dibujarMarcadorError(g, width, height);
                }
            } else {
                dibujarMarcadorError(g, width, height);
            }
        } else {
            dibujarMarcadorVacio(g, width, height);
        }

        g.dispose();
        return new ImageIcon(canvas);
    }

    private static void dibujarMarcadorVacio(Graphics2D g, int width, int height) {
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(width / 4, height / 4, width * 3 / 4, height * 3 / 4);
        g.drawLine(width * 3 / 4, height / 4, width / 4, height * 3 / 4);
    }

    private static void dibujarMarcadorError(Graphics2D g, int width, int height) {
        g.setColor(new Color(200, 80, 80));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(width / 4, height / 4, width * 3 / 4, height * 3 / 4);
        g.drawLine(width * 3 / 4, height / 4, width / 4, height * 3 / 4);
    }

    private static String obtenerExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1 || dot == name.length() - 1) {
            return null;
        }
        return name.substring(dot + 1);
    }

    private static String limpiarNombre(String name) {
        String base = name;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            base = name.substring(0, dot);
        }
        return base.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase(Locale.ROOT);
    }
}
