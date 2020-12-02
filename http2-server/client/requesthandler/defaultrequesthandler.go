package requesthandler

import (
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"

	serverPolicy "github.com/custhk/http2/server/policy"
	"github.com/custhk/http2/task"
	"github.com/custhk/http2/util"
)

// DefaultRequestHandler default request handler
type DefaultRequestHandler struct {
	client      *http.Client
	pushChannel chan *task.PushBossTask
}

// NewDefaultRequestHandler return a new DefaultRequestHandler
func NewDefaultRequestHandler(client *http.Client) *DefaultRequestHandler {
	return &DefaultRequestHandler{
		client:      client,
		pushChannel: make(chan *task.PushBossTask),
	}
}

// PushReq push request
func (handler *DefaultRequestHandler) PushReq(pbt *task.PushBossTask) {
	handler.pushChannel <- pbt
}

// StartReq start to req mpd
// 双向通信请求
func (handler *DefaultRequestHandler) StartReq(mpdURL string) {
	// 创建一个pipe（管道），这个对象实现了 io.Reader和io.Writer方法。先write,后read
	pr, pw := io.Pipe()
	mpdReq, err := http.NewRequest(http.MethodGet, mpdURL, ioutil.NopCloser(pr))
	mpdReq.Header.Set(serverPolicy.HEADERKEY, serverPolicy.CooperaterPusherPolicy)
	if err != nil {
		log.Printf("创建请求失败: %s", err)
		return
	}
	// 发送初始请求
	mpdResp, err := handler.client.Do(mpdReq)
	if err != nil {
		log.Printf("发送请求失败: %s", err)
		return
	}
	// 读取push管道内容，发送后续push请求，当管道内容为空时阻塞，管道默认大小为1
	go func() {
		for {
			var pushBossTask = <-handler.pushChannel
			fmt.Fprintf(pw, pushBossTask.ToJSONString()+"\n")
		}
	}()
	// 保存基础请求返回的文件
	util.SaveFileByURLPath(mpdReq.URL.Path, mpdResp)

}
