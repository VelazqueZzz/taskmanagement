// Анимации и эффекты
class Animations {
    static fadeIn(element, duration = 300) {
        element.style.opacity = '0';
        element.style.display = 'block';

        let start = null;
        function step(timestamp) {
            if (!start) start = timestamp;
            const progress = timestamp - start;
            const opacity = Math.min(progress / duration, 1);
            element.style.opacity = opacity;

            if (progress < duration) {
                window.requestAnimationFrame(step);
            }
        }
        window.requestAnimationFrame(step);
    }

    static fadeOut(element, duration = 300) {
        let start = null;
        function step(timestamp) {
            if (!start) start = timestamp;
            const progress = timestamp - start;
            const opacity = Math.max(1 - progress / duration, 0);
            element.style.opacity = opacity;

            if (progress < duration) {
                window.requestAnimationFrame(step);
            } else {
                element.style.display = 'none';
            }
        }
        window.requestAnimationFrame(step);
    }

    static slideIn(element, duration = 300) {
        element.style.transform = 'translateY(-20px)';
        element.style.opacity = '0';
        element.style.display = 'block';

        let start = null;
        function step(timestamp) {
            if (!start) start = timestamp;
            const progress = timestamp - start;
            const percentage = Math.min(progress / duration, 1);

            element.style.opacity = percentage;
            element.style.transform = `translateY(${-20 + (20 * percentage)}px)`;

            if (progress < duration) {
                window.requestAnimationFrame(step);
            }
        }
        window.requestAnimationFrame(step);
    }

    static highlight(element, color = '#ffeb3b', duration = 1000) {
        const originalColor = element.style.backgroundColor;
        element.style.transition = 'background-color 0.3s ease';
        element.style.backgroundColor = color;

        setTimeout(() => {
            element.style.backgroundColor = originalColor;
            setTimeout(() => {
                element.style.transition = '';
            }, 300);
        }, duration);
    }
}

// Параллакс эффект для header
function initParallax() {
    const header = document.querySelector('.header');
    if (header) {
        window.addEventListener('scroll', () => {
            const scrolled = window.pageYOffset;
            const rate = scrolled * -0.5;
            header.style.transform = `translateY(${rate}px)`;
        });
    }
}

// Инициализация анимаций при загрузке
document.addEventListener('DOMContentLoaded', () => {
    initParallax();

    // Анимация появления элементов
    const animateElements = document.querySelectorAll('[data-animate]');
    animateElements.forEach((element, index) => {
        setTimeout(() => {
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100);
    });
});