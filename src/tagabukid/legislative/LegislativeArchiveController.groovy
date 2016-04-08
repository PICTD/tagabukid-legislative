import com.rameses.rcp.annotations.*;
import com.rameses.rcp.common.*;
import com.rameses.osiris2.common.*;
import com.rameses.osiris2.client.*;
import com.rameses.util.*;
import tagabukid.legislative.*
import javax.swing.JFileChooser;

public class LegislativeArchiveController extends CRUDController
{
    String serviceName = 'TagabukidLegislativeArchiveService'
    String entityName  = 'legislativearchive'
    String prefixId    = 'OR'
    
    def selectedCategory;
    def selectedClassification;
    def selectedSubjectMatter;
    def selectedCommittee;
    def selectedEntity;
    
    def images;
    def selectedItem;
    public Map createEntity(){
        loadImages(); 
        return [ legislativearchivecategory:[],
            legislativearchiveclassification:[],
            legislativearchivesubjectmatter:[],
            legislativearchivecommittee:[],
            legislativearchiveauthor:[],
            legislativearchivecoauthor:[],
            legislativearchiveattachment:[]];
    }
    //override afterOpen to load trainings
    public void afterOpen(Object entity){
        loadImages(); 
        entity.putAll(service.open(entity));
    }
    void entityChanged(){
        categoryHandler.reload();
        classificationHandler.reload();
        committeeHandler.reload();
        subjectmatterHandler.reload();
        coauthorHandler.reload();
        authorHandler.reload();
    }
    def categoryHandler = [
        getRows : {entity.legislativearchivecategory.size() + 1 },
        fetchList: { entity.legislativearchivecategory },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteCategoryItem(it)               
                entity.legislativearchivecategory.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchivecategory << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchivecategory,item);
        }
    ] as EditorListModel; 
    
    
    def classificationHandler = [
        getRows : {entity.legislativearchiveclassification.size() + 1 },
        fetchList: { entity.legislativearchiveclassification },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteClassificationItem(it)               
                entity.legislativearchiveclassification.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchiveclassification << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchiveclassification,item);
        }
    ] as EditorListModel;
    
    def subjectmatterHandler = [
        getRows : {entity.legislativearchivesubjectmatter.size() + 1 },
        fetchList: { entity.legislativearchivesubjectmatter },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteSubjectMatterItem(it)               
                entity.legislativearchivesubjectmatter.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchivesubjectmatter << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchivesubjectmatter,item);
        }
    ] as EditorListModel;
    
    def committeeHandler = [
        getRows : {entity.legislativearchivecommittee.size() + 1 },
        fetchList: { entity.legislativearchivecommittee },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteCommitteeItem(it)               
                entity.legislativearchivecommittee.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchivecommittee << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchivecommittee,item);
        }
    ] as EditorListModel;
    
    def authorHandler = [
        getRows : {entity.legislativearchiveauthor.size() + 1 },
        fetchList: { entity.legislativearchiveauthor },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteAuthorItem(it)               
                entity.legislativearchiveauthor.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchiveauthor << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchiveauthor,item);
        }
    ] as EditorListModel;
    
    def coauthorHandler = [
        getRows : {entity.legislativearchivecoauthor.size() + 1 },
        fetchList: { entity.legislativearchivecoauthor },
        createItem : {
            return[
                objid : entity.objid,
            ]
        },
        onRemoveItem : {
            if (MsgBox.confirm('Delete item?')){                
                service.deleteCoAuthorItem(it)               
                entity.legislativearchivecoauthor.remove(it)
                entityChanged()
                return true;
            }
            return false;
        },
        onAddItem : {
            entity.legislativearchivecoauthor << it; 
        },
        validate:{li->
            def item=li.item;
            checkDuplicate(entity.legislativearchivecoauthor,item);
        }
    ] as EditorListModel;
    
    
    def getLookupCategory(){       
        return Inv.lookupOpener('ordinancecategory:lookup',[
                onselect :{   
                    selectedCategory.objid = it.objid;
                    selectedCategory.name = it.name;
                    selectedCategory.code = it.code;
                }
            ])
    }
    
    def getLookupClassification(){       
        return Inv.lookupOpener('ordinanceclassification:lookup',[
                onselect :{   
                    selectedClassification.objid = it.objid;
                    selectedClassification.name = it.name;
                    selectedClassification.code = it.code;
                }
            ])
    }
    def getLookupSubjectMatter(){       
        return Inv.lookupOpener('ordinancesubjectmatter:lookup',[
                onselect :{   
                    selectedSubjectMatter.objid = it.objid;
                    selectedSubjectMatter.name = it.name;
                    selectedSubjectMatter.code = it.code;
                }
            ])
    }
    def getLookupCommittee(){       
        return Inv.lookupOpener('ordinancecommittee:lookup',[
                onselect :{   
                    selectedCommittee.objid = it.objid;
                    selectedCommittee.name = it.name;
                    selectedCommittee.code = it.code;
                }
            ])
    }
    def getLookupEntity(){       
        return Inv.lookupOpener('ordinanceentity:lookup',[
                onselect :{   
                    selectedEntity.objid = it.objid;
                    selectedEntity.name = it.name;
                    selectedEntity.entityno = it.entityno;
                    selectedEntity.address_text = it.address_text;                     
                    selectedEntity.type = it.type;
                }
            ])
    }
    void checkDuplicate(listtofilter,item){
        def data = listtofilter.find{it.objid == item.objid }
        if (data)
        throw new Exception("Duplicate item is not allowed.")
    }
    
    
      public String getCaption(){
        return entity.requirementtype.name + ' Detail'
    }
    
    
    void loadImages(){
        images = [];
        try{
            images = TagabukidDBImageUtil.getInstance().getImages(entity?.objid);
//              images = entity.legislativearchiveattachment;
        }
        catch(e){
            println 'Load Images error ============';
            e.printStackTrace();
        }
        listHandler?.load();
    }
    
    def listHandler = [
        fetchList : { return images },
    ] as BasicListModel
    
    def addImage(){
        return InvokerUtil.lookupOpener('upload:attachment', [
                entity : entity,
                afterupload: {
//                    entity.legislativearchiveattachment << entity.attachment;
                    loadImages();
                }
            ]);
    }
            
    void deleteImage(){
        if (!selectedItem) return;
        if (MsgBox.confirm('Delete selected image?')){
            TagabukidDBImageUtil.getInstance().deleteImage(selectedItem.objid);
            loadImages();
        }
    }
            
            
    def viewImage(){
        if (!selectedItem) return null;
        
        if (selectedItem.extension.contains("pdf")){
           return InvokerUtil.lookupOpener('attachmentpdf:view', [
                entity : selectedItem,
            ]); 
        }else{
            return InvokerUtil.lookupOpener('attachment:view', [
                entity : selectedItem,
            ]); 
        }
        
    }
}
