package com.frameLab.frameSprite.effect;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Paint implements Command {
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;

    private WritableImage backup;
    private WritableImage present;

    private final double x;
    private final double y;
    private final double width;
    private final double height;


    public Paint(Canvas canvas, double x, double y, double width, double height){
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.graphicsContext = canvas.getGraphicsContext2D();
    }

    private void saveBackup() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        this.backup = canvas.snapshot(params, null);
    }

    public void savePresent() {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        this.present = canvas.snapshot(params, null);
    }

    @Override
    public void execute() {
        if (canvas != null&& backup != null) {
            graphicsContext.clearRect(x,y,width,height);
            graphicsContext.drawImage(present,x,y);
        }
    }

    @Override
    public void undo() {
        if (canvas != null&& backup != null) {
            graphicsContext.clearRect(x,y,width,height);
            graphicsContext.drawImage(backup,x,y);
        }
    }
}
