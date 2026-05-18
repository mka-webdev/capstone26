document.addEventListener('DOMContentLoaded', function () {
    const toggleButton = document.getElementById('toggle-violation-details');
    if (!toggleButton) {
        return;
    }

    const violationCards = Array.from(document.querySelectorAll('.violation-list details.violation-card'));
    if (!violationCards.length) {
        toggleButton.style.display = 'none';
        return;
    }

    const updateButtonState = () => {
        const allExpanded = violationCards.every((detail) => detail.hasAttribute('open'));
        toggleButton.textContent = allExpanded ? 'Collapse All' : 'Expand All';
        toggleButton.setAttribute('aria-label', allExpanded ? 'Collapse all violation details' : 'Expand all violation details');
    };

    toggleButton.addEventListener('click', function () {
        const expand = !violationCards.every((detail) => detail.hasAttribute('open'));
        violationCards.forEach((detail) => {
            if (expand) {
                detail.setAttribute('open', '');
            } else {
                detail.removeAttribute('open');
            }
        });
        updateButtonState();
    });

    violationCards.forEach((detail) => {
        detail.addEventListener('toggle', updateButtonState);
    });

    const detailLinks = Array.from(document.querySelectorAll('.violation-detail-link'));
    detailLinks.forEach((link) => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            const targetId = link.getAttribute('href')?.replace(/^#/, '');
            if (!targetId) {
                return;
            }
            const targetDetail = document.getElementById(targetId);
            if (!targetDetail) {
                return;
            }
            targetDetail.setAttribute('open', '');
            targetDetail.scrollIntoView({ behavior: 'smooth', block: 'start' });
            updateButtonState();
        });
    });

    updateButtonState();
});
