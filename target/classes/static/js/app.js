// Регистрация Service Worker
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js')
        .then(function(registration) {
            console.log('Service Worker registered with scope:', registration.scope);
        })
        .catch(function(error) {
            console.log('Service Worker registration failed:', error);
        });
}

// Базовая функциональность
document.addEventListener('DOMContentLoaded', function() {
    console.log('Task Management System loaded');

    // Обработка форм
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Загрузка...';
            }
        });
    });

    // Подтверждение удаления
    const deleteForms = document.querySelectorAll('form[action*="delete"]');
    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Вы уверены, что хотите удалить этот элемент?')) {
                e.preventDefault();
            }
        });
    });
});
// Обработка клика по задаче
document.addEventListener('DOMContentLoaded', function() {
    // Добавляем обработчики для кликабельных строк
    const taskRows = document.querySelectorAll('.task-row');
    taskRows.forEach(row => {
        row.addEventListener('click', function(e) {
            // Не открываем просмотр если кликнули на кнопку действий
            if (!e.target.closest('a, button, form')) {
                const taskId = this.querySelector('td:first-child').textContent;
                window.location.href = '/tasks/view/' + taskId;
            }
        });
    });

    // Добавляем анимацию hover
    taskRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.style.transition = 'all 0.2s ease';
        });
    });

    // Подсветка просроченных задач
    const dueDates = document.querySelectorAll('.info-value');
    dueDates.forEach(element => {
        if (element.textContent.includes('Просрочена')) {
            element.closest('.info-item').style.borderLeftColor = '#dc3545';
        }
    });
});