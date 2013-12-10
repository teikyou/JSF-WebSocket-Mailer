
var websocket = null;
function init() {
    numberOfMessage = 0;
    document.getElementById("close").style.display = "none";
}

function closeServerEndpoint() {
    websocket.close(4001, "Close connection from client");
    document.getElementById("connect").style.display = "block";
    document.getElementById("close").style.display = "none";
}

function connectServerEndpoint() {

    var wsUri = "ws://localhost:8080/JSF-WebSocket-Mailer/inbox-check";
    if ("WebSocket" in window) {
        websocket = new WebSocket(wsUri);
    } else if ("MozWebSocket" in window) {
        websocket = new MozWebSocket(wsUri);
    }

    websocket.onopen = function(evt) {
        onOpen(evt);
    };
    websocket.onmessage = function(evt) {
        onMessage(evt);
    };
    websocket.onerror = function(evt) {
        onError(evt);
    };
    websocket.onclose = function(evt) {
        closeServerEndpoint();
    };

    document.getElementById("connect").style.display = "none";
    document.getElementById("close").style.display = "block";
}

function onOpen(evt) {
    ;
}

function onMessage(evt) {
    writeToScreen(evt.data);
}

function onError(evt) {
    writeToScreen("ERROR: " + evt.data);
}

function writeToScreen(messages) {
    if (window.JSON)
    {
        var obj = JSON.parse(messages);
        var subject = obj.subject;
        var from = obj.address;
        var summary = obj.summary;

        document.getElementById('form:wssubject').innerHTML = subject;
        document.getElementById('form:wsfrom').innerHTML = from;
        document.getElementById('form:wssummary').innerHTML = summary;
    }
    PF('bar').show();
}
window.addEventListener("load", init, false);
