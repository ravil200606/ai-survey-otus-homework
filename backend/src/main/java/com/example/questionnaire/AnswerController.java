package com.example.questionnaire;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnswerController {

    private static final Set<Integer> KNOWN_QUESTION_IDS = Set.of(1, 2, 3, 4);

    private final AnswerStore answerStore;

    public AnswerController(AnswerStore answerStore) {
        this.answerStore = answerStore;
    }

    @PostMapping("/answers")
    public ResponseEntity<Void> submit(@RequestBody AnswersRequest request) {
        List<Answer> answers = request.answers();
        if (answers == null || answers.isEmpty()) {
            throw new ValidationException("Не переданы ответы.");
        }

        Set<Integer> seen = new HashSet<>();
        for (Answer answer : answers) {
            validate(answer, seen);
            seen.add(answer.questionId());
        }
        if (!seen.equals(KNOWN_QUESTION_IDS)) {
            throw new ValidationException("Ответы должны содержать все 4 вопроса.");
        }

        answerStore.save(answers);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private void validate(Answer answer, Set<Integer> seen) {
        int id = answer.questionId();
        String value = answer.value();
        if (!KNOWN_QUESTION_IDS.contains(id)) {
            throw new ValidationException("Неизвестный вопрос: id=" + id + ".");
        }
        if (seen.contains(id)) {
            throw new ValidationException("Повторный ответ на вопрос id=" + id + ".");
        }
        if (value == null || value.isBlank()) {
            throw new ValidationException(emptyValueMessage(id));
        }
        value = value.trim();
        switch (id) {
            case 1 -> {
                if (value.chars().filter(c -> c != ' ').count() < 2) {
                    throw new ValidationException("Укажите имя и фамилию (два слова через пробел).");
                }
            }
            case 2 -> requireInt(id, value, 1, 150, "Возраст должен быть числом от 1 до 150.");
            case 3 -> {
                if (!value.equals("М") && !value.equals("Ж")) {
                    throw new ValidationException("Пол должен иметь значение «М» или «Ж».");
                }
            }
            case 4 -> requireInt(id, value, 0, 100, "Опыт разработки должен быть числом от 0 до 100.");
            default -> throw new ValidationException(emptyValueMessage(id));
        }
    }

    private void requireInt(int id, String value, int min, int max, String errorMessage) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                throw new ValidationException(errorMessage);
            }
        } catch (NumberFormatException e) {
            throw new ValidationException(errorMessage);
        }
    }

    private String emptyValueMessage(int id) {
        return "Не указано значение для вопроса id=" + id + ".";
    }
}
