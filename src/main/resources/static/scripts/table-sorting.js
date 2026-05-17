document.addEventListener('DOMContentLoaded', function () {
    const tables = document.querySelectorAll('.oagp-table');
    if (!tables.length) return;

    const severityOrder = ['critical', 'serious', 'moderate', 'minor'];

    const parseDateValue = (value) => {
        const parts = value.trim().split(' ');
        if (parts.length !== 2) return new Date(value);

        const [datePart, timePart] = parts;
        const [day, month, year] = datePart.split('-').map(Number);
        const [hour, minute] = timePart.split(':').map(Number);
        return new Date(year, month - 1, day, hour, minute);
    };

    tables.forEach((table) => {
        const headers = Array.from(table.querySelectorAll('thead th'));
        const tbody = table.querySelector('tbody');
        if (!tbody) return;

        headers.forEach((header, index) => {
            const sortKey = header.dataset.sortKey;
            if (!sortKey) return;

            const sortType = header.dataset.sortType || 'string';
            header.setAttribute('data-sortable', 'true');
            header.setAttribute('data-sort-order', 'none');

            header.addEventListener('click', function () {
                const currentOrder = header.getAttribute('data-sort-order');
                const nextOrder = currentOrder === 'asc' ? 'desc' : 'asc';

                headers.forEach((h) => {
                    if (h.hasAttribute('data-sortable')) {
                        h.setAttribute('data-sort-order', 'none');
                    }
                });
                header.setAttribute('data-sort-order', nextOrder);

                const rows = Array.from(tbody.querySelectorAll('tr'));
                const sortedRows = rows.sort((rowA, rowB) => {
                    const cellA = (rowA.cells[index]?.textContent || '').trim();
                    const cellB = (rowB.cells[index]?.textContent || '').trim();

                    if (sortType === 'number') {
                        const valA = parseFloat(cellA.replace(/[^0-9.-]/g, '')) || 0;
                        const valB = parseFloat(cellB.replace(/[^0-9.-]/g, '')) || 0;
                        return nextOrder === 'asc' ? valA - valB : valB - valA;
                    }

                    if (sortType === 'severity') {
                        const rankA = severityOrder.indexOf(cellA.toLowerCase());
                        const rankB = severityOrder.indexOf(cellB.toLowerCase());
                        const aRank = rankA >= 0 ? rankA : severityOrder.length;
                        const bRank = rankB >= 0 ? rankB : severityOrder.length;
                        return nextOrder === 'asc' ? aRank - bRank : bRank - aRank;
                    }

                    if (sortType === 'date') {
                        const dateA = parseDateValue(cellA);
                        const dateB = parseDateValue(cellB);
                        const diff = dateA - dateB;
                        return nextOrder === 'asc' ? diff : -diff;
                    }

                    const comparison = cellA.localeCompare(cellB, undefined, {
                        numeric: true,
                        sensitivity: 'base'
                    });
                    return nextOrder === 'asc' ? comparison : -comparison;
                });

                sortedRows.forEach((row) => tbody.appendChild(row));
            });
        });
    });
});
