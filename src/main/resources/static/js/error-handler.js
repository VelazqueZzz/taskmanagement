// Глобальный обработчик ошибок
window.addEventListener('error', function(e) {
    console.error('Global error:', e.error);
});

// Обработчик ошибок Promise
window.addEventListener('unhandledrejection', function(e) {
    console.error('Unhandled promise rejection:', e.reason);
    e.preventDefault();
});

// Функция для безопасной загрузки ресурсов
function loadResource(url, type) {
    return new Promise((resolve, reject) => {
        if (type === 'script') {
            const script = document.createElement('script');
            script.src = url;
            script.onload = resolve;
            script.onerror = reject;
            document.head.appendChild(script);
        }
    });
}

// ЗАКОММЕНТИРОВАННАЯ регистрация Service Worker
/*
function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js')
            .then(registration => {
                console.log('SW registered:', registration);
            })
            .catch(error => {
                console.log('SW registration failed:', error);
            });
    }
}
*/

// Инициализация при загрузке
document.addEventListener('DOMContentLoaded', function() {
    try {
        // НЕ регистрируем Service Worker
        // registerServiceWorker();

        // Основная логика приложения
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', handleFormSubmit);
        });

    } catch (error) {
        console.error('Initialization error:', error);
    }
});

function handleFormSubmit(e) {
    const submitBtn = this.querySelector('button[type="submit"]');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Загрузка...';
    }
}