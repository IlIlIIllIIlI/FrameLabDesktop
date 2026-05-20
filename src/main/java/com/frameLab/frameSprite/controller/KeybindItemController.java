package com.frameLab.frameSprite.controller;

import com.frameLab.frameSprite.service.SettingsService;
import com.frameLab.frameSprite.utils.Actions;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class KeybindItemController {
    @FXML
    private Label actionLabel;
    @FXML
    private Button captureButton;
    @FXML
    private Button resetButton;

    private Actions action;
    private SettingsService keys = SettingsService.getInstance();

    public void initData(Actions action) {
        this.action = action;

        String formattedName = action.name().replace("_", " ");
        formattedName = formattedName.charAt(0) + formattedName.substring(1).toLowerCase();
        actionLabel.setText(formattedName);

        refreshDisplay();

        captureButton.setOnKeyPressed(this::handleKeyCapture);
    }

    private void handleKeyCapture(KeyEvent keyEvent) {
        if (keyEvent.getCode().isModifierKey()) return;

        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            captureButton.getParent().requestFocus();
            return;
        }

        KeyCombination newCombo = new KeyCodeCombination(
                keyEvent.getCode(),
                keyEvent.isShiftDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                keyEvent.isControlDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                keyEvent.isAltDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                keyEvent.isMetaDown() ? KeyCombination.ModifierValue.DOWN : KeyCombination.ModifierValue.UP,
                KeyCombination.ModifierValue.UP
        );

        keys.updateBind(action, newCombo);
        refreshDisplay();
        keyEvent.consume();
    }

    private void refreshDisplay() {
        KeyCombination currentBind = keys.getBind(action);
        if (currentBind != null) {
            captureButton.setText(currentBind.getDisplayText());
        } else {
            captureButton.setText("Unbound");
        }
    }

    @FXML
    private void handleReset(ActionEvent actionEvent) {
        keys.resetToDefault(action);
        refreshDisplay();
    }
}
