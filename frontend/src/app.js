async function loadQuestions() {
    const response = await fetch('/questions');
    if (!response.ok) {
        throw new Error('Не удалось загрузить вопросы (HTTP ' + response.status + ').');
    }
    return response.json();
}

function buildInput(question) {
    if (question.type === 'select' && Array.isArray(question.options)) {
        const select = document.createElement('select');
        const emptyOption = document.createElement('option');
        emptyOption.value = '';
        emptyOption.textContent = '— выберите —';
        select.appendChild(emptyOption);
        for (const option of question.options) {
            const optionElement = document.createElement('option');
            optionElement.value = option;
            optionElement.textContent = option;
            select.appendChild(optionElement);
        }
        select.required = true;
        return select;
    }

    const input = document.createElement('input');
    input.type = question.type === 'number' ? 'number' : 'text';
    input.required = true;
    if (question.type === 'number') {
        input.min = 0;
    }
    return input;
}

function renderForm(questions) {
    const form = document.getElementById('survey-form');
    for (const question of questions) {
        const label = document.createElement('label');
        label.textContent = question.text;

        const control = buildInput(question);
        control.name = String(question.id);
        control.dataset.type = question.type;

        label.appendChild(control);
        form.appendChild(label);
    }

    const submitButton = document.createElement('button');
    submitButton.type = 'submit';
    submitButton.textContent = 'Отправить';
    form.appendChild(submitButton);

    form.addEventListener('submit', handleSubmit);
    form.classList.remove('hidden');
}

async function handleSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const errorBox = document.getElementById('error');
    errorBox.classList.add('hidden');

    const answers = [];
    for (const control of form.querySelectorAll('input, select')) {
        answers.push({ questionId: Number(control.name), value: control.value });
    }

    try {
        const response = await fetch('/answers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ answers })
        });
        if (!response.ok) {
            let message = 'Ошибка отправки (HTTP ' + response.status + ').';
            try {
                const body = await response.json();
                if (body && body.message) {
                    message = body.message;
                }
            } catch (_) {
                // ответ не JSON — оставляем сообщение по умолчанию
            }
            throw new Error(message);
        }
        form.classList.add('hidden');
        document.getElementById('thanks').classList.remove('hidden');
    } catch (error) {
        errorBox.textContent = error.message;
        errorBox.classList.remove('hidden');
    }
}

async function init() {
    try {
        const questions = await loadQuestions();
        renderForm(questions);
    } catch (error) {
        const errorBox = document.getElementById('error');
        errorBox.textContent = error.message;
        errorBox.classList.remove('hidden');
    }
}

init();
