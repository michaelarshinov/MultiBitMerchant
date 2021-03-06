package org.multibit.mbm.interfaces.rest.resources.item;

import com.google.common.base.Optional;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;
import org.multibit.mbm.interfaces.rest.api.hal.HalMediaType;
import org.multibit.mbm.interfaces.rest.api.response.hal.item.PublicItemBridge;
import org.multibit.mbm.interfaces.rest.api.response.hal.item.PublicItemCollectionBridge;
import org.multibit.mbm.domain.repositories.ItemDao;
import org.multibit.mbm.domain.model.model.Item;
import org.multibit.mbm.domain.model.model.User;
import org.multibit.mbm.interfaces.rest.resources.BaseResource;
import org.multibit.mbm.interfaces.rest.resources.ResourceAsserts;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of REST endpoints to manage Item retrieval operations
 * by the public</li>
 * </ul>
 * <p>This is the main interaction point for the public to get detail on Items for sale.</p>
 *
 * @since 0.0.1
 */
@Component
@Path("/items")
@Produces({HalMediaType.APPLICATION_HAL_JSON, HalMediaType.APPLICATION_HAL_XML, MediaType.APPLICATION_JSON})
public class PublicItemResource extends BaseResource {

  @Resource(name = "hibernateItemDao")
  ItemDao itemDao;

  /**
   * Provide a paged response of all items in the system
   *
   * @param rawPageSize   The unvalidated page size
   * @param rawPageNumber The unvalidated page number
   *
   * @return A response containing a paged list of all items
   */
  @GET
  @Timed
  @Path("/promotion")
  @CacheControl(maxAge = 6, maxAgeUnit = TimeUnit.HOURS)
  public Response retrievePromotionalItemsByPage(
    @QueryParam("ps") Optional<String> rawPageSize,
    @QueryParam("pn") Optional<String> rawPageNumber) {

    // Validation
    int pageSize = Integer.valueOf(rawPageSize.get());
    int pageNumber = Integer.valueOf(rawPageNumber.get());

    List<Item> items = itemDao.getAllByPage(pageSize, pageNumber);

    // Provide a representation to the client
    PublicItemCollectionBridge bridge = new PublicItemCollectionBridge(uriInfo, Optional.<User>absent());

    return ok(bridge, items);

  }

  /**
   * Provide a paged response of all items in the system
   *
   * @param rawSku The unvalidated Stock Keeping Unit (SKU)
   *
   * @return A response containing a paged list of all items
   */
  @GET
  @Timed
  @Path("/{sku}")
  @CacheControl(maxAge = 6, maxAgeUnit = TimeUnit.HOURS)
  public Response retrieveBySku(@PathParam("sku") String rawSku) {

    // Validation
    // TODO Work out how to validate a SKU
    String sku = rawSku;

    Optional<Item> item = itemDao.getBySKU(sku);
    ResourceAsserts.assertPresent(item, "item");

    // Provide a representation to the client
    PublicItemBridge bridge = new PublicItemBridge(uriInfo, Optional.<User>absent());

    return ok(bridge, item.get());

  }

  public void setItemDao(ItemDao itemDao) {
    this.itemDao = itemDao;
  }
}