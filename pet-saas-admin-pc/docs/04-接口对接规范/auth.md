# 认证接口

## 门店管理员登录

**接口**：`POST /api/auth/pc/login`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | string | 是 | 账号 |
| password | string | 是 | 密码 |
| tenantId | number | 是 | 租户ID |

**响应数据**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "pc_xxxxxx",
    "admin": {
      "id": 1,
      "username": "admin",
      "role": "shop_admin",
      "status": 1
    }
  }
}
```

## 平台管理员登录

**接口**：`POST /api/auth/platform/login`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| username | string | 是 | 账号 |
| password | string | 是 | 密码 |

**响应数据**：同门店登录

## 获取当前用户信息

**接口**：`GET /api/auth/current`

**请求头**：`satoken: {token}`

**响应数据**：

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "role": "shop_admin",
    "tenantId": 1
  }
}
```

## 登出

**接口**：`POST /api/auth/logout`

**请求头**：`satoken: {token}`

**响应数据**：

```json
{
  "code": 200,
  "message": "success"
}
```
