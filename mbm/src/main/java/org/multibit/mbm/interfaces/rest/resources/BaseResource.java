package org.multibit.mbm.interfaces.rest.resources;

import com.google.common.base.Optional;
import org.multibit.mbm.interfaces.rest.api.hal.HalMediaType;
import org.multibit.mbm.interfaces.rest.api.response.hal.BaseBridge;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.net.URI;

/**
 * <p>Abstract base class to provide the following to subclasses:</p>
 * <ul>
 * <li>Provision of common methods</li>
 * </ul>
 * <p><code>E</code> is the primary entity</p>
 * <p><code>C</code> is the primary entity in a suitable collection</p>
 *
 * @since 0.0.1
 *         
 */
public abstract class BaseResource {

  // TODO Verify thread safety
  @Context
  protected UriInfo uriInfo;

  // TODO Verify thread safety
  @Context
  protected HttpHeaders httpHeaders;

  /**
   * @param bridge The bridge for the entity
   * @param entity The entity
   * @return A configured HTTP 200 OK response
   */
  protected <T> Response ok(BaseBridge<T> bridge,T entity) {
    MediaType acceptedMediaType = getAcceptedMediaType(httpHeaders);

    String body = bridge.toResource(entity).renderContent(acceptedMediaType.toString());

    return Response.ok().type(acceptedMediaType).entity(body).build();
  }

  /**
   * @param bridge The bridge for the entity
   * @param entity The entity
   * @return A configured HTTP 201 CREATED response
   */
  protected <T> Response created(BaseBridge<T> bridge,T entity, URI location) {
    MediaType acceptedMediaType = getAcceptedMediaType(httpHeaders);

    String body = bridge.toResource(entity).renderContent(acceptedMediaType.toString());

    return Response.created(location).type(acceptedMediaType).entity(body).build();
  }

  /**
   * Determines which of the acceptable media types will be the chosen one
   * based on the q-factor
   *
   * @param httpHeaders The HTTP headers of the request
   *
   * @return The accepted media type to be used
   */
  private MediaType getAcceptedMediaType(HttpHeaders httpHeaders) {
    Optional<MediaType> accepted = Optional.absent();
    for (MediaType mediaType : httpHeaders.getAcceptableMediaTypes()) {
      if (mediaType.isCompatible(HalMediaType.APPLICATION_HAL_JSON_TYPE)) {
        accepted = Optional.of(HalMediaType.APPLICATION_HAL_JSON_TYPE);
        break;
      }
      if (mediaType.isCompatible(HalMediaType.APPLICATION_HAL_XML_TYPE)) {
        accepted = Optional.of(HalMediaType.APPLICATION_HAL_XML_TYPE);
        break;
      }
    }
    if (!accepted.isPresent()) {
      throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
    }
    return accepted.get();
  }

}
