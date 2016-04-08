/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package tagabukid.legislative;

import com.rameses.common.MethodResolver;
import com.rameses.io.StreamUtil;
import com.rameses.osiris2.client.InvokerProxy;
import com.rameses.osiris2.client.OsirisContext;
import com.rameses.rcp.common.MsgBox;
import groovy.lang.GroovyClassLoader;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URLConnection;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.jfree.io.IOUtils;

public class TagabukidDBImageUtil {
    private final int BUFFER_SIZE = 32768;
    private static TagabukidDBImageUtil util;
    private static String serviceName;
    private MethodResolver resolver;
    private Object proxy;
    private GroovyClassLoader classLoader;
    private Date lastErrorConnectionTime;
    public static final String CACHE_IMAGE_KEY = "cached_image_file";
    
    private TagabukidDBImageUtil(String serviceName) {
        this.classLoader = new GroovyClassLoader(this.getClass().getClassLoader());
        this.reconnect();
    }
    
    private void reconnect() {
        long diff = 0;
        long diffMinutes = 0;
        Date currTime = new Date();
        if (this.lastErrorConnectionTime != null) {
            diff = currTime.getTime() - this.lastErrorConnectionTime.getTime();
            diffMinutes = diff / 60000 % 60;
        }
        if (this.lastErrorConnectionTime == null || (double)diffMinutes >= 1.0) {
            System.out.println("Reconnecting to Image Server... ");
            try {
                this.proxy = this.lookupServiceProxy(serviceName);
                this.resolver = MethodResolver.getInstance();
            }
            catch (Exception ex) {
                System.out.println("===============================================");
                System.out.println("Unable to reconnect to Image Server...");
                System.out.println("===============================================");
                ex.printStackTrace();
                this.lastErrorConnectionTime = currTime;
            }
        }
    }
    
    private Object lookupServiceProxy(String name) {
        try {
            return InvokerProxy.getInstance().create(name);
        }
        catch (Exception e) {
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static TagabukidDBImageUtil getInstance() {
        return TagabukidDBImageUtil.getInstance(serviceName);
    }
    
    public static TagabukidDBImageUtil getInstance(String svcName) {
        serviceName = svcName;
        if (util == null) {
            util = new TagabukidDBImageUtil(serviceName);
        }
        return util;
    }
    
    String getCacheDirectory() {
        return System.getProperty("user.dir") + File.separatorChar + "cache";
    }
    
    String getFullFileName(Object objid) {
        String safeid = this.makeSafeId(objid);
        return this.getCacheDirectory() + File.separatorChar + safeid;
    }
    
    String makeSafeId(Object objid) {
        return objid.toString().replaceAll(":", "-");
    }
    
    public byte[] getImage(Object objid) throws Exception {
        this.clearCacheDir(this.getCacheDirectory());
        String filename = this.getFullFileName(objid);
        File file = new File(filename);
        if (file.length() == 0) {
            file.delete();
            System.out.println("File deleted.");
        }
        if (!file.exists()) {
            System.out.println("Saving " + filename + " ");
            this.saveToFile(objid, filename);
            file = new File(filename);
        }
        return StreamUtil.toByteArray((InputStream)new FileInputStream(file));
    }
    
    public File getImage2(Object objid) throws Exception {
        this.clearCacheDir(this.getCacheDirectory());
        String filename = this.getFullFileName(objid);
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Saving " + filename + " ");
            this.saveToFile(objid, filename);
            file = new File(filename);
        }
        return file;
    }
    
    public InputStream getInputStream(Object objid) throws Exception {
        File file = this.getImage2(objid);
        return new FileInputStream(file);
    }
    
    public long saveClipboardImage(Map header) {
        try {
            byte[] bytes = this.getClipboardImage();
            if (bytes != null) {
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                return this.upload(header, is);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public byte[] getClipboardImage() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                BufferedImage bufferedImage = (BufferedImage)transferable.getTransferData(DataFlavor.imageFlavor);
                ImageIcon img = new ImageIcon(bufferedImage);
                BufferedImage bimage = this.convertImage(img.getImage());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage)bimage, "png", baos);
                return baos.toByteArray();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("getImageFromClipboard: That wasn't an image!");
        }
        return null;
    }
    
    private BufferedImage convertImage(Image img) {
        if (img == null) {
            return null;
        }
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bufimg = new BufferedImage(w, h, 2);
        Graphics2D g2 = bufimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, w, h, Color.WHITE, null);
        g2.dispose();
        return bufimg;
    }
    
    public void deleteCachedImage(Object objid) {
        String filename = this.getFullFileName(objid);
        File file = new File(filename);
        if (file.exists() && !file.delete()) {
            throw new RuntimeException("Cannot delete file at this time.");
        }
    }
    
    void clearCacheDir(String cacheDir) {
        File file = new File(cacheDir);
        if (!file.exists()) {
            file.mkdir();
        } else {
            if (System.getProperty("cached_image_file") == null) {
                File[] files;
                System.out.println("Clearing image cache -> " + cacheDir);
                for (File f : files = file.listFiles()) {
                    f.delete();
                }
            }
            System.getProperties().put("cached_image_file", "initialized");
        }
    }
    
    public void saveToFile(Object objid, String fileName) throws Exception {
        FileOutputStream fos = null;
        long fileSize = 0;
        try {
            File file = new File(fileName);
            fos = new FileOutputStream(file, true);
            List<Map> imageItems = this.getImageItems(objid);
            for (Map data : imageItems) {
                byte[] bytes = (byte[])data.get("byte");
                fileSize += (long)bytes.length;
                fos.write(bytes);
                fos.flush();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Exception e) {}
            }
        }
    }
    
    public InputStream saveImageToFile(Object objid, byte[] image) throws Exception {
        this.clearCacheDir(this.getCacheDirectory());
        String filename = this.getFullFileName(objid);
        FileOutputStream fos = null;
        File file = new File(filename);
        boolean success = false;
        try {
            fos = new FileOutputStream(file, true);
            fos.write(image);
            fos.flush();
            success = true;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Exception e) {}
            }
        }
        if (success) {
            return new FileInputStream(file);
        }
        return null;
    }
    
    public long upload(Map header, String filename) throws Exception {
        InputStream is = null;
        String mimeType = "";
        BufferedInputStream bis = null;
        long filesize = 0;
        File file = new File(filename);
        try {
            header.put("filesize", file.length());
            is = new FileInputStream(file);
            String extension = "";

            int i = filename.lastIndexOf('.');
            int p = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

            if (i > p) {
                mimeType = filename.substring(i+1);
            }
            header.put("extension", mimeType);
            bis = new BufferedInputStream(is);
            filesize = this.upload(header, bis);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            if (bis != null) {
                try {
                    bis.close();
                }
                catch (Exception e) {}
            }
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {}
            }
        }
        if (filesize != file.length()) {
            throw new Exception("Unload unsuccessful. Try again.");
        }
        return file.length();
    }
    
    public long upload(Map header, InputStream source) throws Exception {
        byte[] buf = new byte[32768];
        long filesize = 0;
        int len = -1;
        int fileno = 0;
        if (header.get("objid") == null) {
            header.put("objid", "H" + new UID());
        }
        try {
            this.deleteImage(header.get("objid"));
            this.saveHeader(header);
            while ((len = source.read(buf)) != -1) {
                filesize += (long)len;
                HashMap<String, Object> data = new HashMap<String, Object>();
                data.put("objid", "F" + new UID());
                data.put("parentid", header.get("objid"));
                data.put("fileno", ++fileno);
                data.put("byte", buf);
                this.saveItem(data);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return filesize;
    }
    
    public long upload(Map header, byte[] bytes) throws Exception {
        byte[] buf = new byte[32768];
        int fileno = 0;
        header.put("filesize", bytes.length);
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(bytes));
        String mimeType = URLConnection.guessContentTypeFromStream(is);
        header.put("extension", mimeType);
        if (header.get("objid") == null) {
            header.put("objid", "H" + new UID());
        }
        try {
            HashMap<String, Object> data;
            this.deleteImage(header.get("objid"));
            this.saveHeader(header);
            int startidx = fileno * 32768;
            int endidx = startidx + 32768;
            long filesize = 0;
            while (endidx <= bytes.length) {
                System.arraycopy(bytes, startidx, buf, 0, 32768);
                startidx = ++fileno * 32768;
                endidx = startidx + 32768;
                filesize += 32768;
                data = new HashMap();
                data.put("objid", "F" + new UID());
                data.put("parentid", header.get("objid"));
                data.put("fileno", fileno);
                data.put("byte", buf);
                this.saveItem(data);
            }
            if (filesize < (long)bytes.length) {
                System.arraycopy(bytes, startidx, buf, 0, (int)((long)bytes.length - filesize));
                data = new HashMap<String, Object>();
                data.put("objid", "F" + new UID());
                data.put("parentid", header.get("objid"));
                data.put("fileno", ++fileno);
                data.put("byte", buf);
                this.saveItem(data);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return bytes.length;
    }
    
    public void deleteImage(Object objid) {
        this.deleteCachedImage(objid);
        if (!this.isConnectionActive()) {
            throw new RuntimeException("Image Server is not available at this time. Try again later.");
        }
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("objid", objid);
        this.invoke("deleteImage", data);
    }
    
    public void deleteAllImages(Object refid) {
        if (!this.isConnectionActive()) {
            throw new RuntimeException("Image Server is not available at this time. Try again later.");
        }
        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("refid", refid);
        this.invoke("deleteAllImages", data);
    }
    
    public List<Map> getImages(Object objid) {
        List list = new ArrayList<Map>();
        if (this.isConnectionActive()) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("refid", objid);
            list = (List)this.invoke("getImages", data);
        } else {
            this.reconnect();
        }
        return list;
    }
    
    private boolean isConnectionActive() {
        if (this.proxy == null) {
            return false;
        }
        return true;
    }
    
    private List<Map> getImageItems(Object objid) {
        List list = new ArrayList<Map>();
        if (this.isConnectionActive()) {
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("objid", objid);
            list = (List)this.invoke("getImageItems", data);
        }
        return list;
    }
    
    private Object saveItem(Map data) {
        if (!this.isConnectionActive()) {
            throw new RuntimeException("Image Server is not available at this time. Try again later.");
        }
        return this.invoke("saveItem", data);
    }
    
    private Object saveHeader(Map data) {
        if (!this.isConnectionActive()) {
            throw new RuntimeException("Image Server is not available at this time. Try again later.");
        }
        return this.invoke("saveHeader", data);
    }
    
    private Object invoke(String methodName, Object data) {
        try {
            return this.resolver.invoke(this.proxy, methodName, new Object[]{data});
        }
        catch (RuntimeException re) {
            throw re;
        }
        catch (Exception re) {
            throw new RuntimeException(re.getMessage(), re);
        }
    }
    
    private Map getParams() {
        Map appEnv = OsirisContext.getClientContext().getAppEnv();
        Object appHost = appEnv.get("image.server.host");
        Object appContext = appEnv.get("image.server.context");
        Object appCluster = appEnv.get("image.server.cluster");
        if (appHost == null) {
            appHost = appEnv.get("app.host");
            appContext = appEnv.get("app.context");
            appCluster = appEnv.get("app.cluster");
        }
        System.out.println("=====================================");
        System.out.println("image.server.host -> " + appHost);
        System.out.println("image.server.context -> " + appContext);
        System.out.println("image.server.cluster -> " + appCluster);
        System.out.println("=====================================");
        HashMap param = new HashMap();
        param.put("app.host", appHost);
        param.put("app.context", appContext);
        param.put("app.cluster", appCluster);
        return param;
    }
    
    static {
        serviceName = "TagaBukidDBImageService";
    }
    
   
}

