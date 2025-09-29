// file-management.js
class FileManagement {
    constructor() {
        this.init();
    }

    init() {
        this.setupFilePreviews();
    }

    setupFilePreviews() {
        // Для формы создания задачи
        const filesInput = document.getElementById('files');
        if (filesInput) {
            filesInput.addEventListener('change', (e) => this.previewFiles(e.target, 'filePreview'));
        }

        // Для формы редактирования задачи
        const newFilesInput = document.getElementById('newFiles');
        if (newFilesInput) {
            newFilesInput.addEventListener('change', (e) => this.previewFiles(e.target, 'newFilePreview'));
        }
    }

    previewFiles(input, previewContainerId) {
        const previewContainer = document.getElementById(previewContainerId);
        if (!previewContainer) return;

        previewContainer.innerHTML = '';

        if (input.files.length > 0) {
            const fileList = document.createElement('div');
            fileList.className = 'file-list';

            for (let i = 0; i < input.files.length; i++) {
                const file = input.files[i];
                const fileItem = this.createFileItem(file);
                fileList.appendChild(fileItem);
            }

            previewContainer.appendChild(fileList);
        }
    }

    createFileItem(file) {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';
        fileItem.innerHTML = `
            <span class="file-icon">${this.getFileIcon(file.type)}</span>
            <span class="file-name">${file.name}</span>
            <span class="file-size">(${this.formatFileSize(file.size)})</span>
            <button type="button" class="btn-remove" onclick="this.parentElement.remove()">×</button>
        `;
        return fileItem;
    }

    getFileIcon(fileType) {
        if (fileType.startsWith('image/')) return '🖼️';
        if (fileType.includes('pdf')) return '📄';
        if (fileType.includes('word')) return '📝';
        if (fileType.includes('excel')) return '📊';
        if (fileType.includes('zip')) return '📦';
        return '📎';
    }

    formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        else if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        else return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    validateFileSize(files, maxSize = 10 * 1024 * 1024) {
        for (let file of files) {
            if (file.size > maxSize) {
                alert(`Файл "${file.name}" превышает максимальный размер 10MB`);
                return false;
            }
        }
        return true;
    }
}

// Глобальные функции для удаления файлов
function removeFile(element) {
    element.parentElement.remove();
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    window.fileManager = new FileManagement();

    // Валидация формы перед отправкой
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            const fileInputs = this.querySelectorAll('input[type="file"]');
            for (let input of fileInputs) {
                if (!window.fileManager.validateFileSize(input.files)) {
                    e.preventDefault();
                    return false;
                }
            }
        });
    });
});