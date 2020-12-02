package policy

import (
	"bufio"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"
	"time"

	"github.com/custhk/http2/resource"
	"github.com/custhk/http2/task"
)

// CooperaterPusher  push mpd
type CooperaterPusher struct{}

func (cp *CooperaterPusher) parserRequest(r *http.Request) resource.IResource {
	return resource.ParseURL(r.URL.Path)
}

func (cp *CooperaterPusher) parserBody(body string) (*task.PushBossTask, []resource.IResource) {
	body = strings.Trim(body, "\n")
	pushBossTask := task.JSONToPushBossTask(body)
	log.Printf("get push task target: %v\n", pushBossTask.TaskString)
	log.Printf("get push task start time: %v\n", pushBossTask.TaskStartTimeStamp)
	result := []resource.IResource{}
	result = append(result, resource.ParseSVCFileNames(pushBossTask.TaskString)...)
	return pushBossTask, result
}

//Handle 处理协作请求
func (cp *CooperaterPusher) Handle(w http.ResponseWriter, r *http.Request) {
	log.Printf("request from the client:%v", r.RemoteAddr)
	pusher, ok := w.(http.Pusher)
	//开启请求处理线程
	go func() {
		//读取请求body
		buf := bufio.NewReader(r.Body)
		for {
			//每次请求以换行符号分割
			line, err := buf.ReadString('\n')
			if err != nil {
				fmt.Printf("read body err, %v\n", err)
				continue
			}
			//解析body，获取push请求boss id和请求文件集合
			pushBossTask, fileInfos := cp.parserBody(line)
			log.Printf("Task ID: %v\n", pushBossTask.ID)
			length := len(fileInfos)
			if length > 0 {
				if ok {
					//push文件集合
					for _, fileInfo := range fileInfos {
						log.Printf("Try to push: %v", fileInfo.GetURLPath())
						var header http.Header = make(http.Header)
						//promise req的header中添加任务id
						//task id
						header.Add(task.TaskIDKey, pushBossTask.ID)
						// set push header
						pushOption := &http.PushOptions{Header: header}
						// push promise文件
						if err := pusher.Push(fileInfo.GetURLPath(), pushOption); err != nil {
							log.Printf("Failed to push: %v", err)
							if strings.Contains(err.Error(), "recursive") {
								break
							}
						}
					}
				}
			}
		}

	}()
	//协作请求的第一个请求为mpd文件(这里可以改，主要是要用到文件名字)
	fileInfo := cp.parserRequest(r)
	if fileInfo != nil {
		clientGone := w.(http.CloseNotifier).CloseNotify()
		w.Header().Set("Content-Type", "application/octet-stream")
		file, err := ioutil.ReadFile(fileInfo.GetLocalPath())
		if err != nil {
			panic(err)
		}
		w.Write(file)
		//定时器
		ticker := time.NewTicker(5 * time.Second)
		defer ticker.Stop()
		//定时发送mpd文件
		for {
			if ok {
				if err := pusher.Push(fileInfo.GetURLPath(), nil); err != nil {
					log.Printf("Failed to push: %v", err)
					if strings.Contains(err.Error(), "recursive") {
						break
					}
				}
			}
			//阻塞等待信号
			select {
			//定时器时间，发送信号
			case <-ticker.C:
			case <-clientGone:
				log.Printf("Client %v disconnected from the clock", r.RemoteAddr)
				return
			}
		}
	}

}
