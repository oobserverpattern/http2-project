package main

import (
	"log"
	"net/http"
	"time"

	"github.com/custhk/http2/server/policy"
)

const (
	_CertFile = "../pem/cert.pem"
	_KeyFile  = "../pem/key.pem"
)

func main() {
	httpsSrv()
}

//HTTP2服务
func httpsSrv() {
	srv := &http.Server{
		Addr:         ":8080",
		ReadTimeout:  9000 * time.Second,
		WriteTimeout: 9000 * time.Second,
	}
	//以/为前缀的请求，交由policy.Handle方法处理，相当于做了一个绑定
	//http://localhost:8080/
	http.HandleFunc("/", policy.Handle)
	//服务器开始监听和服务，绑定TLS相关证书
	log.Fatal(srv.ListenAndServeTLS(_CertFile, _KeyFile))
}
