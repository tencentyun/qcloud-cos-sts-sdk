export interface GetCredentialOptions {
  /** 云 API 密钥 Id */
  secretId: string;

  /** 云 API 密钥 Key */
  secretKey: string;

  /** 要申请的临时密钥，限定的权限范围 */
  policy: object;

  /** 要申请的临时密钥最长有效时间，单位秒，默认 1800，最大可设置 7200 */
  durationSeconds?: number;

  /** 代理地址，如："http://proxy.example.com:8080" */
  proxy?: string;

  /** 可以通过改参数指定请求的域名 */
  host?: string;

  /** 可以通过改参数指定请求的域名,与host二选一即可 */
  endpoint?: string;
}

export interface GetRoleCredentialOptions extends GetCredentialOptions {
  roleArn: string;
}

export interface CredentialData {
  /** 密钥的起始时间，是 UNIX 时间戳 */
  startTime: number;

  /** 密钥的失效时间，是 UNIX 时间戳 */
  expiredTime: number;

  /** 临时云 API 凭据 */
  credentials: {
    /** 临时密钥 Id，可用于计算签名 */
    tmpSecretId: string;

    /** 临时密钥 Key，可用于计算签名 */
    tmpSecretKey: string;

    /** 请求时需要用的 token 字符串，最终请求 COS API 时，需要放在 Header 的 x-cos-security-token 字段 */
    sessionToken: string;
  }
}

/** 获取临时密钥接口(获取联合身份临时访问凭证)。 */
export function getCredential(options: GetCredentialOptions): Promise<CredentialData>;

/** 获取临时密钥接口(获取联合身份临时访问凭证)。 */
export function getCredential(options: GetCredentialOptions, callback: (err: Object, data: CredentialData) => void): void;

/** 获取临时密钥接口(申请扮演角色)。 */
export function getRoleCredential(options: GetRoleCredentialOptions): Promise<CredentialData>;

/** 获取临时密钥接口(申请扮演角色)。 */
export function getRoleCredential(options: GetRoleCredentialOptions, callback: (err: Object, data: CredentialData) => void): void;

/** 表示当客户端的请求，最少需要什么样的权限，是一个键值对象 */
export interface CosPolicyScope {
  /** 操作名称，如 `"name/cos:PutObject"` */
  action: string | string[];

  /** 存储桶名称，格式：`test-1250000000` */
  bucket: string;

  /** 园区名称，如 `ap-guangzhou` */
  region: string;

  /** 拼接 resource 字段所需的 key 前缀，客户端 SDK 默认传固定文件名如 `"dir/1.txt"`，支持 * 结尾如 `"dir/*"` */
  prefix: string;
}

export interface PolicyDescription {
  version: '2.0',
  statement: PolicyStatement[];
}

export interface PolicyStatement {
  action: string | string[];
  effect: 'allow';
  principal: { qcs: '*' };
  resource: string;
}

/**
 * 获取 policy 接口。本接口适用于接收 Web、iOS、Android 客户端 SDK 提供的 Scope 参数。推荐您把 Scope 参数放在请求的 Body 里面，通过 POST 方式传到后台。
 * @param scopes 表示当客户端的请求，最少需要什么样的权限，是一个键值对象的数组	
 */
export function getPolicy(scopes: CosPolicyScope[]): PolicyDescription;
