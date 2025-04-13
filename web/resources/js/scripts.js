
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



