/**
 * Prompts the admin to enter the number of copies to add to inventory.
 * If input is valid, sets the value in the form and triggers the hidden addCopies button.
 *
 *  Used in: movieManagement.xhtml (Add Copies button)
 */
function addCopiesPrompt() {
    var copies = prompt("Enter the number of copies added to inventory:", "1");
    if (copies !== null && !isNaN(copies) && copies > 0) {
        var addCopiesInput = document.getElementById("manageMovieForm:addCopiesInput");
        addCopiesInput.value = copies;
        document.getElementById("manageMovieForm:addCopiesBtn").click();
    } else {
        alert("Invalid input. Please enter a positive number.");
    }
}

/**
 * Prompts the admin to enter the number of lost copies.
 * If input is valid, sets the value in the form and triggers the lostCopies button.
 *
 *  Used in: movieManagement.xhtml (Lost Copies button)
 */
function lostCopiesPrompt() {
    var lost = prompt("Enter number of lost copies (by company):", "1");
    if (lost !== null && !isNaN(lost) && lost > 0) {
        var lostInput = document.getElementById("manageMovieForm:lostCopiesInput");
        lostInput.value = lost;
        document.getElementById("manageMovieForm:lostCopiesBtn").click();
    } else {
        alert("Invalid input. Please enter a positive number.");
    }
}

/**
 * Enables horizontal scrolling for elements with class 'scroll-container'
 * via click-and-drag mouse interaction .
 *
 * 📍 Used in: index.xhtml, search.xhtml
 */
document.addEventListener('DOMContentLoaded', function () {
    const scrollContainers = document.querySelectorAll('.scroll-container');

    scrollContainers.forEach(container => {
        let isDown = false;
        let startX;
        let scrollLeft;

        container.addEventListener('mousedown', (e) => {
            isDown = true;
            container.classList.add('active');
            startX = e.pageX - container.offsetLeft;
            scrollLeft = container.scrollLeft;
        });

        container.addEventListener('mouseleave', () => {
            isDown = false;
            container.classList.remove('active');
        });

        container.addEventListener('mouseup', () => {
            isDown = false;
            container.classList.remove('active');
        });

        container.addEventListener('mousemove', (e) => {
            if (!isDown)
                return;
            e.preventDefault();
            const x = e.pageX - container.offsetLeft;
            const walk = (x - startX) * 1.5;
            container.scrollLeft = scrollLeft - walk;
        });
    });
});

/**
 * Fades out a given message element (success, error, warning, fatal) over time.
 * Prevents multiple fadeouts using dataset flag. 
 *
 * @param {HTMLElement} msg - the message element to fade
 */
function fadeOutElement(msg) {
    if (!msg)
        return;
    if (!msg.dataset.fading) {
        msg.dataset.fading = "true";
        setTimeout(function () {
            msg.style.transition = 'opacity 0.5s ease';
            msg.style.opacity = '0';
            setTimeout(function () {
                if (msg.parentNode) {
                    msg.parentNode.removeChild(msg);
                }
            }, 500);
        }, 3000); // מחכה 3 שניות ואז מתחיל להעלים
    }
}

/**
 * Scans the page for JSF message elements and triggers fading.
 */
function scanAndFadeMessages() {
    const messages = document.querySelectorAll('li.success-message, li.error-message, li.warn-message, li.fatal-message');
    messages.forEach(fadeOutElement);
}

// Run initial scan after page load
setInterval(scanAndFadeMessages, 2000);

// Re-check every 2 seconds for new messages
document.addEventListener('DOMContentLoaded', function () {
    scanAndFadeMessages();
});
