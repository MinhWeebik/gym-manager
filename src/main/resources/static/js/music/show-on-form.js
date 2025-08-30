$(function () {
var contentType = document.getElementById("contentType").value;
if(contentType != "")
    document.getElementById("contentIdDiv").style.display = "block";
var selectElement = document.getElementById("contentType");
selectElement.addEventListener("change", function() {
    let contentType = document.getElementById("contentType").value;
    if(contentType == "") {
        document.getElementById("contentIdDiv").style.display = "none";
        $('#contentId').empty();
    }
    else {
        document.getElementById("contentIdDiv").style.display = "block";
        contentType = document.getElementById("contentType").value;
        console.log("contentType|" + contentType)
        $('#contentId').empty();
        runSelect2(contentType);
    }
});
function runSelect2(contentType) {
$('#contentId').select2({
        ajax: {
            type: 'GET',
            url: path + '/music/msc-show-on/ajax-search?contentType=' + contentType,
            dataType: 'json',
            delay: 250,
            data: function(params) {
                return {
                    input_: params.term
                };
            },
            processResults: function(data) {
                return {
                    results: data
                };
            },
            cache: true
        },
        placeholder: select2ContentName,
        minimumInputLength: 0,
        allowClear: true
    });
}

window.onload = runSelect2(contentType);
});