package pushhandler

import (
	"log"
	"time"

	"github.com/custhk/http2/resource"
	"github.com/custhk/http2/task"
	"github.com/custhk/http2/util"
	"golang.org/x/net/http2"
)

// DefaultPushHandler default push handler
type DefaultPushHandler struct {
	// key push boostask id
	PushBossTasks map[string]*task.PushBossTask
}

// NewDefaultPushHandler reutrn new push handler
func NewDefaultPushHandler() *DefaultPushHandler {
	return &DefaultPushHandler{PushBossTasks: make(map[string]*task.PushBossTask)}
}

// AddTask addTask
func (ph *DefaultPushHandler) AddTask(task *task.PushBossTask) {
	ph.PushBossTasks[task.ID] = task
}

// HandlePush func to deal with server push
// push处理方法
func (ph *DefaultPushHandler) HandlePush(r *http2.PushedRequest) {
	handleWrite := make(chan struct{})
	// promise request
	promise := r.Promise
	// 开启线程，处理push回来的文件
	go func() {
		defer close(handleWrite)
		if promise == nil {
			log.Printf("promise not received")
			return
		}
		bossTaskID := promise.Header.Get(task.TaskIDKey)
		if bossTaskID == "" {
			log.Printf("miss boss task ID,so it not request push")
		}
		// parse to get fileInfo
		// 解析获取push文件信息
		fileInfo := resource.ParseURL(promise.URL.Path)
		if fileInfo != nil {
			// to get pushBossTaskID from promise request from server
			pushBossTask, ok := ph.PushBossTasks[bossTaskID]
			if ok {
				workerTask := pushBossTask.GetWorkerTask(fileInfo.GetFileName())
				if workerTask != nil {
					workerTask.BindingPushedRequest(r)
				}
			}
			//等待响应
			push, pushErr := r.ReadResponse(r.Promise.Context())
			//receiveTimeStamp := time.Now().UnixNano() / 1e6

			if pushErr != nil {
				log.Printf("push error = %v; want %v", pushErr, nil)
			}
			if push == nil {
				log.Printf("push not received")
			} else {
				//从响应中读取push回来的文件，并更新文件信息属性
				fileInfo = util.SaveFileByFileInfo(fileInfo, push)
				if fileInfo != nil {
					log.Printf("save  push file = %q\n", promise.URL.Path)
					log.Printf("push file size= %v\n", fileInfo.GetDataSize())
					//是协作请求返回的push文件
					if ok {
						//更新子任务完成情况
						pushBossTask.AddDone(fileInfo.GetFileName())
					}
					// repTimeStamp, err := strconv.ParseInt(push.Header.Get("repTimeStamp"), 10, 64)
					// if err != nil {
					// 	log.Printf("parse server rep timestamp error! ERROR:%v", err)
					// }
					// waitTime := receiveTimeStamp - repTimeStamp
					// log.Printf("waitTime:%v ms", waitTime)
					// var bandWidth int64 = 0
					// if waitTime != 0 {
					// 	bandWidth = fileInfo.DataSize / waitTime
					// }

					// log.Printf("bandWidth:%v B/s", bandWidth)
				}

			}
		}

	}()
	//超时取消，如果在5秒钟之内，任务没有完成，则取消当前push 文件处理任务
	select {
	case <-handleWrite:
	case <-time.After(5 * time.Second):
		//case <-time.After(1 * time.Nanosecond):
		r.Cancel()
		log.Printf("-------cancel push file = %q\n-----------", promise.URL.Path)
	}

}
