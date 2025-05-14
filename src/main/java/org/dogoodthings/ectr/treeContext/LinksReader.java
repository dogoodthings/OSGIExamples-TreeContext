package org.dogoodthings.ectr.treeContext;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.dscsag.plm.spi.interfaces.ECTRService;
import com.dscsag.plm.spi.interfaces.objects.PlmObjectKey;
import com.dscsag.plm.spi.interfaces.services.document.key.KeyConverterService;
import com.dscsag.plm.spi.rfc.builder2.FmCallBuilder;
import com.dscsag.plm.spi.rfc.builder2.FmTableBuilder;

/**
 */

@Component (service = LinksReader.class) public class LinksReader
{
  @Reference private ECTRService ectrService;
  @Reference private KeyConverterService keyConverter;

  public Collection<PlmObjectKey> readLinks(PlmObjectKey parent)
  {
    if ("DRAW".equals(parent.getType()))
      return readLinkedObjectsOfDir(parent);
    else
      return readLinkedDirs(parent);
  }

  private Collection<PlmObjectKey> readLinkedDirs(PlmObjectKey parent)
  {
    // /DSCSAG/OBJ_GETLINKED_DOCUMENT
    ectrService.getPlmLogger().debug("LinksReader: readLinkedDirs(" + parent.getKey() + ")");

    var rfcCall = new FmCallBuilder("/DSCSAG/OBJ_GETLINKED_DOCUMENT").importing().scalar("IV_OBJECTTYPE", parent.getType())
        .scalar("IV_OBJECTKEY", parent.getKey()).scalar("IV_GET_CUST_ATTR", "").scalar("IV_READDETAILS", "").build();

    var rfcResult = ectrService.getRfcExecutor().execute(rfcCall);

    return rfcResult.getTable("DOCUMENTS").rowsAsStream().map(
        row -> keyConverter.plmObjectKeyForDocument(row.getFieldValue("DOCUMENTTYPE"), row.getFieldValue("DOCUMENTNUMBER"),
            row.getFieldValue("DOCUMENTVERSION"), row.getFieldValue("DOCUMENTPART"))).toList();

  }

  private Collection<PlmObjectKey> readLinkedObjectsOfDir(PlmObjectKey parent)
  {
    // /DSCSAG/OBJ_GET_MULTIDETAIL2
    ectrService.getPlmLogger().debug("LinksReader: readLinkedObjectsOfDir(" + parent.getKey() + ")");
    var docKey = keyConverter.fromPlmObjectKey(parent);

    var docTable = new FmTableBuilder("DOCUMENTTYPE", "DOCUMENTNUMBER", "DOCUMENTVERSION", "DOCUMENTPART").addRow(docKey.getType(),
        docKey.getNumber(), docKey.getVersion(), docKey.getPart()).build();

    var rfcCall = new FmCallBuilder("/DSCSAG/OBJ_GET_MULTIDETAIL2").importing().scalar("GETOBJECTLINKS", "X").scalar("GETACTIVEFILES", "")
        .scalar("GETCOMPONENTS", "").scalar("GETDOCDATA", "").scalar("GETDOCDESCRIPTIONS", "").scalar("GETDOCFILES", "")
        .scalar("GETLONGTEXTS", "").scalar("GETOMR", "").scalar("GETSTATUS", "").scalar("GETSTATUSLOG", "").scalar("GET_OUT_DOCUMENT", "")
        .scalar("IV_GET_CUST_ATTR", "").tables().table("DOCUMENTS", docTable).build();

    var rfcResult = ectrService.getRfcExecutor().execute(rfcCall);

    return rfcResult.getTable("OBJECTLINKS").rowsAsStream()
        .map(row -> new PlmObjectKey(row.getFieldValue("OBJECTTYPE"), row.getFieldValue("OBJECTKEY"))).toList();
  }

}