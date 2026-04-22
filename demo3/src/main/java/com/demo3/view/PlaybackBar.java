package com.demo3.view;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Bottom playback control bar.
 * Controls: Step Back, Pause, Play, Step Forward | Undo, Redo | Speed slider, Progress bar.
 */
public class PlaybackBar extends HBox {

    public final Button btnStepBack    = new Button("⏮ Step Back");
    public final Button btnPause       = new Button("⏸ Pause");
    public final Button btnPlay        = new Button("▶ Play");
    public final Button btnStepForward = new Button("Step Fwd ⏭");
    public final Button btnUndo        = new Button("↩ Undo");
    public final Button btnRedo        = new Button("Redo ↪");
    public final Slider speedSlider    = new Slider(100, 2000, 600);
    public final ProgressBar progressBar = new ProgressBar(0);

    public PlaybackBar(Runnable onStepBack, Runnable onPause, Runnable onPlay,
                       Runnable onStepForward, Runnable onUndo, Runnable onRedo) {
        getStyleClass().add("playback-bar");
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);

        btnStepBack.setOnAction(e -> onStepBack.run());
        btnPause.setOnAction(e -> onPause.run());
        btnPlay.setOnAction(e -> onPlay.run());
        btnStepForward.setOnAction(e -> onStepForward.run());
        btnUndo.setOnAction(e -> onUndo.run());
        btnRedo.setOnAction(e -> onRedo.run());

        for (Button b : new Button[]{btnStepBack, btnPause, btnPlay,
                                      btnStepForward, btnUndo, btnRedo}) {
            b.getStyleClass().add("button-playback");
        }
        btnPlay.getStyleClass().add("button-primary");

        speedSlider.setPrefWidth(140);
        speedSlider.setShowTickMarks(false);
        progressBar.setPrefWidth(200);
        progressBar.setProgress(0);

        Separator s1 = new Separator(Orientation.VERTICAL);
        Separator s2 = new Separator(Orientation.VERTICAL);
        s1.setOpacity(0.3);
        s2.setOpacity(0.3);

        Label speedLabel = new Label("Speed");
        speedLabel.getStyleClass().add("speed-label");
        VBox speedBox = new VBox(2, speedLabel, speedSlider);
        speedBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(
            btnStepBack, btnPause, btnPlay, btnStepForward,
            s1, btnUndo, btnRedo,
            s2, spacer, speedBox, progressBar
        );
    }
}
