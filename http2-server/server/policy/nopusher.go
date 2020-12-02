package policy

import (
	"io/ioutil"
	"net/http"

	"github.com/custhk/http2/resource"
)

// NoPusher do not push
// 从url解析出请求目标，直接返回请求资源
type NoPusher struct{}

//Handle 处理push 请求
func (np *NoPusher) Handle(w http.ResponseWriter, r *http.Request) {
	//解析URL，获取请求资源
	fileInfo := resource.ParseURL(r.URL.Path)
	if fileInfo != nil {
		file, err := ioutil.ReadFile(fileInfo.GetLocalPath())
		if err != nil {
			panic(err)
		}
		w.Header().Add("Content-Type", "application/octet-stream")
		w.Header().Add("Access-Control-Allow-Origin", "*")
		// w.Header().Add("repTimeStamp", strconv.FormatInt(time.Now().UnixNano()/1e6, 10))
		w.Write(file)
	}
}
