package resource

// FileType means source type
type FileType int8

// FileType
const (
	Other FileType = 0
	Mpd   FileType = 1
	Init  FileType = 2
	Seg   FileType = 3
)

// IResource 资源接口
type IResource interface {
	//获取本地路径
	GetLocalPath() string
	//获取URL路径
	GetURLPath() string
	//获取资源文件名
	GetFileName() string

	//获取保存父路径
	GetSaveParentPath() string
	//获取保持路径
	GetSavePath() string

	//设置文件大小
	SetDataSize(int)
	//获取文件大小
	GetDataSize() int
}
