// file-upload.js
class FileUploadHandler {
    constructor() {
        this.init();
    }

    init() {
        this.setupFilePreview();
        this.setupDragAndDrop();
    }

    setupFilePreview() {
        const fileInput = document.getElementById('fileInput');
        if (fileInput) {
            fileInput.addEventListener('change', function(e) {
                const file = this.files[0];
                if (file) {
                    this.showFilePreview(file);
                }
            });
        }
    }

    showFilePreview(file) {
        const previewContainer = document.querySelector('.file-preview');
        if (!previewContainer) return;

        previewContainer.innerHTML = '';

        if (file.type.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = function(e) {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.alt = 'Preview';
                previewContainer.appendChild(img);
            };
            reader.readAsDataURL(file);
        }

        const fileInfo = document.createElement('div');
        fileInfo.innerHTML = `
            <strong>${file.name}</strong><br>
            <small>Размер: ${this.formatFileSize(file.size)}</small>
        `;
        previewContainer.appendChild(fileInfo);
    }

    setupDragAndDrop() {
        const uploadForm = document.querySelector('.upload-form');
        if (uploadForm) {
            uploadForm.addEventListener('dragover', function(e) {
                e.preventDefault();
                this.style.background = '#f0f9ff';
            });

            uploadForm.addEventListener('dragleave', function(e) {
                e.preventDefault();
                this.style.background = '';
            });

            uploadForm.addEventListener('drop', function(e) {
                e.preventDefault();
                this.style.background = '';

                const files = e.dataTransfer.files;
                if (files.length > 0) {
                    document.getElementById('fileInput').files = files;
                    this.showFilePreview(files[0]);
                }
            });
        }
    }

    formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        else if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    new FileUploadHandler();
});