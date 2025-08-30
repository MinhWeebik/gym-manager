var buuLibrary = (function () {
  let upLoadImage = function (a, b, c, d) {
    const inputFormFile = document.querySelector(a);
    const buu = document.querySelector(b);
    const buuImage = document.querySelector(c);
    const buuDelete = document.querySelector(d);

    buu.addEventListener("click", function () {
      inputFormFile.click();
    });

    buuDelete.onclick = function (e) {
      e.stopPropagation();
      buuImage.src = "";
      inputFormFile.value = "";
      buuDelete.style.display = "none";
    };

    buu.addEventListener("mouseover", function () {
      if (buuImage.src == window.location.href) {
        console.log("hello");
      } else {
        buuDelete.style.display = "block";
      }
    });

    buu.addEventListener("mouseout", function () {
      buuDelete.style.display = "none";
    });
  };
  return {
    upLoadImage,
  };
})();
