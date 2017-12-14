package com.codingblocks.chatbot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.UUID;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;

public class ImageUtil {


    public static String processImage(String location)  {

        int total_Faces = -1;
        File file = null;

        try {
            URL url  = new URL(location);
            file = File.createTempFile("attachment-", FilenameUtils.getName(url.getPath()));
            FileUtils.copyURLToFile(url, file);
            System.out.println(file.getAbsolutePath());

            opencv_core.IplImage src = cvLoadImage(file.getAbsolutePath());

            URL resource = ImageUtil.class.getResource("/haarcascade_frontalface_default.xml");
            System.out.println(resource.getFile());
            String path = Paths.get(resource.toURI()).toFile().getAbsolutePath();

            opencv_objdetect.CvHaarClassifierCascade cascade = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(path));
            opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
            opencv_core.CvSeq sign = cvHaarDetectObjects(src, cascade, storage, 1.5, 1, CV_HAAR_DO_CANNY_PRUNING);

            cvClearMemStorage(storage);

            total_Faces = sign.total();

            for(int i = 0; i < total_Faces; i++){
                opencv_core.CvRect r = new opencv_core.CvRect(cvGetSeqElem(sign, i));
                System.out.println(r.width() + r.x() + " " + r.height() + r.y());
                cvRectangle (src, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 6, CV_AA, 0);
            }

            cvSaveImage(file.getAbsolutePath(), src);

            System.out.println(" We have got " + total_Faces);

        } catch (Exception e) {
            System.out.println("Failed to load the image");
        }

        String random = UUID.randomUUID().toString();

        MainVerticle.cache.put(random, file.getAbsolutePath());

        return "https://6c5a443b.ngrok.io/images/"+random;

    }
}
