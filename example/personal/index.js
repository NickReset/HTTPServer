// setInterval for every second that will update my age
let birthDate = new Date('01-29-2008');
updateAge();

setInterval(() => updateAge(), 1000);

function updateAge() {
    document.getElementById('age').innerText = ((new Date() - birthDate) / 31556952000).toFixed(3);
}