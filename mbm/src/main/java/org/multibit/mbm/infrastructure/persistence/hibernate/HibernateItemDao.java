package org.multibit.mbm.infrastructure.persistence.hibernate;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.multibit.mbm.domain.repositories.ItemDao;
import org.multibit.mbm.domain.repositories.ItemNotFoundException;
import org.multibit.mbm.domain.model.model.Item;
import org.multibit.mbm.domain.model.model.ItemField;
import org.multibit.mbm.domain.model.model.ItemFieldDetail;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository("hibernateItemDao")
public class HibernateItemDao extends BaseHibernateDao<Item> implements ItemDao {

  @Override
  public Optional<Item> getById(Long id) throws ItemNotFoundException {
    return getById(Item.class, id);
  }

  @Override
  public Optional<Item> getBySKU(String sku) {
    List items = hibernateTemplate.find("from Item i where i.sku = ?", sku);
    return first(items);
  }


  @Override
  public Optional<Item> getByGTIN(String gtin) {
    List items = hibernateTemplate.find("from Item i where i.gtin = ?", gtin);
    return first(items);
  }

  @SuppressWarnings("unchecked")
  public List<Item> getAllByPage(final int pageSize, final int pageNumber) {
    return (List<Item>) hibernateTemplate.executeFind(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createQuery("from Item");
        query.setMaxResults(pageSize);
        query.setFirstResult(pageSize * pageNumber);
        return query.list();
      }
    });
  }

  /**
   * Initialize various collections since we are targeting the individual entity (perhaps for display)
   *
   * @param entity The entity
   *
   * @return The entity with all collections initialized
   */
  @Override
  protected Item initialized(Item entity) {
    return entity;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Deprecated
  public List<Item> getByExampleByPage(final int pageSize, final int pageNumber, final Item example) {
    Preconditions.checkNotNull(example, "example cannot be null");

    return hibernateTemplate.executeFind(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {

        // Examine the example object to determine the query
        String hql = buildHql(example);
        
        // Do the work now that the HQL is created
        return (List<Item>) session
          .createQuery(hql)
          .setFirstResult(pageSize * pageNumber)
          .setMaxResults(pageSize)
          .setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
          .list();
      }

      /**
       * @param example The example entity
       * @return The HQL required to locate matching entities
       */
      private String buildHql(Item example) {
        Preconditions.checkNotNull(example, "example cannot be null");

        // The basic starting point
        String hql = "select i from Item i ";

        if (example != null) {
          // Check for simple inline fields (if applicable)
          
          // Check for complex joined fields (if applicable)
          if (!example.getItemFieldMap().isEmpty()) {
            // Potential for a complex query
            // Require an inner join on the item field map since there is at least one entry using it
            hql += "inner join i.itemFieldMap ifm where ";
            
            // Currently restrict searching against primary field entries (makes code and query simpler)
            boolean first=true;
            for (Map.Entry<ItemField, ItemFieldDetail> entry: example.getItemFieldMap().entrySet()) {
              ItemFieldDetail itemFieldDetail = entry.getValue();
              // Compare against the ItemField ordinal and apply a wildcard using a disjunction (OR) between fields
              if (!first) {
                hql += "or ";
              }
              hql += "(ifm.itemField = "+ itemFieldDetail.getItemField().ordinal();
              hql += " and lower(ifm.primaryDetail.content) like lower('%"+itemFieldDetail.getPrimaryDetail().getContent()+"%')) ";
              first=false;
            }
          }
        }

        return hql;
      }

    });

  }

  @Override
  public Item saveOrUpdate(Item item) {
    hibernateTemplate.saveOrUpdate(item);
    return item;
  }

  /**
   * Force an immediate in-transaction flush (normally only used in test code)
   */
  public void flush() {
    hibernateTemplate.flush();
  }


  public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
    this.hibernateTemplate = hibernateTemplate;
  }
}
