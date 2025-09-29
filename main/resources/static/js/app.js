class TaskManager {
    constructor() {
        this.init();
        this.setupEventListeners();
        this.loadUserPreferences();
    }

    init() {
        console.log('Task Management System initialized');
        this.updateActiveNavLink();
        this.setupTaskRowHandlers();
        this.setupFormHandlers();
        this.setupModalHandlers();
    }

    setupEventListeners() {
        // Обработчики навигации
        document.addEventListener('click', (e) => {
            if (e.target.matches('.nav-link')) {
                this.handleNavClick(e);
            }
        });

        // Обработчики форм
        document.addEventListener('submit', (e) => {
            if (e.target.matches('form')) {
                this.handleFormSubmit(e);
            }
        });

        // Обработчики модальных окон
        document.addEventListener('click', (e) => {
            if (e.target.matches('[data-modal]')) {
                this.openModal(e.target.dataset.modal);
            }
            if (e.target.matches('.close, .modal')) {
                this.closeModal();
            }
        });

        // Обработчик клавиатуры
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                this.closeModal();
            }
        });
    }

    setupTaskRowHandlers() {
        document.addEventListener('click', (e) => {
            const taskRow = e.target.closest('.task-row');
            if (taskRow && !e.target.matches('a, button, input, select, textarea')) {
                const taskId = taskRow.dataset.taskId || taskRow.querySelector('td:first-child').textContent;
                this.viewTask(taskId);
            }
        });
    }

    setupFormHandlers() {
        // Валидация форм
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', (e) => {
                if (!this.validateForm(form)) {
                    e.preventDefault();
                }
            });
        });

        // Динамическое обновление форм
        document.addEventListener('change', (e) => {
            if (e.target.matches('[data-dependent]')) {
                this.updateDependentFields(e.target);
            }
        });
    }

    setupModalHandlers() {
        // Закрытие модальных окон по клику вне контента
        document.addEventListener('click', (e) => {
            if (e.target.matches('.modal')) {
                this.closeModal();
            }
        });
    }

    viewTask(taskId) {
        window.location.href = `/tasks/view/${taskId}`;
    }

    handleNavClick(e) {
        e.preventDefault();
        const url = e.target.href;
        this.showLoading();
        window.location.href = url;
    }

    handleFormSubmit(e) {
        const form = e.target;
        const submitBtn = form.querySelector('button[type="submit"]');

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="loading"></span> Загрузка...';
        }

        // Добавляем задержку для демонстрации loading
        setTimeout(() => {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = submitBtn.dataset.originalText || 'Сохранить';
            }
        }, 2000);
    }

    validateForm(form) {
        let isValid = true;
        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');

        inputs.forEach(input => {
            if (!input.value.trim()) {
                this.showError(input, 'Это поле обязательно для заполнения');
                isValid = false;
            } else {
                this.clearError(input);
            }
        });

        return isValid;
    }

    showError(input, message) {
        this.clearError(input);

        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.style.color = '#ef4444';
        errorDiv.style.fontSize = '0.8rem';
        errorDiv.style.marginTop = '0.25rem';
        errorDiv.textContent = message;

        input.style.borderColor = '#ef4444';
        input.parentNode.appendChild(errorDiv);
    }

    clearError(input) {
        const errorDiv = input.parentNode.querySelector('.error-message');
        if (errorDiv) {
            errorDiv.remove();
        }
        input.style.borderColor = '';
    }

    openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'block';
            document.body.style.overflow = 'hidden';
        }
    }

    closeModal() {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            modal.style.display = 'none';
        });
        document.body.style.overflow = '';
    }

    showLoading() {
        // Можно добавить глобальный индикатор загрузки
        const loading = document.createElement('div');
        loading.id = 'global-loading';
        loading.style.position = 'fixed';
        loading.style.top = '0';
        loading.style.left = '0';
        loading.style.width = '100%';
        loading.style.height = '3px';
        loading.style.background = 'linear-gradient(90deg, var(--primary), var(--secondary))';
        loading.style.zIndex = '9999';
        loading.style.animation = 'loading 2s infinite';

        document.body.appendChild(loading);

        const style = document.createElement('style');
        style.textContent = `
            @keyframes loading {
                0% { transform: translateX(-100%); }
                100% { transform: translateX(100%); }
            }
        `;
        document.head.appendChild(style);
    }

    hideLoading() {
        const loading = document.getElementById('global-loading');
        if (loading) {
            loading.remove();
        }
    }

    updateActiveNavLink() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-link');

        navLinks.forEach(link => {
            if (link.getAttribute('href') === currentPath) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    loadUserPreferences() {
        // Загрузка пользовательских предпочтений (тема, настройки)
        const theme = localStorage.getItem('theme') || 'light';
        this.setTheme(theme);
    }

    setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    }

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        this.setTheme(newTheme);
    }

    // Уведомления
    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()">×</button>
        `;

        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '1rem',
            background: type === 'error' ? '#ef4444' : type === 'success' ? '#10b981' : '#6366f1',
            color: 'white',
            borderRadius: 'var(--border-radius)',
            boxShadow: 'var(--shadow-lg)',
            zIndex: '1000',
            display: 'flex',
            alignItems: 'center',
            gap: '1rem'
        });

        document.body.appendChild(notification);

        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }

    // Работа с API
    async fetchData(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Fetch error:', error);
            this.showNotification('Ошибка загрузки данных', 'error');
            throw error;
        }
    }
}

// Вспомогательные функции
const utils = {
    formatDate(date) {
        return new Date(date).toLocaleDateString('ru-RU', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    },

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    },

    getCSRFToken() {
        return document.querySelector('meta[name="_csrf"]')?.content || '';
    }
};

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    window.taskManager = new TaskManager();

    // Добавляем кнопку переключения темы
    const themeToggle = document.createElement('button');
    themeToggle.innerHTML = '🌓';
    themeToggle.style.position = 'fixed';
    themeToggle.style.bottom = '20px';
    themeToggle.style.right = '20px';
    themeToggle.style.padding = '0.5rem';
    themeToggle.style.background = 'var(--primary)';
    themeToggle.style.color = 'white';
    themeToggle.style.border = 'none';
    themeToggle.style.borderRadius = '50%';
    themeToggle.style.cursor = 'pointer';
    themeToggle.style.zIndex = '100';
    themeToggle.onclick = () => window.taskManager.toggleTheme();

    document.body.appendChild(themeToggle);
});

// Глобальные обработчики
document.addEventListener('click', (e) => {
    if (e.target.matches('[data-confirm]')) {
        const message = e.target.dataset.confirm;
        if (!confirm(message)) {
            e.preventDefault();
        }
    }
});