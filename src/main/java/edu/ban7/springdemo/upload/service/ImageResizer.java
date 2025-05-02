package edu.ban7.springdemo.upload.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageResizer {

    //Note : a la différence de la propriété css object-fit:contain, il n'y a pas de background-color ajouté.
    // C'est a l'utilisateur de centrer et ajouter cette couleur en css s'il le souhaite
    public enum ResizeMode {
        COVER, CONTAINS, FILL
    }

    public InputStream resizeAndCrop(MultipartFile file, int targetWidth, int targetHeight) throws IOException {
        return resizeAndCrop( file,  targetWidth,  targetHeight, ResizeMode.COVER);
    }

    public InputStream resizeAndCrop(MultipartFile file, int targetWidth, int targetHeight, ResizeMode mode) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        BufferedImage resizedImage = switch (mode) {
            case COVER -> processCoverMode(originalImage, targetWidth, targetHeight);
            case CONTAINS -> processContainsMode(originalImage, targetWidth, targetHeight);
            case FILL -> processFillMode(originalImage, targetWidth, targetHeight);
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, getFileExtension(file), baos);
        baos.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private BufferedImage processCoverMode(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate the crop dimensions
        int cropX, cropY, cropWidth, cropHeight;
        if (originalWidth > originalHeight) {
            // Landscape image
            cropWidth = originalHeight;
            cropHeight = originalHeight;
            cropX = (originalWidth - cropWidth) / 2;
            cropY = 0;
        } else {
            // Portrait image
            cropWidth = originalWidth;
            cropHeight = originalWidth;
            cropX = 0;
            cropY = (originalHeight - cropHeight) / 2;
        }

        // Crop the image
        BufferedImage croppedImage = originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

        // Resize the cropped image to target dimensions
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(croppedImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    private BufferedImage processContainsMode(BufferedImage originalImage, int targetWidth, int targetHeight) {
        double scale = Math.min((double) targetWidth / originalImage.getWidth(), (double) targetHeight / originalImage.getHeight());
        int scaledWidth = (int) (originalImage.getWidth() * scale);
        int scaledHeight = (int) (originalImage.getHeight() * scale);

        BufferedImage resizedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    private BufferedImage processFillMode(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();

        return resizedImage;
    }

    private String getFileExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}