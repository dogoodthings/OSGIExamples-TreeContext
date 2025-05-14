package org.dogoodthings.ectr.treeContext;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.dscsag.plm.spi.interfaces.ECTRService;
import com.dscsag.plm.spi.interfaces.gui.TreeContextStructureProvider;
import com.dscsag.plm.spi.interfaces.objects.PlmObjectKey;

/**
 */
@Component (service = TreeContextStructureProvider.class) public class LinkedObjectsTreeContextStructureProvider
    implements TreeContextStructureProvider
{
  @Reference private ECTRService ectrService;
  @Reference private LinksReader linksReader;

  /**
   *
   * @return unique key for this context
   */
  @Override
  public String getContextKey()
  {
    return "ctx_olinks";
  }

  /**
   *
   * @param plmObjectKey - key parent of parent object
   * @return keys of all objects linked to given input object
   */
  @Override
  public Collection<PlmObjectKey> getChildren(PlmObjectKey plmObjectKey)
  {
    return linksReader.readLinks(plmObjectKey);
  }

  /**
   * we are supporting all objects here, somehow
   * @param plmObjectKey
   * @return true
   */
  @Override
  public boolean isRootDropSupported(PlmObjectKey plmObjectKey)
  {
    return true;
  }
}