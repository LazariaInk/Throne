package com.lazar;

import com.badlogic.gdx.Game;
import com.lazar.screens.GameScreen;

public class StartGame extends Game {

    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
