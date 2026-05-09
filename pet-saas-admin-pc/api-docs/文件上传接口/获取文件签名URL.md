

## 获取文件签名URL


**接口地址**:`/api/pc/file/sign-url`


**请求方式**:`POST`


**请求数据类型**:`application/x-www-form-urlencoded,application/json`


**响应数据类型**:`*/*`


**接口描述**:<p>将原始OSS URL转换为带签名的临时访问URL，用于预览图片</p>



**请求示例**:


```javascript
{
  "fileUrl": ""
}
```


**请求参数**:


**请求参数**:


| 参数名称 | 参数说明 | 请求类型    | 是否必须 | 数据类型 | schema |
| -------- | -------- | ----- | -------- | -------- | ------ |
|fileSignUrlReq|获取文件签名URL请求|body|true|FileSignUrlReq|FileSignUrlReq|
|&emsp;&emsp;fileUrl|原始文件URL||true|string||


**响应状态**:


| 状态码 | 说明 | schema |
| -------- | -------- | ----- | 
|200|OK|RString|


**响应参数**:


| 参数名称 | 参数说明 | 类型 | schema |
| -------- | -------- | ----- |----- | 
|code||integer(int32)|integer(int32)|
|message||string||
|data||string||
|success||boolean||


**响应示例**:
```javascript
{
	"code": 0,
	"message": "",
	"data": "",
	"success": true
}
```