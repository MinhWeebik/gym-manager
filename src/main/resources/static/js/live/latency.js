$('#stream_latency').on('change', function() {
    var id_live = $('#id_live').val();
    var stream_latency =  this.value;
    var type = 0;

    if(stream_latency != 0) {
        setData(stream_latency,id_live,type);
    }
});
function setData(stream_latency,id_live,type) {
    $.ajax({
        url: urlStreamLatency,
        method: 'POST',
        data:{
            stream_latency:stream_latency,
            id_live:id_live,
            type:type,
        },
        success:function(data){
            // console.log(data.enable_dvr, data.hls_play_link);
            // if (data.enable_dvr == 1) {
                // let myPlayer = videojs('video_js2');
                // myPlayer.src ({type: 'application/x-mpegURL',
                //     src: 'http://125.234.170.241/routing-livestream' + data.hls_play_link});
                // myPlayer.ready (function () {
                //     myPlayer.play();
                // });
            // }
        },

        error:function(){
            alert(myConstant.getParameter("ERROR"))
        }
    });
}

$(document).ready(function() {
    var enable_dvr = 0;
    var type = 1;
    var id_live = $('#id_live').val();
    $('#enable_dvr').change(function() {

        if($(this).is(":checked")) {

            enable_dvr = 1;
        }
        else {

            enable_dvr = 0;
        }
        setData(enable_dvr,id_live,type);
    });
});

