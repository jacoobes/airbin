// When plain htmx isn't quite enough, you can stick some custom JS here.


document.querySelector(".cpy").onclick = function(){
    console.log("copied");
    document.getElementById("codearea").select();
    document.execCommand('copy');
    alert("copied text")
}
