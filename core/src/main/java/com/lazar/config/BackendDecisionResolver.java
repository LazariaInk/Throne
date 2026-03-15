package com.lazar.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.lazar.dto.ResolveDecisionRequest;
import com.lazar.dto.ResolveDecisionResponse;
import com.lazar.model.Consequence;
import com.lazar.model.DecisionOption;
import com.lazar.model.DecisionResolution;
import com.lazar.model.EventCard;

public class BackendDecisionResolver implements DecisionResolver {

    private final Json json;
    private final DecisionRequestMapper requestMapper;
    private final ConsequenceMapper consequenceMapper;

    public BackendDecisionResolver() {
        this.json = new Json();
        this.json.setIgnoreUnknownFields(true);
        this.json.setOutputType(JsonWriter.OutputType.json);
        this.json.setUsePrototypes(false);

        this.requestMapper = new DecisionRequestMapper();
        this.consequenceMapper = new ConsequenceMapper();
    }

    @Override
    public void resolveDecision(EventCard event, String playerInput, Callback callback) {
        ResolveDecisionRequest requestBody = requestMapper.toRequest(event, playerInput);
        String requestJson = json.toJson(requestBody);

        Gdx.app.log("BackendDecisionResolver", "Request JSON: " + requestJson);

        HttpRequestBuilder builder = new HttpRequestBuilder();
        Net.HttpRequest request = builder
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(GameConfig.BASE_URL + "/api/game/resolve")
            .timeout(GameConfig.HTTP_TIMEOUT_MS)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .content(requestJson)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseText = httpResponse.getResultAsString();

                Gdx.app.postRunnable(() -> {
                    Gdx.app.log("BackendDecisionResolver", "Response status: " + statusCode);
                    Gdx.app.log("BackendDecisionResolver", "Response body: " + responseText);

                    if (statusCode < 200 || statusCode >= 300) {
                        callback.onError("Backend error " + statusCode);
                        return;
                    }

                    try {
                        ResolveDecisionResponse response =
                            json.fromJson(ResolveDecisionResponse.class, responseText);

                        if (response == null || response.consequence == null || response.resolvedOption == null) {
                            callback.onError("Raspuns invalid de la server");
                            return;
                        }

                        Consequence consequence = consequenceMapper.fromDto(response.consequence);
                        if (consequence == null) {
                            callback.onError("Raspuns invalid de la server");
                            return;
                        }

                        DecisionOption option;
                        try {
                            option = DecisionOption.valueOf(response.resolvedOption.trim().toUpperCase());
                        } catch (Exception ex) {
                            callback.onError("Optiune invalida primita de la server");
                            return;
                        }

                        callback.onSuccess(new DecisionResolution(option, consequence));
                    } catch (Exception e) {
                        Gdx.app.error("BackendDecisionResolver", "JSON parse error", e);
                        callback.onError("Parsare JSON esuata");
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    Gdx.app.error("BackendDecisionResolver", "Request failed", t);
                    callback.onError("Nu pot contacta backendul");
                });
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Cererea a fost anulata"));
            }
        });
    }
}
