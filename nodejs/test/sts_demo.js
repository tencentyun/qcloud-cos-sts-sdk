var sts = require('../index')

var options;
try {
    var keyData = require("./key.json");
    options = {
        secretId: keyData.secret_id,
        secretKey: keyData.secret_key,
        durationInSeconds: 1800,
        proxy: keyData.proxy.http,
    };
} 
catch (e) {
    options = {
        // 您的 secretId
        secretId: 'xxx',
        // 您的 secretKey
        secretKey: 'xxx',
        // 临时密钥有效时长，单位是秒
        durationInSeconds: 1800
    };
}
console.debug(options);


sts.getCredential(options, function(data) {
    console.info(data)
});