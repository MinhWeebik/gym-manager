$(document).ready(function() {
const dateFormat = /*[[#{dateFormat}]]*/ 'dd-MM-yyyy';
  // Lấy reference tới element canvas trong HTML
  var ctx = document.getElementById('myChart').getContext('2d');
  var data = {
    labels: arrLabel,
    datasets: [{
      label: title,
      data: arrData,
      fill: false,
      borderColor: 'rgb(50,117,71)',
      borderWidth: 1
    },
    {
      label: title2,
      data: arrData2,
      fill: false,
      borderColor: 'rgb(255, 0, 0)', // Màu của đường thứ hai
      borderWidth: 1
    }]
  };
// Tạo biểu đồ dạng line bằng Chart.js
  new Chart(ctx, {
    type: 'line',
    data: data,
    options: {
//      responsive: false, // Tắt chế độ phản ứng
      maintainAspectRatio: false,
      scales: {
        x: {
        display: true,
        title: {
          display: true
//          text: dateFormat
        }
      },
        y: {
          display: true,
          min: 0,
          title: {
            display: true,
            text: titleY
          },
          ticks: {
//            stepSize: 100, // Khoảng cách giữa các mốc
            beginAtZero: true, // Bắt đầu từ 0
            precision: 2 // Số thập phân thứ hai
          }
        }
      }
    }
  });
  });