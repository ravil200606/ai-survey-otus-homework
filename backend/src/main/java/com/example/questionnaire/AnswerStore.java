package com.example.questionnaire;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Хранилище ответов в памяти процесса. Данные существуют только
 * пока жив JVM и исчезают при перезапуске приложения.
 */
@Component
public class AnswerStore {

    private final List<Answer> answers = new ArrayList<>();

    public synchronized void save(List<Answer> newAnswers) {
        answers.addAll(newAnswers);
    }

    public synchronized List<Answer> findAll() {
        return List.copyOf(answers);
    }
}
