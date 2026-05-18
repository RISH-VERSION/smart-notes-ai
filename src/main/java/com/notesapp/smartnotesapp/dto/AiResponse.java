package com.notesapp.smartnotesapp.dto;

public class AiResponse {
    private String result;
    private String error;

    public AiResponse() {}

    public AiResponse(String result, String error) {
        this.result = result;
        this.error = error;
    }

    public static AiResponse success(String result) {
        return new AiResponse(result, null);
    }

    public static AiResponse failure(String error) {
        return new AiResponse(null, error);
    }

    public String getResult() {
        return result;
    }

    public String getError() {
        return error;
    }
}