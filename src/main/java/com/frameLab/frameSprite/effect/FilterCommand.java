package com.frameLab.frameSprite.effect;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class FilterCommand implements Command {

    private final Canvas canvas;
    private final GraphicsContext gc;

    private final WritableImage backup;
    private final WritableImage present;

    public FilterCommand(Canvas canvas, Filter filter) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        this.backup = canvas.snapshot(params, null);

        int width = (int) backup.getWidth();
        int height = (int) backup.getHeight();
        this.present = new WritableImage(width, height);


        PixelReader reader = backup.getPixelReader();
        PixelWriter writer = present.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalArgb = reader.getArgb(x, y);

                int newArgb = filter.apply(originalArgb);

                writer.setArgb(x, y, newArgb);
            }
        }

        execute();

    }

    @Override
    public void execute() {
        if (canvas != null && present != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(present, 0, 0);
        }
    }

    @Override
    public void undo() {
        if (canvas != null && backup != null) {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(backup, 0, 0);
        }
    }
}
