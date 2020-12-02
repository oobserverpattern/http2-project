package main

import (
	"crypto/tls"
	"crypto/x509"
	"io/ioutil"
	"log"
	"net/http"
	"time"

	"github.com/custhk/http2/client/pushhandler"
	"github.com/custhk/http2/client/requesthandler"
	"github.com/custhk/http2/resource"
	serverPolicy "github.com/custhk/http2/server/policy"
	"github.com/custhk/http2/task"
	"github.com/custhk/http2/util"
	"golang.org/x/net/http2"
)

const (
	_CertFile = "../pem/cert.pem"
)

//NewPushHandlerSupportTransport set transport push support handler
func NewPushHandlerSupportTransport(pushHandler http2.PushHandler) *http2.Transport {
	// Create a pool with the server certificate since it is not signed
	// by a known CA
	caCert, err := ioutil.ReadFile(_CertFile)
	if err != nil {
		log.Fatalf("Reading server certificate: %s", err)
	}
	caCertPool := x509.NewCertPool()
	caCertPool.AppendCertsFromPEM(caCert)

	// Create TLS configuration with the certificate of the server
	tlsConfig := &tls.Config{
		RootCAs: caCertPool,
	}

	tr := &http2.Transport{
		TLSClientConfig: tlsConfig,
	}
	// set push handler ,that's why client can support push handler
	tr.PushHandler = pushHandler
	return tr
}

// NewClient transport binding
func NewClient(tr *http2.Transport) *http.Client {
	client := http.Client{}
	client.Transport = tr
	return &client
}

// NewPushHandlerSupportClient new push handler support client
func NewPushHandlerSupportClient(pushHandler http2.PushHandler) *http.Client {
	return NewClient(NewPushHandlerSupportTransport(pushHandler))
}

// CooperaterPusherClient to test CooperaterPusher
func CooperaterPusherClient() {
	pushHandler := pushhandler.NewDefaultPushHandler()
	client := NewPushHandlerSupportClient(pushHandler)
	done := make(chan bool)
	reqHandler := requesthandler.NewDefaultRequestHandler(client)
	var mpdURL string = "https://localhost:8080/svc/BBB-II-720p/BBB-II-720p.mpd"
	//开始请求处理服务(StartReq方法是阻塞的)
	go func() {
		reqHandler.StartReq(mpdURL)
	}()

	pushBossTask := task.NewPushBossTask()
	push1 := task.NewPushWorkerTask(resource.NewSVCResource("BBB-II-720p.seg37-L0.svc"))
	pushBossTask.AddWorkerTask(push1)
	push2 := task.NewPushWorkerTask(resource.NewSVCResource("BBB-II-720p.seg38-L0.svc"))
	pushBossTask.AddWorkerTask(push2)
	push3 := task.NewPushWorkerTask(resource.NewSVCResource("BBB-II-720p.seg39-L0.svc"))
	pushBossTask.AddWorkerTask(push3)
	pushHandler.AddTask(pushBossTask)
	reqHandler.PushReq(pushBossTask)
	// time.Sleep(1 * time.Second)
	//push1.Cancel()
	pushBossTask.Cancel()
	// if pushBossTask.IsDone() {
	// 	pushBossTask := task.NewPushBossTask()
	// 	push1 := task.NewPushWorkerTask(resource.NewFileInfo("BBB-II-720p.seg37-L1.svc"))
	// 	pushBossTask.AddWorkerTask(push1)
	// 	push2 := task.NewPushWorkerTask(resource.NewFileInfo("BBB-II-720p.seg38-L1.svc"))
	// 	pushBossTask.AddWorkerTask(push2)
	// 	push3 := task.NewPushWorkerTask(resource.NewFileInfo("BBB-II-720p.seg39-L1.svc"))
	// 	pushBossTask.AddWorkerTask(push3)
	// 	pushHandler.AddTask(pushBossTask)
	// 	reqHandler.PushReq(pushBossTask)
	// }
	<-done
}

// NoPusherClient to test NoPusher
func NoPusherClient() {
	pushHandler := pushhandler.NewDefaultPushHandler()
	client := NewPushHandlerSupportClient(pushHandler)
	var mpdURL string = "https://localhost:8080/svc/BBB-II-720p/BBB-II-720p.mpd"
	mpdReq, err := http.NewRequest(http.MethodGet, mpdURL, nil)
	if err != nil {
		log.Printf("创建请求失败: %s", err)
		return
	}
	// 发送初始请求
	mpdResp, err := client.Do(mpdReq)
	if err != nil {
		log.Printf("发送请求失败: %s", err)
		return
	}
	util.SaveFileByURLPath(mpdReq.URL.Path, mpdResp)
	time.Sleep(20 * time.Second)
}

// SpecifiedPusherClient to test SpecifiedPusher
func SpecifiedPusherClient() {
	pushHandler := pushhandler.NewDefaultPushHandler()
	client := NewPushHandlerSupportClient(pushHandler)
	var mpdURL string = "https://localhost:8080/svc/BBB-II-720p/BBB-II-720p.mpd"
	mpdReq, err := http.NewRequest(http.MethodGet, mpdURL, nil)
	mpdReq.Header.Set(serverPolicy.HEADERKEY, serverPolicy.SpecifiedPusherPolicy)
	mpdReq.Header.Set(serverPolicy.GetSpecPromiseHeaderKey(), "BBB-II-720p.seg30-L0.svc,BBB-II-720p.seg31-L0.svc,BBB-II-720p.seg32-L0.svc")
	if err != nil {
		log.Printf("创建请求失败: %s", err)
		return
	}
	// 发送初始请求
	mpdResp, err := client.Do(mpdReq)
	if err != nil {
		log.Printf("发送请求失败: %s", err)
		return
	}
	util.SaveFileByURLPath(mpdReq.URL.Path, mpdResp)
	time.Sleep(20 * time.Second)
}
func main() {
	NoPusherClient()
	//pecifiedPusherClient()
	//CooperaterPusherClient()

}
