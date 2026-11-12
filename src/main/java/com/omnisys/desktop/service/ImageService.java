package com.omnisys.desktop.service;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Zerbitzua Odoo-ko irudiak JavaFX formatura bihurtzeko.
 */
public class ImageService {

    public Image convertBase64ToImage(String imageBase64) {

        if (imageBase64 == null || imageBase64.isBlank()) {
            return null;
        }

        try {
            String cleanBase64 = imageBase64;

            if (cleanBase64.startsWith("data:")) {
                cleanBase64 = cleanBase64.substring(
                        cleanBase64.indexOf(',') + 1
                );
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);

            // WebP irudia irakurri.
            BufferedImage bufferedImage = ImageIO.read(
                    new ByteArrayInputStream(imageBytes)
            );

            if (bufferedImage == null) {
                return null;
            }

            // PNG formatura bihurtu memoria barruan.
            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            boolean converted = ImageIO.write(
                    bufferedImage,
                    "png",
                    outputStream
            );

            if (!converted) {
                return null;
            }

            byte[] pngBytes = outputStream.toByteArray();

            return new Image(
                    new ByteArrayInputStream(pngBytes)
            );

        } catch (Exception e) {
            return null;
        }
    }
}