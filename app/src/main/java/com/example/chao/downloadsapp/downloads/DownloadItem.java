package com.example.chao.downloadsapp.downloads;

/**
 * Created by chao on 8/31/16.
 */
public class DownloadItem {
    private String localPath;
    private String HttpURL;
    private long modifyDateInLocal;
    private String getModifyDateInServer;
    private long lengthInLocal;
    private long lengthInServer;
    private boolean isFinished;

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getHttpURL() {
        return HttpURL;
    }

    public void setHttpURL(String httpURL) {
        HttpURL = httpURL;
    }

    public String getGetModifyDateInServer() {
        return getModifyDateInServer;
    }

    public void setGetModifyDateInServer(String getModifyDateInServer) {
        this.getModifyDateInServer = getModifyDateInServer;
    }

    public long getModifyDateInLocal() {
        return modifyDateInLocal;
    }

    public void setModifyDateInLocal(long modifyDateInLocal) {
        this.modifyDateInLocal = modifyDateInLocal;
    }

    public long getLengthInLocal() {
        return lengthInLocal;
    }

    public void setLengthInLocal(long lengthInLocal) {
        this.lengthInLocal = lengthInLocal;
    }

    public long getLengthInServer() {
        return lengthInServer;
    }

    public void setLengthInServer(long lengthInServer) {
        this.lengthInServer = lengthInServer;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
