package com.lazar;

import com.badlogic.gdx.Game;
import com.lazar.screens.GameScreen;
import com.lazar.screens.StartScreen;

public class StartGame extends Game {

    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }
}
