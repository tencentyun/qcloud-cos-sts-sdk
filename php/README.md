## 获取 SDK

- composer 安装
```
创建composer.json的文件，内容如下：
{
    "require":{
        "qcloud_sts/qcloud-sts-sdk": "3.0.*"
    }
}
```

## 查看SDK源码

PHP SDK 源码已迁移到 [COS STS PHP SDK](https://github.com/tencentyun/qcloud-cos-sts-php-sdk)。

## 查看使用示例

请查看 [demo 示例](https://github.com/tencentyun/qcloud-cos-sts-sdk/tree/master/php/demo)，里面描述了如何调用SDK。

### 返回数据示例

```
{
	"expiredTime": 1589258683,
	"expiration": "2020-05-12T04:44:43Z",
	"credentials": {
		"sessionToken": "Biypn6exa48PpMe7wFerEnNMpBKKPQo180c57e0a5275ebae506d7851a85f36a4P0TV5UFR3FYJjsoZA1tk6uRKoDRzc6-60BmwLqdS75OhjHEa7GlVYpL_ofKQJTpPKziKX7FnI10D_6qtLdjzf2NdsyUtQEd5kPpDCOQJZn9-BpleqWQe8oyH_2u7xi2f0FtjCYaoGIZ_lUqlILXQwr0B0t3hLfL4xNE-EmVjUlUXa16HxVCn4_hJetqo9LmI0AgLOjCbYx9aVrsV10eDsRta-TQSIXmJNP3aJ6oz8d8GBTgTE1I2qSFDnv9pjtQKW8HZWI_glPIfmHXCCwAESxEFL_owGz839Va0qYhF6LkfVmsuoU1zNcvJR1w3cIE6izV3SKHaOtWaew3IOervuOPoN3S2oYGNwv2EavtDAWyUBIeI7X6nMVzlpnyJ-3bkIhOq9QVIQAs8wh5A0u9mvKWugT5t6qgyEgvEZSj9k6p-JjwxMgLC6s5uK1i_nnf4fN7ZQ6I-JAfHnH4jEDiVtJgXqfuWPX_vnzskyR2Co6E",
		"tmpSecretId": "AKIDTRPc-oe6c_avPSRwFVsPDyy3IoAr3szMajlOGuoEXY1232YLy6j4f-xZ5zL-NBMG",
		"tmpSecretKey": "2v29SZztGYk6SGwHYm\/chJXdD3zPRFasmPoJiCmlR\/I="
	},
	"requestId": "69ef6295-b981-464d-9816-9c2ef92189d1",
	"startTime": 1589256883
}
```
