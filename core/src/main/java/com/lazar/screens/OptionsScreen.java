package com.lazar.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.lazar.StartGame;
import com.lazar.ai.model.AiDecisionPayload;
import com.lazar.ai.service.*;
import com.lazar.config.LocalizationManager;
import com.lazar.ui.SettingsTheme;

/** Scrollable medieval settings, with AI actions kept inside their own section. */
public final class OptionsScreen extends BaseMenuScreen {
    private static final String[] LANGUAGES = {"en", "ru", "ro"};
    private static final String[] LANGUAGE_NAMES = {"English", "Русский", "Română"};
    private static final String[] REMOTE_MODELS = {"gpt-4.1-mini", "gpt-4.1-nano", "gpt-4.1"};
    private static final String[] LOCAL_MODELS = {"qwen2.5:7b-instruct", "qwen2.5:3b", "qwen2.5:1.5b"};
    private final Screen continueScreen;
    private Stage stage;
    private SettingsTheme theme;
    private Preferences prefs;
    private AiSettings ai;
    private ScrollPane scroll;
    private Table aiFields;
    private Label status;
    private SelectBox<String> providerSelect, modelSelect, languageSelect;
    private TextField keyField, addressField, customModelField;
    private TextButton applyButton, testButton;
    private AiSettings.Provider provider;
    private String localModel, remoteModel, draftKey, draftAddress;
    private boolean busy, disposed, building, aiDirty;
    private final Array<SelectBox<String>> dropdowns = new Array<>();

    public OptionsScreen(StartGame game, Screen continueScreen) {
        super(game);
        this.continueScreen = continueScreen;
    }

    private String t(String key) { return LocalizationManager.get(key); }

    @Override protected void onShow() {
        prefs = Gdx.app.getPreferences(StartGame.SETTINGS_PREFS);
        ai = game.getAiSettings();
        provider = ai.getProvider();
        localModel = ai.getModel(AiSettings.Provider.OLLAMA);
        remoteModel = ai.getModel(AiSettings.Provider.OPENAI);
        draftKey = ai.getSessionKey(); draftAddress = ai.getLocalUrl();
        theme = new SettingsTheme();
        stage = new Stage(viewport);
        build(0);
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            @Override public boolean keyDown(int key) {
                if (key != Input.Keys.ESCAPE) return false;
                for (SelectBox<String> dropdown : dropdowns) {
                    if (dropdown.getScrollPane().getStage() != null) { dropdown.hideList(); return true; }
                }
                goBack(); return true;
            }
        }, stage));
    }

    private void build(float scrollPercent) {
        building = true;
        stage.clear(); dropdowns.clear();
        Table panel = new Table(); panel.setName("settings-panel");
        panel.setBackground(theme.panel); panel.setBounds(128, 32, 1024, 656);
        stage.addActor(panel);
        Table header = new Table(); header.defaults().left();
        Label brand = new Label("THRONE", theme.skin, "small"); brand.setColor(SettingsTheme.GOLD);
        header.add(brand).expandX().row();
        header.add(new Label(t("options.title"), theme.skin, "title")).padTop(5).row();
        header.add(new Label(t("settings.subtitle"), theme.skin, "small")).padTop(5).row();
        panel.add(header).growX().pad(23, 32, 18, 32).row();
        Table content = new Table(); content.top(); content.defaults().growX().padBottom(14);
        content.add(displaySection()).row();
        content.add(audioSection()).row();
        content.add(languageSection()).row();
        content.add(aiSection()).row();
        scroll = new ScrollPane(content, theme.skin); scroll.setName("settings-scroll");
        scroll.setScrollingDisabled(true, false); scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false); scroll.setSmoothScrolling(true);
        scroll.setFlickScroll(true); scroll.setScrollbarsOnTop(false);
        panel.add(scroll).grow().minHeight(0).pad(0, 24, 0, 24).row();
        Table footer = new Table();
        TextButton back = new TextButton(t("button.back"), theme.skin); back.setName("settings-back");
        changed(back, this::goBack);
        footer.add(new Label(t("settings.saved"), theme.skin, "small")).center().row();
        footer.add(back).width(180).height(44).padTop(10);
        panel.add(footer).growX().pad(12, 32, 16, 32);
        panel.validate(); scroll.layout();
        scroll.setScrollPercentY(scrollPercent); scroll.updateVisualScroll();
        stage.setScrollFocus(scroll);
        building = false;
    }

    private Table section(String title, String subtitle) {
        Table card = new Table(); card.setBackground(theme.card); card.pad(22, 25, 7, 25);
        card.defaults().growX();
        Label heading = new Label(t(title), theme.skin, "section");
        card.add(heading).left().row();
        Label description = new Label(t(subtitle), theme.skin, "small"); description.setWrap(true);
        card.add(description).padTop(4).padBottom(20).row();
        return card;
    }

    private Table description(String title, String subtitle) {
        Table labels = new Table(); labels.defaults().left().growX();
        labels.add(new Label(t(title), theme.skin)).row();
        if (subtitle != null) {
            Label help = new Label(t(subtitle), theme.skin, "small"); help.setWrap(true);
            labels.add(help).padTop(4).row();
        }
        return labels;
    }

    private void row(Table card, String title, String subtitle, Actor control) {
        Table row = new Table();
        row.add(description(title, subtitle)).growX().minWidth(0).padRight(28);
        row.add(control).width(332).minHeight(46).right();
        card.add(row).growX().padBottom(18).row();
    }

    private Table displaySection() {
        Table card = section("settings.display", "settings.display_help");
        row(card, "options.fullscreen", "settings.fullscreen_help", toggle("fullscreen", false, () -> {
            boolean fullscreen = prefs.getBoolean("fullscreen", false);
            if (fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            else Gdx.graphics.setWindowedMode(1280, 720);
        }));
        row(card, "options.vsync", "settings.vsync_help", toggle("vsync", true,
            () -> Gdx.graphics.setVSync(prefs.getBoolean("vsync", true))));
        return card;
    }

    private Table toggle(String setting, boolean fallback, Runnable effect) {
        Table holder = new Table();
        TextButton button = new TextButton("", theme.skin, "toggle"); button.setName("settings-" + setting);
        button.setChecked(prefs.getBoolean(setting, fallback));
        button.setText(t(button.isChecked() ? "settings.on" : "settings.off"));
        changed(button, () -> {
            prefs.putBoolean(setting, button.isChecked()); prefs.flush();
            button.setText(t(button.isChecked() ? "settings.on" : "settings.off")); effect.run();
        });
        holder.add().growX(); holder.add(button).size(126, 43);
        return holder;
    }

    private Table audioSection() {
        Table card = section("settings.audio", "settings.audio_help");
        row(card, "options.music", null, volume(true));
        row(card, "options.effects", null, volume(false));
        return card;
    }

    private Table volume(boolean music) {
        Table holder = new Table();
        Slider slider = new Slider(0, 100, 1, false, theme.skin); slider.setName(music ? "settings-music" : "settings-effects");
        slider.setValue(music ? game.getSoundManager().getMusicVolume() : game.getSoundManager().getEffectsVolume());
        Label value = new Label((int) slider.getValue() + "%", theme.skin, "small"); value.setAlignment(Align.right);
        changed(slider, () -> {
            value.setText((int) slider.getValue() + "%");
            if (music) game.getSoundManager().setMusicVolume((int) slider.getValue());
            else game.getSoundManager().setEffectsVolume((int) slider.getValue());
        });
        holder.add(slider).growX().height(32); holder.add(value).width(58).padLeft(12);
        return holder;
    }

    private Table languageSection() {
        Table card = section("settings.language", "settings.language_help");
        languageSelect = select("settings-language", LANGUAGE_NAMES);
        String language = prefs.getString("language", "en");
        for (int i = 0; i < LANGUAGES.length; i++) if (LANGUAGES[i].equals(language)) languageSelect.setSelectedIndex(i);
        changed(languageSelect, () -> {
            if (building) return;
            captureDraft(); float position = scroll.getScrollPercentY();
            String code = LANGUAGES[languageSelect.getSelectedIndex()];
            prefs.putString("language", code); prefs.flush();
            languageSelect.hideList(); LocalizationManager.loadLanguage(code); build(position);
        });
        row(card, "language.label", null, dropdown(languageSelect));
        return card;
    }

    private Table aiSection() {
        Table card = section("ai.settings", "settings.ai_help");
        providerSelect = select("settings-provider", t("ai.local"), t("ai.cloud"));
        providerSelect.setSelectedIndex(provider == AiSettings.Provider.OLLAMA ? 0 : 1);
        changed(providerSelect, () -> {
            if (building) return;
            captureDraft();
            provider = providerSelect.getSelectedIndex() == 0 ? AiSettings.Provider.OLLAMA : AiSettings.Provider.OPENAI;
            rebuildAiFields(); markDirty();
        });
        row(card, "settings.provider", "settings.provider_help", dropdown(providerSelect));
        aiFields = new Table(); aiFields.defaults().growX();
        card.add(aiFields).growX().row(); rebuildAiFields();
        return card;
    }

    private void rebuildAiFields() {
        building = true;
        aiFields.clear();
        String current = provider == AiSettings.Provider.OPENAI ? remoteModel : localModel;
        String[] presets = provider == AiSettings.Provider.OPENAI ? REMOTE_MODELS : LOCAL_MODELS;
        Array<String> choices = new Array<>(presets); choices.add(t("settings.custom_model"));
        if (modelSelect != null) { modelSelect.hideList(); dropdowns.removeValue(modelSelect, true); }
        modelSelect = select("settings-model", choices.toArray(String.class));
        int index = choices.indexOf(current, false);
        modelSelect.setSelectedIndex(index < 0 ? choices.size - 1 : index);
        customModelField = field("settings-custom-model", current, false); customModelField.setMaxLength(160);
        Table customHolder = new Table();
        Runnable updateCustom = () -> {
            customHolder.clear();
            if (modelSelect.getSelectedIndex() == modelSelect.getItems().size - 1) {
                row(customHolder, "settings.model_name", null, customModelField);
            }
            aiFields.invalidateHierarchy();
        };
        changed(modelSelect, () -> { updateCustom.run(); markDirty(); });
        row(aiFields, "ai.model", provider == AiSettings.Provider.OPENAI ? "settings.remote_model_help" : "settings.local_model_help", dropdown(modelSelect));
        aiFields.add(customHolder).growX().row(); updateCustom.run();
        if (provider == AiSettings.Provider.OPENAI) {
            keyField = field("settings-api-key", draftKey, true);
            keyField.setMessageText(t("settings.key_placeholder")); keyField.setMaxLength(1024);
            row(aiFields, "ai.key", "settings.key_help", keyField);
            addressField = null;
        } else {
            addressField = field("settings-local-address", draftAddress, false); addressField.setMaxLength(256);
            row(aiFields, "ai.address", "settings.local_address_help", addressField);
            keyField = null;
        }
        Label notice = new Label(t(provider == AiSettings.Provider.OPENAI ? "ai.cloud_notice" : "ai.local_notice"), theme.skin, "small");
        notice.setWrap(true); aiFields.add(notice).growX().padBottom(12).row();
        if (provider == AiSettings.Provider.OPENAI) {
            Label billing = new Label(t("ai.cloud_test_notice"), theme.skin, "small"); billing.setWrap(true);
            aiFields.add(billing).growX().padBottom(15).row();
        } else {
            Label command = new Label(t("ai.local_command").replace("{model}", current), theme.skin, "small"); command.setWrap(true);
            aiFields.add(command).growX().padBottom(15).row();
            changed(modelSelect, () -> command.setText(t("ai.local_command").replace("{model}", selectedModel())));
            changed(customModelField, () -> command.setText(t("ai.local_command").replace("{model}", selectedModel())));
        }
        Table actions = new Table(); actions.defaults().height(38);
        actions.add().growX();
        testButton = new TextButton(t("settings.check"), theme.skin); testButton.setName("settings-test");
        changed(testButton, this::testConnection); actions.add(testButton).width(130).padRight(10);
        applyButton = new TextButton(t("ai.apply"), theme.skin, "primary"); applyButton.setName("settings-apply");
        changed(applyButton, this::applyAi); actions.add(applyButton).width(145);
        aiFields.add(actions).growX().padBottom(14).row();
        status = new Label(t(aiDirty ? "settings.unsaved" : "settings.ai_ready"), theme.skin, "small");
        status.setWrap(true); status.setName("settings-status");
        aiFields.add(status).growX().minHeight(28).padBottom(12).row();
        building = false;
    }

    private String selectedModel() {
        return modelSelect.getSelectedIndex() == modelSelect.getItems().size - 1 ? customModelField.getText() : modelSelect.getSelected();
    }

    private void captureDraft() {
        if (modelSelect == null) return;
        if (provider == AiSettings.Provider.OPENAI) {
            remoteModel = selectedModel(); if (keyField != null) draftKey = keyField.getText();
        } else {
            localModel = selectedModel(); if (addressField != null) draftAddress = addressField.getText();
        }
    }

    private boolean applyAi() {
        captureDraft();
        try {
            ai.apply(provider, provider == AiSettings.Provider.OPENAI ? remoteModel : localModel, draftAddress, draftKey);
            aiDirty = false;
            feedback(t(provider == AiSettings.Provider.OPENAI && draftKey.trim().isEmpty() ? "ai.saved_missing_key" : "ai.applied"), false);
            return true;
        } catch (AiServiceException error) { feedback(error.getMessage(), true); return false; }
    }

    private void testConnection() {
        if (busy) return;
        captureDraft();
        final AiClient client;
        try { client = ai.createClient(provider, provider == AiSettings.Provider.OPENAI ? remoteModel : localModel, draftAddress, draftKey); }
        catch (AiServiceException error) { feedback(error.getMessage(), true); return; }
        busy = true; setAiDisabled(true); feedback(t("ai.testing"), false);
        Thread worker = new Thread(() -> {
            boolean success = false; String message;
            try {
                AiDecisionPayload answer = client.classifyDecision("Return only JSON: {\"decision\":\"A\",\"narrative\":\"OK\",\"reason\":\"Test\"}", "A");
                if (answer == null || answer.decision == null) throw new IllegalStateException();
                success = true; message = null;
            } catch (Exception error) { message = AiServiceException.messageFor(error); }
            final boolean ok = success; final String result = message;
            Gdx.app.postRunnable(() -> {
                if (disposed) return;
                busy = false; setAiDisabled(false); feedback(ok ? t("settings.check_success") : result, !ok);
            });
        }, "throne-ai-test");
        worker.setDaemon(true); worker.start();
    }

    private void setAiDisabled(boolean disabled) {
        providerSelect.setDisabled(disabled); modelSelect.setDisabled(disabled);
        customModelField.setDisabled(disabled);
        if (keyField != null) keyField.setDisabled(disabled);
        if (addressField != null) addressField.setDisabled(disabled);
        testButton.setDisabled(disabled); applyButton.setDisabled(disabled); languageSelect.setDisabled(disabled);
    }

    private SelectBox<String> select(String name, String... items) {
        SelectBox<String> select = new SelectBox<String>(theme.skin) {
            @Override protected void onShow(Actor list, boolean below) {
                list.clearActions(); list.getColor().a = 1f;
            }
            @Override protected void onHide(Actor list) { list.remove(); }
        };
        select.setName(name); select.setItems(items);
        select.setMaxListCount(5); select.setAlignment(Align.left); dropdowns.add(select); return select;
    }

    private Actor dropdown(SelectBox<String> select) {
        Stack stack = new Stack(); stack.add(select);
        Table arrow = new Table(); arrow.setTouchable(Touchable.disabled); arrow.right();
        arrow.add(new Image(theme.caret)).size(10, 6).padRight(15); stack.add(arrow); return stack;
    }

    private TextField field(String name, String value, boolean secret) {
        TextField field = new TextField(value, theme.skin); field.setName(name);
        field.setPasswordMode(secret); field.setPasswordCharacter('*');
        changed(field, this::markDirty); return field;
    }

    private void changed(Actor actor, Runnable action) {
        actor.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor source) {
                if (!building && !disposed) action.run();
            }
        });
    }

    private void markDirty() {
        if (building) return;
        aiDirty = true;
        if (status != null) feedback(t("settings.unsaved"), false);
    }
    private void feedback(String message, boolean error) {
        status.setText(message); status.setColor(error ? com.badlogic.gdx.graphics.Color.valueOf("943C26") : SettingsTheme.GREEN);
    }
    private void goBack() { game.setScreen(new MainMenuScreen(game, continueScreen)); dispose(); }

    @Override public void render(float delta) {
        beginFrame(); drawBackground();
        endFrame();
        stage.act(Math.min(delta, 0.05f)); stage.draw();
    }

    @Override public void hide() { for (SelectBox<String> dropdown : dropdowns) dropdown.hideList(); }
    @Override public void dispose() {
        if (disposed) return;
        disposed = true; draftKey = "";
        if (keyField != null) keyField.setText("");
        if (stage != null) stage.dispose();
        if (theme != null) theme.dispose();
        super.dispose();
    }
}
