package policy

import (
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"github.com/custhk/http2/resource"
)

const (
	_SpecifiedPushList = "promiseList"
)

// GetSpecPromiseHeaderKey for push
func GetSpecPromiseHeaderKey() string {
	return _SpecifiedPushList
}

// SpecifiedPusher do not push
// 指定push文件列表
type SpecifiedPusher struct{}

// parserRequest 解析请求
func (sp *SpecifiedPusher) parserRequest(r *http.Request) []resource.IResource {
	//在header中利用SpecPromiseHeaderKey来获取push文件列表
	promiseListStr := r.Header.Get(_SpecifiedPushList)
	result := []resource.IResource{}
	//解析URL获取基础请求资源信息
	fileInfo := resource.ParseURL(r.URL.Path)
	if fileInfo != nil {
		result = append(result, fileInfo)
	}
	if promiseListStr == "" {
		return result
	}
	//从promiseList中解析SVC文件列表，并加入到请求资源集中
	result = append(result, resource.ParseSVCFileNames(promiseListStr)...)
	r.Header.Del(_SpecifiedPushList)
	return result
}

//Handle 处理push
func (sp *SpecifiedPusher) Handle(w http.ResponseWriter, r *http.Request) {
	//解析请求
	fileInfos := sp.parserRequest(r)
	//读取基础请求资源
	file, err := ioutil.ReadFile(fileInfos[0].GetLocalPath())
	if err != nil {
		panic(err)
	}
	length := len(fileInfos)
	//如果有promiseList,尝试push
	if length > 0 {
		pusher, ok := w.(http.Pusher)
		if ok {
			//遍历push
			for index := 1; index < length; index++ {
				log.Printf("Try to push: %v", fileInfos[index].GetURLPath())
				if err := pusher.Push(fileInfos[index].GetURLPath(), nil); err != nil {
					log.Printf("Failed to push: %v", err)
					if strings.Contains(err.Error(), "recursive") {
						break
					}
				}
			}
		}
	}
	w.Header().Add("Content-Type", "application/octet-stream")
	w.Write(file)
}
