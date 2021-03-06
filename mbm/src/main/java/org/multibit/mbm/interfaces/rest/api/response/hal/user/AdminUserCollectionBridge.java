package org.multibit.mbm.interfaces.rest.api.response.hal.user;

import com.google.common.base.Optional;
import com.theoryinpractise.halbuilder.ResourceFactory;
import com.theoryinpractise.halbuilder.spi.Resource;
import org.multibit.mbm.interfaces.rest.api.response.hal.BaseBridge;
import org.multibit.mbm.domain.model.model.User;
import org.multibit.mbm.interfaces.rest.resources.ResourceAsserts;

import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * <p>Bridge to provide the following to {@link org.multibit.mbm.domain.model.model.User}:</p>
 * <ul>
 * <li>Creates representation of multiple Users for an administrator</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class AdminUserCollectionBridge extends BaseBridge<List<User>> {

  private final CustomerUserBridge customerUserBridge;

  /**
   * @param uriInfo   The {@link javax.ws.rs.core.UriInfo} containing the originating request information
   * @param principal An optional {@link org.multibit.mbm.domain.model.model.User} to provide a security principal
   */
  public AdminUserCollectionBridge(UriInfo uriInfo, Optional<User> principal) {
    super(uriInfo, principal);
    customerUserBridge = new CustomerUserBridge(uriInfo,principal);
  }

  public Resource toResource(List<User> users) {
    ResourceAsserts.assertNotNull(users, "users");

    ResourceFactory resourceFactory = getResourceFactory();

    Resource userList = resourceFactory.newResource(uriInfo.getRequestUri());

    for (User user : users) {
      Resource userResource = customerUserBridge.toResource(user);

      // TODO Fill this in for all admin fields
      //userResource.withProperty("id", user.getId())
        // End of build
        ;

      userList.withSubresource("/user/"+user.getId(), userResource);
    }

    return userList;

  }

}
