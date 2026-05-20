package com.frameLab.frameSprite.service;

import com.frameLab.frameSprite.effect.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class HistoryServiceTest {

    private HistoryService historyService;
    private Command mockCommand1;
    private Command mockCommand2;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        // ARRANGE
        mockCommand1 = mock(Command.class);
        mockCommand2 = mock(Command.class);
    }

    @Test
    void shouldUndoCommand() {
        // ARRANGE
        historyService.addCommand(mockCommand1);

        // ACT
        historyService.undo();

        // ASSERT
        verify(mockCommand1, times(1)).undo();
    }

    @Test
    void shouldRedoCommand() {
        // ARRANGE
        historyService.addCommand(mockCommand1);
        historyService.undo();

        // ACT
        historyService.redo();

        // ASSERT
        verify(mockCommand1, times(1)).execute();
    }

    @Test
    void shouldClearRedoStackWhenNewCommandIsAdded() {
        // ARRANGE
        historyService.addCommand(mockCommand1);
        historyService.undo();

        // ACT
        historyService.addCommand(mockCommand2);
        historyService.redo();

        // ASSERT
        verify(mockCommand1, never()).execute();
    }

    @Test
    void shouldDoNothingWhenUndoStackIsEmpty() {
        // ACT
        historyService.undo();

        // ASSERT
        verifyNoInteractions(mockCommand1);
    }

    @Test
    void shouldDoNothingWhenRedoStackIsEmpty() {
        // ACT
        historyService.redo();

        // ASSERT
        verifyNoInteractions(mockCommand1);
    }
}