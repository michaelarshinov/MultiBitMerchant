package org.multibit.mbm.client.handlers.item;

import org.junit.Test;
import org.multibit.mbm.client.PublicMerchantClient;
import org.multibit.mbm.client.handlers.BaseHandlerTest;
import org.multibit.mbm.model.ClientItem;
import org.multibit.mbm.testing.FixtureAsserts;

import java.net.URI;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ClientItemCollectionHandlerTest extends BaseHandlerTest {

  @Test
  public void retrievePromotionalItemsByPage() throws Exception {

    // Arrange
    URI expectedUri = URI.create("http://localhost:8080/mbm/items/promotion?pn=0&ps=1");

    // Test-specific JerseyClient behaviour
    when(client.resource(expectedUri)).thenReturn(webResource);
    when(webResource.get(String.class)).thenReturn(
      FixtureAsserts.jsonFixture("/fixtures/hal/item/expected-public-retrieve-items-page-1.json")
    );

    // Act
    List<ClientItem> items = PublicMerchantClient
      .newInstance(locale)
      .items()
      .retrievePromotionalItemsByPage(0,1);

    // Assert
    assertEquals("Unexpected number of items", 1, items.size());
    assertEquals("Expected title", "Cryptonomicon", items.get(0).getOptionalProperties().get("title"));
    assertEquals("Expected uri", "http://multibit-store.herokuapp.com/images/book.jpg", items.get(0).getOptionalProperties().get("image_thumbnail_uri"));
    assertEquals("Expected uri", "http://localhost:8080/item/0099410672", items.get(0).getOptionalProperties().get("item self"));

  }

}
