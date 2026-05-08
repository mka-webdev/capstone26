//script to show loading over both scanning and AI generation
document.addEventListener('DOMContentLoaded', function() {
    const scanForm = document.querySelector('.scan-form');
    const pageOverlay = document.getElementById('page-loading-overlay');
    const aiForm = document.querySelector('.ai-remediation-form');
    const aiOverlay = document.getElementById('ai-loading-overlay');
    const aiReportDetails = document.querySelector('.ai-remediation-output');
    const mainContent = document.querySelector('.main-content');

    //check if loading scan
    if (scanForm && pageOverlay) {
        scanForm.addEventListener('submit', function() {
            pageOverlay.style.display = 'flex';
        });
    }

    //check if loading ai remediation
    if (aiForm && aiOverlay) {
        aiForm.addEventListener('submit', function() {
            
            //close AI remediation if open/present when starting a new AI generation
            if (aiReportDetails && aiReportDetails.hasAttribute('open')) {
                aiReportDetails.removeAttribute('open');
            }
            
            aiOverlay.style.display = 'flex';
            sessionStorage.setItem('aiReportGenerated', 'true');
        });
    }

    //open ai remediation upon loading and scroll to page
    if (aiReportDetails && mainContent && sessionStorage.getItem('aiReportGenerated')) {
        sessionStorage.removeItem('aiReportGenerated');
        //open ai remediation report
        aiReportDetails.setAttribute('open', '');
        
        //scroll to ai remediation output
        setTimeout(function() {
            const offsetTop = aiReportDetails.offsetTop;
            mainContent.scrollTo({
                top: offsetTop,
                behavior: 'smooth'
            });
        }, 100);
    }
});