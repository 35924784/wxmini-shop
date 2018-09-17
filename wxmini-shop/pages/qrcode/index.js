const App = getApp();

Page({
  data: {
    qrurl: '',
  },
  onLoad: function (options) {

    const token = App.WxService.getStorageSync('token')
    App.HttpService.checkSession(token)
        .then(res => {
            console.log(res) //session有效则不隐藏转发按钮
            const data = res.data
            if (data.retCode == '0') {
                this.setData({
                    qrurl: 'https://www.example.com/restapi/salesqrcode?token=' + token
                })
            }           
        }).catch((err) => {
            console.log(err)
            if(err.statusCode == 456){
                App.WxService.removeStorageSync('token')
            }
            
        })
  },
  previewImage: function (e) {
    wx.previewImage({
      urls: [this.data.qrurl]
    })
  },
  saveImage: function (e) {
    wx.getImageInfo({
        src: this.data.qrurl,
        success: function (ret) {
            var path = ret.path;
            wx.saveImageToPhotosAlbum({
                filePath: path,
                success(result) {
                    console.log(result)
                }
            })
        }
    })
  }
})