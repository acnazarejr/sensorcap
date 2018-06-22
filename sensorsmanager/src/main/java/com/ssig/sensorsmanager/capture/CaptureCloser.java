//package com.ssig.sensorsmanager.capture;
//
//import com.ssig.sensorsmanager.data.CaptureData;
//
//import org.zeroturnaround.zip.ZipUtil;
//
//import java.io.File;
//
//public final class CaptureCloser {
//
//    public enum CaptureCloseResponse{
//        SUCCESS
//    }
//
//    public static CaptureCloseResponse close(CaptureData captureData, File systemCapturesFolder){
//
//        File captureFolder = new File(String.format("%s%s%s", systemCapturesFolder, File.separator, captureData.getCaptureDataUUID()));
//        ZipUtil.unexplode(captureFolder);
//
//        return CaptureCloseResponse.SUCCESS;
//    }
//
//}
