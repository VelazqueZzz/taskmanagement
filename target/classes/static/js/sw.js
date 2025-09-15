// Простой Service Worker
self.addEventListener('install', function(event) {
    console.log('Service Worker installed');
});

self.addEventListener('activate', function(event) {
    console.log('Service Worker activated');
});

self.addEventListener('fetch', function(event) {
    // Просто пропускаем все запросы
    event.respondWith(fetch(event.request));
});