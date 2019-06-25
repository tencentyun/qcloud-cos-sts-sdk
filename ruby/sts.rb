require 'base64'
require 'cgi'
require 'openssl'
require 'uri'
require 'net/http'
require 'json'

class Sts
    def initialize(**options)
        return unless options[:allow_actions]
        @allow_actions = options[:allow_actions]
        @duration = options[:duration_seconds]
        @sts_url = 'sts.tencentcloudapi.com/'
        @sts_scheme = 'https://'
        @secret_id = options[:secret_id]
        @secret_key = options[:secret_key]
        @proxy = options[:proxy]
        @region = options[:region]
        bucket = options[:bucket]
        appid = options[:appid]
        allow_prefix = options[:allow_prefix]
        @resource = "qcs::cos:#{@region}:uid/#{appid}:prefix//#{appid}/#{bucket}/#{allow_prefix}"
    end

    def get_credential
        policy = {
            version:"2.0",
            statement:{
                action:@allow_actions,
                resource:@resource,
                effect:"allow",
                principal:{qcs: "*"}

            }
        }
        policy_encode = URI.encode(policy.to_json)
        current_timestamp = Time.now.to_i
        data = {
            'SecretId': @secret_id,
            'Timestamp': current_timestamp,
            'Nonce': 6.times.map{rand(10)}.join,
            'Action': 'GetFederationToken',
            'Version': '2018-08-13',
            'DurationSeconds': @duration,
            'Name': 'cos-sts-ruby',
            'Policy': policy_encode,
            'Region': @region
        }

        data['Signature'] = encrypt('POST', @sts_url, data)

        uri = URI.parse(@sts_scheme+@sts_url)
        http = Net::HTTP.new(uri.host, uri.port)
        http.use_ssl = (uri.scheme == 'https')
        res = Net::HTTP.post_form(uri, data)
        result_json = JSON.parse res.body
        result_json = result_json['Response']
        if res.code == '200' && !result_json['Error']
            result_json['startTime'] = result_json['ExpiredTime'] - @duration
            stringify_all_keys result_json
        else
            result_json
        end
    end

    private
    def encrypt(method, url, data)
        source = data.sort.to_h.map{|k,v| "#{k}=#{v}"}.join("&")
        source = method + url + '?' + source
        hmac =OpenSSL::HMAC.digest('sha1', @secret_key, source)
        Base64.strict_encode64(hmac)
    end

    def stringify_all_keys(hash)
        stringified_hash = {}
        hash.each do |k, v|
          stringified_hash[k.downcase] = v.is_a?(Hash) ? stringify_all_keys(v) : v
        end
        stringified_hash
    end
end