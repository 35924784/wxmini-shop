const App = getApp();

Page({
  data: {
    acctimg: 'https://www.example.com/restapi/image/da9769cdf413496c85cfef1b11229bad',
  },
  onLoad: function (options) {
  },
  previewImage: function (e) {
  	wx.previewImage({
  		urls: [this.data.acctimg]    // 需要预览的图片http链接列表
	})
  }
})