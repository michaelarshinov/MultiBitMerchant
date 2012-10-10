package org.multibit.mbm.db.dao.hibernate;

import com.google.common.base.Optional;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.multibit.mbm.db.dao.UserDao;
import org.multibit.mbm.db.dto.User;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository("hibernateUserDao")
public class HibernateUserDao extends BaseHibernateDao implements UserDao {

  @SuppressWarnings("unchecked")
  @Override
  public Optional<User> getById(Long id) {
    return getById(User.class,id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<User> getByOpenId(String openId) {
    List users = hibernateTemplate.find("from User u where u.openId = ?", openId);
    return first(users,User.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<User> getByUUID(String uuid) {
    List users = hibernateTemplate.find("from User u where u.uuid = ?", uuid);
    return first(users,User.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<User> getByCredentials(String username, String password) {
    List<User> users = hibernateTemplate.find("from User u where u.username = ? ", username);

    if (isNotFound(users)) return Optional.absent();

    StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

    // Check the password against all matching Users
    for (User user : users) {
      if (passwordEncryptor.checkPassword(password, user.getPassword())) {
        return Optional.of(user);
      }
    }

    // Must have failed to be here
    return Optional.absent();
  }

  @SuppressWarnings("unchecked")
  public List<User> getAllByPage(final int pageSize, final int pageNumber) {
    return (List<User>) hibernateTemplate.executeFind(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query query = session.createQuery("from User");
        query.setMaxResults(pageSize);
        query.setFirstResult(pageSize * pageNumber);
        return query.list();
      }
    });
  }

  @Override
  public User saveOrUpdate(User user) {
    hibernateTemplate.saveOrUpdate(user);
    return user;
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