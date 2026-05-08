//Functionality for scroll to top button when user scrolls down a page
document.addEventListener('DOMContentLoaded', function() {
    const scrollBtn = document.getElementById('scroll-to-top');
    const mainContent = document.querySelector('.main-content');
    
    //Change button visibility based on page scoll position
    mainContent.addEventListener('scroll', function() {
        if (mainContent.scrollTop > 500) { //show button after user has scrolled down 100 pixels
            scrollBtn.style.display = 'block';
        } else {
            scrollBtn.style.display = 'none';
        }
    });

    //Scroll to top of page on click
    scrollBtn.addEventListener('click', function() {
        mainContent.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    });
});