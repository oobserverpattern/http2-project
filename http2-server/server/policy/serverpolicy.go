package policy

import "net/http"

//IServerPolicy 策略接口，相关struct只要实现了接口所有的方法，即是对该接口的实现
type IServerPolicy interface {
	Handle(w http.ResponseWriter, r *http.Request)
}

const (
	// NoPusherPolicy NoPusher policy
	NoPusherPolicy string = ""
	// SpecifiedPusherPolicy SpecifiedPusher policy
	SpecifiedPusherPolicy string = "specified"
	// CooperaterPusherPolicy CooperaterPusher policy
	CooperaterPusherPolicy string = "cooperater"
)

// HEADERKEY req Header key to get police Name
const HEADERKEY = "serverPolicyName"

var handleMap = make(map[string]IServerPolicy)

//初始化方法，构建一个处理map，绑定处理策略
func init() {
	handleMap[NoPusherPolicy] = &NoPusher{}
	handleMap[SpecifiedPusherPolicy] = &SpecifiedPusher{}
	handleMap[CooperaterPusherPolicy] = &CooperaterPusher{}
}

//Handle select policy to handle req
func Handle(w http.ResponseWriter, r *http.Request) {
	policyName := r.Header.Get(HEADERKEY)
	//在header中利用HEADERKEY获取客户端希望服务器采用的策略，默认情况下采用nopusher
	nowPolicy, ok := handleMap[policyName]
	if ok {
		nowPolicy.Handle(w, r)
	}
}
