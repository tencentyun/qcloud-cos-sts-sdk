package sts

import (
	"crypto/hmac"
	"crypto/sha1"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"math/rand"
	"net/http"
	"net/url"
	"sort"
	"time"
)

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
	rand.Seed(time.Now().UnixNano())
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
	rand.Seed(time.Now().UnixNano())
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
	rand.Seed(time.Now().UnixNano())
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
