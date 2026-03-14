package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lazar.config.BackendDecisionResolver;
import com.lazar.config.DecisionResolver;
import com.lazar.engine.GameEngine;
import com.lazar.engine.GameOverType;
import com.lazar.logic.GameStats;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;
import com.lazar.ui.background.BlurBackgroundRenderer;
import com.lazar.ui.card.CardPresenter;
import com.lazar.ui.card.CardRenderResources;
import com.lazar.ui.card.CardRenderer;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Texture background;
    private Texture whiteTexture;
    private TextureRegion whiteRegion;
    private Texture sendButtonTexture;

    private Texture moneyIcon;
    private Texture armyIcon;
    private Texture peopleIcon;
    private Texture religionIcon;

    private Music backgroundMusic;
    private Sound cardSwapSound;

    private OrthographicCamera camera;
    private Viewport viewport;

    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private BitmapFont hintFont;
    private BitmapFont inputFont;
    private BitmapFont hudFont;

    private GlyphLayout layout;

    private ShaderProgram blurShader;
    private ShaderProgram ovalMaskShader;

    private BlurBackgroundRenderer backgroundRenderer;
    private CardPresenter cardPresenter;

    private final DecisionResolver decisionResolver;
    private final GameEngine gameEngine;

    private final StringBuilder typedMessage = new StringBuilder();
    private final Rectangle sendButtonBounds = new Rectangle();
    private final Rectangle inputBounds = new Rectangle();
    private final Vector3 touchPoint = new Vector3();

    private boolean showCaret = true;
    private float caretTimer = 0f;

    private boolean requestInFlight = false;
    private String uiMessage = null;
    private GameOverType gameOverType = null;

    private static final Color STAT_FILL_COLOR = new Color(0x8B572Aff);
    private static final Color STAT_EMPTY_TINT = new Color(0.22f, 0.17f, 0.12f, 0.28f);
    private static final Color HUD_PANEL_DARK = new Color(0.16f, 0.10f, 0.05f, 0.82f);
    private static final Color HUD_PANEL_LIGHT = new Color(0.93f, 0.87f, 0.73f, 0.94f);
    private static final Color BADGE_OUTER = new Color(0.20f, 0.13f, 0.07f, 0.96f);
    private static final Color BADGE_INNER = new Color(0.95f, 0.90f, 0.78f, 0.98f);
    private static final Color BADGE_SHADOW = new Color(0f, 0f, 0f, 0.16f);
    private static final Color BADGE_RING = new Color(0.55f, 0.38f, 0.20f, 0.55f);

    private static final float BACKGROUND_MUSIC_VOLUME = 0.35f;
    private static final float CARD_SWAP_VOLUME = 0.75f;

    public GameScreen() {
        this(new BackendDecisionResolver(), new GameEngine());
    }

    public GameScreen(DecisionResolver decisionResolver, GameEngine gameEngine) {
        this.decisionResolver = decisionResolver;
        this.gameEngine = gameEngine;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        background = new Texture(Gdx.files.internal("images/background.png"));

        moneyIcon = new Texture(Gdx.files.internal("images/ui/money.png"));
        armyIcon = new Texture(Gdx.files.internal("images/ui/army.png"));
        peopleIcon = new Texture(Gdx.files.internal("images/ui/people.png"));
        religionIcon = new Texture(Gdx.files.internal("images/ui/religion.png"));
        sendButtonTexture = new Texture(Gdx.files.internal("images/ui/send-btn.png"));

        loadAudio();
        startBackgroundMusic();

        sendButtonTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        moneyIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        armyIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        peopleIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        religionIcon.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();

        camera.position.set(640, 360, 0);
        camera.update();

        whiteTexture = createWhiteTexture();
        whiteRegion = new TextureRegion(whiteTexture);

        titleFont = generateFont(26, new Color(0.14f, 0.08f, 0.04f, 1f));
        bodyFont = generateFont(18, new Color(0.18f, 0.11f, 0.06f, 1f));
        hintFont = generateFont(15, new Color(0.20f, 0.13f, 0.07f, 0.95f));
        inputFont = generateFont(17, new Color(0.20f, 0.13f, 0.07f, 1f));
        hudFont = generateFont(15, new Color(0.18f, 0.11f, 0.06f, 1f));

        layout = new GlyphLayout();

        initShaders();

        CardRenderResources cardRenderResources = new CardRenderResources(
            whiteRegion,
            titleFont,
            bodyFont,
            ovalMaskShader
        );

        CardRenderer cardRenderer = new CardRenderer(cardRenderResources);
        cardPresenter = new CardPresenter(cardRenderer);

        backgroundRenderer = new BlurBackgroundRenderer(background, whiteRegion, blurShader);

        EventCard firstCard = gameEngine.nextCard();
        cardPresenter.showNewEvent(firstCard, gameEngine.getRunState().getStats());

        installInputProcessor();
    }

    private void loadAudio() {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/background-music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(BACKGROUND_MUSIC_VOLUME);

        cardSwapSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card-swap.mp3"));
    }

    private void startBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    private void playCardSwapSound() {
        if (cardSwapSound != null) {
            cardSwapSound.play(CARD_SWAP_VOLUME);
        }
    }

    private Texture createWhiteTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void installInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!cardPresenter.canTypeMessage() || gameOverType != null) {
                    return false;
                }

                if (character == '\b') {
                    if (typedMessage.length() > 0) {
                        typedMessage.deleteCharAt(typedMessage.length() - 1);
                    }
                    return true;
                }

                if (character == '\r' || character == '\n') {
                    submitPlayerMessage();
                    return true;
                }

                if (!Character.isISOControl(character)) {
                    if (typedMessage.length() < 120) {
                        typedMessage.append(character);
                    }
                    return true;
                }

                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button != Input.Buttons.LEFT) {
                    return false;
                }

                viewport.unproject(touchPoint.set(screenX, screenY, 0f));
                float worldX = touchPoint.x;
                float worldY = touchPoint.y;

                if (cardPresenter.canTypeMessage() && sendButtonBounds.contains(worldX, worldY)) {
                    submitPlayerMessage();
                    return true;
                }

                if (cardPresenter.canAdvanceCard()) {
                    advanceToNextCard();
                    return true;
                }

                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (cardPresenter.canAdvanceCard() && keycode == Input.Keys.SPACE) {
                    advanceToNextCard();
                    return true;
                }
                return false;
            }
        });
    }

    private void submitPlayerMessage() {
        if (!cardPresenter.canTypeMessage() || requestInFlight || gameOverType != null) {
            return;
        }

        String message = typedMessage.toString().trim();
        if (message.isEmpty()) {
            return;
        }

        requestInFlight = true;
        uiMessage = null;
        cardPresenter.markSubmitting();

        gameEngine.submitPlayerText(decisionResolver, message, new DecisionResolver.Callback() {
            @Override
            public void onSuccess(DecisionResolution resolution) {
                requestInFlight = false;
                playCardSwapSound();

                cardPresenter.resolveFromBackend(
                    resolution,
                    gameEngine.getRunState().getStats()
                );

                gameOverType = gameEngine.checkGameOver();
                if (gameOverType != null) {
                    uiMessage = buildGameOverMessage(gameOverType);
                } else {
                    uiMessage = buildResolutionMessage(resolution);
                }
            }

            @Override
            public void onError(String message) {
                requestInFlight = false;
                cardPresenter.cancelSubmitting();
                uiMessage = message;
            }
        });
    }

    private void advanceToNextCard() {
        if (!cardPresenter.canAdvanceCard() || gameOverType != null) {
            return;
        }

        playCardSwapSound();

        EventCard nextCard = gameEngine.nextCard();
        cardPresenter.showNewEvent(nextCard, gameEngine.getRunState().getStats());

        typedMessage.setLength(0);
        uiMessage = null;
    }

    @Override
    public void render(float delta) {
        camera.update();
        cardPresenter.update(delta);
        updateCaret(delta);

        if (cardPresenter.shouldRenderBlurredBackground()) {
            backgroundRenderer.renderBlurred(batch, camera, viewport, cardPresenter.getBlurAlpha());
        } else {
            backgroundRenderer.renderNormal(batch, camera, viewport);
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        drawTopStats(gameEngine.getRunState().getStats(), viewport.getWorldWidth(), viewport.getWorldHeight());
        cardPresenter.render(batch, viewport.getWorldWidth(), viewport.getWorldHeight());

        if (gameOverType != null) {
            drawGameOverHint(viewport.getWorldWidth());
        } else if (cardPresenter.canAdvanceCard()) {
            drawNextHint(viewport.getWorldWidth());
        } else if (cardPresenter.canTypeMessage()) {
            drawBottomInput(viewport.getWorldWidth());
        } else if (cardPresenter.isWaitingForBackend()) {
            drawLoadingHint(viewport.getWorldWidth());
        }

        if (uiMessage != null && !uiMessage.isEmpty()) {
            drawUiMessage(viewport.getWorldWidth());
        }

        batch.end();
    }

    private void updateCaret(float delta) {
        caretTimer += delta;
        if (caretTimer >= 0.45f) {
            caretTimer = 0f;
            showCaret = !showCaret;
        }
    }

    private void drawTopStats(GameStats stats, float worldWidth, float worldHeight) {
        float badgeSize = 82f;
        float gap = 26f;
        float panelPaddingX = 28f;
        float panelPaddingY = 16f;

        float totalBadgesWidth = badgeSize * 4f + gap * 3f;
        float panelWidth = totalBadgesWidth + panelPaddingX * 2f;
        float panelHeight = badgeSize + panelPaddingY * 2f;

        float panelX = worldWidth / 2f - panelWidth / 2f;
        float panelY = worldHeight - panelHeight - 18f;

        drawHudPanel(panelX, panelY, panelWidth, panelHeight);

        float startX = panelX + panelPaddingX;
        float badgeY = panelY + panelPaddingY;

        drawStatBadge(startX + (badgeSize + gap) * 0f, badgeY, badgeSize, religionIcon, stats.getReligion());
        drawStatBadge(startX + (badgeSize + gap), badgeY, badgeSize, peopleIcon, stats.getPeople());
        drawStatBadge(startX + (badgeSize + gap) * 2f, badgeY, badgeSize, armyIcon, stats.getArmy());
        drawStatBadge(startX + (badgeSize + gap) * 3f, badgeY, badgeSize, moneyIcon, stats.getMoney());
    }

    private void drawHudPanel(float x, float y, float w, float h) {
        batch.setColor(0f, 0f, 0f, 0.18f);
        batch.draw(whiteRegion, x + 6f, y - 6f, w, h);

        batch.setColor(HUD_PANEL_DARK);
        batch.draw(whiteRegion, x, y, w, h);

        batch.setColor(HUD_PANEL_LIGHT);
        batch.draw(whiteRegion, x + 3f, y + 3f, w - 6f, h - 6f);

        batch.setColor(1f, 1f, 1f, 0.05f);
        batch.draw(whiteRegion, x + 10f, y + h - 18f, w - 20f, 6f);

        batch.setColor(Color.WHITE);
    }

    private void drawStatBadge(float x, float y, float size, Texture icon, int value) {
        float clamped = Math.max(0f, Math.min(100f, value));
        float fill = clamped / 100f;

        drawBadgeBackground(x, y, size);
        drawFilledIcon(x, y, size, icon, fill);
    }

    private void drawBadgeBackground(float x, float y, float size) {
        batch.setColor(BADGE_SHADOW);
        batch.draw(whiteRegion, x + 4f, y - 4f, size, size);

        batch.setColor(BADGE_OUTER);
        batch.draw(whiteRegion, x, y, size, size);

        batch.setColor(BADGE_INNER);
        batch.draw(whiteRegion, x + 4f, y + 4f, size - 8f, size - 8f);

        batch.setColor(BADGE_RING);
        batch.draw(whiteRegion, x + 8f, y + 8f, size - 16f, 2f);
        batch.draw(whiteRegion, x + 8f, y + size - 10f, size - 16f, 2f);
        batch.draw(whiteRegion, x + 8f, y + 8f, 2f, size - 16f);
        batch.draw(whiteRegion, x + size - 10f, y + 8f, 2f, size - 16f);

        batch.setColor(Color.WHITE);
    }

    private void drawFilledIcon(float x, float y, float badgeSize, Texture icon, float fill) {
        float iconSize = badgeSize * 0.58f;
        float iconX = x + (badgeSize - iconSize) / 2f;
        float iconY = y + (badgeSize - iconSize) / 2f;

        batch.setColor(STAT_EMPTY_TINT);
        batch.draw(icon, iconX, iconY, iconSize, iconSize);

        if (fill > 0f) {
            int texW = icon.getWidth();
            int texH = icon.getHeight();
            int filledPx = Math.max(1, Math.round(texH * fill));
            float filledHeight = iconSize * fill;

            batch.setColor(STAT_FILL_COLOR);
            batch.draw(
                icon,
                iconX,
                iconY,
                iconSize,
                filledHeight,
                0,
                texH - filledPx,
                texW,
                filledPx,
                false,
                false
            );
        }

        batch.setColor(Color.WHITE);
    }

    private void drawNextHint(float worldWidth) {
        String hint = "SPACE / CLICK pentru urmatoarea carte";
        hintFont.setColor(0.25f, 0.16f, 0.08f, 0.95f);
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, (worldWidth - layout.width) / 2f, 44f);
    }

    private void drawLoadingHint(float worldWidth) {
        String hint = "Consilierii judeca hotararea ta...";
        hintFont.setColor(0.25f, 0.16f, 0.08f, 0.95f);
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, (worldWidth - layout.width) / 2f, 44f);
    }

    private void drawGameOverHint(float worldWidth) {
        String hint = "Partida s-a incheiat";
        hintFont.setColor(0.55f, 0.12f, 0.08f, 0.95f);
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, (worldWidth - layout.width) / 2f, 44f);
    }

    private void drawUiMessage(float worldWidth) {
        hintFont.setColor(0.55f, 0.12f, 0.08f, 0.95f);
        layout.setText(hintFont, uiMessage, hintFont.getColor(), worldWidth - 120f, Align.center, true);
        hintFont.draw(batch, layout, 60f, 92f);
    }

    private void drawBottomInput(float worldWidth) {
        float inputW = 420f;
        float inputH = 54f;
        float inputX = worldWidth / 2f - inputW / 2f;
        float inputY = 24f;

        inputBounds.set(inputX, inputY, inputW, inputH);
        sendButtonBounds.set(inputX + inputW + 14f, inputY, 54f, 54f);

        batch.setColor(0f, 0f, 0f, 0.14f);
        batch.draw(whiteRegion, inputX + 6f, inputY - 6f, inputW, inputH);

        batch.setColor(0.25f, 0.16f, 0.08f, 0.95f);
        batch.draw(whiteRegion, inputX, inputY, inputW, inputH);

        batch.setColor(0.93f, 0.87f, 0.73f, 0.98f);
        batch.draw(whiteRegion, inputX + 3f, inputY + 3f, inputW - 6f, inputH - 6f);

        String textToDraw;
        Color fontColor;

        if (typedMessage.length() == 0) {
            textToDraw = "type your message here...";
            fontColor = new Color(0.33f, 0.24f, 0.15f, 0.68f);
        } else {
            textToDraw = typedMessage.toString();
            if (showCaret) {
                textToDraw += "|";
            }
            fontColor = new Color(0.20f, 0.13f, 0.07f, 1f);
        }

        inputFont.setColor(fontColor);
        layout.setText(inputFont, textToDraw, fontColor, inputW - 26f, Align.left, false);
        inputFont.draw(batch, layout, inputX + 14f, inputY + 34f);

        drawSendButton(sendButtonBounds.x, sendButtonBounds.y, sendButtonBounds.width, sendButtonBounds.height);
    }

    private void drawSendButton(float x, float y, float w, float h) {
        batch.setColor(0f, 0f, 0f, 0.14f);
        batch.draw(whiteRegion, x + 5f, y - 5f, w, h);

        batch.setColor(0.25f, 0.16f, 0.08f, 0.95f);
        batch.draw(whiteRegion, x, y, w, h);

        batch.setColor(0.93f, 0.87f, 0.73f, 0.98f);
        batch.draw(whiteRegion, x + 3f, y + 3f, w - 6f, h - 6f);

        batch.setColor(1f, 1f, 1f, 0.10f);
        batch.draw(whiteRegion, x + 8f, y + h - 12f, w - 16f, 3f);

        float pad = 9f;
        batch.setColor(Color.WHITE);
        batch.draw(sendButtonTexture, x + pad, y + pad, w - pad * 2f, h - pad * 2f);
    }

    private String buildResolutionMessage(DecisionResolution resolution) {
        if (resolution == null || resolution.option == null) {
            return null;
        }

        switch (resolution.option) {
            case A:
                return "AI-ul a judecat raspunsul tau ca optiunea A.";
            case B:
                return "AI-ul a judecat raspunsul tau ca optiunea B.";
            case C:
                return "AI-ul a judecat raspunsul tau ca raspuns neclar sau in afara contextului (C).";
            default:
                return null;
        }
    }

    private String buildGameOverMessage(GameOverType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case INVASION:
                return "Armata s-a prabusit. Regatul ramane fara aparare si este zdrobit de invazie.";
            case MILITARY_COUP:
                return "Armata a devenit mai puternica decat coroana. Generalii iti iau tronul.";
            case BANKRUPTCY:
                return "Vistieria este goala. Coroana cade in ruina si neplata.";
            case OLIGARCHY:
                return "Aurul a corupt totul. Coroana devine marioneta negustorilor.";
            case RELIGIOUS_REVOLT:
                return "Legitimitatea sacra s-a frant. Regatul este inghitit de revolta religioasa.";
            case THEOCRACY:
                return "Credinta a inghitit coroana. Tronul tau este redus la umbra unei teocratii.";
            case DEAD_KINGDOM:
                return "Tinuturile s-au golit. Regatul a murit.";
            case OVERCROWDED_COLLAPSE:
                return "Supraaglomerarea si haosul au rupt ordinea regatului.";
            default:
                return "Regatul tau a cazut.";
        }
    }

    private BitmapFont generateFont(int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/medieval.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;
        parameter.color = color;
        parameter.kerning = true;
        parameter.hinting = FreeTypeFontGenerator.Hinting.Slight;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.borderWidth = 0f;
        parameter.shadowOffsetX = 0;
        parameter.shadowOffsetY = 0;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ĂÂÎȘȚăâîșț";

        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();

        font.setUseIntegerPositions(false);
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        return font;
    }

    private void initShaders() {
        ShaderProgram.pedantic = false;

        String vertexShader =
            "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                "uniform mat4 u_projTrans;\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "void main() {\n" +
                "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" +
                "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                "   gl_Position = u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                "}";

        String blurFragmentShader =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "uniform vec2 u_dir;\n" +
                "void main() {\n" +
                "    vec4 sum = vec4(0.0);\n" +
                "    sum += texture2D(u_texture, v_texCoords - 4.0 * u_dir) * 0.05;\n" +
                "    sum += texture2D(u_texture, v_texCoords - 3.0 * u_dir) * 0.09;\n" +
                "    sum += texture2D(u_texture, v_texCoords - 2.0 * u_dir) * 0.12;\n" +
                "    sum += texture2D(u_texture, v_texCoords - 1.0 * u_dir) * 0.15;\n" +
                "    sum += texture2D(u_texture, v_texCoords) * 0.18;\n" +
                "    sum += texture2D(u_texture, v_texCoords + 1.0 * u_dir) * 0.15;\n" +
                "    sum += texture2D(u_texture, v_texCoords + 2.0 * u_dir) * 0.12;\n" +
                "    sum += texture2D(u_texture, v_texCoords + 3.0 * u_dir) * 0.09;\n" +
                "    sum += texture2D(u_texture, v_texCoords + 4.0 * u_dir) * 0.05;\n" +
                "    gl_FragColor = sum * v_color;\n" +
                "}";

        String ovalMaskFragmentShader =
            "#ifdef GL_ES\n" +
                "precision mediump float;\n" +
                "#endif\n" +
                "varying vec4 v_color;\n" +
                "varying vec2 v_texCoords;\n" +
                "uniform sampler2D u_texture;\n" +
                "uniform vec2 u_center;\n" +
                "uniform vec2 u_radius;\n" +
                "uniform float u_softness;\n" +
                "void main() {\n" +
                "    vec2 p = (v_texCoords - u_center) / u_radius;\n" +
                "    float d = dot(p, p);\n" +
                "    float alpha = 1.0 - smoothstep(1.0 - u_softness, 1.0, d);\n" +
                "    vec4 tex = texture2D(u_texture, v_texCoords);\n" +
                "    gl_FragColor = vec4(tex.rgb * v_color.rgb, tex.a * v_color.a * alpha);\n" +
                "}";

        blurShader = new ShaderProgram(vertexShader, blurFragmentShader);
        if (!blurShader.isCompiled()) {
            throw new IllegalStateException("Blur shader error:\n" + blurShader.getLog());
        }

        ovalMaskShader = new ShaderProgram(vertexShader, ovalMaskFragmentShader);
        if (!ovalMaskShader.isCompiled()) {
            throw new IllegalStateException("Oval shader error:\n" + ovalMaskShader.getLog());
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (backgroundRenderer != null) {
            backgroundRenderer.resize();
        }
    }

    @Override
    public void pause() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        startBackgroundMusic();
    }

    @Override
    public void hide() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void dispose() {
        if (cardPresenter != null) {
            cardPresenter.dispose();
        }

        if (backgroundRenderer != null) {
            backgroundRenderer.dispose();
        }

        if (batch != null) batch.dispose();
        if (background != null) background.dispose();
        if (whiteTexture != null) whiteTexture.dispose();

        if (moneyIcon != null) moneyIcon.dispose();
        if (armyIcon != null) armyIcon.dispose();
        if (peopleIcon != null) peopleIcon.dispose();
        if (religionIcon != null) religionIcon.dispose();
        if (sendButtonTexture != null) sendButtonTexture.dispose();

        if (titleFont != null) titleFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (hintFont != null) hintFont.dispose();
        if (inputFont != null) inputFont.dispose();
        if (hudFont != null) hudFont.dispose();

        if (blurShader != null) blurShader.dispose();
        if (ovalMaskShader != null) ovalMaskShader.dispose();

        if (backgroundMusic != null) backgroundMusic.dispose();
        if (cardSwapSound != null) cardSwapSound.dispose();
    }
}
