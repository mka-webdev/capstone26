// Dark Mode Toggle Functionality
class DarkModeToggle {
    constructor() {
        this.key = 'theme-preference';
        this.darkModeClass = 'dark-mode';
        this.init();
    }

    init() {
        // Load saved preference or check system preference
        const savedTheme = localStorage.getItem(this.key);
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        if (savedTheme) {
            this.setTheme(savedTheme);
        } else if (prefersDark) {
            this.setTheme('dark');
        } else {
            this.setTheme('light');
        }

        this.setupToggleButton();
        this.watchSystemThemeChanges();
    }

    setTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            localStorage.setItem(this.key, 'dark');
            this.updateToggleButton('dark');
        } else {
            document.documentElement.removeAttribute('data-theme');
            localStorage.setItem(this.key, 'light');
            this.updateToggleButton('light');
        }
    }

    toggleTheme() {
        const currentTheme = document.documentElement.getAttribute('data-theme');
        this.setTheme(currentTheme === 'dark' ? 'light' : 'dark');
    }

    setupToggleButton() {
        const button = document.getElementById('theme-toggle-btn');
        if (button) {
            button.addEventListener('click', () => this.toggleTheme());
        }
    }

    updateToggleButton(theme) {
        const button = document.getElementById('theme-toggle-btn');
        if (button) {
            button.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'light' : 'dark'} mode`);
            button.textContent = theme === 'dark' ? '☀️' : '🌙';
        }
    }

    watchSystemThemeChanges() {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        mediaQuery.addEventListener('change', (e) => {
            // Only auto-switch if user hasn't set a preference
            if (!localStorage.getItem(this.key)) {
                this.setTheme(e.matches ? 'dark' : 'light');
            }
        });
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    new DarkModeToggle();
});
