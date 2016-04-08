import com.rameses.rcp.common.*
import com.rameses.rcp.annotations.*
import com.rameses.osiris2.client.*
import tagabukid.legislative.*

public class UploadAttachmentController 
{
    
    @Binding
    def binding;
    
    
    def afterupload;  
    def entity;
    def showheader = true;
    
    def file;
    def header;
    def objid;
    def autoClose = false;
    
    def IMAGE_TYPE_FILE = 'file';
    def IMAGE_TYPE_CAMERA = 'camera';
    
    def imagetype;
    
    
    @PropertyChangeListener
    def listener = [
        'file' : { 
            photo = [:];
            imagetype = IMAGE_TYPE_FILE;
            binding.refresh('image|photo.image');
        },
    ]
    
    void init(){
        header = [ 
            objid   : (objid ? objid : 'I'+ new java.rmi.server.UID()), 
            refid   : entity.objid, 
            title   : (showheader ? null : 'SKETCH'),
        ]
        file = null;
        photo = [:]
    }
    
    def upload(){
        if (imagetype == IMAGE_TYPE_FILE )
            uploadImageFile()
        else if (imagetype == IMAGE_TYPE_CAMERA )
            uploadCameraImage()
        
        if (autoClose)
            return '_close';
    }
    
    void uploadImageFile(){
        if (!file) 
            throw new Exception('File to upload is required.');
        
        TagabukidDBImageUtil.getInstance().upload(header, file.getAbsolutePath());
        
        if (afterupload ) afterupload();
        init();
        binding.refresh('image|photo.image');
        binding.focus('header.title');
    }
    
    void uploadCameraImage(){
        if ( ! photo.image || photo.image.length == 0 )
            throw new Exception('Captured image is required.');
       
//        entity.attachment = TagabukidDBImageUtil.getInstance().upload(header, photo.image);
        TagabukidDBImageUtil.getInstance().upload(header, photo.image);
        if (afterupload ) afterupload();
        init();
        binding.refresh('image|photo.image');
        binding.focus('header.title');
    }
    
    def getImage(){
        return file;
    }

    def photo = [:];

    def cameraHandler = [
        onselect : { bytes->
            photo = [image: bytes];
            imagetype = IMAGE_TYPE_CAMERA;
            binding.refresh('image|photo.image');
        },
        isAlwaysOnTop : { return true },
        modal : { return false },
        isAutoCloseOnSelect  : {return false },
    ] as CameraModel 
}