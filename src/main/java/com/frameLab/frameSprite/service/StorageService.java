package com.frameLab.frameSprite.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frameLab.frameSprite.Sprites.SpriteLayer;
import com.frameLab.frameSprite.dto.SpriteLayerDTO;
import com.frameLab.frameSprite.model.Project;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class StorageService {
    private final String BASE_PATH = "projects/";
    private final ObjectMapper mapper = new ObjectMapper();

    StorageService(){

    }

    public void saveFiles(Project project) throws IOException {
        Path path = Path.of(BASE_PATH + project.getId());
        if (!Files.exists(path)){
            Files.createDirectories(path);
        }

        List<SpriteLayerDTO> toParse = new ArrayList<>();

        for (SpriteLayer layer : project.getLayers()){
            if (layer.getImage()!= null){
                File file = new File(path.toFile(),layer.getImageFileName());

                ImageIO.write(SwingFXUtils.fromFXImage(layer.getImage(), null), "png", file);

            }

            toParse.add(new SpriteLayerDTO(layer));
        }


        File jsonFile = new File(path+"/metadata.json");

        mapper.writeValue(jsonFile,toParse);
        generateThumbnail(project,path.toFile());
    }

    public void loadFiles(Project project) throws IOException {
        Path path = Path.of(BASE_PATH + project.getId());
        File jsonFile = new File(path.toFile(),"metadata.json");
        if (!jsonFile.exists()){
          return;
        }

        List<SpriteLayerDTO> toFormat = mapper.readValue(jsonFile, new TypeReference<List<SpriteLayerDTO>>(){});

        List<SpriteLayer> layers = new ArrayList<>();

        for (SpriteLayerDTO dto : toFormat){
            SpriteLayer layer = dto.toLayer();

            File fileImage = new File(path+layer.getImageFileName());

            if (fileImage.exists()) {
                Image image = new Image(fileImage.toURI().toString());
                layer.setImage(new WritableImage(image.getPixelReader(),
                        (int) image.getWidth(),
                        (int) image.getHeight()));

            } else{
                layer.setImage(new WritableImage(project.getWidth(),project.getHeight()));
            }
            layers.add(layer);
        }

        project.setLayers(layers);

    }

    public String getPreviewPath(int projectId) {
        return BASE_PATH + projectId + "/preview.png";
    }

    private void generateThumbnail(Project project,File folder) throws IOException {
        int w = project.getWidth();
        int h = project.getHeight();

        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.TRANSPARENT);
        gc.fillRect(0, 0, w, h);

        for (SpriteLayer layer : project.getLayers()) {
            if (layer.isVisible && layer.image != null) {

                gc.setGlobalAlpha(layer.opacity);
                gc.drawImage(layer.image, 0, 0);
            }

        }

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);

        double scale = 300.0 / Math.max(w, h);
        if (scale < 1.0) {
            params.setTransform(new Scale(scale, scale));
        }

        WritableImage thumbnail = canvas.snapshot(params, null);

        File previewFile = new File(folder, "preview.png");
        ImageIO.write(SwingFXUtils.fromFXImage(thumbnail, null), "png", previewFile);
    }
}
