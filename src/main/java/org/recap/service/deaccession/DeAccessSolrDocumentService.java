package org.recap.service.deaccession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.common.SolrInputDocument;
import org.recap.RecapConstants;
import org.recap.model.jpa.BibliographicEntity;
import org.recap.model.jpa.HoldingsEntity;
import org.recap.model.jpa.ItemEntity;
import org.recap.repository.jpa.BibliographicDetailsRepository;
import org.recap.repository.jpa.HoldingsDetailsRepository;
import org.recap.repository.jpa.ItemDetailsRepository;
import org.recap.util.BibJSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Created by angelind on 10/11/16.
 */

@Component
public class DeAccessSolrDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DeAccessSolrDocumentService.class);

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    HoldingsDetailsRepository holdingDetailRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @Autowired
    SolrTemplate solrTemplate;

    public BibJSONUtil getBibJSONUtil(){
        return new BibJSONUtil();
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public HoldingsDetailsRepository getHoldingDetailRepository() {
        return holdingDetailRepository;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public SolrTemplate getSolrTemplate() {
        return solrTemplate;
    }

    public String updateIsDeletedBibByBibId(@RequestBody List<Integer> bibIds){
        try{
            for(Integer bibId : bibIds){
                BibliographicEntity bibEntity = getBibliographicDetailsRepository().findByBibliographicId(bibId);
                SolrInputDocument bibSolrInputDocument = getBibJSONUtil().generateBibAndItemsForIndex(bibEntity, getSolrTemplate(), getBibliographicDetailsRepository(), getHoldingDetailRepository());
                bibSolrInputDocument.setField(RecapConstants.IS_DELETED_BIB,true);
                getSolrTemplate().saveDocument(bibSolrInputDocument);
                getSolrTemplate().commit();
            }
            return "Bib documents updated successfully.";
        }catch(Exception ex){
            logger.error(RecapConstants.LOG_ERROR,ex);
            return "Bib documents failed to update.";
        }
    }

    public String updateIsDeletedHoldingsByHoldingsId(@RequestBody  List<Integer> holdingsIds){
        try{
            for(Integer holdingsId : holdingsIds){
                HoldingsEntity holdingsEntity = getHoldingDetailRepository().findByHoldingsId(holdingsId);
                if(holdingsEntity != null && CollectionUtils.isNotEmpty(holdingsEntity.getBibliographicEntities())) {
                    for(BibliographicEntity bibliographicEntity : holdingsEntity.getBibliographicEntities()) {
                        SolrInputDocument bibSolrInputDocument = getBibJSONUtil().generateBibAndItemsForIndex(bibliographicEntity, getSolrTemplate(), getBibliographicDetailsRepository(), getHoldingDetailRepository());
                        for (SolrInputDocument holdingsSolrInputDocument : bibSolrInputDocument.getChildDocuments()) {
                            if (holdingsId.equals(holdingsSolrInputDocument.get(RecapConstants.HOLDING_ID).getValue())) {
                                holdingsSolrInputDocument.setField(RecapConstants.IS_DELETED_HOLDINGS, true);
                            }
                        }
                        getSolrTemplate().saveDocument(bibSolrInputDocument);
                    }
                }
                getSolrTemplate().commit();
            }
            return "Holdings documents updated successfully.";
        }catch(Exception ex){
            logger.error(RecapConstants.LOG_ERROR,ex);
            return "Holdings documents failed to update.";
        }
    }

    public String updateIsDeletedItemByItemIds(@RequestBody  List<Integer> itemIds){
        try{
            for(Integer itemId : itemIds){
                ItemEntity itemEntity = getItemDetailsRepository().findByItemId(itemId);
                if(itemEntity != null && CollectionUtils.isNotEmpty(itemEntity.getBibliographicEntities())) {
                    for(BibliographicEntity bibliographicEntity : itemEntity.getBibliographicEntities()) {
                        SolrInputDocument bibSolrInputDocument = getBibJSONUtil().generateBibAndItemsForIndex(bibliographicEntity, getSolrTemplate(), getBibliographicDetailsRepository(), getHoldingDetailRepository());
                        for (SolrInputDocument holdingsSolrInputDocument : bibSolrInputDocument.getChildDocuments()) {
                            for (SolrInputDocument itemSolrInputDocument : holdingsSolrInputDocument.getChildDocuments()) {
                                if (itemId.equals(itemSolrInputDocument.get(RecapConstants.ITEM_ID).getValue())) {
                                    itemSolrInputDocument.setField(RecapConstants.IS_DELETED_ITEM,true);
                                }
                            }
                        }
                        getSolrTemplate().saveDocument(bibSolrInputDocument);
                    }
                }
                getSolrTemplate().commit();
            }
            return "Item documents updated successfully.";
        }catch(Exception ex){
            logger.error(RecapConstants.LOG_ERROR,ex);
            return "Item documents failed to update.";
        }
    }
}