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
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StorageService {
    private final String BASE_PATH = "projects/";
    private final ObjectMapper mapper = new ObjectMapper();

    public StorageService(){

    }

    public void saveFiles(Project project) throws IOException {
        Path path = Path.of(BASE_PATH + project.getId());
        if (!Files.exists(path)){
            Files.createDirectories(path);
        }

        List<SpriteLayerDTO> toParse = new ArrayList<>();
        HashSet<String> safeFiles = new HashSet<String>();

        safeFiles.add("metadata.json");
        safeFiles.add("preview.png");
        for (SpriteLayer layer : project.getLayers()){
            if (layer.getImage()!= null){
                File file = new File(path.toFile(),layer.getImageFileName());

                ImageIO.write(SwingFXUtils.fromFXImage(layer.getImage(), null), "png", file);

                safeFiles.add(layer.getImageFileName());
            }

            toParse.add(new SpriteLayerDTO(layer));
        }


        File jsonFile = new File(path.toFile(),"metadata.json");

        mapper.writeValue(jsonFile,toParse);
        generateThumbnail(project,path.toFile());

        File[] files = path.toFile().listFiles();
        if (files != null) {
            for (File file: files){
                if(!safeFiles.contains(file.getName())){
                    Files.delete(file.toPath());
                }
            }
        }
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

            File fileImage = new File(path.toFile(),layer.getImageFileName());

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
        File file = new File(BASE_PATH + projectId + "/preview.png");
        return file.toURI().toString();
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


    public void exportProjectAsZip(Project project, File targetZipFile) throws IOException {
        Path sourceDir = Path.of(BASE_PATH + project.getId());
        if (!Files.exists(sourceDir)) {
            throw new IOException("File Doesn't exist : " + sourceDir);
        }

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZipFile));

        Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("Can't compress file : " + path, e);
                        }
                    });

    }


    public void deleteProjectFiles(int projectId) throws IOException {
        Path path = Path.of(BASE_PATH + projectId);
        if (Files.exists(path)) {
            File[] files = path.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    Files.delete(file.toPath());
                }
            }
            Files.delete(path);
        }
    }
}
