package util

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	"github.com/custhk/http2/resource"
)

// MakeDirAll create dir
func MakeDirAll(dirPath string) {
	_, err := os.Stat(dirPath)
	if err == nil {
		return
	}
	if os.IsNotExist(err) {
		err = os.MkdirAll(dirPath, 0777)
	}

}

// SaveFileByURLPath save receive file
func SaveFileByURLPath(urlPath string, resp *http.Response) resource.IResource {
	fileInfo := resource.ParseURL(urlPath)
	if fileInfo != nil {
		return SaveFileByFileInfo(fileInfo, resp)
	}
	return nil
}

// SaveFileByFileInfo save file by FileInfo
func SaveFileByFileInfo(fileInfo resource.IResource, resp *http.Response) resource.IResource {
	MakeDirAll(fileInfo.GetSaveParentPath())
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		fmt.Println("push body read error is ", err)
		return nil
	}
	dataSize := len(body)
	err = ioutil.WriteFile(fileInfo.GetSavePath(), body, 0644)
	if err != nil {
		fmt.Println("push body write error is ", err)
		return nil
	}
	fileInfo.SetDataSize(dataSize)
	return fileInfo
}
