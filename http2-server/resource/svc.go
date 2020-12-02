package resource

import (
	"strconv"
	"strings"
)

const (
	_LocalSVCResDir = "../svc/"
	_SVCURLPrefix   = "/svc/"
	_SVCReceiveDir  = "../receive/svc/"
	_LevelMix       = 3
)

func getInitSvcURLPath(resourceName string) string {
	initSvcName := resourceName + ".init.svc"
	return getSVCURLPath(initSvcName)
}
func getSvcReativePath(resourceName string, segIndex int, level int) string {
	return resourceName + "/" + resourceName + ".seg" + strconv.Itoa(segIndex) + "-L" + strconv.Itoa(level) + ".svc"
}
func getSVCURLPath(relativePath string) string {
	return _SVCURLPrefix + relativePath
}

func getSVCResLocalPath(resourceName string, fileName string) string {
	return _LocalSVCResDir + resourceName + "/" + fileName
}

// SVCParser SVC Parser
type SVCParser struct {
}

// ParseURL 解析请求
// 这里返回的是slice，主要是为了扩展方便，可以从一个url获取一个或多个资源
func (svcParser *SVCParser) ParseURL(urlComs *[]string) IResource {
	if comsSize := len(*urlComs); comsSize > 1 {
		//https://localhost:8080/svc/BBB-II-720p/BBB-II-720p.mpd
		//svc-0 BBB-II-720p-1 BBB-II-720p.mpd-2
		resFileName := (*urlComs)[comsSize-1]
		return NewSVCResource(resFileName)
	}
	return nil
}

// NewSVCResource from fileName
func NewSVCResource(fileName string) *SVCResource {
	parts := strings.Split(fileName, ".")
	partsLen := len(parts)
	if partsLen == 2 && parts[1] == "mpd" {
		return &SVCResource{
			ResName:   parts[0],
			FileName:  fileName,
			localPath: getSVCResLocalPath(parts[0], fileName),
			urlPath:   getSVCURLPath(parts[0] + "/" + fileName),
			FileType:  Mpd,
		}
	}
	if partsLen == 3 && parts[1] == "init" && parts[2] == "svc" {
		return &SVCResource{
			ResName:   parts[0],
			FileName:  fileName,
			localPath: getSVCResLocalPath(parts[0], fileName),
			urlPath:   getSVCURLPath(parts[0] + "/" + fileName),
			FileType:  Init,
		}
	}

	if partsLen == 3 && parts[2] == "svc" {
		segLabel := strings.Split(parts[1], "-")
		if len(segLabel) == 2 {
			index, indexErr := strconv.Atoi(strings.TrimPrefix(segLabel[0], "seg"))
			level, levelErr := strconv.Atoi(strings.TrimPrefix(segLabel[1], "L"))
			if indexErr == nil && levelErr == nil {
				return &SVCResource{
					ResName:   parts[0],
					FileName:  fileName,
					SegIndex:  index,
					Level:     level,
					localPath: getSVCResLocalPath(parts[0], fileName),
					urlPath:   getSVCURLPath(parts[0] + "/" + fileName),
					FileType:  Seg,
				}
			}
		}

	}
	return &SVCResource{
		FileName: fileName,
		FileType: Other,
	}
}

// SVCResource SVC resource
type SVCResource struct {
	ResName   string
	FileName  string
	SegIndex  int
	Level     int
	FileType  FileType
	DataSize  int
	localPath string
	urlPath   string
}

// GetLocalPath for svc
func (svcRes *SVCResource) GetLocalPath() string {
	return svcRes.localPath
}

// GetURLPath for svc
func (svcRes *SVCResource) GetURLPath() string {
	return svcRes.urlPath
}

// GetFileName for svc
func (svcRes *SVCResource) GetFileName() string {
	return svcRes.FileName
}

//GetSaveParentPath 获取保存父路径
func (svcRes *SVCResource) GetSaveParentPath() string {
	return _SVCReceiveDir + "/" + svcRes.ResName
}

//GetSavePath 获取保持路径
func (svcRes *SVCResource) GetSavePath() string {
	return svcRes.GetSaveParentPath() + "/" + svcRes.FileName
}

// SetDataSize for svc
func (svcRes *SVCResource) SetDataSize(size int) {
	svcRes.DataSize = size
}

// GetDataSize for svc
func (svcRes *SVCResource) GetDataSize() int {
	return svcRes.DataSize
}

// ParseSVCFileNames 从list中解析请求的文件信息
func ParseSVCFileNames(fileNames string) []IResource {
	names := strings.Split(fileNames, ",")
	size := len(names)
	result := []IResource{}
	if size > 0 {
		for _, fileName := range names {
			result = append(result, NewSVCResource(fileName))
		}
	}
	return result
}
