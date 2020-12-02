package task

import (
	"encoding/json"
	"log"
	"strconv"
	"sync"
	"time"

	"github.com/custhk/http2/resource"
	"golang.org/x/net/http2"
)

var (
	nowID int = 0
	mu    sync.Mutex
)

const (
	//TaskIDKey task id
	TaskIDKey string = "ID"
	// ResourceKey file name
	ResourceKey string = "FileName"
)

// GetNowID get now task id
func GetNowID() string {
	mu.Lock()
	nowID++
	temp := strconv.Itoa(nowID)
	mu.Unlock()
	return temp
}

// PushBossTask push task struct to record push list
type PushBossTask struct {
	ID                  string
	TaskStartTimeStamp  int64
	TaskFinishTimeStamp int64
	// push list
	TaskString  string
	workerTasks []*PushWorkerTask
	// fileName arrayIndex
	appenedTasks map[string]int
	// to rember if done
	doneTasks map[string]int
	done      chan struct{}
}

// NewPushBossTask return new PushBossTask
func NewPushBossTask() *PushBossTask {
	return &PushBossTask{
		ID:           GetNowID(),
		workerTasks:  []*PushWorkerTask{},
		appenedTasks: make(map[string]int),
		doneTasks:    make(map[string]int),
		done:         make(chan struct{}),
	}

}

// IsDone to judge if all push task complish
func (pbt *PushBossTask) IsDone() bool {
	select {
	case <-pbt.done:
		return true
	case <-time.After(10 * time.Second):
		log.Printf("***wait time %vs, it is to long****", "10")
		return false
	}

}

// AddWorkerTask add worker task to boss task
func (pbt *PushBossTask) AddWorkerTask(pwt *PushWorkerTask) {
	key := pwt.resource.GetFileName()
	len := len(pbt.appenedTasks)
	index, ok := pbt.appenedTasks[key]
	if !ok {
		pbt.appenedTasks[key] = len
		pbt.workerTasks = append(pbt.workerTasks, pwt)
	} else {
		pbt.workerTasks[index] = pwt
	}
}

// Cancel cancel request
func (pbt *PushBossTask) Cancel() {
	for _, pwt := range pbt.workerTasks {
		pwt.Cancel()
	}
}

//GetWorkerTask get worker task by fileName
func (pbt *PushBossTask) GetWorkerTask(fileName string) *PushWorkerTask {
	index, ok := pbt.appenedTasks[fileName]
	if ok {
		return pbt.workerTasks[index]
	}
	return nil
}

//AddDone add push done
func (pbt *PushBossTask) AddDone(fileName string) {
	_, ok := pbt.appenedTasks[fileName]
	if ok {
		pbt.doneTasks[fileName] = 1
		log.Printf("***task to get %v is done***", fileName)
		if len(pbt.appenedTasks) == len(pbt.doneTasks) {
			close(pbt.done)
		}
	}
}

//WorkerTasksToString worket Tasks To String
func (pbt *PushBossTask) WorkerTasksToString() string {
	result := ""
	for index, pwt := range pbt.workerTasks {
		if index != 0 {
			result += ","
		}
		result += pwt.ResourceToString()
	}
	pbt.TaskString = result
	return result
}

//ToJSONString to json string
func (pbt *PushBossTask) ToJSONString() string {
	pbt.WorkerTasksToString()
	pbt.TaskStartTimeStamp = time.Now().UnixNano() / 1e6
	//需要注意，可导性，只有大写开头的才可以转换
	jsonBytes, err := json.Marshal(pbt)
	if err != nil {
		log.Printf("Failed to convert from  PushBossTask To JSON: %v", err)
		return ""
	}
	log.Printf("****PushBossTask JSON: %v***", string(jsonBytes))
	return string(jsonBytes)
}

// JSONToPushBossTask 转换函数
func JSONToPushBossTask(jsonStr string) *PushBossTask {
	var pushBossTask PushBossTask
	json.Unmarshal([]byte(jsonStr), &pushBossTask)
	return &pushBossTask
}

// PushWorkerTask to record single push task
type PushWorkerTask struct {
	TaskStartTimeStamp  int64
	TaskFinishTimeStamp int64
	TaskString          string
	resource            resource.IResource
	pushedRequest       *http2.PushedRequest
	done                chan struct{}
}

// NewPushWorkerTask reutrn a new push work task
func NewPushWorkerTask(fileInfo resource.IResource) *PushWorkerTask {
	return &PushWorkerTask{
		resource: fileInfo,
		done:     make(chan struct{}),
	}
}

// ToJSONString PushWorkerTask to  json string
func (pwt *PushWorkerTask) ToJSONString() string {
	pwt.ResourceToString()
	pwt.TaskStartTimeStamp = time.Now().UnixNano() / 1e6
	//需要注意，可导性，只有大写开头的才可以转换
	jsonBytes, err := json.Marshal(pwt)
	if err != nil {
		log.Printf("Failed to convert from  PushWorkerTask To JSON: %v", err)
		return ""
	}
	log.Printf(" PushWorkerTask JSON: %v", string(jsonBytes))
	return string(jsonBytes)
}

// ResourceToString PushWorkerTask to string
func (pwt *PushWorkerTask) ResourceToString() string {
	if pwt.resource != nil {
		pwt.TaskString = pwt.resource.GetFileName()
		return pwt.TaskString
	}
	return ""
}

// BindingPushedRequest PushWorkerTask to string
func (pwt *PushWorkerTask) BindingPushedRequest(pushedRequest *http2.PushedRequest) {
	pwt.pushedRequest = pushedRequest
}

// Cancel cancel request
func (pwt *PushWorkerTask) Cancel() {
	if pwt.pushedRequest != nil {
		pwt.pushedRequest.Cancel()
		log.Printf("*********cancel push file = %q\n*********", pwt.resource.GetFileName())
	}
}
