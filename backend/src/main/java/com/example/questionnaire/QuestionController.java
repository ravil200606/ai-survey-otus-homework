package com.example.questionnaire;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {

    private static final List<Question> QUESTIONS = List.of(
            Question.text(1, "Укажите ваши имя и фамилию."),
            Question.number(2, "Укажите ваш возраст."),
            Question.select(3, "Укажите ваш пол.", List.of("М", "Ж")),
            Question.number(4, "Укажите ваш опыт разработки (лет).")
    );

    @GetMapping("/questions")
    public List<Question> questions() {
        return QUESTIONS;
    }
}
