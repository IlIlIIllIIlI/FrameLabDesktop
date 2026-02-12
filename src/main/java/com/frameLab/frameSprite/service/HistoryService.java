package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.effect.Command;

import java.util.Stack;

public class HistoryService {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();


    public void addCommand(Command command) {
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo(){
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    public void redo(){
        if (!redoStack.isEmpty()){
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
}
