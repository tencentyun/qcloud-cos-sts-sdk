require './sts'


config = {
    duration_seconds: 1800,
    secret_id: '',
    secret_key: '',
    appid: '',
    bucket: '',
    region: '',
    allow_prefix: 'exampleobject',
    allow_actions: [
        'name/cos:PutObject',
        'name/cos:InitiateMultipartUpload',
        'name/cos:ListMultipartUploads',
        'name/cos:ListParts',
        'name/cos:UploadPart',
        'name/cos:CompleteMultipartUpload'
    ]
}


sts = Sts.new(config)
puts sts.get_credential
