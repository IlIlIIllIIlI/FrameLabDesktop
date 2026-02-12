package com.frameLab.frameSprite.effect;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;

public class Paint implements Command {
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;

    private WritableImage backup;
    private WritableImage present;

    private final int x;
    private final int y;
    private final int width;
    private final int height;


    Paint(Canvas canvas,int x,int y, int width, int height){
        this.canvas = canvas;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.graphicsContext = canvas.getGraphicsContext2D();
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
