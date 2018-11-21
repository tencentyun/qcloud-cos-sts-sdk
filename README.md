# QCloud COS 服务临时密钥 SDK

本 SDK 可以帮助您在后台快速搭建一个临时密钥服务，用于访问腾讯云 COS 下的资源。

## 访问管理

腾讯云 COS 服务在使用时需要对请求进行访问管理。通过临时密钥机制，您可以临时授权您的 App 访问您的存储资源，而不会泄露您的永久密钥。密钥的有效期由您指定，过期后自动失效。**通常，我们都不建议您把永久密钥放到客户端代码中。**本文主要介绍如何在后台快速搭建一个临时密钥服务，通过生成的临时密钥来对上传或者下载请求进行签名，从而保证您数据的安全性。

## 架构

### 整体架构图如下所示：

![cos接入cam框架图](http://mc.qcloudimg.com/static/img/b1e187a9ec129ffc766c07a733ef4dd6/image.jpg)

其中：

- 应用 APP：即用户手机上的 App。
 
- COS：[腾讯云对象存储](https://cloud.tencent.com/product/cos)，负责存储 App 上传的数据。

- CAM：[腾讯云访问管理](https://cloud.tencent.com/product/cam)，用于生成 COS 的临时密钥。

- 应用服务器：用户自己的后台服务器，这里用于获取临时密钥，并返回给应用 App。

## 获取永久密钥

临时密钥需要通过永久密钥才能生成。请登录 [腾讯云访问管理控制台](https://console.cloud.tencent.com/cam/capi) 获取，包含：

- SecretId
- SecretKey
  
## 如何快速搭建临时密钥服务

### 集成 SDK 到 您的后台服务

如果您已经有独立的后台服务，我们建议您直接集成我们提供的 SDK 到现在的后台服务中。

#### 第一步：集成服务器 SDK

目前我们提供了以下语言的 SDK，帮忙您快速在后台集成生成密钥的功能，您可以根据自己后台的架构自行选择：

* Java
* Nodejs
* PHP
* Python

#### 第二步：发布 Web API

将您的服务以 Web API 的方式发布出去，这样您授权的客户端 App，如 Android、iOS，都可以通过标准的 HTTP 协议请求到临时密钥，访问 COS 服务。
