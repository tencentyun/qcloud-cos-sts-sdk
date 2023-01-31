package sts

import (
	"bytes"
	"crypto/hmac"
	"crypto/sha1"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"math/rand"
	"net/http"
	"net/url"
	"sort"
	"strconv"
	"time"
)

func init() {
	rand.Seed(time.Now().UnixNano())
}

const (
	kHost = "sts.tencentcloudapi.com"
)

type CredentialPolicyStatement struct {
	Action    []string                          `json:"action,omitempty"`
	Effect    string                            `json:"effect,omitempty"`
	Resource  []string                          `json:"resource,omitempty"`
	Condition map[string]map[string]interface{} `json:"condition,omitempty"`
}

type CredentialPolicy struct {
	Version   string                      `json:"version,omitempty"`
	Statement []CredentialPolicyStatement `json:"statement,omitempty"`
}

type CredentialOptions struct {
	Policy          *CredentialPolicy
	Region          string
	DurationSeconds int64
	RoleArn         string
	RoleSessionName string
	ExternalId      string
}

type Credentials struct {
	TmpSecretID  string `json:"TmpSecretId,omitempty"`
	TmpSecretKey string `json:"TmpSecretKey,omitempty"`
	SessionToken string `json:"Token,omitempty"`
}

type CredentialError struct {
	Code      string `json:"Code,omitempty"`
	Message   string `json:"Message,omitempty"`
	RequestId string `json:"RequestId,omitempty"`
}

type CredentialResult struct {
	Credentials *Credentials     `json:"Credentials,omitempty"`
	ExpiredTime int              `json:"ExpiredTime,omitempty"`
	Expiration  string           `json:"Expiration,omitempty"`
	StartTime   int              `json:"StartTime,omitempty"`
	RequestId   string           `json:"RequestId,omitempty"`
	Error       *CredentialError `json:"Error,omitempty"`
}

type WebIdentityOptions struct {
	Region           string `json:"-"`
	ProviderId       string
	WebIdentityToken string
	RoleArn          string
	RoleSessionName  string
	DurationSeconds  int64
}

func (e *CredentialError) Error() string {
	return fmt.Sprintf("Code: %v, Message: %v, RequestId: %v", e.Code, e.Message, e.RequestId)
}

type ClientConf struct {
	Host   string
	Scheme string
}
type Client struct {
	client     *http.Client
	Credential CredentialIface
	conf       ClientConf
}

type CredentialIface interface {
	GetSecretId() string
	GetSecretKey() string
	GetToken() string
}
type Credential struct {
	SecretId  string
	SecretKey string
	Token     string // 不支持Token
}

func (c *Credential) GetSecretId() string {
	return c.SecretId
}
func (c *Credential) GetSecretKey() string {
	return c.SecretKey
}
func (c *Credential) GetToken() string {
	return c.Token
}

type ClientOption = func(*Client)

func Host(host string) ClientOption {
	return func(cli *Client) {
		cli.SetHost(host)
	}
}

func Scheme(scheme string) ClientOption {
	return func(cli *Client) {
		cli.SetScheme(scheme)
	}
}

func NewClient(secretId, secretKey string, hc *http.Client, opt ...ClientOption) *Client {
	if hc == nil {
		hc = &http.Client{}
	}
	c := &Client{
		client: hc,
		Credential: &Credential{
			SecretId:  secretId,
			SecretKey: secretKey,
		},
		conf: ClientConf{
			Host:   kHost,
			Scheme: "https",
		},
	}
	for _, fn := range opt {
		fn(c)
	}
	return c
}

func NewClientWithCredential(cred CredentialIface, hc *http.Client, opt ...ClientOption) *Client {
	if hc == nil {
		hc = &http.Client{}
	}
	c := &Client{
		client:     hc,
		Credential: cred,
		conf: ClientConf{
			Host:   kHost,
			Scheme: "https",
		},
	}
	for _, fn := range opt {
		fn(c)
	}
	return c
}

func (c *Client) SetHost(host string) {
	c.conf.Host = host
}

func (c *Client) SetScheme(scheme string) {
	c.conf.Scheme = scheme
}

func getPolicy(policy *CredentialPolicy) (string, error) {
	if policy == nil {
		return "", nil
	}
	res := policy
	if policy.Version == "" {
		res = &CredentialPolicy{
			Version:   "2.0",
			Statement: policy.Statement,
		}
	}
	bs, err := json.Marshal(res)
	if err != nil {
		return "", err
	}
	return string(bs), nil
}

func makeFlat(params map[string]interface{}) string {
	keys := make([]string, 0, len(params))
	for k, _ := range params {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	var plainParms string
	for _, k := range keys {
		plainParms += fmt.Sprintf("&%v=%v", k, params[k])
	}
	return plainParms[1:]
}

func (c *Client) signed(method string, params map[string]interface{}) string {
	source := method + c.conf.Host + "/?" + makeFlat(params)

	hmacObj := hmac.New(sha1.New, []byte(c.Credential.GetSecretKey()))
	hmacObj.Write([]byte(source))

	sign := base64.StdEncoding.EncodeToString(hmacObj.Sum(nil))

	return sign
}

func sha256hex(s string) string {
	b := sha256.Sum256([]byte(s))
	return hex.EncodeToString(b[:])
}

func hmacsha256(s, key string) string {
	hashed := hmac.New(sha256.New, []byte(key))
	hashed.Write([]byte(s))
	return string(hashed.Sum(nil))
}

func (c *Client) GetCredential(opt *CredentialOptions) (*CredentialResult, error) {
	if opt == nil || opt.Policy == nil {
		return nil, errors.New("CredentialOptions is illegal")
	}
	region := opt.Region
	if region == "" {
		region = "ap-guangzhou"
	}
	durationSeconds := opt.DurationSeconds
	if durationSeconds == 0 {
		durationSeconds = 1800
	}
	policy, err := getPolicy(opt.Policy)
	if err != nil {
		return nil, err
	}
	params := map[string]interface{}{
		"SecretId":        c.Credential.GetSecretId(),
		"Policy":          url.QueryEscape(policy),
		"DurationSeconds": durationSeconds,
		"Region":          region,
		"Timestamp":       time.Now().Unix(),
		"Nonce":           rand.Int(),
		"Name":            "cos-sts-go",
		"Action":          "GetFederationToken",
		"Version":         "2018-08-13",
	}
	resp, err := c.sendRequest(params)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	result := &CredentialCompleteResult{}
	err = json.NewDecoder(resp.Body).Decode(result)
	if err == io.EOF {
		err = nil // ignore EOF errors caused by empty response body
	}
	if err != nil {
		return nil, err
	}
	if result.Response != nil && result.Response.Error != nil {
		result.Response.Error.RequestId = result.Response.RequestId
		return nil, result.Response.Error
	}
	if result.Response != nil && result.Response.Credentials != nil {
		result.Response.StartTime = result.Response.ExpiredTime - int(durationSeconds)
		return result.Response, nil
	}
	return nil, errors.New(fmt.Sprintf("GetCredential failed, result: %v", result.Response))
}

type CredentialCompleteResult struct {
	Response *CredentialResult `json:"Response"`
}

func (c *Client) RequestCredential(opt *CredentialOptions) (*http.Response, error) {
	if opt == nil || opt.Policy == nil {
		return nil, errors.New("CredentialOptions is illegal")
	}
	region := opt.Region
	if region == "" {
		region = "ap-guangzhou"
	}
	durationSeconds := opt.DurationSeconds
	if durationSeconds == 0 {
		durationSeconds = 1800
	}
	policy, err := getPolicy(opt.Policy)
	if err != nil {
		return nil, err
	}
	params := map[string]interface{}{
		"SecretId":        c.Credential.GetSecretId(),
		"Policy":          url.QueryEscape(policy),
		"DurationSeconds": durationSeconds,
		"Region":          region,
		"Timestamp":       time.Now().Unix(),
		"Nonce":           rand.Int(),
		"Name":            "cos-sts-go",
		"Action":          "GetFederationToken",
		"Version":         "2018-08-13",
	}
	return c.sendRequest(params)
}

func (c *Client) GetRoleCredential(opt *CredentialOptions) (*CredentialResult, error) {
	if opt == nil || opt.RoleArn == "" || opt.RoleSessionName == "" {
		return nil, errors.New("CredentialOptions is illegal")
	}
	region := opt.Region
	if region == "" {
		region = "ap-guangzhou"
	}
	durationSeconds := opt.DurationSeconds
	if durationSeconds == 0 {
		durationSeconds = 1800
	}
	policy, err := getPolicy(opt.Policy)
	if err != nil {
		return nil, err
	}
	params := map[string]interface{}{
		"SecretId":        c.Credential.GetSecretId(),
		"Policy":          url.QueryEscape(policy),
		"RoleArn":         opt.RoleArn,
		"RoleSessionName": opt.RoleSessionName,
		"ExternalId":      opt.ExternalId,
		"DurationSeconds": durationSeconds,
		"Region":          region,
		"Timestamp":       time.Now().Unix(),
		"Nonce":           rand.Int(),
		"Action":          "AssumeRole",
		"Version":         "2018-08-13",
	}

	resp, err := c.sendRequest(params)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	result := &CredentialCompleteResult{}
	err = json.NewDecoder(resp.Body).Decode(result)
	if err == io.EOF {
		err = nil // ignore EOF errors caused by empty response body
	}
	if err != nil {
		return nil, err
	}
	if result.Response != nil && result.Response.Error != nil {
		result.Response.Error.RequestId = result.Response.RequestId
		return nil, result.Response.Error
	}
	if result.Response != nil && result.Response.Credentials != nil {
		result.Response.StartTime = result.Response.ExpiredTime - int(durationSeconds)
		return result.Response, nil
	}
	return nil, errors.New(fmt.Sprintf("GetCredential failed, result: %v", result.Response))

}

func (c *Client) AssumeRoleWithWebIdentity(opt *WebIdentityOptions) (*CredentialResult, error) {
	if opt == nil || opt.ProviderId == "" || opt.WebIdentityToken == "" || opt.RoleArn == "" {
		return nil, errors.New("WebIdentityOptions is illegal")
	}
	topt := *opt
	if topt.Region == "" {
		topt.Region = "ap-guangzhou"
	}
	if topt.DurationSeconds == 0 {
		topt.DurationSeconds = 1800
	}
	body, err := json.Marshal(topt)
	if err != nil {
		return nil, err
	}
	params := map[string]string{
		"Host":           c.conf.Host,
		"X-TC-Action":    "AssumeRoleWithWebIdentity",
		"X-TC-Version":   "2018-08-13",
		"X-TC-Region":    topt.Region,
		"Content-Type":   "application/json",
		"X-TC-Timestamp": fmt.Sprintf("%v", time.Now().Unix()),
		"Authorization":  "SKIP",
	}

	resp, err := c.sendRequestv3(http.MethodPost, params, body)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	result := &CredentialCompleteResult{}
	err = json.NewDecoder(resp.Body).Decode(result)
	if err == io.EOF {
		err = nil // ignore EOF errors caused by empty response body
	}
	if err != nil {
		return nil, err
	}
	if result.Response != nil && result.Response.Error != nil {
		result.Response.Error.RequestId = result.Response.RequestId
		return nil, result.Response.Error
	}
	if result.Response != nil && result.Response.Credentials != nil {
		result.Response.StartTime = result.Response.ExpiredTime - int(topt.DurationSeconds)
		return result.Response, nil
	}
	return nil, errors.New(fmt.Sprintf("GetCredential failed, result: %v", result.Response))
}

func (c *Client) sendRequest(params map[string]interface{}) (*http.Response, error) {
	paramValues := url.Values{}
	for k, v := range params {
		paramValues.Add(fmt.Sprintf("%v", k), fmt.Sprintf("%v", v))
	}
	sign := c.signed("POST", params)
	paramValues.Add("Signature", sign)

	urlStr := fmt.Sprintf("%s://%s", c.conf.Scheme, c.conf.Host)
	resp, err := c.client.PostForm(urlStr, paramValues)
	return resp, err
}

func (c *Client) sendRequestv3(method string, params map[string]string, body []byte) (*http.Response, error) {
	urlStr := fmt.Sprintf("%s://%s", c.conf.Scheme, c.conf.Host)
	req, err := http.NewRequest(method, urlStr, bytes.NewReader(body))
	if err != nil {
		return nil, err
	}
	for key, value := range params {
		req.Header.Add(key, value)
	}
	if req.Header.Get("Authorization") == "" {
		c.signedv3(req, body)
	}
	resp, err := c.client.Do(req)
	return resp, err
}

func (c *Client) signedv3(request *http.Request, body []byte) {
	canonicalHeaders := fmt.Sprintf("content-type:%s\nhost:%s\n", request.Header.Get("Content-Type"), request.Header.Get("Host"))
	signedHeaders := "content-type;host"
	hashedRequestPayload := sha256hex(string(body))

	canonicalRequest := fmt.Sprintf("%s\n%s\n%s\n%s\n%s\n%s",
		request.Method,
		"/",
		"",
		canonicalHeaders,
		signedHeaders,
		hashedRequestPayload)

	algorithm := "TC3-HMAC-SHA256"
	requestTimestamp := request.Header.Get("X-TC-Timestamp")
	timestamp, _ := strconv.ParseInt(requestTimestamp, 10, 64)
	t := time.Unix(timestamp, 0).UTC()
	// must be the format 2006-01-02, ref to package time for more info
	date := t.Format("2006-01-02")
	credentialScope := fmt.Sprintf("%s/%s/tc3_request", date, "sts")
	hashedCanonicalRequest := sha256hex(canonicalRequest)
	string2sign := fmt.Sprintf("%s\n%s\n%s\n%s",
		algorithm,
		requestTimestamp,
		credentialScope,
		hashedCanonicalRequest)

	secretDate := hmacsha256(date, "TC3"+c.Credential.GetSecretKey())
	secretService := hmacsha256("sts", secretDate)
	secretKey := hmacsha256("tc3_request", secretService)
	signature := hex.EncodeToString([]byte(hmacsha256(string2sign, secretKey)))

	// build authorization
	authorization := fmt.Sprintf("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s",
		algorithm,
		c.Credential.GetSecretId(),
		credentialScope,
		signedHeaders,
		signature)

	request.Header.Set("Authorization", authorization)
}
