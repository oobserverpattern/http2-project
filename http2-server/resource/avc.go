package resource

import (
	"strings"
)

const (
	_LocalAVCResDir = "../avc/"
	_AVCURLPrefix   = "/avc/"
	_AVCReceiveDir  = "../receive/avc/"
)

func getAVCURLPath(relativePath string) string {
	return _AVCURLPrefix + relativePath
}

func getAVCResLocalPath(resourceName string, fileName string) string {
	return _LocalAVCResDir + resourceName + "/" + fileName
}

// AVCParser avc AVCParser
type AVCParser struct {
}

// ParseURL AVC parser
func (avcParser *AVCParser) ParseURL(urlComs *[]string) IResource {
	if comsSize := len(*urlComs); comsSize > 1 {
		//https://localhost:8080/avc/1/1.mpd
		resName := (*urlComs)[1]
		resFileName := (*urlComs)[comsSize-1]
		return NewAVCResource(resName, resFileName)
	}
	return nil
}

// NewAVCResource from fileName
func NewAVCResource(resName string, fileName string) *AVCResource {
	parts := strings.Split(fileName, ".")
	partsLen := len(parts)
	fileType := Other
	if partsLen == 2 {
		if parts[1] == "mpd" {
			fileType = Mpd
		} else if parts[1] == "m4s" {
			if prefixs := strings.Split(parts[0], "-"); prefixs != nil && len(prefixs) > 1 {
				if prefixs[0] == "init" {
					fileType = Init
				} else if prefixs[0] == "chunk" {
					fileType = Seg
				}
			}
		}

	}
	return &AVCResource{
		ResName:   resName,
		FileName:  fileName,
		localPath: getAVCResLocalPath(resName, fileName),
		urlPath:   getAVCURLPath(resName + "/" + fileName),
		FileType:  fileType,
	}
}

// AVCResource AVC resource
type AVCResource struct {
	ResName   string
	FileName  string
	FileType  FileType
	DataSize  int
	localPath string
	urlPath   string
}

// GetLocalPath for avc
func (avcRes *AVCResource) GetLocalPath() string {
	return avcRes.localPath
}

// GetURLPath for avc
func (avcRes *AVCResource) GetURLPath() string {
	return avcRes.urlPath
}

// GetFileName for avc
func (avcRes *AVCResource) GetFileName() string {
	return avcRes.FileName
}

//GetSaveParentPath 获取保存父路径
func (avcRes *AVCResource) GetSaveParentPath() string {
	return _AVCReceiveDir + "/" + avcRes.ResName
}

//GetSavePath 获取保持路径
func (avcRes *AVCResource) GetSavePath() string {
	return avcRes.GetSaveParentPath() + "/" + avcRes.FileName
}

// SetDataSize for avc
func (avcRes *AVCResource) SetDataSize(size int) {
	avcRes.DataSize = size
}

// GetDataSize for avc
func (avcRes *AVCResource) GetDataSize() int {
	return avcRes.DataSize
}
