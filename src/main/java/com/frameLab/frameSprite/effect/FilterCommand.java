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

        boolean ogVisible = canvas.isVisible();
        double ogOpacity = canvas.getOpacity();

        canvas.setVisible(true);
        canvas.setOpacity(1.0);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        this.backup = canvas.snapshot(params, null);

        canvas.setOpacity(ogOpacity);
        canvas.setVisible(ogVisible);
        this.present = filter.apply(backup);

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
