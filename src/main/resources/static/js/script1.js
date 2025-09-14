//var uploadForm = document.getElementById("uploadForm");

let sndButton = document.getElementById("sndButton");
let fileInput = document.getElementById("uploadForm");
let pricesFiles = document.getElementById("pricesFiles");
let loadPiks = document.getElementById("loadPiks");

let responseTextArea = document.getElementById("responseTextArea");
let responseTextArea1 = document.getElementById("responseTextArea1");


asyns function loadPiks() {

  let obj = {
    name : name.value,
    login : login.value,
    email : email.value,
    age : age.value,
    password : password.value,
  };

  let request = await fetch("/put-piks",
  // Если указать путь URI --> "https", то будет выскакивать ошибка Failed to load resource: net::ERR_SSL_PROTOCOL_ERROR
    {
      method: 'POST',
      headers: {"Content-Type" : "application/json"},
      body: JSON.stringify(obj)
    });

  if(request.ok) {
    //alert("User created!");
    //window.location.replace("/");
    confirmText.className = "show";

  } else {
    alert("HTTP error: "+ request.status);
  }
}

pricesFiles.addEventListener("change", async function() {
    let fd = new FormData();
    let pricesList = pricesFiles.files;
    for(let i=0; i< pricesList.length; i++) {
        let file = pricesList[i].name;
        fd.append('file', pricesList[i]);
        responseTextArea1.value += file;
    }

    let response = await fetch("http://localhost:8080/updprices", {
      method: 'POST',
      body: fd
    });

    if (response.ok) {
              alert("Good! Пост запрос успешно прошел!");
    //        let json = await response.json();
              let text = await response.text();
    //        responseTextArea1.value = JSON.stringify(json);
              responseTextArea1.value = text;
    } else {
        alert("Ошибка HTTP: " + response.status);
    }

});


//
loadPiks.addEventListener("click", async function() {

});
