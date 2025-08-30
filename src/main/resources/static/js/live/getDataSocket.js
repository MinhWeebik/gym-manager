var id_live = $('#id_live').val();
var hls_play_link = $('#hls_play_link').val();

var subscription_id = null;
var userID = null;
var topic_id = null;
var stompClient = null;
var smsgId = null;
var streamer_id = $('#streamer_id').val();


function onNewMessage(result) {
    console.log('onNewMessage===' + result);
    var message = JSON.parse(JSON.parse(result.body));

    if (message.type === "notify_live") {

        if (message.contentType === "START") {
            $(".btn_stop_live").css("display", "block");
            $("#msg-not-live").hide();
            //ẩn avatar để hiển thị livestream
            $(".overlay-avatar").css("display", "none");

            let myPlayer = videojs('video_js2', { liveui: true });
            myPlayer.src({
                type: 'application/x-mpegURL',
                src: myPlayer.currentSrc()
            });
            myPlayer.ready(function () {
                myPlayer.play();
            });

            $('#stream_latency').prop('disabled', true);
        } else if (message.contentType === "STOP") {
            $(".info_stop_live").click();
            //hiện avatar để che video
            $(".overlay-avatar").css("display", "block");
        }
    } else if (message.type === "chat" || message.type === "donate") {
        appendLiveChat(message);
    } else if (message.type === "seen_number") {
        $("#total_view").text(message.number);
    } else if (message.type === "like") {
        if (message.totalLike) {

            $("#total_like").text(message.totalLike);
        }
    }

}

function onLiveChat(result) {
    console.log('onLiveChat===' + result);
    var message = JSON.parse(result.body);
    appendLiveChatAdmin(JSON.parse(message));
}

function onUserWatchingMessage(result) {
    console.log('onUserWatchingMessage===' + result);
    var message = JSON.parse(JSON.parse(result.body));

    if (message.type === "notify_live") {

        if (message.contentType === "START") {
            $(".btn_stop_live").css("display", "block");
            // $(".enable_dvr").css("display", "none");
            $("#msg-not-live").hide();
            //ẩn avatar để hiển thị livestream
            $(".overlay-avatar").css("display", "none");

            let myPlayer = videojs('video_js2', { liveui: true });
            myPlayer.src({
                type: 'application/x-mpegURL',
                src: myPlayer.currentSrc()
            });
            myPlayer.ready(function () {
                myPlayer.play();
            });

            $('#stream_latency').prop('disabled', true);
        }
    } else if (message.type === "HEALTH_CHECK") {
        appendLiveStatus(message);
    } else if (message.type === "seen_number") {
        $("#total_view").text(message.number);
    } else if (message.type === "like") {
        $("#total_like").text(message.totalLike);
    } else if (message.type === "chat" || message.type === "donate") {
        appendLiveChat(message);
    }

    // isOnButtonStopLive(JSON.parse(message));
    // setSrcPlayer(JSON.parse(message));
}

window.onbeforeunload = function () {
    socket.onclose = function () {
    }; // disable onclose handler first
    stompClient.unsubscribe(`/watching/` + id_live, onNewMessage);
    stompClient.unsubscribe(`/user/watching/`, onUserWatchingMessage);
    stompClient.unsubscribe(`/chat/message/`, onLiveChat);
    socket.close();
    stompClient.disconnect();
};

var idliveStream = id_live; //id cua video live trong bang livestream

var thisheaders = {
    user: idliveStream,
    // passcode: 'AuWcecmbtSz2',
    // AuthToken: getItem('Authentication')//get your authentication token here
};

function connectWebSocket() {
    console.log("====> socketUrl = " + socketUrl);
    var socket = new SockJS(socketUrl);
    stompClient = Stomp.over(socket);
    //console.log("====> 1");
    stompClient.connect(thisheaders, (frame) => {
        stompClient.subscribe(`/user/watching`, onUserWatchingMessage); // topic cua user thay

        subscription_id = stompClient.subscribe(`/watching/` + id_live, onNewMessage); // topic cua room, cả zoom thay: message= {"type":"seen_number","topicId":"undefined","number":1,"createdAt":"23:28"}

        stompClient.subscribe(`/chat/message/` + id_live, onLiveChat); // topic cua user thay

    });
}

function isOnButtonStopLive(statusLive) {

    if (statusLive.type == "HEALTH_CHECK") {
        $(".btn_stop_live").css("display", "block");
    } else {
        console.log('message isOnButtonStopLive', statusLive);
    }
}

function setSrcPlayer(statusLive) {

    if (statusLive.type == "HEALTH_CHECK" && statusLive.statusLive == "Good") {
        //Todo: src lay tu luc vao page livestream
        var videoFile = "hls_link abc.m3u8"
        var video = $('.box_video_live video')[0];
        video.src = videoFile;
        video.load();
        video.play();
    } else {
        console.log('message setSrcPlayer', statusLive);
    }
}


function appendLiveStatus(msg) {
    if (msg.type == "HEALTH_CHECK") {
        var timeServer = moment(msg.createdAt).format("HH:mm:ss");

        $('.tin_hieu_live').html(msg.statusLive);
        $('.box_data_live_status').prepend(`
                    <div class="display_box">
                        <span class="time" style="margin-right: 10px">${timeServer}</span>
                             <div class="conten_mess">
                                   <strong>${msg.statusLive}</strong>
                                   <p>${msg.descStatusLive}</p>
                             </div>
                    </div>
                    `);
        if (msg.statusLive === "No data") {
            $(".box_data svg circle").removeAttr("fill");
            $(".box_data svg circle").attr("fill", "#b7b7b7");

            $("#msg-not-live svg circle").removeAttr("fill");
            $("#msg-not-live svg circle").attr("fill", "#b7b7b7");

            $("#msg-not-live span").html("To go live, send your video using live streaming software");
            $("#msg-not-live").show();

            $('#stream_latency').prop('disabled', false);
        } else if (msg.statusLive === "Current frame rate is incorrect") {
            $(".box_data svg circle").removeAttr("fill");
            $(".box_data svg circle").attr("fill", "#f5d442");

            $("#msg-not-live svg circle").removeAttr("fill");
            $("#msg-not-live svg circle").attr("fill", "#f5d442");

            $("#msg-not-live span").html(msg.statusLive);
            $("#msg-not-live").show();
        } else if (msg.statusLive === "Resolution of the video is incorrect") {
            $(".box_data svg circle").removeAttr("fill");
            $(".box_data svg circle").attr("fill", "#f5d442");

            $("#msg-not-live svg circle").removeAttr("fill");
            $("#msg-not-live svg circle").attr("fill", "#f5d442");

            $("#msg-not-live span").html(msg.statusLive);
            $("#msg-not-live").show();
        } else {
            $(".box_data svg circle").removeAttr("fill");
            $(".box_data svg circle").attr("fill", "#eb4f37");

            $("#msg-not-live").hide();
        }
    }

    var objDiv = document.getElementsByClassName("box_data_live_status");
    objDiv.scrollTop = objDiv.scrollHeight;
}

function block_mess(commentId, userId, avatar, userName) {
    console.log('block_mess=', commentId, userId);

    $.ajax({
        url: domainUrl + "/report_comment/cms",
        method: 'POST',
        data: {
            blockId: 5,
            commentId: commentId,
            livestreamId: id_live,
            streamerId: streamer_id,
            userId: userId,
            avatar: avatar,
            userName: userName,
        },
        success: function (data) {
            console.log(data);
            console.log(domainUrl + "/report_comment/cms");
            getListBlock();
            alert('Block comment successfully!');
            //$('.block_mess_' + commentId + ' i').css('color', 'red');

        },
        error: function () {
            alert("Error block comment");
        }
    });
}

function delete_mess(commentId, userId, avatar, userName) {
    console.log('delete_mess=', commentId, userId);
    $.ajax({
        url: domainUrl + "/report_comment",
        method: 'POST',
        data: {
            blockId: 3,
            commentId: commentId,
            livestreamId: id_live,
            streamerId: streamer_id,
            userId: userId,
            avatar: avatar,
            userName: userName,
        },
        success: function (data) {
            console.log(data);
            alert('Delete comment successfully!');
            console.log(domainUrl + "/report_comment");
            $('#content_cmt_' + commentId).css("display", "none");
        },
        error: function () {
            alert("Error delete comment");
        }
    });
}

function unblock_mess(commentId, userId, avatar, userName) {
    console.log('unblock_mess=', commentId, userId);

    $.ajax({
        url: domainUrl + "/report_comment/cms",
        method: 'POST',
        data: {
            blockId: 6,
            commentId: commentId,
            livestreamId: id_live,
            streamerId: streamer_id,
            userId: userId,
            avatar: avatar,
            userName: userName,
        },
        success: function (data) {
            console.log(data);
            console.log(domainUrl + "/report_comment/cms");
            //$('.block_mess_' + commentId + ' i').css('color', '#333');
            $('#uncontent_cmt_' + commentId).css("display", "none");
            alert('Unblock comment successfully!');


        },
        error: function () {
            alert("Error block comment");
        }
    });
}



function appendLiveChat(chat) {

    console.log(chat);
    if (chat.type == "chat") {
        var timeServer = moment(chat.createdAt).utcOffset('+1600').format("HH:mm");
        //console.log(chat.createdAt);
        var avatar = chat.avatar.replace('https', 'http')
        userID = chat.userId;
        topic_id = chat.roomID;
        smsgId = chat.smsgId;
        if (avatar.trim() === "") {
            avatar = '/img/avatar.jpeg';
        }

        if (chat.userName.includes('Streamer')) {
            $('#use_comment').append(`
                    <div class="content_cmt" id="content_cmt_${chat.smsgId}">
                            <div class="avarta">
                              <img id="img" class="img-responsive" width="35" style="border-radius:30%" src="${avatar}">
                            </div>
                            <div class="comment">

                                <div style="display: flex; align-items: center">
                                    <span class="time-content" style="color: rgb(51,51,51); font-size: 12px">${timeServer}</span>
                                    <p class="name-avatar-comment" style="background-color: yellow">${chat.userName}</p>
                                    <p class="content_mb" style="color: rgb(82, 82, 82); width:300px;">${chat.msgBody}</p>

                                </div>

                            </div>

                    </div>

                        `)
        } else {
            $('#use_comment').append(`
                    <div class="content_cmt" id="content_cmt_${chat.smsgId}">
                            <div class="avarta">
                              <img id="img" class="img-responsive" width="35" src="${avatar}">
                            </div>
                            <div class="comment">

                                <div style="display: flex; align-items: center">
                                    <span class="time-content" style="color: rgb(51,51,51); font-size: 12px">${timeServer}</span>
                                    <p class="name-avatar-comment">${chat.userName}</p>
                                    <p class="content_mb" style="color: rgb(82, 82, 82);width:300px;">${chat.msgBody}</p>
                                    <p class="block_mess_${chat.smsgId}" style="margin-right: 10px; cursor: pointer" onclick="block_mess('${chat.smsgId}','${chat.userId}','${avatar}','${chat.userName}')">
                                          <i class="fa fa-lock" aria-hidden="true"></i></span>
                                     </p>
                                     <p class="delete_mess_${chat.smsgId}" style="margin-right: 10px; cursor: pointer" onclick="delete_mess('${chat.smsgId}','${chat.userId}','${avatar}','${chat.userName}')">
                                           <i style="text-align: right; color: red" class="fa fa-trash" aria-hidden="true"></i></span>
                                     </p>

                                </div>
                            </div>
                    </div>

                        `)
        }

        var objDiv = document.getElementById("use_comment");
        objDiv.scrollTop = objDiv.scrollHeight;
    } else if (chat.type == "donate") {
        // getListDonate();


    }
    else if (chat.type == "drop_star") {
        // getHistoryDropStar();


    }
}

function appendLiveChatAdmin(chat) {
    if (chat.type == "chat") {
        var timeServer = moment(chat.createdAt).utcOffset('+1600').format("YYYY-MM-DD HH:mm");
        var avatar = chat.avatar.replace('https', 'http')
        userID = chat.userId;
        topic_id = chat.roomID;
        smsgId = chat.smsgId;
        //console.log(avatar);
        if (avatar.trim() === "") {
            avatar = '/img/avatar.jpeg';
        }
        $('.comment_admin').append(`
                    <div class="div_1"">
                            <span class="user_mess user_mess_my">My message</span>
                            <div class="message_bot message_my">
                                        <p class="time_bot">
                                            ${timeServer}
                                        </p>
                                        <p class="text_mess_my">
                                           ${chat.msgBody}
                                        </p>
                           </div>

                    </div>
                        `)
    }


}



function sendMessage(text_cmt, cIdMessage) {

    var datetime = moment(new Date()).utcOffset('-0000').format("YYYY-MM-DD HH:mm:ss").toString();
    stompClient.send("/chat/message/" + id_live, {},
        JSON.stringify({
            'msgBody': text_cmt,
            'type': 'chat',
            'userId': streamer_id,
            'userName': 'Streamer_' + streamer_id,
            'cIdMessage': cIdMessage,
            'sIdMessage': '',
            'roomID': id_live,
            'avatar': '/img/avatar.jpeg',
            'createdAt': datetime
        }));
}




$(document).ready(function () {
    connectWebSocket();

    getListBlock();
    // getListDonate();
    // getHistoryDropStar();

    $("#block_user_tab").on('click', function (e) {
        getListBlock();
    });

    // $("#btn-dropstar").on('click', function (e) {
    //     getDropStar();
    // });
    // $("#btn-stopdrop").on('click', function (e) {
    //     getStopDropStar();
    // });


    let btn = document.getElementById('send');

    // when the btn is clicked print info in console 
    btn.addEventListener('click', (ev) => {

    });

    document.addEventListener('keypress', (event) => {

        // event.keyCode or event.which  property will have the code of the pressed key
        let keyCode = event.keyCode ? event.keyCode : event.which;

        // 13 points the enter key
        if (keyCode === 13) {
            // call click function of the buttonn 
            btn.click();
        }

    });

    $('#send').click(function () {
        let cIdMessage = (Math.random() + 1).toString(36).substring(4);
        var text_cmt = $('#text_mess_send').val();
        $('#text_mess_send').val("");
        if (text_cmt != "") {
            sendMessage(text_cmt, cIdMessage);
        }
    })

    $(".block_mess").on('click', function (e) {
        e.preventDefault();
        block_mess(id_live);
    });
    $(".delete_mess").on('click', function (e) {
        e.preventDefault();
        block_mess(id_live);
    });


    // Live chat
    $('.zoom_live').click(function () {

        $(".hello_live").toggle(500);
        // $(".hello_live").css("display", "none");
        $(".user_mess").css("display", "block");
    })
    $('.text_mess_my').click(function () {
        // $(".content_live_chart").toggle(500);

        $(".modal_action_mess").css("display", "block");
        $(".div_1").addClass("show_mess_action");

    })
    $('.bg_action_mess').click(function () {
        // $(".content_live_chart").toggle(500);
        $(".modal_action_mess").css("display", "none");
        $(".div_1").removeClass("show_mess_action");
    })
    $('.gim_mess').click(function () {

        $(".gim_mess").addClass("active");
        $(".delete_mess").removeClass("active");
    })
    $('.delete_mess').click(function () {
        $(".delete_mess").addClass("active");
        $(".gim_mess").removeClass("active");
    })
    $('.show_chat').click(function () {

        $(".content_live_chart").toggle(500);
        $(".mess_send").toggle(500);
        $("#use_comment").toggle(500);

    })
});


//block user

function getListBlock() {
    // getCheckDropStar();
    $.ajax({
        //url: domainUrl+"/chart/stat?id=" + id_live,
        url: domainUrl + "/report_comment/user",
        method: 'GET',
        data: {
            streamerId: streamer_id,
            livestreamId: id_live
        },
        success: function (data) {
            var list_user = data.data;
            $('#list_user_block').html('');
            for (let i = 0; i < list_user.length; i++) {
                if (list_user[i].avatar == "") {
                    list_user[i].avatar = "/img/avatar.jpeg";
                }
                $('#list_user_block').append(`
            <div class="content_cmt" id="uncontent_cmt_${list_user[i].commentId}">
                    <div class="avarta">
                      <img id="img" class="img-responsive" width="35" src="${list_user[i].avatar}">
                    </div>
                    <div class="comment">

                        <div style="display: flex; align-items: center;justify-content:space-between">
                            <p class="name-avatar-comment">${list_user[i].userName}</p>
                            <p  style="margin-right: 10px; cursor: pointer;text-color:red" onclick="unblock_mess('${list_user[i].commentId}','${list_user[i].userId}','${list_user[i].avatar}','${list_user[i].userName}')">
                                  <i class="fa fa-lock" aria-hidden="true"></i></span>
                             </p>
                             

                        </div>
                    </div>

            </div>

                `)
            }

        },
        error: function () {
            // alert(myConstant.getParameter("ERROR"))

        }
    });
}


//list donate

// function getListDonate() {
//     $.ajax({
//         //url: domainUrl+"/chart/stat?id=" + id_live,
//         url: "/transaction-star/" + id_live,
//         method: 'GET',
//
//         success: function (data) {
//             var list_donate = data;
//             $('#list_donate').html('');
//             for (let i = 0; i < list_donate.length; i++) {
//                 if (list_donate[i].avatar == "") {
//                     list_donate[i].avatar = "/img/avatar.jpeg";
//                 }
//                 $('#list_donate').append(`
//         <div class="content_cmt" id="donate_${list_donate[i].id}">
//         <div class="avarta">
//           <img id="img" class="img-responsive" width="35" src="${list_donate[i].avatar}">
//         </div>
//         <div class="comment">
//
//             <div style="display: flex; align-items: center">
//                 <span class="time-content" style="color: rgb(51,51,51); font-size: 12px">${list_donate[i].time_request}</span>
//                 <p class="name-avatar-comment">${list_donate[i].user_name}</p>
//                 <p class="content_mb" style="color: rgb(82, 82, 82);width:300px;background:none;">Send ${list_donate[i].amount_star} stars</p>
//
//
//             </div>
//         </div>
//
// </div>
//
//                 `)
//             }
//
//         },
//         error: function (e) {
//             //console.log(e);
//
//         }
//     });
// }


// api fall star

// function getDropStar() {
//     var formdata = $('#form-star').serializeArray();
//     if (!$('#form-star')[0].checkValidity()) {
//         $('#form-star')[0].reportValidity();
//     } else if (formdata[1].value == '') {
//         alert('Total users required');
//     } else if (formdata[0].value == '') {
//         alert('Total star required');
//     } else {
//         $.ajax({
//             //url: domainUrl+"/chart/stat?id=" + id_live,
//             url: domainUrl + "/donate/drop-star?amountStar=" + formdata[0].value + "&amountUser=" + formdata[1].value + "&livestreamId=" + id_live,
//             method: 'POST',
//             success: function (data) {
//                 // console.log(data);
//                 // $('#total_user').val('');
//                 // $('#total_star').val('');
//                 $("#btn-dropstar").hide();
//                 $("#btn-stopdrop").show();
//                 getHistoryDropStar();
//                 alert('Drop star success!');
//
//             },
//             error: function () {
//                 // alert(myConstant.getParameter("ERROR"))
//
//             }
//         });
//     }
//
// }

// function getStopDropStar() {
//
//         $.ajax({
//             //url: domainUrl+"/chart/stat?id=" + id_live,
//             url: domainUrl + "/donate/stop/drop-star?livestreamId=" + id_live,
//             method: 'POST',
//             success: function (data) {
//
//                 $("#btn-dropstar").show();
//                 $("#btn-stopdrop").hide();
//                 getHistoryDropStar();
//                 alert('Stop drop star success!');
//
//             },
//             error: function () {
//                 // alert(myConstant.getParameter("ERROR"))
//
//             }
//         });
//
//
// }

// function getHistoryDropStar() {
//
//     $.ajax({
//         url: "/transaction-star/history/" + id_live,
//         method: 'GET',
//         success: function (data) {
//             $('#history-drop').html('');
//             for (let i = 0; i < data.length; i++) {
//                 var stt='Dropping';
//                 if(data[i].status==5){
//                     stt ='Complete';
//                 }
//                 $('#history-drop').append(`<li class="list-group-item"><strong>Setup:</strong> ${data[i].amount_star} stars / ${data[i].amount_user} users | <strong>Received:</strong> ${data[i].star_received} stars / ${data[i].user_received} users ( ${stt} )</li> `);
//             }
//         },
//         error: function () {
//             alert(myConstant.getParameter("ERROR"))
//
//         }
//     });
//
//
// }

// function getCheckDropStar() {
//
//     $.ajax({
//         url: "/transaction-star/check-drop/" + id_live,
//         method: 'GET',
//         success: function (data) {
//             if(data ==0){
//                 $("#btn-dropstar").show();
//                 $("#btn-stopdrop").hide();
//             }else{
//                 $("#btn-dropstar").hide();
//                 $("#btn-stopdrop").show();
//             }
//
//         },
//         error: function () {
//             alert(myConstant.getParameter("ERROR"))
//
//         }
//     });
//
//
// }
