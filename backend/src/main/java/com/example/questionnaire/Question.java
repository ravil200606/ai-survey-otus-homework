package com.example.questionnaire;

import java.util.List;

public record Question(int id, String text, String type, List<String> options) {

    public static Question text(int id, String text) {
        return new Question(id, text, "text", null);
    }

    public static Question number(int id, String text) {
        return new Question(id, text, "number", null);
    }

    public static Question select(int id, String text, List<String> options) {
        return new Question(id, text, "select", options);
    }
}
