// Отключаем Service Worker
self.addEventListener('install', function(event) {
    console.log('Service Worker disabled');
    self.skipWaiting();
});

self.addEventListener('activate', function(event) {
    console.log('Service Worker deactivated');
});

// Пропускаем все запросы без обработки
self.addEventListener('fetch', function(event) {
    // Не перехватываем запросы
    return;
});