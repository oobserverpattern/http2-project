package resource

import (
	"strings"
)

// IParser 请求解析器
type IParser interface {
	ParseURL(urlComs *[]string) IResource
}

const (
	// AVCREQ avc resource req
	AVCREQ string = "avc"
	// SVCREQ svc resource req
	SVCREQ string = "svc"
)

var parserMap = make(map[string]IParser)

func init() {
	parserMap[AVCREQ] = &AVCParser{}
	parserMap[SVCREQ] = &SVCParser{}
}

// ParseURL 从req从获取起源
func ParseURL(urlPath string) IResource {
	urlPath = strings.Trim(urlPath, "/")
	//以/拆分url
	components := strings.Split(urlPath, "/")
	if len(components) > 0 {
		//第一个代表请求类型
		resourceType := strings.ToLower(components[0])
		parser, ok := parserMap[resourceType]
		if ok {
			return parser.ParseURL(&components)
		}
	}

	return nil
}
